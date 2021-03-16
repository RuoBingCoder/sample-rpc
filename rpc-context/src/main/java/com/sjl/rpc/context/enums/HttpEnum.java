package com.sjl.rpc.context.enums;

/**
 * @author jianlei.shi
 * @date 2021/2/20 2:20 下午
 * @description Http
 */

public enum HttpEnum {

    CONTENT_TYPE_JSON("application/json"),
    SEND_MESSAGE_FAILED("http请求发送失败!已经重试3次"),
    HTTP_CONFIG_PREFIX("http.url")
    ;
    protected String type;

    HttpEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
