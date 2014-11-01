package indexation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import common.Common;
import common.Utils;
import td.td2.TD2;
import td.td3.TD3;
import tools.Normalizer;

public class TaskIndexing implements Runnable {
	
	private int start_doc = 0;
	private int end_doc = 0;
	private HashMap<Integer, String[]> corpus;
	
	//on stocke l'index sous la forme d'un treemap de
	//String = mot en clé, et un HashMap<DocID,tf> en valeur
	//2 entiers sont plus légers en mémoire qu'une string de DocID:Tf
	private TreeMap<String, HashMap<Integer,Integer>> index;
	private Normalizer normalizer;
	private boolean stopwords;
	private LinkedList<String> tmp_idx;
	private Thread th;
	private int reached = 0;
	private int cur_index = 0;
	private String name_idx = "";

	/**
	 * permet de fusionner les indexes finaux générer par les threads
	 * @param threads_count
	 */
	public static void fusionThreadsIndexes(int threads_count){
		LinkedList<String> threadsIndex = new LinkedList<>();
		for (int i = 0; i < threads_count ; i++){
			String indexName =  Common.DIRINDEX+"index"+i+Common.extIDX;
			threadsIndex.add(indexName);
		}
		fusionIndexes(threadsIndex, "index");
	}
	
	/**
	 * retourne les premières lettres d'un mot de la ligne d'index
	 * vérifie si certains mots ont une taille inférieur à x
	 * @param line 
	 * @param x
	 * @return
	 */
	private static String firstOcc(String line, int x){
		if (line == null)
			return null;
		String word = line.split("\t")[0];
		if (word.length() < x)
			return word;
		else
			return word.substring(0,x);
	}
	
	/**
	 * permet de découper l'index selon les X premières lettres des mots
	 * @param x
	 */
	public static void splitIndex(int x){
		FileReader fr;
		FileWriter fw;
		try {
			File fIndex = new File(Common.DIRINDEX+"index"+Common.extIDX);
			fr = new FileReader(fIndex);
			BufferedReader br = new BufferedReader(fr);
			
			String line1 = br.readLine();
			String line2 = br.readLine();
			
			String firstOccLine1;
			String firstOccLine2;
			firstOccLine1 = firstOcc(line1,x);
			firstOccLine2 = firstOcc(line2,x);
			fw = new FileWriter(Common.DIRINDEX+firstOccLine1+Common.extIDX,true);
			BufferedWriter bw = new BufferedWriter(fw);
			while (line2 != null){
				if (firstOccLine1.equals(firstOccLine2)){
					bw.write(line1);
					bw.newLine();
				}
				else {
					bw.write(line1);
					bw.newLine();
					bw.close();
					fw = new FileWriter(Common.DIRINDEX+firstOccLine2+Common.extIDX,true);
					bw = new BufferedWriter(fw);
				}
				line1 = line2;
				line2 = br.readLine();
				
				firstOccLine1 = firstOcc(line1,x);
				firstOccLine2 = firstOcc(line2,x);
			}
			bw.write(line1);
			bw.newLine();
			bw.close();
			br.close();
			fIndex.delete();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				doc = corpus.get(i)[0];

				tf_doc = TD2.getTermFrequencies(doc, normalizer, stopwords);
				for (Map.Entry<String, Integer> word : tf_doc.entrySet()) {
					
					if (!Common.isEmptyWord(word.getKey())) {
						HashMap<Integer, Integer> listDocs = index.get(word.getKey());
						if (listDocs != null) {
							// le mot existe dans l'index, on l'ajoute dans la
							// liste des documents
							listDocs.put(i, word.getValue());
							index.put(word.getKey(), listDocs);
						} else {
							HashMap<Integer, Integer> list_docs = new HashMap<>();
							list_docs.put(i, word.getValue());
							index.put(word.getKey(), list_docs);
						}
					}
				}	
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			if(Utils.getUsedMemory()>=1000000000){
//			System.out.println(i);
//			System.out.println(Utils.memoryInfo());
//			saveTempIndex();
//			}
			if (compteur == this.reached){
					System.out.println(Utils.memoryInfo());
					compteur = 0;
					saveTempIndex();
			}
			compteur++;
		}
		saveTempIndex();
		fusionIndexes(this.tmp_idx,this.name_idx);
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(estimatedTime / 1000);
	}
	
	private void saveTempIndex(){
		try {
			String out_idx = Common.DIRINDEX+this.name_idx+this.cur_index+Common.extIDX;
			this.tmp_idx.add(out_idx);
			TD3.saveInvertedFile(index, new File(out_idx));
			this.index.clear();
			this.cur_index++;
			System.gc();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * permet de fusionner tous les indexes temporaires de la LinkedList passer en paramètre
	 * en un seul fichier d'index. Chaque entrée de la LinkedList est un chemin vers un index temporaire
	 * 
	 * @param tmpIndexes est un LinkedList contenant des chemins vers des indexes temporaires
	 * @param nameIndex correspond au prefix pour les noms des fichiers d'index temporaire
	 */
	private static void fusionIndexes(LinkedList<String> tmpIndexes, String nameIndex){
		//compteur pour gérer les indexes temporaires
		int counter = tmpIndexes.size();
		//tant qu'il reste au moins 2 indexes temporaires
		while (tmpIndexes.size() > 1){
			//on dépile 2 indexes temporaires
			String outIndex1 = tmpIndexes.pollFirst();
			String outIndex2 = tmpIndexes.pollFirst();
			//on prépare les deux indexes
			File f1 = new File(outIndex1);
			File f2 = new File(outIndex2);
			//on prépare un nouvelle index temporaire fusion de f1 et f2
			String outIndexMerge = Common.DIRINDEX+nameIndex+counter+Common.extIDX;
			counter++;
			//on fusionne les deux indexes temporaires
			File fMerge = new File(outIndexMerge);
			try {
				FusionIndex.mergeInvertedFiles(f1, f2, fMerge);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//on ajoute le nouvel index fusionné à la fin de la liste des indexes temporaires
			tmpIndexes.addLast(outIndexMerge);
			//on supprime les 2 fichiers d'index temporaire
			f1.delete();
			f2.delete();
		}
		//on renomme à présent l'index temporaire résultat de la fusion de tous les indexes temporaires
		String tmpIndex = tmpIndexes.poll();
		File f = new File(tmpIndex);
		String outIndex = Common.DIRINDEX+nameIndex+Common.extIDX;
		f.renameTo(new File(outIndex));
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

	public TaskIndexing(HashMap<Integer, String[]> h, int start_doc,
			int end_doc, Normalizer n, boolean stopwords, String name_idx,
			int reached) {
		this.index = new TreeMap<>();
		this.start_doc = start_doc;
		this.end_doc = end_doc;
		this.normalizer = n;
		this.stopwords = stopwords;
		this.corpus = h;
		this.tmp_idx = new LinkedList<>();
		this.name_idx = name_idx;
		this.th = new Thread(this);
		this.reached = reached;
	}

}
