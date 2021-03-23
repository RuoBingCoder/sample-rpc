package com.github.rpc.context.remote.handler.abs;

import cn.hutool.core.collection.CollectionUtil;
import com.github.rpc.context.exception.RocketException;
import com.github.rpc.context.util.StringUtils;

import java.util.List;

/**
 * @author: JianLei
 * @date: 2020/9/5 8:19 下午
 * @description:
 */

public abstract class BaseLoadBalance {

    public String loadBalance(List<String> services, String version) {
        if (CollectionUtil.isEmpty(services)) {
            return null;
        }
        if (services.size() == 1) {
            final String meta = services.get(0);
            final String vs = StringUtils.getVersion(meta);
            if (vs.equals(version)) {
                return services.get(0);
            }
            throw new RocketException("没有找到相应的服务:service metaData is :" + services.get(0) + "search version is:" + version);
        }

        return selectService(services,version);
    }

    public abstract <T> String selectService(T datas, String version);
}
