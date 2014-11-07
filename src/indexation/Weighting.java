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
	private TreeMap<Integer,Float> sum_weights;
	private TreeMap<Integer, String> index;
	private int start_doc = 0;
	private int end_doc = 0;
	private int sizeCorpus = 0;
	
	
	public Weighting(TreeMap<Integer,Float> weights, TreeMap<Integer, String> index,
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
		
		String tmpIDX = index.substring(0, index.lastIndexOf(".idx"))+0+Common.extTMP;
		File fTmpIndex = new File(tmpIDX);
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fwTemp = null;
		BufferedWriter bwTemp = null;
		try {
			//prépare la lecture de l'index dont le chemin est en paramètre
			fr=new FileReader(new File(index));
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
				stbDocsWeight.append(lineIndex[0] + "\t" );
				
				//on parcourt la liste des docs
				for (int i = 0 ; i < docs.length ; i++){
					//on split pour récupérer le docID, et le tf
					String[] docTf_ = docs[i].split(":");
					
					int docId = Integer.parseInt(docTf_[0]);
					
					synchronized (sum_weights) {
						//On calcule le df_t
						float df_t = sizeCorpus / Float.parseFloat(lineIndex[1]);
						//on calcule le poids pour le mot associé au document courant
						float weight = (float) (Float.parseFloat(docTf_[1]) * Math.log10(df_t));
						
						if (i < docs.length-1)
							stbDocsWeight.append(docTf_[0]+":"+weight+",");
						else
							stbDocsWeight.append(docTf_[0]+":"+weight);
						
						Float docWeight = sum_weights.get(docId);
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
				doc = index.get(i);
				
			}
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
	
	public static void saveWeights(String file,TreeMap<Integer,Float> sum_weights) throws IOException{
		File f = new File(file);
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		for(Map.Entry<Integer, Float> weight_doc : sum_weights.entrySet()){
			bw.write(String.valueOf((float)(Math.sqrt(weight_doc.getValue()))));
			bw.newLine();
		}
		bw.close();
	}
	
	public static void saveWeights2() throws IOException{
		File f1 = new File(Common.DIRINDEX + "docWeight"+Common.extWEIGHT);
		File f2 = new File(Common.DIRRSC+"all.corpus");
		FileReader fr1 = new FileReader(f1);
		FileReader fr2 = new FileReader(f2);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		
		File fres = new File(Common.DIRINDEX+"weight"+Common.extWEIGHT);
		FileWriter fwres = new FileWriter(fres);
		BufferedWriter bw = new BufferedWriter(fwres);
		
		String line1,line2;
		int cpt=0;
		while ((line1 = br1.readLine()) != null){
			line2=br2.readLine();
			bw.write(cpt+"\t"+line1+"\t"+line2);
			bw.newLine();
			cpt++;
		}
		br1.close();
		br2.close();
		bw.close();
		f1.delete();
		f2.delete();
	}
	
}
