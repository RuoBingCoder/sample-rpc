package com.sjl.rpc.context.zk.provider;

import java.io.IOException;

public interface ServiceProvider {

    void publishService(String serviceName) throws Exception;
}
