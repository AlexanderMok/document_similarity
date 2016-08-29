package similarity.multithread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import similarity.tokenizer.KeyWordExtractor;


public class TfIdfJob extends Job {
	private String jobId;
	private String base;
	private Map<String,String> compare;
	

	
	public TfIdfJob(String jobId, String base, Map<String, String> compare) {
		super.setJobId(jobId);
		this.jobId = jobId;
		this.base = base;
		this.compare = compare;
	}
	
	private static class Container {
		private static final ConcurrentHashMap<String, Double> cosinMap = new ConcurrentHashMap<>();
	}

	@Override
	public Map<String, Double> calcCosinSimilarity() {
		double cosin = 0.0;
		for (Map.Entry<String, String> me : compare.entrySet()) {
			cosin = KeyWordExtractor.calcSimilarity(base, me.getValue(), -1);
			Container.cosinMap.put(me.getKey(), cosin);
		}
		return Container.cosinMap;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public Map<String, String> getCompare() {
		return compare;
	}

	public void setCompare(Map<String, String> compare) {
		this.compare = compare;
	}
}
