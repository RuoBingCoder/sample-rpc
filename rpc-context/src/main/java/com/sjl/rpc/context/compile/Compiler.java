package com.sjl.rpc.context.compile;

import com.sjl.rpc.context.spring.annotation.spi.SPI;

@SPI(name = "javassist")
public interface Compiler {
    /**
     * 编译java源代码
     * @param code
     * @param classLoader
     * @return
     */
    Class<?> compile(String code, ClassLoader classLoader);
}
