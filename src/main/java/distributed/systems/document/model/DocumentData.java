package distributed.systems.document.model;

import java.util.HashMap;
import java.util.Map;

public class DocumentData {
	private Map<String, Double> termFrequency = new HashMap<>();
	
	public void putTermFrequency(String term, double frequency) {
		termFrequency.put(term, frequency);
	}
	
	public double getTermFrequency(String term) {
		return termFrequency.get(term);
	}
}
