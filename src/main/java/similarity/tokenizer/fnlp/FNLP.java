package similarity.tokenizer.fnlp;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.fnlp.app.keyword.AbstractExtractor;
import org.fnlp.app.keyword.WordExtract;
import org.fnlp.nlp.cn.tag.CWSTagger;
import org.fnlp.nlp.cn.tag.POSTagger;
import org.fnlp.nlp.corpus.StopWords;
import org.fnlp.nlp.parser.dep.JointParser;
import org.fnlp.util.exception.LoadModelException;



/**
 * 
 * @description FNLP分词器,透过Text Rank算法提取关键词和权重。FNLP有原生的CNFactory统一入口，不完全的单例模式，不适宜多线程环境,这里重新
 * @author Alexander Mok
 * @date 2016年8月5日
 */
public class FNLP {
	
	
	private final static String SW_PATH = "models/stopwords";
	private final static String SEG_PATH = "models/seg.m";
	private final static String DEP_PATH = "models/dep.m";
	private final static String POS_PATH = "models/pos.m";
	
	private static CWSTagger segment;
	private static JointParser parser;
	private static POSTagger tagger;
	private FNLP() {
	}
	
	/**
	 * static inner class singleton pattern and it is friendly to multithread
	 */
	private static class newInstance {
		private static final StopWords stopWords = new StopWords(SW_PATH);
		
		private static final AbstractExtractor key = new WordExtract(seg(), stopWords);
		
		/**
		 * secure singleton instance
		 * @return
		 */
		private static CWSTagger seg() {
			if (segment == null) {
				try {
					segment = new CWSTagger(SEG_PATH);
					// 是否对英文单词进行预处理，将连续的英文字母看成一个单词
					segment.setEnFilter(true);
					return segment;
				} catch (LoadModelException e) {
					e.printStackTrace();
				}
			}
			return segment;
		}
		
		private static JointParser dep() {
			if (parser == null) {
				try {
					parser = new JointParser(DEP_PATH);
					return parser;
				} catch (LoadModelException e) {
					e.printStackTrace();
				}
			}
			return parser;
		}
		
		private static POSTagger tag() {
			if (tagger == null) {
				try {
					tagger = new POSTagger(seg(), POS_PATH);
					return tagger;
				} catch (LoadModelException e) {
					e.printStackTrace();
				}
			}
			return tagger;
		}
	}
	
	/**
	 * get WordExtractor to extract keyword and weight 分词器实例
	 * @return WordExtract
	 */
	public static AbstractExtractor FNLPExtractor() {
		return FNLP.newInstance.key;
	}
	
	/**
	 * 依存句法分析器实例
	 * @return
	 */
	public static JointParser FNLPDepanency () {
		return FNLP.newInstance.dep();
	}
	
	/**
	 * Part of Speech Tagger 词性标注器实例
	 * @return
	 */
	public static POSTagger FNLPTagger () {
		return FNLP.newInstance.tag();
	}
	
	/**
	 * Tokenize the doc and convert tokens to String array
	 * @param doc
	 * @param count
	 * @return
	 */
	public static String[] token2Array(String doc, int count) {
		Set<String> itr = FNLP.FNLPExtractor().extract(doc, count).keySet();
		
		if (itr.size() < count) {
			for (int i = itr.size(); i < count; i++) {
				itr.add("");
			}
		}
		return itr.toArray(new String[count]);
	}
	
	
	/**
	 * Retrieve weight array 获取TextRank算法计算出来的分词权重
	 * @param doc
	 * @param count
	 * @return
	 */
	public static int[] token2WeightArray(String doc, int count){
		Collection<Integer> tokenWeight = FNLP.FNLPExtractor().extract(doc, count).values();
		if (tokenWeight.size() < count) {
			for (int i = tokenWeight.size(); i < count; i++) {
				tokenWeight.add(0);
			}
		}
		return ArrayUtils.toPrimitive(tokenWeight.toArray(new Integer[count]));
	}
	
}
