package com.sjl.server.goods;

import com.sjl.rpc.context.annotation.EnableRocketScan;
import com.sjl.rpc.context.annotation.spi.ExtensionLoader;
import com.sjl.server.goods.spi.SpiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

@SpringBootApplication
@ComponentScan({"com.sjl.server", "com.sjl.rpc.context"})
@EnableRocketScan(basePackages = "com.sjl.server.goods.service")
@Slf4j
public class GoodsApplication {

  public static void main(String[] args) {
    SpringApplication.run(GoodsApplication.class, args);
  }

  @Bean
  public ApplicationRunner run() throws IOException, IllegalAccessException, InstantiationException {
    ExtensionLoader<SpiService> loader = ExtensionLoader.getExtensionLoader(SpiService.class);
    SpiService spiService = loader.getExtension("test1");
    System.out.println(spiService.testSpiService("哈哈哈"));
    return args -> log.info("test spi finish!");
  }
}
