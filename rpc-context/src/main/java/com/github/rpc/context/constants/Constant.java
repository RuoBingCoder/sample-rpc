package com.github.rpc.context.constants;

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
  public static final String PROVIDER = "provider";
  public static final String CONSUMER = "consumer";
  public static final Integer PORT=8817;
  public static final String PING="ping";
  public static final String PONG="pong";
  public static final String RPC="rpc";
  public static final String AUTH="auth";
  public static final String NETTY="netty";
  public static final String HTTP="http";
  public static final String PROTOCOL="rocket.protocol.name";
  public static final String PROTOCOL_PORT="rocket.protocol.port";
  public static final String TCP_PROTOCOL="tcp";
  public static final String HTTP_PROTOCOL="http";
  public static final String VERSION="version";








}
