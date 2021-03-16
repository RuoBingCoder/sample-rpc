package com.sjl.rpc.context.netty.service;

import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;
import com.sjl.rpc.context.netty.abs.BaseClientTransporter;

/**
 * @author: JianLei
 * @date: 2020/9/11 1:49 下午
 * @description:
 */

public interface Transporter {


   default void bind(){

   }


   default void connect(RocketRequest request, BaseClientTransporter ct){
       return;
   }
}
