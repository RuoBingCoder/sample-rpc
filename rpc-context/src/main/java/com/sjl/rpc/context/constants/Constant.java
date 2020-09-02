package com.sjl.rpc.context.constants;

import org.springframework.core.annotation.AnnotationAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: JianLei
 * @date: 2020/6/14 5:49 下午
 * @description:
 */

public class Constant {

  public static final String SCANNER_PACKAGES = "api.service";

  public static Map<String, AnnotationAttributes> CACHE_SERVICE_ATTRIBUTES_MAP =new ConcurrentHashMap<>();

  public static String ZK_ADDRESS_PREFIX ="zk.addr";

  public static String ROOT_PATH="/rocket/";


}