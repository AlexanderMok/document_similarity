package similarity.multithread;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import common.StringTool;

public class Test {
	
	public static void main(String[] args) {
		
		long start = System.nanoTime();
		
		
		Random r = new Random();
		BigInteger base = new BigInteger("12422190482967595305");
		Master master=null;
		HammingWorker hammingWorker = null;
		Map<String,BigInteger> compare = new HashMap<>();
		HammingJob job = null;
		for (int i = 0; i < 500; i++) {
			//模拟一堆文档
			compare.put(StringTool.getRandomString(8), BigInteger.probablePrime(64, r));
			job = new HammingJob(StringTool.getRandomString(8), base, compare);
			hammingWorker = new HammingWorker();
			master = new HammingMaster(hammingWorker, Runtime.getRuntime().availableProcessors());
			master.submit(job);
		}
		
		
		//模拟一堆job计算
		
		System.out.println("Master提交job后的hammingWorker运行状态1：" + hammingWorker.isRunning);
		
		master.executeMutiWorker();
		
		
		
		System.out.println("Master执行job后的hammingWorker运行状态2：" + hammingWorker.isRunning);
		while(true){
			if(master.isCompleted()){
				System.out.println("Master执行job后的hammingWorker运行状态3：" + hammingWorker.isRunning);
				Map<String, Integer> result = master.processMultiResult();
				long end = System.nanoTime() - start;
				System.out.println("最终结果：" + result + ", \n执行时间(秒)：" + end/10E9 + ", 结果大小： " + result.size());
				break;
			}
		}
		
	}
}
