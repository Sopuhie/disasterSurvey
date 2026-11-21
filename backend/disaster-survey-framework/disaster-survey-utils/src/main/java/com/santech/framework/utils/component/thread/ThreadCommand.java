package com.santech.framework.utils.component.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @explain
 */
public abstract class ThreadCommand implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 回调方法，触发节点：执行完成、拒绝加入队列
     */
    public void callback() {
    }

    /**
     * 返回错误信息
     */
    public String errorMessage() {
        return null;
    }

    /**
     * 拒绝加入队列时是否抛出异常
     * @return
     */
    public boolean throwable(){
        return false;
    }
    @Override 
    public void run() {
        try {
            this.execute();
        } catch (Exception e) {
            logger.error(errorMessage(), e);
        } finally {
            this.callback();
        }
    }

    /**
     * 线程执行的主方法，此方法内写操作
     */
    public abstract void execute() throws Exception;
}
 