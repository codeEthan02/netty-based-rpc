package client.core;

import io.netty.channel.ChannelFuture;

import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelManager {

    public static CopyOnWriteArrayList<ChannelFuture> channelFutures = new CopyOnWriteArrayList<>();
    public static HashSet<String> realServerPaths;
    static AtomicInteger position = new AtomicInteger(0);

    public static void removeFuture(ChannelFuture channelFuture) {
        channelFutures.remove(channelFuture);
    }

    public static void add(ChannelFuture channelFuture) {
        channelFutures.add(channelFuture);
    }

    public static void clear() {
        channelFutures.clear();
    }

    public static ChannelFuture get(AtomicInteger i) {
        int size = channelFutures.size();
        ChannelFuture channel = null;
        if (i.get() > size) {
            channel = channelFutures.get(0);
            position = new AtomicInteger(1);
        } else {
            channel = channelFutures.get(i.getAndIncrement());
        }

        if (!channel.channel().isActive()) {
            channelFutures.remove(channel);
            return get(position);
        }
        return channel;
    }
}
