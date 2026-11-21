package com.santech.framework.utils.component.thread;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @explain
 */
public class TheadRejectHandler implements RejectedExecutionHandler {

    private Logger logger = LoggerFactory.getLogger(TheadRejectHandler.class);


    /**
     * 拒绝加入队列时的方法
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (r instanceof ThreadCommand) {
            ThreadCommand command = (ThreadCommand) r;
            command.callback();

            RejectedExecutionException e = new RejectedExecutionException("加入线程队列失败：" + command.errorMessage());
            logger.error(e.getMessage());
            if (command.throwable()) {
                throw e;
            }
        }
    }

}
 