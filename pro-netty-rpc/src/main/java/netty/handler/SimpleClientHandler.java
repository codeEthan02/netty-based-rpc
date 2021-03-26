package netty.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import netty.client.DefaultFuture;
import netty.util.Response;

public class SimpleClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg.toString());
        if ("ping".equals(msg.toString())) {
            ctx.channel().writeAndFlush("ping\r\n");
            return;
        }

        ctx.channel().attr(AttributeKey.valueOf("sssss")).set(msg);
        Response response = JSONObject.parseObject(msg.toString(), Response.class);
        DefaultFuture.recieve(response);

        //ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}
