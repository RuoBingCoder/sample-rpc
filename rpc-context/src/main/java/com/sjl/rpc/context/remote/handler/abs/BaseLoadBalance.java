package com.sjl.rpc.context.remote.handler.abs;

import cn.hutool.core.collection.CollectionUtil;

import java.util.List;

/**
 * @author: JianLei
 * @date: 2020/9/5 8:19 下午
 * @description:
 */

public abstract class BaseLoadBalance {

    public String loadBalance(List<String> services) {
        if (CollectionUtil.isEmpty(services)) {
            return null;
        }
        if (services.size() == 1) {
            return services.get(0);
        }

        return selectService(services);
    }

    public abstract <T> String selectService(T datas);
}
