package document_similarity;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Test;

import similarity.tokenizer.jieba.WordDictionary;
import similarity.tokenizer.jieba.keyword.IDFLoader;
import similarity.tokenizer.jieba.keyword.StopwordsLoader;
import similarity.tokenizer.jieba.viterbi.FinalSeg;


public class DictTest {
	
	@Test
	public void testStopWords(){
		StopwordsLoader loader = new StopwordsLoader();
		loader.loadStopWordsDefault();
		
	}
	
	@Test
	public void testIDFloader(){
		Path path = FileSystems.getDefault().getPath(".", "/models/stopwords/idf.txt");
		IDFLoader idfld = new IDFLoader(path);
		System.out.println(idfld.medianIDF());
	}
	
	@Test
	public void testDict(){
		WordDictionary.getInstance();
	}
	
	@Test
	public void testProbModel(){
		FinalSeg.getInstance();
	} 
	
	
}
