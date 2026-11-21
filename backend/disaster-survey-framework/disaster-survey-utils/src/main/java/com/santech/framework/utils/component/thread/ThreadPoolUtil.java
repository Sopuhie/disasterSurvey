package com.santech.framework.utils.component.thread;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @explain 线程池工具类
 */
public class ThreadPoolUtil {
	
	private int						corePoolSize	= 5;	//最小线程数量 
	private int						maximumPoolSize	= 100;	//最大线程数量 
	private int						keepAliveTime	= 30;	//线程保留时间 单位 秒
	private int						capacity		= 500;	//队列数量 

	private static ThreadPoolUtil	instance;
	private ThreadPoolExecutor		threadPool;

	
	private ThreadPoolUtil() {
		threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(capacity),
				new TheadRejectHandler());
	}


	public static synchronized ThreadPoolUtil getInstance() {
		if (instance == null) {
			instance = new ThreadPoolUtil();
		}
		return instance;
	}


	public static void startTask(Runnable command) {
		ThreadPoolUtil.getInstance().threadPool.execute(command);
	}


	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}


	public void setThreadPool(ThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
	}
	
}
 