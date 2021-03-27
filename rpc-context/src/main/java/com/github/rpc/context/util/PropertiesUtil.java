package com.github.rpc.context.util;

import com.github.rpc.context.constants.Constant;
import com.github.rpc.context.exception.RocketException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author: JianLei
 * @date: 2020/9/1 4:49 下午
 * @description: zk地址获取
 */

public class PropertiesUtil {

    public static String getZkAddr(Environment env, String key) throws IOException {
        return doGetAddress(env, key);
      /*  Properties properties=new Properties();
        properties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream("zk.properties"));
      return properties.getProperty(key);*/

    }

    public static String doGetAddress(Environment env, String key) {
        if (env != null) {
            final String addr = env.getProperty(key);
            if (StringUtils.isBlank(addr)) {
                throw new RocketException("zk address is null");
            }
            if (addr.contains("//")) {
                final String[] split = addr.split("//");
                if (Constant.REGISTRY_PROTOCOL.equals(split[0])) {
                    return split[1];
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
//        System.out.println(PropertiesUtil.getZkAddr("zk.addr"));
        Properties properties = new Properties();
        String fileName=System.getProperty("user.home")+"/develop/test.cache";
        properties.setProperty("a","1,");
        properties.setProperty("b","1,");
//        properties.setProperty("c","3,");
//        properties.setProperty("d","4,");
        File file=new File(fileName);
        if (!file.exists()){
            file.createNewFile();
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
          properties.store(fileOutputStream,"Test");
        }

    }
}
