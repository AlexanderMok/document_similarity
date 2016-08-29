package similarity.multithread;

import java.util.Map;

public class TfIdfWorker extends Worker<Map<String, Double>>{
	
	private Map<String, Double> result;
	
	@Override
	public void run() {
		// retrieve job from jobQueue
		while (true) {
			TfIdfJob job = (TfIdfJob) super.jobQueue.poll();

			if (job == null)
				break;

			result = handle(job);
			super.resultMap.put(job.getJobId(), result);
		}
	}

	@Override
	public Map<String, Double> handle(Job job) {
		result = job.calcCosinSimilarity();
		return result;
	}

}
