package similarity.multithread;

import java.util.Map;

public class HammingWorker extends Worker<Map<String, Integer>> {

	private Map<String, Integer> result;
	
	@Override
	public void run() {
		// retrieve job from jobQueue
		while (true) {
			Job job = super.jobQueue.poll();

			if (job == null)
				break;
			
			if (job instanceof HammingJob) {
				job = (HammingJob)job;
				result = handle(job);
//			
//			if (result.size() == this.job.getCompare().size()) {
//				stop();
//			}
				super.resultMap.put(job.getJobId(), result);
			}

		}
	}

	@Override
	public Map<String, Integer> handle(Job job) {
		result = job.calcHammingDistance();
		return result;
	}
	
}
