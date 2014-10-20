package indexation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import td.td2.TD2;
import td.td3.TD3;
import tools.FrenchStemmer;
import tools.Normalizer;
import common.Common;

public class TaskIndexing implements Runnable {
	
	private int start_doc = 0;
	private int end_doc = 0;
	private HashMap<Integer,String[]> corpus;
	private TreeMap<String,TreeMap<Integer,Integer>> index;
	private Normalizer normalizer;
	private boolean stopwords;
	private String out_idx = "index1";

	public static void main(String[] args) throws IOException {
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis(); 
		for (int i = this.start_doc;  i < this.end_doc ; i++){
			try {
				System.out.println(i);
				HashMap<String,Integer> tf_doc = TD2.getTermFrequencies(corpus.get(i)[0],normalizer,stopwords);
				for (Map.Entry<String, Integer> word : tf_doc.entrySet()){
					if (index.containsKey(word.getKey())){
						//le mot existe dans l'index, on l'ajoute dans la liste des documents
						TreeMap<Integer, Integer> list_docs = index.get(word.getKey());
						list_docs.put(i, word.getValue());
						index.put(word.getKey(), list_docs);
					}
					else {
						TreeMap<Integer, Integer> list_docs = new TreeMap<>();
						list_docs.put(i, word.getValue());
						index.put(word.getKey(), list_docs);
					}
					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			TD3.saveInvertedFile(index, new File(out_idx));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(estimatedTime / 1000);
		System.out.println();
		
		
	}
	
	public TaskIndexing(HashMap<Integer,String[]> h, int start_doc, int end_doc, Normalizer n, boolean stopwords, String out_idx){
		this.index = new TreeMap<>();
		this.start_doc = start_doc;
		this.end_doc = end_doc;
		this.normalizer = n;
		this.stopwords = stopwords;
		this.corpus = h;
		this.out_idx = out_idx;
		//Thread th = new Thread(this);
		//th.start();
	}

}
