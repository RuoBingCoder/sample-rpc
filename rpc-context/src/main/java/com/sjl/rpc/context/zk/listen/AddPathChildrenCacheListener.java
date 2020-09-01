package com.sjl.rpc.context.zk.listen;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

/**
 * @author: JianLei
 * @date: 2020/8/30 4:13 下午
 * @description: 添加节点监听
 */

public class AddPathChildrenCacheListener implements PathChildrenCacheListener {
    @Override
    public void childEvent(CuratorFramework cf, PathChildrenCacheEvent pe) throws Exception {
        //TODO
       if (pe.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){

        }
    }
}
