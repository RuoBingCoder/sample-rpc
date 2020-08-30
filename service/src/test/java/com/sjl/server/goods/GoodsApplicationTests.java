package com.sjl.server.goods;

import api.service.IGoodsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

@SpringBootTest
class GoodsApplicationTests {
@Autowired
private IGoodsService iGoodsService;
    @Test
    void contextLoads() {
    Object o;
    Set set = Collections.singleton(Collections.newSetFromMap(new HashMap<>(10)));
    set.add(1);
    System.out.println("============>:"+iGoodsService.helloRpc("rpc"));
    }

}
