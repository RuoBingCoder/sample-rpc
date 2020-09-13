package com.sjl.rpc.context.codec;

import com.sjl.rpc.context.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * RPC请求编码，只需扩展 Netty 的MessageToByteEncoder抽象类，并且实现其encode方法即可
 */
public class RocketEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RocketEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if(genericClass.isInstance(in)){
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
