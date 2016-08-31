package similarity.tokenizer.jieba;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import common.MapUtil;
import similarity.tokenizer.jieba.keyword.IDFLoader;
import similarity.tokenizer.jieba.keyword.KeywordWeightPair;
import similarity.tokenizer.jieba.keyword.StopwordsLoader;


public class JiebaNLP extends StopwordsLoader{
	private static JiebaSegmenter tokenizer;
	private static IDFLoader idfLoader;
	private static HashMap<String, Double> corpus_idf;
	private static double avgIdf;
	private static final Path DEFAULT_IDF_PATH = FileSystems.getDefault().getPath(".", "/models/stopwords/idf.txt");
	
	private JiebaNLP(){
		init(DEFAULT_IDF_PATH);
	}
	
	private static void init(Path idfPath) {
		tokenizer = new JiebaSegmenter();
		idfLoader = new IDFLoader(idfPath);
		corpus_idf = idfLoader.idfFreq();
		avgIdf = idfLoader.medianIDF();
	}
	
	private static class newInstance {
		private static final JiebaNLP key = new JiebaNLP();
	}
	
	public static JiebaNLP JiebaExtractor(){
		return JiebaNLP.newInstance.key;
	}
	
	/**
	 * 分词
	 * @param text text to extract
	 * @param topN Return all of keywords
	 * @return tokens - a list of tokens
	 */
	public List<String> JiebaNLPKeyword(String text) {
		List<String> tokens = tokenizer.sentenceProcess(text);
		return tokens;
	}
	
	/**
	 * 返回词频最高的topN个词，倒序
	 * @param text
	 * @param topN
	 * @return tokens - a list of tokens 
	 */
	public List<String> JiebaNLPKeywordByTF(String text, int topN) {

		Map<String, Integer> tfMap = calc_tf(text, topN);
		List<String> tokens = new ArrayList<String>();
		for (String string : tfMap.keySet()) {
			tokens.add(string);
		}
		tfMap = null;
		return tokens;
	}
	
	
	public List<String> JiebaNLPKeywordByTF8(String text, int topN){
		
		Map<String, Integer> tfMap = calc_tf(text, topN);
		return tfMap.keySet().stream()
		  .limit(topN)
		  .collect(Collectors.toList());
	}
	
	
	
	/**
	 * 与calc_tfidf(String text, int topN) 一样
	 * @param text text to extract
	 * @param topN Return number of top keywords, -1 for all words
	 * @return KeywordWeightPair - an ArrayList of Pair (word,weight)
	 */
	public List<KeywordWeightPair> tokenWeightPair(String text, int topN){
		long s = System.currentTimeMillis();
		Map<String, Double> weightMap = calc_tfidf(text,topN);
		List<KeywordWeightPair> tags = new ArrayList<KeywordWeightPair>();
		if (topN == -1)
			topN = Integer.MAX_VALUE;
		
		for (Entry<String, Double> entry : weightMap.entrySet()) {
			if (tags.size() == topN){
				break;
			}	
			tags.add(new KeywordWeightPair(entry.getKey(), entry.getValue()));
		}
		System.out.println(
				String.format(Locale.getDefault(), "Term-Frequency * Inverse-Document-Frequency wrapped in KeywordWeightPair calc finished, tot words:%d, time elapsed:%dms",
						tags.size(), System.currentTimeMillis() - s));
		return tags;
	}
	
	
	
	/**
	 * 计算  weight = tf * idf / totalTf，作为 tf*idf 原始公式的变通
	 * @param text text to extract
	 * @param topN Return number of top keywords, -1 for all words
	 * @return KeywordWeightPair - an Map of Pair (word,weight)
	 */
	public Map<String, Double> calc_tfidf(String text, int topN) {
		long s = System.currentTimeMillis();
		double totalIf = 0.0;
		double idf = 0.0;
		double weight = 0.0;

		Map<String, Integer> tfMap = calc_tf(text, topN);
		
		HashMap<String, Double> weightMap = new HashMap<String, Double>();

		for (double tf : tfMap.values()) {
			totalIf += tf;
		}

		for (String word : tfMap.keySet()) {

			idf = corpus_idf.containsKey(word) ? corpus_idf.get(word) : avgIdf;

			weight = tfMap.get(word) * idf / totalIf;

			weightMap.put(word, weight);
		}
		System.out.println(
				String.format(Locale.getDefault(), "Term-Frequency * Inverse-Document-Frequency calc finished, tot words:%d, time elapsed:%dms",
						weightMap.size(), System.currentTimeMillis() - s));

		return MapUtil.sortByValue(weightMap);
	}
	
	/**
	 * 该方法完成分词、去除停用词和根据词频排序
	 * @param doc text to be calculated
	 * @param topN 指定取topN个词的tf，若为-1，输出全部分词
	 * @return
	 */
	public Map<String, Integer> calc_tf (String doc, int topN) {
		long s = System.currentTimeMillis();
		List<String> words = JiebaNLP.JiebaExtractor().JiebaNLPKeyword(doc);
		
		Map<String, Integer> tf = new TreeMap<String, Integer>();
		
		for (String word : words) {
			//eliminate stopwords
			if (word.trim().length() < 2 || stopWords.contains(word.toLowerCase())) {
				continue;
			}
			// count word frequency in a doc
			tf.put(word, tf.get(word) != null ? (tf.get(word) + 1) : 1);
		}
		words= null;
		tf = MapUtil.sortByValue(tf);
		
		if (topN == -1) {
			System.out.println(
					String.format(Locale.getDefault(), "Term Frequency calc finished, tot words:%d, time elapsed:%dms",
							tf.size(), System.currentTimeMillis() - s));
			return tf;
		}
		
		//截取前topN个
		Map<String, Integer> tfMap = new TreeMap<String, Integer>();
		
		
		for (Map.Entry<String, Integer> me : tf.entrySet()) {
			if (tfMap.size() == topN) {
				break;
			}
			tfMap.put(me.getKey(), me.getValue());
		}
		System.out.println(
				String.format(Locale.getDefault(), "Term Frequency calc finished, tot words:%d, time elapsed:%dms",
						tfMap.size(), System.currentTimeMillis() - s));
		return MapUtil.sortByValue(tfMap);
	}
	
}
