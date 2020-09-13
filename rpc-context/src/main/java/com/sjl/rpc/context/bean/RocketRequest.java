package com.sjl.rpc.context.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: JianLei
 * @date: 2020/6/14 2:35 下午
 * @description:
 */
@Data
public class RocketRequest implements Serializable {

  private static final long serialVersionUID = -2625585669090924236L;

  /*请求ID*/
  private String requestId;
  /*调用class类名*/
  private String className;
  /*调用方法名*/
  private String methodName;
  /*调用参数类型集合*/
  private Class<?>[] parameterTypes;
  /*调用参数集合*/
  private Object[] parameters;
  /** 版本号 */
  private String version;
}
