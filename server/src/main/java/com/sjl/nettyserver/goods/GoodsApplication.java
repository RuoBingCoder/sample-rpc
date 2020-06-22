package com.sjl.nettyserver.goods;

import com.sjl.nettyserver.goods.service.GoodsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@Import(GoodsServiceImpl.class)
public class GoodsApplication {


    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(GoodsApplication.class, args);
        System.out.println("----------启动成功-------------");
    }

}
