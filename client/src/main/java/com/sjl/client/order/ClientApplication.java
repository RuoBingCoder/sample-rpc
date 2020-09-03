package com.sjl.client.order;

import com.sjl.rpc.context.annotation.RpcInterfacesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sjl.client","com.sjl.rpc.context"})
@RpcInterfacesScan(basePackages = "api.service")
public class ClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClientApplication.class, args);
  }
}
