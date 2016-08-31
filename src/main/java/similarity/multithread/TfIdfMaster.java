package similarity.multithread;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import common.MapUtil;

public class TfIdfMaster extends Master<Map<String, Double>>{

	public TfIdfMaster(Worker<Map<String, Double>> worker, int workerCount) {
		super(worker, workerCount);
	}

	@Override
	public Map<String, Double> processMultiResult() {
		Map<String, Double> result = new HashMap<>();
		//for buffer
		Map<String, Double> map;
		//对result做合并排序
		for(Entry<String, Map<String, Double>> me : super.resultMap.entrySet()){
			map =  me.getValue();
			result.putAll(map);
		}
		//let GC do the work
		map = null;
		System.out.println("total size: " + result.size());
		return MapUtil.sortByValue(result);
	}

}
