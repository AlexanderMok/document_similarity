package similarity.tokenizer.jieba.keyword;

public class KeywordWeightPair {
	private String key = "";
	private Double weight = 0.0;

	public KeywordWeightPair(String key, double weight) {
		this.key = key;
		this.weight = weight;
	}
	
	public String getKey() {
		return key;
	}

	public Double getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return "(" + key + "," + weight + ")";
	}

}
