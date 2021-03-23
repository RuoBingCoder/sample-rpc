package com.github.rpc.context.codec;

import com.github.rpc.context.util.SerializationUtil;
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

    /**
     * 编码
     * 基于outBound write 顺序不能倒 链表tail->tail.pre->head
     * InBound read 则相反 tail<-tail.pre<-head
     *
     * @param ctx ctx
     * @param in  在
     * @param out 出
     * @return
     * @author jianlei.shi
     * @date 2021-03-20 16:32:43
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if(genericClass.isInstance(in)){
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);//写入4个字节
            out.writeBytes(data); //
        }
    }
}
