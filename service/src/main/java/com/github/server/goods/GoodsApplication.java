package com.github.server.goods;

import com.github.rpc.context.spring.annotation.EnableRocketScan;
import com.github.rpc.context.spring.annotation.spi.ExtensionLoader;
import com.github.server.goods.spi.SpiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.github.server", "com.github.rpc.context"})
@EnableRocketScan(basePackages = "com.github.server.goods.service")
@Slf4j
public class GoodsApplication {

  public static void main(String[] args) {
    SpringApplication.run(GoodsApplication.class, args);
  }

  @Bean
  public ApplicationRunner run() {
   /* ExtensionLoader<SpiService> loader = ExtensionLoader.getExtensionLoader(SpiService.class);
    SpiService spiService = loader.getExtension("test1");
    System.out.println(spiService.testSpiService("哈哈哈"));
    return args -> log.info("test spi finish!");*/
    return args -> {

    };
  }
}
