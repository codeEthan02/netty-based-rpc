package client.core;

import client.constant.Constants;
import client.core.DefaultFuture;
import client.handler.SimpleClientHandler;
import client.param.ClientRequest;
import client.param.Response;
import client.zk.ZookeeperFactory;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.Watcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TcpClient {

    static Set<String> realServerPaths = new HashSet<>();
    static final Bootstrap b = new Bootstrap();
    static ChannelFuture f = null;
    static {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
                ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast(new IdleStateHandler(60, 45, 20, TimeUnit.SECONDS));
                ch.pipeline().addLast(new SimpleClientHandler());
                ch.pipeline().addLast(new StringEncoder());
            }
        });

        CuratorFramework client = ZookeeperFactory.create();
        String host = "localhost";
        int port = 8000;

        try {
            List<String> serverPaths = client.getChildren().forPath(Constants.SERVER_PATH);

            //加上zk监听服务器的变化
            CuratorWatcher watcher = new ServerWatcher();
            client.getChildren().usingWatcher(watcher).forPath(Constants.SERVER_PATH);



            for (String serverPath : serverPaths) {
                String[] str = serverPath.split("#");
                ChannelManager.realServerPaths.add(str[0] + "#" + str[1]);

                ChannelFuture channel = TcpClient.b.connect(str[0], Integer.parseInt(str[1]));
                ChannelManager.add(channel);
            }
            if (ChannelManager.realServerPaths.size() > 0) {
                String[] hostAndPort = ChannelManager.realServerPaths.toArray()[0].toString().split("#");
                host = hostAndPort[0];
                port = Integer.parseInt(hostAndPort[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Start the client.
//        try {
//            f = b.connect(host, port).sync(); // (5)
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }



    //注意：1每一个请求都是同一个连接，并发问题
    //Request：1.唯一请求id 2.请求内容
    //Response：1.相应唯一id 2.相应内容
    //发送数据
    public static Response send(ClientRequest request) {
        f = ChannelManager.get(ChannelManager.position);

        f.channel().writeAndFlush(JSONObject.toJSONString(request));
        f.channel().writeAndFlush("\r\n");
        DefaultFuture df = new DefaultFuture(request);

        return df.get();
    }


}
