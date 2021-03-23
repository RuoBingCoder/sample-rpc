package com.github.rpc.context.codec;

import com.github.rpc.context.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.List;

/**
 * RPC请求解码，只需扩展Netty的ByteToMessageDecoder抽象类，并且实现其decode方法即可
 * @see P 210
 * @see LengthFieldBasedFrameDecoder
 * <a href="https://zhuanlan.zhihu.com/p/95621344"/>
 */
public class RocketDecoder extends ByteToMessageDecoder {
    private Class<?> genericClass;

    public RocketDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 4){ //获取可读缓冲区大小,相当于获取当前writeIndex-readIndex的值
            return;
        }

        in.markReaderIndex(); //在读数据之前,将readIndex的状态保存起来,方便在读完数据之后将readIndex复原
        int dataLength = in.readInt();
        if(dataLength < 0){
            ctx.close();
        }
        //
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
