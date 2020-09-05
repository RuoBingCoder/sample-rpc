package com.sjl.rpc.context.bean;

import lombok.*;

/**
 * @author: JianLei
 * @date: 2020/9/1 2:25 下午
 * @description:
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Deprecated
public class ReferenceBean {

    private Class<?> interfaceClass;
    private String interfaceName;


}
