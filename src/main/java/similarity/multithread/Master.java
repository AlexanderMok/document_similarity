package similarity.multithread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class Master<V> {
	
	/**
	 * TODO should read from configuration file
	 */
	private static final int CORESIZE_OF_THREAD = 4;
	
	private static final int MAXSIZE_OF_THREAD = 8;
	
	/**
	 * <P>Thread pool/worker pool</p>
	 * <P>This pool is set public for ServletOFSimilarity to use, may expand to other circumstances as well</p>
	 * <P>空闲线程30S后回收，超过最大线程量，缓存在阻塞队列中等待<p>
	 */
	
	public static final ExecutorService pool = new ThreadPoolExecutor(CORESIZE_OF_THREAD, MAXSIZE_OF_THREAD, 30L,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),Executors.defaultThreadFactory());

	/**
	 * 1. a concurrent jobQueue for multi-thread to retrieve
	 */
	protected static final ConcurrentLinkedQueue<Job> jobQueue = new ConcurrentLinkedQueue<>();
	
	/**
	 * 2. a concurrent collection to store results from multi-thread
	 */
	protected ConcurrentHashMap<String, V> resultMap = new ConcurrentHashMap<>();
	
	/**
	 * 3. a collection to control Workers and allocate Jobs to Workers
	 */
//	protected static final HashMap<String, Worker<?>> workers = new HashMap<>();
	protected static final HashMap<String, Thread> workers = new HashMap<>();
	
	
	
	/**
	 * 4. assign workers
	 * @param worker
	 * @param workerCount
	 */
	protected Master(Worker<V> worker,int workerCount) {
		//worker needs to get a job and submit result
		worker.setJobQueue(Master.jobQueue);
		worker.setResultMap(this.resultMap);
		worker.setPool(pool);
		//allocate job to each worker
		for (int i = 0; i < workerCount; i++) {
			workers.put("worker node-" + i, Executors.defaultThreadFactory().newThread(worker));
		}
	}
	
	/**
	 * 5. submit job to job queue in Master
	 * @param job
	 */
	public void submit(Job job) {
		Master.jobQueue.add(job);
	}
	
	/**
	 * 6. start the application and each worker. Execute jobs.
	 */
	public void executeMutiWorker() {
		/*for(Map.Entry<String, Worker<?>> me: workers.entrySet()) {
			pool.submit(me.getValue());
		}
		pool.shutdown(); // open this if 
		*/
		for(Map.Entry<String, Thread> me: workers.entrySet()) {
			me.getValue().start();
		}
	}
	
	/**
	 * 8. Whether workers/threads complete their jobs or not
	 * @return true if completed or false if one worker/thread among the submitted is still Running
	 */
	public boolean isCompleted() {
//		for(Map.Entry<String, Worker<?>> me: workers.entrySet()) {
//			if(me.getValue(). == true) {
//				return false;
//			}
//		}
//		return true;
		
		for(Map.Entry<String, Thread> me: workers.entrySet()) {
			if (me.getValue().getState() != Thread.State.TERMINATED) {
				return false; 
			}
		}
		return true;
//		return pool.isTerminated();
	}
	
	/**
	 * 9. reduce process, Sub Master must override this method.
	 * @return
	 */
	public abstract Map<String, ?> processMultiResult();

}
