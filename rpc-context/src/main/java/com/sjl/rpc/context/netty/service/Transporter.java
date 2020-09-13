package com.sjl.rpc.context.netty.service;

import com.sjl.rpc.context.bean.RocketRequest;
import com.sjl.rpc.context.bean.RocketResponse;

/**
 * @author: JianLei
 * @date: 2020/9/11 1:49 下午
 * @description:
 */

public interface Transporter {


   default void bind(){

   }


   default RocketResponse connect(RocketRequest request){
       return null;
   }
}
