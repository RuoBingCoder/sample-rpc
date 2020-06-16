package com.sjl.nettyserver.goods.codec;

import com.sjl.nettyserver.goods.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC请求解码，只需扩展Netty的ByteToMessageDecoder抽象类，并且实现其decode方法即可
 *
 */
public class RpcDecoder extends ByteToMessageDecoder {
    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 4){
            return;
        }

        in.markReaderIndex();
        int dataLength = in.readInt();
        if(dataLength < 0){
            ctx.close();
        }

        if(in.readableBytes() < dataLength){
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = SerializationUtil.deSerialize(data, genericClass);
        out.add(obj);
    }
}
