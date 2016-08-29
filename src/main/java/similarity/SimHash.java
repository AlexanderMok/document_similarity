package similarity;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.fnlp.util.exception.LoadModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import similarity.hash.HashFunction;
import similarity.tokenizer.KeyWordExtractor;



/**
 * 
 * @description
 * Simhash算法步骤
 * <p>1.文档分词，提取关键词与权重，抽取n个<feature,weight> pair</p>
 * <p>2.把当中的feature映射为hashCode, 64位.其他位数也可以</p>
 * <p>3.<hash(feature),weight>做位运算，位的纵向累加。若该位为1，则+weight，为0则-weight</p>
 * <p>4.经过3运算，生成1 * 64维向量</p>
 * <p>5.遍历向量，正数转化为1，负数转化为0。得到SimHash</p>
 * @date 2016年8月3日
 */
public class SimHash {
	
	private static final Logger logger = LoggerFactory.getLogger(SimHash.class);

	
	private final static int BITS_LENGTH = 64;
	
	/**
	 * 1.提取(feature,weight) pair
	 * @param doc
	 * @param topN
	 * @return
	 * @throws LoadModelException 
	 */
	private static Map<String,Integer> extract(String doc, int topN) throws LoadModelException {
		return KeyWordExtractor.FNLPKeyWordExtract(doc, topN);
	}
	
	
	/**
	 * TODO 如何应对分出的词少于64个的情况
	 * 2.把当中的feature映射为hashCode,64位
	 * @param doc
	 * @param topN
	 * @return
	 * @throws LoadModelException 
	 */
	private static Map<BigInteger, Integer> hashFeature(String doc,int topN) throws LoadModelException {
		
		Map<String,Integer> feature_weight_pair = extract(doc, topN);
		
		if (feature_weight_pair == null) {
			logger.error("extract (feature,weight) pair failed");
			return null;
		}
		
		if (feature_weight_pair.size() < 64) {
			int size = feature_weight_pair.size();
			for(int i = size; i < 64; i++) {
				feature_weight_pair.put("0" + i, 0);
			}
		}
		
		String feature;
		Integer weight;
		Map<BigInteger,Integer> hash_weight_pair = new HashMap<BigInteger,Integer>(BITS_LENGTH);
		
		Iterator<Entry<String, Integer>> iterator = feature_weight_pair.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			
			feature = entry.getKey();
			weight = entry.getValue();
			hash_weight_pair.put(HashFunction.hashUnsigned64(feature).toBigIntegerExact(), weight);
		}
		logger.info("hash_weight_pair successfully created {}",hash_weight_pair);
		return hash_weight_pair;
	}
	
	/**
	 * 位的累加。若该位为1，则+weight，为0则-weight
	 * @param doc
	 * @param topN
	 * @return
	 * @throws LoadModelException 
	 */
	private static int[] bitCalculation(String doc,int topN) throws LoadModelException {
		
		Map<BigInteger,Integer> hash_weight_pair = hashFeature(doc, topN);
		
		if (hash_weight_pair == null) {
			logger.error("hash (feature,weight) pair failed");
			return null;
		}
		
		Iterator<Entry<BigInteger,Integer>> iterator = hash_weight_pair.entrySet().iterator();
        
		int i;
		final long u64_1 = 1L;
		int[] weights = new int[BITS_LENGTH]; //存放simhash的数组
		BigInteger hashFeature;
		Integer weight;
		
		while (iterator.hasNext()) {
			Entry<BigInteger, Integer> entry = iterator.next();
			
			hashFeature = entry.getKey();
			weight = entry.getValue();
			
			for (i = 0; i< BITS_LENGTH; i++) {
				//C++ 的实现,可控制符号位，weights[j] += ( ( (u64_1 << j) & (feature) ) != 0 ? 1: -1 ) * weight;
				//必须使用BigInteger避免  二进制位 符号位 引起的异常
				weights[i] += (((new BigInteger(String.valueOf(u64_1 << i)).and(hashFeature))
							.compareTo(BigInteger.ZERO)) != 0 ? 1 : -1) * weight;
			}
		}
		logger.info("Weights array successfully transformed. Length is [{}]",weights.length);
		return weights;
	}
	
	
	/**
	 * 5. 遍历权重数组/向量，正数为1，负数为0
	 * @param doc
	 * @param topN
	 * @return
	 * @throws LoadModelException 
	 */
	public static BigInteger simHashCode(String doc, int topN) throws LoadModelException {
		
		long begin = System.nanoTime();
		
		int[] weights = bitCalculation(doc, topN);
		
		if(weights == null || weights.length < 0) {
			logger.error("weights vector caculation error");
		}
		
		
		BigInteger simhashCode = BigInteger.ZERO;
		
		for (int i = 0; i< BITS_LENGTH; i++) {
			if (weights[i] > 0) {
				simhashCode = simhashCode.add(BigInteger.ONE.shiftLeft(i));
			}	
		}
		
		long end = System.nanoTime();
		
		logger.info("SimHash by BigInteger [{}] successfully generated. Cost [{}] seconds", simhashCode, (end - begin)/10E9);
		return simhashCode;
	}
	
	/**
	 * 根据海明距离判断是否相似 
	 * @param leftHash doc simhash one
	 * @param rightHash doc simhash two
	 * @param n 相似阀值
	 * @return
	 */
	public static boolean hammingDistanceSimilarity(BigInteger leftHash, BigInteger rightHash, short n) {
		
		int threshold = hammingDistance(leftHash, rightHash);
		
		// n 表示 阀值,小于n,判为相似
		if (threshold <= n) {
			return true;
		}
        return false;
	}
	
	/**
	 * 计算汉明距离
	 * <p>统计x中二进制位数为1的个数</p>
	 * <p>我们想想，一个二进制数减去1，那么，从最后那个1（包括那个1）后面的数字全都反了，对吧，然后，n&(n-1)就相当于把后面的数字清0，</p>
	 * <p>我们看n能做多少次这样的操作就OK了。</p>
	 * @param BigInteger leftHash
	 * @param BigInteger rightHash
	 * @return hamming distance
	 */
	public static int hammingDistance(BigInteger leftHash, BigInteger rightHash) {
		BigInteger x = leftHash.xor(rightHash);
		int distance = 0;
		//只要二进制的x不为0,当为0时，二进制中的1全部变为0，退出循环
		while (x.signum() != 0) {
			x = x.and(x.subtract(BigInteger.ONE));
			distance += 1;
			
		}
		return distance;
	}
	
	/**
	 * 计算汉明距离
	 * 将64位二进制数字符串转换成数组/向量，计算不一致的数量
	 * @param String binaryStringOne
	 * @param String binaryStringTwo
	 * @return
	 */
	public static short hammingDistance(String binaryStringOne, String binaryStringTwo) {
		char[] one = binaryStringOne.toCharArray();
		char[] two = binaryStringTwo.toCharArray();

		short distance = 0;
		for (int i = 0; i < one.length; i++) {
			if (one[i] != two[i]) {
				distance += 1;
			}
		}
		return distance;
	}
}