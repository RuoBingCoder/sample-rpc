package com.github.rpc.context.netty.transport;

import com.github.rpc.context.bean.RocketRequest;
import com.github.rpc.context.netty.transport.BaseClientTransporter;

/**
 * @author: JianLei
 * @date: 2020/9/11 1:49 下午
 * @description:
 */

public interface Transporter {


   default void bind(Integer port){

   }


   default void connect(String[] hosts){
       return;
   }


   default void open(RocketRequest request, BaseClientTransporter ct){
       return;
   }
}
