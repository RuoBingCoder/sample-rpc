package com.sjl.client.order;

import com.sjl.rpc.context.annotation.SjlRpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sjl.client.order","com.sjl.rpc.context"})
@SjlRpcScan(basePackages = "api.service",type = "consumer")
public class ClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClientApplication.class, args);
  }
}
