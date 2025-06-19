package distributed.systems.document.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import distributed.systems.document.model.DocumentData;

public class SequentialSearch {
	
	public static final String BOOKS_DIRECTORY = "\\resources\\books";
	public static final String SEARCH_QUERY_1 = "test1";
	public static final String SEARCH_QUERY_2 = "test2";
	public static final String SEARCH_QUERY_3 = "test3";

	
	public static void main(String [] args)  throws FileNotFoundException{
	    String rootPath = System.getProperty("user.dir") + "\\src\\main";
	    String path = rootPath + BOOKS_DIRECTORY;
		File documentsDirectory = new File(path);
		List<String> documents = Arrays.asList(documentsDirectory.list()).stream().map(documentName -> path + "\\" + documentName)
																					.collect(Collectors.toList());
		if(documents.size() > 0) {
			List<String> terms = TFIDF.getWordsFromLine(SEARCH_QUERY_1);
			
			findMostRelevantDocuments(documents, terms);	

		}

	}
	
	private static void findMostRelevantDocuments(List<String> documents, List<String> terms) throws FileNotFoundException {
		Map<String, DocumentData> documentDataMap = new HashMap<>();
		
		for(String doc: documents) {
			BufferedReader buffer = new BufferedReader(new FileReader(doc));
			List<String> lines = buffer.lines().collect(Collectors.toList());
			List<String> words = TFIDF.getWordsFromLines(lines);
			DocumentData data = TFIDF.createDocumentData(words, terms);
			documentDataMap.put(doc, data);
		}
		Map<Double, List<String>> sortedScores = TFIDF.getDocumentsSortedByScore(terms, documentDataMap);
		printResults(sortedScores);
	}

	private static void printResults(Map<Double, List<String>> sortedScores) {
		for(Map.Entry<Double, List<String>> pair: sortedScores.entrySet()) {
			double score = pair.getKey();
			for(String doc: pair.getValue()) {
				List<String> arr = Arrays.asList(doc.split("/"));
				System.out.println(String.format("Book: %s - score: %f ", arr.get(arr.size() - 1), score));
			}
		}
		
	}
}
