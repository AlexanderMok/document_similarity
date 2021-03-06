package similarity.tokenizer;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.fnlp.util.exception.LoadModelException;
import org.ujmp.core.Matrix;

import similarity.tokenizer.fnlp.FNLP;
import similarity.tokenizer.jieba.JiebaNLP;
import similarity.tokenizer.jieba.keyword.KeywordWeightPair;

/**
 * 
 * @package com.kass.app.app_user.models.similarity.tokenizer
 * @project app-kasscloud
 * @description 分词统一接口，在此可添加其他分词器，如jieba分词器，IKAnalyzer分词器等等
 * @author 莫庆来
 * @date 2016年8月5日
 */
public class KeyWordExtractor {

    /**
     * 复旦NLP
     * @param text 文档内容
     * @param topN 关键词数
     * @return TextRank算法的分词，带分词权重
     * @throws LoadModelException
     */
	public static Map<String, Integer> FNLPKeyWordExtract(String text, int topN) throws LoadModelException {
		topN = checkTopN(topN);
		return FNLP.FNLPExtractor().extract(text, topN);
	}
	
	/**
	 * 
	 * @param text
	 * @param topN
	 * @return TextRank算法的分词，不带分词权重
	 */
	public static List<String> FNLPKeyWord(String text, int topN) {
		topN = checkTopN(topN);
		Map<String, Integer> words = FNLP.FNLPExtractor().extract(text, topN);
		List<String> keywords = new ArrayList<String>();
		for (String word : words.keySet()) {
			keywords.add(word);
		}
		words = null;
		System.out.println("分词:" + keywords.toString());
		return keywords;
	}
	
	/**
	 * Java 8 Version
	 * @param text
	 * @param topN
	 * @return TextRank算法的分词，不带分词权重
	 */
	public static List<String> FNLPKeyWord8(String text, int topN) {
		topN = checkTopN(topN);
		Map<String, Integer> words = FNLP.FNLPExtractor().extract(text, topN);
		return words.keySet().stream().collect(Collectors.toList());
	}
	
	/**
	 * topN检验，-1为输出全部分词
	 * @param topN
	 * @return
	 */
	private static int checkTopN(int topN) {
		if (topN == -1) {
			topN = Integer.MAX_VALUE;
		}
		if (topN < 1) {
			System.err.println("number error. You must extract at least one word");
		}
		if (topN > Integer.MAX_VALUE) {
			System.err.println("number error. Number out of range.");
		}
		return topN;
	}
	
	/**
	 * 不排序分词
	 * @param text
	 * @return list 全部分词 
	 */
	public static List<String> JiebaNLPKeyword(String text) {
		JiebaNLP jieba = JiebaNLP.JiebaExtractor();
		jieba.loadStopWordsDefault();
		return jieba.JiebaNLPKeyword(text);
	}
	
	/**
	 * 返回词频最高的topN个词，倒序
	 * @param text
	 * @param topN
	 * @return tokens - a list of tokens 
	 */
	public List<String> JiebaNLPKeywordByTF(String text, int topN){
		JiebaNLP jieba = JiebaNLP.JiebaExtractor();
		jieba.loadStopWordsDefault();
		return jieba.JiebaNLPKeywordByTF(text, topN);
	}
	
	/**
	 * 该方法完成分词、去除停用词和根据词频排序
	 * @param doc text to be calculated
	 * @param topN 指定取topN个词的tf，若为-1，输出全部分词
	 * @return
	 */
	public Map<String, Integer> calc_tf (String doc, int topN) {
		JiebaNLP jieba = JiebaNLP.JiebaExtractor();
		jieba.loadStopWordsDefault();
		return jieba.calc_tf(doc, topN);
	}
	
	/**
	 * 计算  weight = tf * idf / totalTf，作为 tf*idf 原始公式的变通
	 * @param text text to extract
	 * @param topN Return number of top keywords, -1 for all words
	 * @return KeywordWeightPair - an Map of Pair (word,weight)
	 */
	public Map<String, Double> calc_tfidf(String text, int topN) {
		JiebaNLP jieba = JiebaNLP.JiebaExtractor();
		jieba.loadStopWordsDefault();
		return jieba.calc_tfidf(text, topN);
	}
	
	/**
	 * 与calc_tfidf(String text, int topN) 一样
	 * @param text text to extract
	 * @param topN Return number of top keywords, -1 for all words
	 * @return KeywordWeightPair - a List of Pair (word,weight)
	 */
	public List<KeywordWeightPair> tokenWeightPair(String text, int topN){
		JiebaNLP jieba = JiebaNLP.JiebaExtractor();
		jieba.loadStopWordsDefault();
		return jieba.tokenWeightPair(text, topN);
	}
	
	/**
	 * Retrieve tf*idf array
	 * @param text
	 * @param topN
	 * @return
	 */
	public static double[] token2TfIdfArray(String text, int topN) {
		JiebaNLP jieba = JiebaNLP.JiebaExtractor();
		jieba.loadStopWordsDefault();
		Collection<Double> weight = jieba.calc_tfidf(text, topN).values();
		if (weight.size() < topN) {
			for (int i = weight.size(); i < topN; i++) {
				weight.add(0.0);
			}
		}
		return ArrayUtils.toPrimitive(weight.toArray(new Double[topN]));
	}
	
	
	/**
	 * Retrieve weight array calc by TexRank
	 * @param doc
	 * @param count
	 * @return
	 */
	public static int[] token2TexRankArray(String doc, int count){
		Collection<Integer> tokenWeight = FNLP.FNLPExtractor().extract(doc, count).values();
		if (tokenWeight.size() < count) {
			for (int i = tokenWeight.size(); i < count; i++) {
				tokenWeight.add(0);
			}
		}
		return ArrayUtils.toPrimitive(tokenWeight.toArray(new Integer[count]));
	}
	
	/**
	 * @TODO 改成多线程，利用多核提高并行度，提高速度
	 * @param text1 - text to calc
	 * @param text2 - another text to calc
	 * @param topN - tokenize topN terms. It is strongly recommended to set topN as '-1' to extract all terms
	 * @return cosineSimilarity
	 */
	public static double calcSimilarity(String text1, String text2, int topN){
		long s = System.currentTimeMillis();
		Map<String, Double> doc1 = JiebaNLP.JiebaExtractor().calc_tfidf(text1, topN);
		Map<String, Double> doc2 = JiebaNLP.JiebaExtractor().calc_tfidf(text2, topN);
		
		List<String> commonStr = new ArrayList<String>();
		for (Map.Entry<String, Double> me : doc1.entrySet()) {
			if (doc2.containsKey(me.getKey())) {
				commonStr.add(me.getKey());
			}
		}
		int size = commonStr.size();
		double[] arr1 = new double[size];
		double[] arr2 = new double[size];
		
		for (int i = 0; i < size; i++) {
			arr1[i] = doc1.get(commonStr.get(i));
			arr2[i] = doc2.get(commonStr.get(i));
		}
		commonStr = null;
		doc1 = null;
		doc2 = null;
		
		double result = Matrix.Factory.importFromArray(arr1).cosineSimilarityTo(Matrix.Factory.importFromArray(arr2), true);
		
		System.out.println(
				String.format(Locale.getDefault(), "Similarity calc based on TFIDF and Cosin finished, CosinSimilarity:%s, time elapsed:%dms",
						result
						
						, System.currentTimeMillis() - s));
		return result;
	}
	
	public static void main(String[] args) {
		String doc= "目前拿到的知名公司的offer是腾讯和新美大（大众点评、美团）。一直想写一篇面经分享给大家，但因为一些琐碎的事情，就一直耽误着。 今天就特意拿出一些时间去写它。很多人都分享了很多的面经，而且还有好多面试题目在很多社区都有相关的资料。很多面试题也都有相关的解析，但是仅仅是刷这些面试题其实并不一定可以找到自己心仪的工作。下面就主要介绍下我是如何在面试前做准备工作的。我经历的一些面试因为大三了，从今年3月份开始，自己开始找工作，开始也不明白该怎么做，就简单写了一了一份简历，简历上就是一些自己相关的学习经历。最开始的时候选择了一些自己觉得可以的小公司，并且获得了一些面试机会，第一次面试很紧张，不知道该跟面试官说什么，不知道该如何去介绍自己，不知道该怎么做，就是傻傻的一顿被问，正如你所想的，我不可能所有的问题答的都很好，因为基本上面试官是想到什么就问什么(他可能是准备好的问题，但是我真的觉得我的第一次面试是他想到什么就问什么)。面试结束后，我就好好总结了下，然后又开始了第二家。这么经历了几次就发现，自己不在那么紧张了，自己开始占据主动权，引领着面试官走。所以现在回忆一下，多面一些还是能增长很多经验的。不管是你表达能力的提升，我所经历的面试官人都非常好，开始可能会很紧张，但是后来感觉就像是在聊天，两个志同道合的人聊着同一个话题，从这个聊天的过程当中也收获了很多自己不知道的东西。新美大面试经历美大的面试官一定要点个赞，其实面过以后会发现，不要很紧张什么。美大的面试官很随和具体的面试问题就不说了，基本上一面的时候还是很关注基础的，然后最后的20分钟左右都是聊一些经验，我当时是说起了一个话题，因为开始的时候聊一些基础，然后就是聊到我的项目中的图片的动态缓存方案，就一直在聊这个，最后又提起了组件化的开发方案，我们最后就是一直在聊这个话题。一面就最后接到通知就过了。二面的时候，就是视频面，在一个平台上，可以视频，并且我在那个平台上写的代码对方也是可以看见，给大家一个建议，别人看你写代码你可能会很紧张，但是不要太慌张，我感觉正常发挥就好了，对方主要还是想看看的代码的风格怎么样？是不是很规范，注释啊，还有一些变量的命名啊，这些是否很规范。另一个就是想看大家的处理能力，其实给你的题目都不是很难，都是一些简单的基础的东西，不会说让你现场写出一个什么很麻烦的布局，很牛的特效的。这次面试大概经历了一个小时左右，最后一部分时间还聊了很多生活上的事情。例如就是来到美大后准备想学一些什么啊？你对美大哪一方面的技术很感兴趣啊？就是这些，喜欢什么就聊一些什么就可以了。整体的两轮面试还是学到很多东西的，毕竟现在在学校不知道外面的公司是什么样的，通过面试官还是能了解到外面很多东西的。腾讯的面试经历腾讯的面试就是自己经历了腾讯的笔试，然后一面，二面，最后HR面，然后等消息，最后得到offer。笔试的话基本上就是一些基础知识，像数据结构，计算机组成原理，操作系统，计算机网络这些。上课认真听听，笔试前刷一些题目问题还是不大的。腾讯一面，主要就是聊一些基础，我是去面移动客户端，但是整个面试移动相关的问题很少，主要还是在围绕着一些基础再聊。如果你想拿到腾讯的offer，我觉得基础是非常重要的。面试题的话，我觉得这个其实没什么可提的，因为这些题目都是不固定的，面试官主要还是在围绕着你的简历再跟你聊，你的简历中写到你的APP中涉及过下载，那么网络必问，问什么？我觉得TCP，HTTP都是值得问的。有一本书叫《程序员的自我修养》我觉得这本书有时间还是很需要去读一下的。我见到过很多人吐槽说，我是去面iOS，面试我的竟然是一个Android的，从我个人的角度来看，无论是iOS，还是Android在很多的设计思想上面还是有很多相通的地方的。说出你的想法就可以了。我个人觉得思想还是很重要的。腾讯二面给我最大的印象就是面试官说的一句话，挑一个你觉得可以的项目，然后我们聊一些东西。从这句话我感觉很多人都能够感觉出来，二面主要还是聊你的，一定给有一个项目才行。并且好好去准备这个东西。同样没有什么具体的问题，每个人的项目都不一样，实现方案也不可能完全一样。所以聊的东西也就不可能完全一样。二面什么太多的经验，主要还是认真的去准备一个项目。面试前的准备这是我最想跟大家聊的东西，因为关于面试前的准备，或者是准备什么还是很有必要重点分享下的。很多人面试的时候不知道说什么，同样很多人都说要引领着面试官，不要被面试官引领。但是该怎么做呢？难道我们不让面试官说话？我们给面试官规定题目？这都是不现实的，对吧？但是通过一些准备，还是可以实现让我们去给面试官”规定题目”的，怎么做？举一些简单的例子，例如：你的简历当中写到自己对Socket有一些了解，那么很自然的就会聊到TCP，你提前好好准备下，你自己都是可以预知的，当面试官问听到你Socket的时候，自然就会走到TCP上。如何准备？很多人上网上查了一些，TCP很经典的就是连接的三次握手，断开时的四次挥手。你也简单的查了一下三次握手什么样，四次挥手什么样，但是这仅仅就够了吗？我们为什么不在往更深层去准备下呢？例如连接为什么是三次，断开为什么是四次？连接如果两次会发生什么？三次握手中总共发送了三个包，那么仅仅就是做了三个作用吗？更往深处可能会涉及到TCP的头部结构，以及TCP连接，断开时SYN，FIN，ACK的有效值设置又是什么样的？我相信你把这些都说出来肯定是会被加分的。当然你想很好的说出来不是说你面试前查查就可以了，肯定是需要去好好的去理解的。这也就是我说的面试前的准备，不要为了面试而面试，而是要为了面试而去学习，提升自己。我可以再举个简单的例子，关于图片缓存这个东西，在iOS，Android中有很多动态的图片缓存解决方案。你的简中如果写到做过相关的操作，我觉得这个东西还是很值得去问的。明知道会被问，面试前肯定要好好准备下啊。准备的过程当中我觉得需要总结出自己的方案中优点有哪些，缺点有哪些，针对你的缺点你准备怎么做，或者说你可以针对你知道的一些知名的开源库是如何解决的，你有什么可以借鉴的，这样我感觉你肯定是要被加分的，最起码你是阅读过开源库，并且认真的去思考过。我就见过有人被问到如何做图片缓存的时候，他就简单的说了一句通过URL下载图片然后保存起来，以便利用。这个话题当中有很多东西都是可以去值得聊的，就说这么一句话，我觉得面试官不可能得到他想要的答案，他没有得到答案，肯定是要去接着问你的。这个时候你就会显着的很被动，接下来就是要被面试官引领着走了。有人会问有什么可以聊的？我举一些简单的例子，例如图片的缓存方案肯定是要涉及到图片的下载问题，既然是下载，那么下载进度信息的回调你怎么做的?下载失败怎么办？下载完成的回调怎么做的?下载肯定涉及到多线程的下载，那么这些线程你是如果管理的？线程管理器的结构又是什么样的？甚至是一个URL下载连接正在被下载，另一个地方发起了一个相同URL的下载，这种情况我认为为了节省资源不应该在新开一个线程去下载，那么这种情况你有做处理吗？如何做的?其实有很多值得考虑的问题和值得去说的。做缓存，你做的是磁盘缓存啊？还是内存缓存？还是两者都有？是否支持磁盘空间大小的设置，图片有效期的设置如何做的，图片的清理工作，我相信无论在iOS还是Android那些知名的开源库不可能就是简单的有一个清理方案，肯定是支持部分清理，或者全部清理，部分清理的依据又是什么？例如依据图片的有效期来做清理，过了我们设定的有效期，这个时候我们就需要去清理掉这部分内容。另外如果如果所有缓存文件的总大小超过这一大小，则会按照文件最后修改时间的逆序，以每次一半的递归来移除那些过早的文件，直到缓存的实际大小小于我们设置的最大使用空间。这些东西都是有很多值得去说的，当然你只要去感悟的越深，理解的越深，就会体会的越深，表达的就会越好。刚刚提到的东西我觉得聊个20分钟还是很容易的，因为这个里面存在的东西和值得注意的地方太多了。准备好一个项目，找出你项目中的一个亮点，然后认真的去总结，并且看一些别人的方案，看看有哪些你没有的，你没有的就是你方案的缺陷，看看该怎么解决。我觉得，缺陷还是很重要的，没有什么很完美的解决方案，方案好也仅仅是针对满足了现在的所有业务需求而谈的。针对你的业务需求，谈出你的设计理念。项目不用多，认真的去总结一个就可以了。关键是很多人做了很多项目，但是却没有认真的去体会总结一个，做了也是白做，仅仅是代码熟练度增加了而已。后话，不要为了面试而面试，要为了提高而学习，提高了自然也就能从容的面对知名公司的面试了。";
		String doc2 = "作为一个一年多的前端工程师，之前的前端老大离职后，自己就扛起了一些前端方面的事务。面试当然是最具挑战的事情了，首先是筛选简历，接着是邀请到公司面对面聊。说说自己的体会吧。筛选简历：偏实习和经验较少的求职者，一般还是抱着求学的态度，但是带人确实是一件很累的事情，公司事情多，人少就不会考虑招这类的求职者。目前我们就是这种情况，所以直接就没怎么考虑了。而且用的技术比较多，实际要求比较高，确实不愿意花这个成本。一般来说，简历能体现出一个人很多东西。比较重要的是，我希望能看到项目链接、个人博客地址。博客希望有原创的内容。看项目链接是看用了哪些技术，代码怎么写的，结构如何划分，综合看一下，就算不完全是他自己写的也没关系，最后面试会根据他的项目，问他自己的理解。列出的技能树，只能做一点小参考，面试的时候，会问我会的技术，我不会的不问。有培训经历的，在我看来反而是减分项，我的想法是培训学校浮躁的教学态度，甚至教你怎么装饰自己等等，会对学生造成很多消极的影响，当然也可能有好的培训，只是比较少吧。关于这点也是根据普遍现象来看的，虽然有点带减分，但是不缺乏优秀的人，所以并不会影响求职。关于跳槽频繁，我倒觉得并不是坏事，比较重要的是跳槽理由和过程。很多人觉得跳槽频繁不好，我觉得还是得看具体情况，坚持自己的原则和追求就好。做决定要果断。技能树上会一点的、懂一点就写熟悉、精通什么的，面试时被问到就是大大的减分项。写了解什么的，还说明这人比较诚实，但是也可能因为技能看起来不够而失去面试机会。面试：面试就比较有意思了，会遇到各种各样的人。先说说我自己的一些特点，因为这个会影响到求职者的态度。我自己呢，比较年轻，看起来很多人都以为是个高中生，实际有20多了。所以有些求职者以为公司找了个新手来面试，我比较斯文，像个小白脸什么的，求职者可能会低估。第一次面试的那个求职者还比较有意思，因为那会也是第一次面试别人，所以难免有点不知所措。刚好这个人就职在这边比较出名的一个公司，他一来就把节奏带乱了，先说了他自己和一群人做了canvas的游戏（只是一个没什么功能的demo），都是从底层写的，没用三方的引擎什么的，然后说了一堆理论的东西，其实我并没有听清楚。后来想想，实际他自己可能并没有做什么贡献。开始以为是大神，后来慢慢觉得他应该是很多不懂，所以故意带节奏，说一些天马行空的东西。问他有没有了解过我们公司的产品，他说了解了，觉得还不错（实际并不了解）。而后开始给我们推荐其他公司的app，说了很久，打断了一次，还要接着说。非常无语，也是因为是第一次，自己也没把握到节奏，就被他一起牵到走，最后什么技术问题都没问。cto来和他聊了几句，就让他走了。之后的面试，再也没有这种情况了。都是我问什么，别人答什么。也有4、5年经验的，在我提问的时候，反问我底层和什么原理的，其实他自己也不怎么懂，可能就是看了下，我懂的我就回答了，不懂的就说的不懂。后来就不反问了，开始虚心请教了，其实看出来他挺尴尬的。也有求职者做了几年，就只会切个图什么的，实在没办法要，只能给她提一些意见。简历上都会写到的技术，我会的都会问一下，比如有人写的熟悉grunt、gulp等构建工具、熟悉angular，bootstrap等。实际很多都不会用，或者是看过一点入门教程。有的还是不错，可能还缺乏一些总结，所以回答的时候含糊不清，只有去一点点的问这个怎么做的，那个怎么做的。有很多求职者是后端转前端的，因为他们之前多多少少要做前端的事情，我也不知道是不是现在前端待遇比较好什么的，很多都是后端转前端的。有后端的技术是加分项，但是大部分的人，前端技术就停留在用用boostrap，用下JQ操作一点DOM，angular也只是用到路由、基本的双向绑定和默认指令。我比较看重的还是，js和css，因为这两个东西很核心。缺乏css经验和js能力，很难写出健壮的可维护性代码。每次我都会问求职者，对css有什么自己的心得，很多人都答不上来，脑子里什么也没有闪现，不知道该说什么，其实这就已经说明css的经验还比较欠缺。也会问是否自己亲自完成过js的插件，比如幻灯之类的，这也是综合能力的考察，有时候需要实现的功能网上不一定有现成的或者适应需求的，所以需要自己编写，很多人畏惧自己写这些，或者写不下去就不坚持了。其实一点一点的解决每个难点，最后是一定能实现的。脚踏实地 之前的前端老大很不错，他总结的四个要求还是很不错：性格 、 经验、 视野 、基础。（权重按照当前顺序划分）性格是最重要的，因为这个涉及到方方面面。经验堆叠起来的技术是强壮的，能有更多预见性能力，改bug，找问题，组织架构等 视野还是看人的学习积极性，视野宽阔，在遇到问题和选择技术的时候很有帮助 基础这个不说啦，决定上层建筑的现在前端行情比较好，很多的求职者都是想要更高的工资，技术其实并没有那个水平，自信满满，可能去一些没前端基础的公司，还真能拿到相应的薪资，但是这种公司不一定稳定，等行情一过，前端没那么火的时候，这种泡沫破裂的时候，失业的可能就是这部分人。所以还是脚踏实地。";
		String doc3 = "日本木质寺庙通常被列为古建筑。然而，很多寺庙基本都是重建而成，这不由得让人想起老祖父那把铲子的那个经典玩笑：铲子已更换过两回，把手更换了三回，但仍视为同一把铲子。但是，日本人与西方人在保护古建筑原始风貌的价值观上大相径庭，他们通过一代代重建重修，努力寻求建筑外貌与实质古今一脉相承。这些古建筑的共同之处就是保护精湛的传统木工与榫卯技艺（这是日本无比尊崇的技法）。对传统技艺的推崇丝毫不影响创新：日本最出色的木匠仍展现高超的技艺。矢泽金太郎(Kintaro Yazawa)是榫卯技艺的顶级大师。他承认自己最初并不是出类拔萃的学生，但出于兴趣，自己特别想当手工匠。出生于1946年的他最初在一家工程公司当推销员。让他苦不堪言的是：“几乎每天的工作就是招待客户吃喝玩乐，自己觉得这样的生活索然无味，于是辞了职，随后又结婚成家。”为了打发频繁应酬间的无聊时光，矢泽制作了一把木风琴。他喜欢这份闲情逸致，并下定决心要拜世上最出色的风琴制作师为师——法国的马克•卡尼尔(Marc Garnier)。不久，他来到距家6000英里的法国、转投卡尼尔门下为徒。这段学徒生涯让他明白了两件事：他觉得自己并无制作风琴所具备的音乐才华，但他的确拥有锯凿、成型以及榫卯接合的天分。他与妻子于是回到家乡——位于日本南部的宫崎市(Miyazaki)。他在这里建起了自己的木工作坊，并结识了一位制作家具的退休木匠。老先生问矢泽是否曾尝试过燕尾绞榫法(twisted dovetail joint)。萌生浓厚兴趣的矢泽于是观摩了具体做法。他把业余时间全部花在了设计与打造绞榫木作品上，一辈子沉醉其中。选择从事建筑还是家具榫卯技艺时，他说自己觉得后者更为迫切，原因是该门技艺日见式微。“在如今的建筑领域，传统榫卯技艺仍在使用，因为政府支持和保护重要的古建筑。而在家具行业，传统榫卯技艺则凤毛麟角。”日本如今的家具生产商生存举步维艰，这与修缮寺庙的木匠境况大相径庭。但矢泽名声远扬，作坊的年销售额超过了1亿日元（约合93.3万美元）。他解释说：“自己开设木匠作坊后，来自京都的一位买家登门拜访，他非常喜欢我的木工件，于是建议我成为该技艺在日本的独家代理人。在10年中，客户的订单源源不断。”矢泽而后成为代表日本传统手工技艺最高水准的工艺美术协会(Japan Art Crafts Association)会员。“我充分利用自己的会员身份，实现了衣食无忧。我通过知名私人美术画廊以及大型百货店里艺术画廊举办的展览推销自己的作品。我的主要客户是医生与商界人士。”他认为：与客户处于同等生活水准有助于自己了解对方。这些人对简约设计风格孜孜以求，而精心打造的东西价格不菲。简约型家具也会很时尚，但颇具讽刺意味的是，这些传统技艺却隐藏于日本现代极简主义风格、依靠缠绕燕尾榫这种暗接合法的艺术作品中。机器割锯的暗榫结构并不会影响物件的外观，而且相比手工打造的东西大为便宜。但在崇尚外形与内涵原汁原味的日本，真正的价值在于手工打造物件所耗费的时间、工艺以及所拥有的那份自豪感。传统技艺是作品的根本，但创新仍至关重要。木匠作坊开张后的最初三年，随着营业额增加以及声名鹊起，矢泽不断尝试改进暗榫接合的新技法。34岁时，他首倡了“贯穿式榫卯”法(“through tenon” joint)。有一天，他翻阅一本英国木工类杂志时，被其中的一个细节所吸引。上面刊登的正是其首倡的贯穿式榫卯结构，但却用在英国传统工艺大师欧内斯特•吉姆森(Ernest Gimson, 1864-1919)设计的一款家具上。矢泽立即带着身怀六甲的妻子以及三岁大的儿子启程飞往英国观摩该款家具，并结识了家具设计师爱德华•巴恩斯利(Edward Barnsley)。巴恩斯利是其父西德尼（Sidney，曾与吉姆森共事过）传统工艺的嫡传者。巴恩斯利夫妻俩的生活给矢泽留下了深刻印象。“我对他们简朴而又充实的生活深有同感。”他说，“对方随后向我们引见了自己的得意门生——阿兰•彼特斯(Alan Peters)，我们在其工作室呆了一周时间，共同打造了一款日式缠绕榫卯结构的家具。”矢泽被日本与英国100多年前的榫卯结构“殊途同归”的现象留下了深刻印象。“我原以为吉姆森的榫卯技艺是自己努力的方向。”他说，“这种震撼感会一直影响我的余生。”日式家具采用的材质也别具特色。波纹纹理、暖色调以及不变形的榉树是打造家具的最常用材质。除了铁质及硬钢锯子凿子外，矢泽还使用精心锻造的日式榔头(genno)与小刨子。木头表面用yari ganna这种予形刨子沿纹理方向进行刨刮和平整。矢泽的部分家具涂抹了取材日本漆树的彩漆。配备上述工具后，他的家具制作稳步推进。他对自己运用一种叫“mizugumi”的传统榫卯技艺打造的一款书桌颇感自豪。Mizu与gumi分别是“水”与“榫卯”的意思，这类榫卯技艺常见于木质火盆架，它们与水关联后有了“避防火灾”之功效。他乐于见到自己的传统珍贵技艺薪火相传。“如今，越来越多的年轻木匠采用传统技法打造现代家具。它不会成为主流，但毫无疑问会不断传承下去。因此，家具的日式榫卯结构前景并不会太暗淡。”";
		
		
		double d1 = calcSimilarity(doc,doc2,-1);
		System.out.println(d1);
		
		double d2 = calcSimilarity(doc,doc3,-1);
		System.out.println(d2);
	}

}
