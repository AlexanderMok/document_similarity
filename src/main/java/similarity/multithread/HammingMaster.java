package similarity.multithread;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import common.MapUtil;
/**
 * 
 * @description
 *     Implementation of specific logic of reduce process of HammingDistance calculation     
 * @author Alexander Mok
 * @date 2016年8月15日
 */
public class HammingMaster extends Master<Map<String, Integer>> {

	public HammingMaster(Worker<Map<String, Integer>> worker, int workerCount) {
		super(worker, workerCount);
	}

	@Override
	public Map<String, Integer> processMultiResult() {
		Map<String, Integer> result = new HashMap<>();
		//for buffer
		Map<String, Integer> map;
		//对result做合并排序
		for(Entry<String, Map<String, Integer>> me : super.resultMap.entrySet()){
			map =  me.getValue();
			result.putAll(map);
		}
		//let GC do the work
		map = null;
		System.out.println("总的size: " + result.size());
		return MapUtil.sortByValue(result);
	}

}
