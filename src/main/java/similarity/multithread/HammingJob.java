package similarity.multithread;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import similarity.SimHash;



public class HammingJob extends Job {
	
	private String jobId;
	private BigInteger base;
	private Map<String,BigInteger> compare;
	

	
	public HammingJob(String jobId, BigInteger base, Map<String, BigInteger> compare) {
		super.setJobId(jobId);
		this.jobId = jobId;
		this.base = base;
		this.compare = compare;
	}
	
	private static class Container {
		private static final ConcurrentHashMap<String, Integer> distanceMap = new ConcurrentHashMap<>();
	}


	@Override
	public Map<String, Integer> calcHammingDistance() {

		int distance = 0;
		for (Map.Entry<String, BigInteger> iterator : compare.entrySet()) {
			distance = SimHash.hammingDistance(base, iterator.getValue());
			Container.distanceMap.put(iterator.getKey(), distance);
		}
		return Container.distanceMap;
	}
	
	
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public BigInteger getBase() {
		return base;
	}

	public void setBase(BigInteger base) {
		this.base = base;
	}

	public Map<String, BigInteger> getCompare() {
		return compare;
	}

	public void setCompare(Map<String, BigInteger> compare) {
		this.compare = compare;
	}

}
