package common;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @description sort a map by its value. ascend order
 * @author Alexander Mok
 * @date 2016年8月15日
 */
public class MapUtil {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * jdk 8
	 * @param tfMap
	 * @return
	 */
	public static Map<String, Integer> sort_tfByValue(Map<String, Integer> tfMap) {
		return tfMap.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	public static double max_tf(Map<String, Integer> sortedMap) {
		return sortedMap.entrySet().stream()
				.findFirst()
				.get()
				.getValue();
	}
	
	public static double max_tf_map(Map<String, Integer> sortedMap) {
		double max = 0.0;
		for (Map.Entry<String, Integer> me : sortedMap.entrySet()) {
			max = me.getValue();
			break;
		}
		return max;
	}
}
