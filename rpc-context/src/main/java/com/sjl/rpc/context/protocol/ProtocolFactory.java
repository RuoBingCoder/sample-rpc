package com.sjl.rpc.context.protocol;

import com.sjl.rpc.context.constants.Constant;

/**
 * @author jianlei.shi
 * @date 2021/1/10 2:29 下午
 * @description ProtocolFactory
 */

public class ProtocolFactory {


    public static  <T> T getProtocol(String protocolName) {
        if (Constant.NETTY.equals(protocolName)){
            return (T) new RocketNettyProtocol();
            
        }else if (Constant.HTTP.equals(protocolName)){
            return (T) new RocketHttpProtocol();

        }
        return (T) new RocketNettyProtocol();
    }
}
