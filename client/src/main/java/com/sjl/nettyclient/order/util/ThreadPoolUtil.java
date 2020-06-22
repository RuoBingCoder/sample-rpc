package com.sjl.nettyclient.order.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author: jianlei
 * @date: 2020/6/22
 * @description: ThreadPoolUtil
 */
@Slf4j
public class ThreadPoolUtil {

  public static Object submit(Callable task) throws ExecutionException, InterruptedException {
    Future<?> submit = null;
    ThreadPoolExecutor threadPoolExecutor =
        new ThreadPoolExecutor(2, 4, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
    try {
      submit = threadPoolExecutor.submit(task);
    } catch (Exception e) {
      log.error("线程池执行任务异常", e);
    } finally {
      if (threadPoolExecutor.isShutdown()) {
        threadPoolExecutor.shutdown();
      }
    }

    assert submit != null;
    return submit.get();
  }


}
