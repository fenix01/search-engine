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
import java.util.Map.Entry;

import common.Common;
import common.Utils;
import tools.Normalizer;

public class TaskIndexing implements Runnable {

	// private TreeMap<Integer, String[]> corpus;
	private String corpus;
	// on stocke l'index sous la forme d'un treemap de
	// String = mot en clé, et un TreeMap<DocID,tf> en valeur
	// pour la recherche on a besoin de trier les documents
	private TreeMap<String, TreeMap<Integer, Integer>> index;
	private Normalizer normalizer;
	private boolean stopwords;
	private LinkedList<String> tmp_idx;
	private Thread th;
	private int reached = 0;
	private int cur_index = 0;
	private String name_idx = "";

	/**
	 * permet de fusionner les indexes finaux générer par les threads
	 * 
	 * @param threads_count
	 */
	public static void fusionThreadsIndexes(int threads_count) {
		LinkedList<String> threadsIndex = new LinkedList<>();
		for (int i = 0; i < threads_count; i++) {
			String indexName = Common.DIRINDEX + "index" + i + Common.extIDX;
			threadsIndex.add(indexName);
		}
		fusionIndexes(threadsIndex, "index");
	}

	/**
	 * permet de découper l'index selon les X premières lettres des mots
	 * 
	 * @param x
	 */

	public static void splitIndex(int x) {
		FileReader fr;
		FileWriter fw;
		try {
			File fIndex = new File(Common.DIRINDEX + "index" + Common.extIDX);
			fr = new FileReader(fIndex);
			BufferedReader br = new BufferedReader(fr);

			String line1 = br.readLine();
			String line2 = br.readLine();

			String firstOccLine1;
			String firstOccLine2;
			firstOccLine1 = Common.firstOcc(line1, x);
			firstOccLine2 = Common.firstOcc(line2, x);
			fw = new FileWriter(
					Common.DIRINDEX + firstOccLine1 + Common.extIDX, true);
			BufferedWriter bw = new BufferedWriter(fw);
			while (line2 != null) {
				if (firstOccLine1.equals(firstOccLine2)) {
					if (Integer.parseInt(line1.split("\t")[1]) > 1) {
						bw.write(line1);
						bw.newLine();
					}
				} else {

					if (Integer.parseInt(line1.split("\t")[1]) > 1) {
						bw.write(line1);
						bw.newLine();
						bw.close();
					}
					bw.close();
					fw = new FileWriter(Common.DIRINDEX + firstOccLine2
							+ Common.extIDX, true);
					bw = new BufferedWriter(fw);
				}

				line1 = line2;
				line2 = br.readLine();

				firstOccLine1 = Common.firstOcc(line1, x);
				firstOccLine2 = Common.firstOcc(line2, x);
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
		FileReader fr;

		try {
			fr = new FileReader(new File(Common.DIRRSC + corpus));
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				try {
					// System.out.println(i);
					HashMap<String, Integer> tf_doc;

					String[] doc = line.split("\t");
					int hash_doc = Integer.parseInt((doc[0]));
					tf_doc = getTermFrequencies(doc[1], normalizer,
							stopwords);
					for (Map.Entry<String, Integer> word : tf_doc.entrySet()) {

						if (!Common.isEmptyWord(word.getKey())) {
							TreeMap<Integer, Integer> listDocs = index.get(word
									.getKey());
							if (listDocs != null) {
								// le mot existe dans l'index, on l'ajoute dans
								// la
								// liste des documents
								listDocs.put(hash_doc, word.getValue());
								index.put(word.getKey(), listDocs);
							} else {
								TreeMap<Integer, Integer> list_docs = new TreeMap<>();
								list_docs.put(hash_doc, word.getValue());
								index.put(word.getKey(), list_docs);
							}
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// if(Utils.getUsedMemory()>=1000000000){
				// System.out.println(i);
				// System.out.println(Utils.memoryInfo());
				// saveTempIndex();
				// }
				if (compteur == this.reached) {
					System.out.println(Utils.memoryInfo());
					compteur = 0;
					saveTempIndex();
				}
				line=br.readLine();
				compteur++;
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		saveTempIndex();
		fusionIndexes(this.tmp_idx, this.name_idx);
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(estimatedTime / 1000);
	}

	private void saveTempIndex() {
		try {
			String out_idx = Common.DIRINDEX + this.name_idx + this.cur_index
					+ Common.extIDX;
			this.tmp_idx.add(out_idx);
			saveInvertedFile(index, new File(out_idx));
			this.index.clear();
			this.cur_index++;
			System.gc();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void saveInvertedFile(
			TreeMap<String, TreeMap<Integer,Integer>> invertedFile, File outFile)
			throws IOException {
		FileWriter fw = new FileWriter(outFile);
		for (Entry<String, TreeMap<Integer, Integer>> word : invertedFile.entrySet()) {
			String word_ = word.getKey();
			TreeMap<Integer,Integer> fileList = word.getValue();
			String line = word_ + "\t" + fileList.size()+"\t";
			String files = "";
			int i = 0;
			for (Map.Entry<Integer, Integer> doc : fileList.entrySet()) {
				files += (i==0)?doc.getKey()+":"+doc.getValue():","+doc.getKey()+":"+doc.getValue();
				i++;
			}
			line += files + "\n";
			fw.write(line);
		}
		fw.close();
	}
	
	/**
	 * Une méthode renvoyant le nombre d'occurrences
	 * de chaque mot dans un fichier.
	 * @param fileName le fichier à analyser
	 * @param normalizer la classe de normalisation utilisée
	 * @param removeStopWords
	 * @throws IOException
	 */
	private static HashMap<String, Integer> getTermFrequencies(String fileName, Normalizer normalizer, boolean removeStopWords) throws IOException {
		// Création de la table des mots
		HashMap<String, Integer> hits = new HashMap<String, Integer>();
		
		// Appel de la méthode de normalisation
		ArrayList<String> words = normalizer.normalize(new File(fileName), removeStopWords);
		Integer number;
		// Pour chaque mot de la liste, on remplit un dictionnaire
		// du nombre d'occurrences pour ce mot
		for (String word : words) {
			word = word.toLowerCase();
			// on récupère le nombre d'occurrences pour ce mot
			number = hits.get(word);
			// Si ce mot n'était pas encore présent dans le dictionnaire,
			// on l'ajoute (nombre d'occurrences = 1)
			if (number == null) {
				hits.put(word, 1);
			}
			// Sinon, on incrémente le nombre d'occurrence
			else {
				hits.put(word, ++number);
			}
		}
		return hits;
//		// Affichage du résultat
//		for (Map.Entry<String, Integer> hit : hits.entrySet()) {
//			System.out.println(hit.getKey() + "\t" + hit.getValue());
//		}
	}

	/**
	 * permet de fusionner tous les indexes temporaires de la LinkedList passer
	 * en paramètre en un seul fichier d'index. Chaque entrée de la LinkedList
	 * est un chemin vers un index temporaire
	 * 
	 * @param tmpIndexes
	 *            est un LinkedList contenant des chemins vers des indexes
	 *            temporaires
	 * @param nameIndex
	 *            correspond au prefix pour les noms des fichiers d'index
	 *            temporaire
	 */
	private static void fusionIndexes(LinkedList<String> tmpIndexes,
			String nameIndex) {
		// compteur pour gérer les indexes temporaires
		int counter = tmpIndexes.size();
		// tant qu'il reste au moins 2 indexes temporaires
		while (tmpIndexes.size() > 1) {
			// on dépile 2 indexes temporaires
			String outIndex1 = tmpIndexes.pollFirst();
			String outIndex2 = tmpIndexes.pollFirst();
			// on prépare les deux indexes
			File f1 = new File(outIndex1);
			File f2 = new File(outIndex2);
			// on prépare un nouvelle index temporaire fusion de f1 et f2
			String outIndexMerge = Common.DIRINDEX + nameIndex + counter
					+ Common.extIDX;
			counter++;
			// on fusionne les deux indexes temporaires
			File fMerge = new File(outIndexMerge);
			try {
				FusionIndex.mergeInvertedFiles(f1, f2, fMerge);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// on ajoute le nouvel index fusionné à la fin de la liste des
			// indexes temporaires
			tmpIndexes.addLast(outIndexMerge);
			// on supprime les 2 fichiers d'index temporaire
			f1.delete();
			f2.delete();
		}
		// on renomme à présent l'index temporaire résultat de la fusion de tous
		// les indexes temporaires
		String tmpIndex = tmpIndexes.poll();
		File f = new File(tmpIndex);
		String outIndex = Common.DIRINDEX + nameIndex + Common.extIDX;
		f.renameTo(new File(outIndex));
	}

	public void start() {
		th.start();
	}

	public void join() {
		try {
			th.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TaskIndexing(String fic_corpus, Normalizer n, boolean stopwords,
			String name_idx, int reached) {
		this.index = new TreeMap<>();
		this.normalizer = n;
		this.stopwords = stopwords;
		this.corpus = fic_corpus;
		this.tmp_idx = new LinkedList<>();
		this.name_idx = name_idx;
		this.th = new Thread(this);
		this.reached = reached;
	}

}
