package com.sjl.nettyclient.order.listen;

import com.alibaba.fastjson.JSON;
import com.sjl.nettyclient.order.client.NettyClient;
import com.sjl.nettyclient.order.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: jianlei
 * @date: 2020/6/22
 * @description: 实现远程配置中心
 */

public class RemoteConfigListener implements SpringApplicationRunListener {
  public static final Logger log = LoggerFactory.getLogger(RemoteConfigListener.class);
  private final SpringApplication application;
  private final String[] args;
  private final CountDownLatch countDownLatch = new CountDownLatch(1);

  public RemoteConfigListener(SpringApplication application, String[] args) {
    this.application = application;
    this.args = args;
  }

  @Override
  public void starting() {
    log.info("spring boot starting...");
  }

  @Override
  public void environmentPrepared(ConfigurableEnvironment environment) {
    // 获取配置文件属性值
    Properties properties = new Properties();
    Map parseObject = null;
    try {
      // 通过获取远程配置服务进行运行注入
      properties.load(this.getClass().getClassLoader().getResourceAsStream("my.properties"));
      String config2 =
          (String)
              ThreadPoolUtil.submit(
                  () -> {
                    log.info("-------线程池开始执行--------当前线程是:{}",Thread.currentThread().getName());
                    String config = NettyClient.remoteConfigStart("config");
                    log.info("-------获取配置中心json数据为--------:{}", config);
                    countDownLatch.countDown();
                    return config;
                  });
      countDownLatch.await();

      if (config2 != null) {
        parseObject = JSON.parseObject(config2, Map.class);
        for (Object key : parseObject.keySet()) {
          properties.setProperty((String) key, (String) parseObject.get(key));
        }
      }
      // 读取名称
      PropertySource propertySource = new PropertiesPropertySource("my", properties);
      // 将资源添加到springboot容器中
      MutablePropertySources mutablePropertySources = environment.getPropertySources();
      // 通过api接口可以读取配置文件
      mutablePropertySources.addLast(propertySource);
    } catch (IOException | InterruptedException | ExecutionException e) {
      log.error("监听器添加配置异常",e);
    }
  }
}

