package netty.client;

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
import netty.handler.SimpleClientHandler;
import netty.util.Response;

import java.util.concurrent.TimeUnit;

public class TcpClient {

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

        String host = "localhost";
        int port = 8000;
        // Start the client.
        try {
            f = b.connect(host, port).sync(); // (5)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //注意：1每一个请求都是同一个连接，并发问题
    //Request：1.唯一请求id 2.请求内容
    //Response：1.相应唯一id 2.相应内容
    //发送数据
    public static Response send(ClientRequest request) {
        f.channel().writeAndFlush(JSONObject.toJSONString(request));
        f.channel().writeAndFlush("\r\n");
        DefaultFuture df = new DefaultFuture(request);

        return df.get();
    }


}
