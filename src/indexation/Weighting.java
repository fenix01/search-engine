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

import common.Common;

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
	 * et restructure tous les indexes afin d'intégrer le poids de chaque mot dans un document
	 * par exemple une ligne de l'index avant l'appel de cette fonction est de la forme :
	 * cheval df docID1:tf1,docID2:tf2
	 * après l'appel la ligne est de la forme
	 * cheval df docID1:tf1:weight1,docID2:tf2:weight2
	 */
	public void updateSumWeights(String index){
		File fIndex = new File(index);
		
		String tmpIDX = index.substring(0, index.lastIndexOf(".idx"))+0+Common.extIDX;
		File fTmpIndex = new File(tmpIDX);
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fwTemp = null;
		BufferedWriter bwTemp = null;
		try {
			//prépare la lecture de l'index dont le chemin est en paramètre
			fr = new FileReader(fIndex);
			br = new BufferedReader(fr);
			//prépare la réécriture de l'index
			fwTemp = new FileWriter(fTmpIndex);
			bwTemp = new BufferedWriter(fwTemp);
			
			String line;
			//on parcourt toutes les lignes de l'index
			while ((line = br.readLine()) != null){
				//on split la ligne pour récupérer le mot, le df, et la liste des docs
				String[] lineIndex = line.split("\t");
				//on split la ligne pour récupérer la liste des documents avec le tf
				String[] docs = lineIndex[2].split(",");
				
				//on prépare la construction de la nouvelle ligne
				StringBuilder stbDocsWeight = new StringBuilder();
				//contient pour l'instant le mot et le df
				stbDocsWeight.append(lineIndex[0] + "\t" + lineIndex[1] + "\t");
				
				//on parcourt la liste des docs
				for (int i = 0 ; i < docs.length ; i++){
					//on split pour récupérer le docID, et le tf
					String[] docTf_ = docs[i].split(":");
					
					int docId = Integer.parseInt(docTf_[0]);
					
					synchronized (sum_weights) {
						//On calcule le df_t
						double df_t = sizeCorpus / Double.parseDouble(lineIndex[1]);
						//on calcule le poids pour le mot associé au document courant
						double weight = Double.parseDouble(docTf_[1]) * Math.log10(df_t);
						
						if (i < docs.length-1)
							stbDocsWeight.append(docs[i]+":"+weight+",");
						else
							stbDocsWeight.append(docs[i]+":"+weight);
						
						Double docWeight = sum_weights.get(docId);
						if (docWeight == null){
							sum_weights.put(docId, weight*weight);
						}
						else {
							sum_weights.put(docId, docWeight+weight*weight);
						}	
					}
				}
				//on peut réécrire la ligne à présent
				bwTemp.write(stbDocsWeight.toString());
				bwTemp.newLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
			bwTemp.close();
			fIndex.delete();
			fTmpIndex.renameTo(fIndex);
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
