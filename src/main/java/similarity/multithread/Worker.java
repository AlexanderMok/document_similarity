package similarity.multithread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;


public abstract class Worker<V> implements Runnable{

	
	protected ConcurrentLinkedQueue<Job> jobQueue;
	protected ConcurrentHashMap<String, V> resultMap;
	protected ExecutorService pool;
	
	
	//多线程间是否启动变量，强制从主内存中刷新。即时返回线程的状态
	//因为在test package要单元测试，改为public 
	public volatile boolean isRunning = true;
	
	protected void setJobQueue(ConcurrentLinkedQueue<Job> jobQueue) {
		this.jobQueue = jobQueue;
	}
	
	protected void setResultMap(ConcurrentHashMap<String, V> resultMap) {
		this.resultMap = resultMap;
	}
	
	protected void setPool(ExecutorService pool) {
		this.pool = pool;
	}

	/**
	 * 7. execute job
	 */
	@Override
	public void run() {
		/*//retrieve job from jobQueue
		while(true){
			Job job = jobQueue.poll();
			
			if (job == null) break;
			
			Object result = handle(job);
			
			this.resultMap.put("jobId", result);
		}*/
	}
	
	public void stop() {
		this.isRunning = false;
	}
	
	public abstract Map<String, ?> handle(Job job);


}
