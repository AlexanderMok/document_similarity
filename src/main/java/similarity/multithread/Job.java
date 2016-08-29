package similarity.multithread;

import java.math.BigInteger;
import java.util.Map;

/**
 * 
 * @package com.kass.app.app_user.models.similarity.multithread
 * @project app-kasscloud
 * @description abstract class for job. Methods here are empty implementation.
 *              Subclasses must override methods needed
 * @author 莫庆来
 * @date 2016年8月15日
 */
public abstract class Job {
	
	private String jobId;
	
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Map<String, Integer> calcHammingDistance(){return null;};

	public Map<String, BigInteger> calcSimhashCode(){return null;};
	
	public Map<String, Double> calcCosinSimilarity(){return null;};

}
