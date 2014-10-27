package indexation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Weighting implements Runnable {

	private Thread th;
	private TreeMap<Integer,Double> sum_weights;
	private HashMap<Integer, String[]> index;
	private int start_doc = 0;
	private int end_doc = 0;
	private int sizeCorpus = 0;
	
	
	public Weighting(TreeMap<Integer,Double> weights, HashMap<Integer, String[]> index,
			int start_doc, int end_doc, int sizeCorpus){
		this.th = new Thread(this);
		this.sum_weights = weights;
		this.index = index;
		this.start_doc = start_doc;
		this.end_doc = end_doc;
		this.sizeCorpus = sizeCorpus;
	}
	
	/*
	 * met à jour la somme des poids d'un document en parcourant tous les indexs données
	 */
	public void updateSumWeights(String index){
		File fIndex = new File(index);
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(fIndex);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null){
				String[] lineIndex = line.split("\t");
				String[] docs = lineIndex[2].split(",");
				for (String docTf : docs){
					String[] docTf_ = docTf.split(":");
					int docId = Integer.parseInt(docTf_[0]);
					
					synchronized (sum_weights) {
						
						double df_ = sizeCorpus / Double.parseDouble(lineIndex[1]);
						double weight = Double.parseDouble(docTf_[1]) * Math.log10(df_);
						
						Double docWeight = sum_weights.get(docId);
						if (docWeight == null){
							sum_weights.put(docId, weight*weight);
						}
						else {
							sum_weights.put(docId, docWeight+weight*weight);
						}	
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	@Override
	public void run() {
		for (int i = this.start_doc; i < this.end_doc; i++) {

			String doc;
			
			synchronized (index) {
				doc = index.get(i)[0];
			}
			System.out.println(doc);
			updateSumWeights(doc);
		}
		
	}
	
	public void start() {
		th.start();
	}
	
	public void join(){
		try {
			th.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveWeights(String file,TreeMap<Integer,Double> sum_weights) throws IOException{
		File f = new File(file);
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		for(Map.Entry<Integer, Double> weight_doc : sum_weights.entrySet()){
			bw.write(weight_doc.getKey()+"\t"+Math.sqrt(weight_doc.getValue()));
			bw.newLine();
		}
		bw.close();
	}
	
}
