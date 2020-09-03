package com.sjl.server.goods;

import com.sjl.rpc.context.annotation.RpcInterfacesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.sjl.server", "com.sjl.rpc.context"})
@RpcInterfacesScan(value = "com.sjl.server.goods.service",type = "service")
public class GoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class, args);
    }

}
