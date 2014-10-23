package indexation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import common.Common;

import td.td2.TD2;
import td.td3.TD3;
import tools.Normalizer;

public class TaskIndexing implements Runnable {

	private int start_doc = 0;
	private int end_doc = 0;
	private HashMap<Integer, String[]> corpus;
	private TreeMap<String, TreeMap<Integer, Integer>> index;
	private Normalizer normalizer;
	private boolean stopwords;
	private ArrayList<String> tmp_idx;
	private Thread th;
	private int reached = 0;
	private int cur_index = 0;
	private String name_idx = "";

	public static void main(String[] args) throws IOException {
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		int compteur = 0;
		for (int i = this.start_doc; i < this.end_doc; i++) {
			try {
				// System.out.println(i);

				HashMap<String, Integer> tf_doc;
				String doc;
				synchronized (corpus) {
					doc = corpus.get(i)[0];

				}
				tf_doc = TD2.getTermFrequencies(doc, normalizer, stopwords);
				for (Map.Entry<String, Integer> word : tf_doc.entrySet()) {
					if (!common.Common.isEmptyWord(word.getKey())) {
						if (index.containsKey(word.getKey())) {
							// le mot existe dans l'index, on l'ajoute dans la
							// liste des documents
							TreeMap<Integer, Integer> list_docs = index
									.get(word.getKey());
							list_docs.put(i, word.getValue());
							index.put(word.getKey(), list_docs);
						} else {
							TreeMap<Integer, Integer> list_docs = new TreeMap<>();
							list_docs.put(i, word.getValue());
							index.put(word.getKey(), list_docs);
						}
					}

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (compteur == this.reached){
					compteur = 0;
					saveTempIndex();
			}
			compteur++;
		}
		saveTempIndex();
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(estimatedTime / 1000);
	}
	
	private void saveTempIndex(){
		try {
			String out_idx = Common.DIRRSC+this.name_idx+this.cur_index;
			TD3.saveInvertedFile(index, new File(out_idx));
			this.cur_index++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void fusionIndexes(){
		if (this.tmp_idx.size() > 2){
			for (int i = 0 ; i < this.tmp_idx.size() ; i+=2){
				String out_idx1 = this.tmp_idx.get(i);
				String out_idx2 = this.tmp_idx.get(i+1);
				File f1 = new File(out_idx1);
				File f2 = new File(out_idx2);
				File fMerge = new File("");
				try {
					FusionIndex.mergeInvertedFiles(f1, f2, fMerge);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		
	}

	public void start() {
		th.start();
	}

	public TaskIndexing(HashMap<Integer, String[]> h, int start_doc,
			int end_doc, Normalizer n, boolean stopwords, String name_idx,
			int reached) {
		this.index = new TreeMap<>();
		this.start_doc = start_doc;
		this.end_doc = end_doc;
		this.normalizer = n;
		this.stopwords = stopwords;
		this.corpus = h;
		this.tmp_idx = new ArrayList<>();
		this.name_idx = name_idx;
		this.th = new Thread(this);
		this.reached = reached;
	}

}
