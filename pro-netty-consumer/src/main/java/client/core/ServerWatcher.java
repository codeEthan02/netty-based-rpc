package client.core;

import client.zk.ZookeeperFactory;
import io.netty.channel.ChannelFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import java.util.HashSet;
import java.util.List;

public class ServerWatcher implements CuratorWatcher {

    @Override
    public void process(WatchedEvent watchedEvent) throws Exception {

        CuratorFramework client = ZookeeperFactory.create();
        String path = watchedEvent.getPath();

        client.getChildren().usingWatcher(this);
        List<String> serverPaths = client.getChildren().forPath(path);
        ChannelManager.realServerPaths.clear();


        for (String serverPath : serverPaths) {

            String[] str = serverPath.split("#");

            ChannelManager.realServerPaths.add(str[0] + "#" + str[1]);

        }
        ChannelManager.clear();
        for (String realServer : ChannelManager.realServerPaths) {
            String[] str = realServer.split("#");
            ChannelFuture channel = TcpClient.b.connect(str[0], Integer.parseInt(str[1]));

            ChannelManager.add(channel);
        }



    }
}
