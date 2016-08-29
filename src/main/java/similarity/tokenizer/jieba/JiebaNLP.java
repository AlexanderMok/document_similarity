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

import similarity.MapUtil;
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
	
	/*
	public List<String> JiebaNLPKeywordByTF8(String text, int topN){
		
		Map<String, Integer> tfMap = calc_tf(text, topN);
		return tfMap.keySet().stream()
		  .limit(topN)
		  .collect(Collectors.toList());
	}
	*/
	
	
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
	
	
	public static void main(String[] args) {
		
		String doc= "目前拿到的知名公司的offer是腾讯和新美大（大众点评、美团）。一直想写一篇面经分享给大家，但因为一些琐碎的事情，就一直耽误着。 今天就特意拿出一些时间去写它。很多人都分享了很多的面经，而且还有好多面试题目在很多社区都有相关的资料。很多面试题也都有相关的解析，但是仅仅是刷这些面试题其实并不一定可以找到自己心仪的工作。下面就主要介绍下我是如何在面试前做准备工作的。我经历的一些面试因为大三了，从今年3月份开始，自己开始找工作，开始也不明白该怎么做，就简单写了一了一份简历，简历上就是一些自己相关的学习经历。最开始的时候选择了一些自己觉得可以的小公司，并且获得了一些面试机会，第一次面试很紧张，不知道该跟面试官说什么，不知道该如何去介绍自己，不知道该怎么做，就是傻傻的一顿被问，正如你所想的，我不可能所有的问题答的都很好，因为基本上面试官是想到什么就问什么(他可能是准备好的问题，但是我真的觉得我的第一次面试是他想到什么就问什么)。面试结束后，我就好好总结了下，然后又开始了第二家。这么经历了几次就发现，自己不在那么紧张了，自己开始占据主动权，引领着面试官走。所以现在回忆一下，多面一些还是能增长很多经验的。不管是你表达能力的提升，我所经历的面试官人都非常好，开始可能会很紧张，但是后来感觉就像是在聊天，两个志同道合的人聊着同一个话题，从这个聊天的过程当中也收获了很多自己不知道的东西。新美大面试经历美大的面试官一定要点个赞，其实面过以后会发现，不要很紧张什么。美大的面试官很随和具体的面试问题就不说了，基本上一面的时候还是很关注基础的，然后最后的20分钟左右都是聊一些经验，我当时是说起了一个话题，因为开始的时候聊一些基础，然后就是聊到我的项目中的图片的动态缓存方案，就一直在聊这个，最后又提起了组件化的开发方案，我们最后就是一直在聊这个话题。一面就最后接到通知就过了。二面的时候，就是视频面，在一个平台上，可以视频，并且我在那个平台上写的代码对方也是可以看见，给大家一个建议，别人看你写代码你可能会很紧张，但是不要太慌张，我感觉正常发挥就好了，对方主要还是想看看的代码的风格怎么样？是不是很规范，注释啊，还有一些变量的命名啊，这些是否很规范。另一个就是想看大家的处理能力，其实给你的题目都不是很难，都是一些简单的基础的东西，不会说让你现场写出一个什么很麻烦的布局，很牛的特效的。这次面试大概经历了一个小时左右，最后一部分时间还聊了很多生活上的事情。例如就是来到美大后准备想学一些什么啊？你对美大哪一方面的技术很感兴趣啊？就是这些，喜欢什么就聊一些什么就可以了。整体的两轮面试还是学到很多东西的，毕竟现在在学校不知道外面的公司是什么样的，通过面试官还是能了解到外面很多东西的。腾讯的面试经历腾讯的面试就是自己经历了腾讯的笔试，然后一面，二面，最后HR面，然后等消息，最后得到offer。笔试的话基本上就是一些基础知识，像数据结构，计算机组成原理，操作系统，计算机网络这些。上课认真听听，笔试前刷一些题目问题还是不大的。腾讯一面，主要就是聊一些基础，我是去面移动客户端，但是整个面试移动相关的问题很少，主要还是在围绕着一些基础再聊。如果你想拿到腾讯的offer，我觉得基础是非常重要的。面试题的话，我觉得这个其实没什么可提的，因为这些题目都是不固定的，面试官主要还是在围绕着你的简历再跟你聊，你的简历中写到你的APP中涉及过下载，那么网络必问，问什么？我觉得TCP，HTTP都是值得问的。有一本书叫《程序员的自我修养》我觉得这本书有时间还是很需要去读一下的。我见到过很多人吐槽说，我是去面iOS，面试我的竟然是一个Android的，从我个人的角度来看，无论是iOS，还是Android在很多的设计思想上面还是有很多相通的地方的。说出你的想法就可以了。我个人觉得思想还是很重要的。腾讯二面给我最大的印象就是面试官说的一句话，挑一个你觉得可以的项目，然后我们聊一些东西。从这句话我感觉很多人都能够感觉出来，二面主要还是聊你的，一定给有一个项目才行。并且好好去准备这个东西。同样没有什么具体的问题，每个人的项目都不一样，实现方案也不可能完全一样。所以聊的东西也就不可能完全一样。二面什么太多的经验，主要还是认真的去准备一个项目。面试前的准备这是我最想跟大家聊的东西，因为关于面试前的准备，或者是准备什么还是很有必要重点分享下的。很多人面试的时候不知道说什么，同样很多人都说要引领着面试官，不要被面试官引领。但是该怎么做呢？难道我们不让面试官说话？我们给面试官规定题目？这都是不现实的，对吧？但是通过一些准备，还是可以实现让我们去给面试官”规定题目”的，怎么做？举一些简单的例子，例如：你的简历当中写到自己对Socket有一些了解，那么很自然的就会聊到TCP，你提前好好准备下，你自己都是可以预知的，当面试官问听到你Socket的时候，自然就会走到TCP上。如何准备？很多人上网上查了一些，TCP很经典的就是连接的三次握手，断开时的四次挥手。你也简单的查了一下三次握手什么样，四次挥手什么样，但是这仅仅就够了吗？我们为什么不在往更深层去准备下呢？例如连接为什么是三次，断开为什么是四次？连接如果两次会发生什么？三次握手中总共发送了三个包，那么仅仅就是做了三个作用吗？更往深处可能会涉及到TCP的头部结构，以及TCP连接，断开时SYN，FIN，ACK的有效值设置又是什么样的？我相信你把这些都说出来肯定是会被加分的。当然你想很好的说出来不是说你面试前查查就可以了，肯定是需要去好好的去理解的。这也就是我说的面试前的准备，不要为了面试而面试，而是要为了面试而去学习，提升自己。我可以再举个简单的例子，关于图片缓存这个东西，在iOS，Android中有很多动态的图片缓存解决方案。你的简中如果写到做过相关的操作，我觉得这个东西还是很值得去问的。明知道会被问，面试前肯定要好好准备下啊。准备的过程当中我觉得需要总结出自己的方案中优点有哪些，缺点有哪些，针对你的缺点你准备怎么做，或者说你可以针对你知道的一些知名的开源库是如何解决的，你有什么可以借鉴的，这样我感觉你肯定是要被加分的，最起码你是阅读过开源库，并且认真的去思考过。我就见过有人被问到如何做图片缓存的时候，他就简单的说了一句通过URL下载图片然后保存起来，以便利用。这个话题当中有很多东西都是可以去值得聊的，就说这么一句话，我觉得面试官不可能得到他想要的答案，他没有得到答案，肯定是要去接着问你的。这个时候你就会显着的很被动，接下来就是要被面试官引领着走了。有人会问有什么可以聊的？我举一些简单的例子，例如图片的缓存方案肯定是要涉及到图片的下载问题，既然是下载，那么下载进度信息的回调你怎么做的?下载失败怎么办？下载完成的回调怎么做的?下载肯定涉及到多线程的下载，那么这些线程你是如果管理的？线程管理器的结构又是什么样的？甚至是一个URL下载连接正在被下载，另一个地方发起了一个相同URL的下载，这种情况我认为为了节省资源不应该在新开一个线程去下载，那么这种情况你有做处理吗？如何做的?其实有很多值得考虑的问题和值得去说的。做缓存，你做的是磁盘缓存啊？还是内存缓存？还是两者都有？是否支持磁盘空间大小的设置，图片有效期的设置如何做的，图片的清理工作，我相信无论在iOS还是Android那些知名的开源库不可能就是简单的有一个清理方案，肯定是支持部分清理，或者全部清理，部分清理的依据又是什么？例如依据图片的有效期来做清理，过了我们设定的有效期，这个时候我们就需要去清理掉这部分内容。另外如果如果所有缓存文件的总大小超过这一大小，则会按照文件最后修改时间的逆序，以每次一半的递归来移除那些过早的文件，直到缓存的实际大小小于我们设置的最大使用空间。这些东西都是有很多值得去说的，当然你只要去感悟的越深，理解的越深，就会体会的越深，表达的就会越好。刚刚提到的东西我觉得聊个20分钟还是很容易的，因为这个里面存在的东西和值得注意的地方太多了。准备好一个项目，找出你项目中的一个亮点，然后认真的去总结，并且看一些别人的方案，看看有哪些你没有的，你没有的就是你方案的缺陷，看看该怎么解决。我觉得，缺陷还是很重要的，没有什么很完美的解决方案，方案好也仅仅是针对满足了现在的所有业务需求而谈的。针对你的业务需求，谈出你的设计理念。项目不用多，认真的去总结一个就可以了。关键是很多人做了很多项目，但是却没有认真的去体会总结一个，做了也是白做，仅仅是代码熟练度增加了而已。后话，不要为了面试而面试，要为了提高而学习，提高了自然也就能从容的面对知名公司的面试了。";
		
		JiebaNLP nlp = JiebaNLP.JiebaExtractor();
		nlp.loadStopWordsDefault();
		//仅仅分词，不排序
//		List<String> list = nlp.JiebaNLPKeyword(doc);
//		List<String> list = nlp.JiebaNLPKeywordByTF8(doc, 64);
		//指定数字的分词，按词频倒序排列，-1提取全部词
		List<String> list = nlp.JiebaNLPKeywordByTF(doc, 64);
		System.out.println(list.size());
		for (String string : list) {
			System.out.print(string + " : ");
		}
		
		//分词，带频率，倒序排列
		Map<String, Integer> tfMap = nlp.calc_tf(doc, 64);
		System.out.println(tfMap);
		
		
		Map<String, Double> tfidfMap = nlp.calc_tfidf(doc,64);
		System.out.println(tfidfMap);
		
		List<KeywordWeightPair> kwp = nlp.tokenWeightPair(doc,64);
		for (KeywordWeightPair kw : kwp) {
			System.out.print(kw.getKey() + ":" + kw.getWeight());
		}
	}
}
