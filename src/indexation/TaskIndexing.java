package indexation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import tools.Normalizer;

/**
 * Thread créant l'index
 */
public class TaskIndexing implements Runnable {

	private String corpus;
	// on stocke l'index sous la forme d'un treemap de
	// String = mot en clé, et un TreeMap<DocID,tf> en valeur
	// pour la recherche on a besoin de trier les documents
	private TreeMap<String, TreeMap<Integer, Short>> index;
	private Normalizer normalizer;
	private boolean stopwords;
	private LinkedList<String> tmp_idx;
	private Thread th;
	private int reached = 0;
	private int cur_index = 0;
	private String name_idx = "";
	public static String index_dir;

	/**
	 * permet de fusionner les index finaux générer par les threads 
	 * @param threads_count
	 */
	public static void fusionThreadsIndexes(int threads_count) {
		LinkedList<String> threadsIndex = new LinkedList<>();
		for (int i = 0; i < threads_count; i++) {
			String indexName = index_dir + "index" + i + Common.extIDX;
			threadsIndex.add(indexName);
		}
		fusionIndexes(threadsIndex, "index");
	}

	/**
	 * permet de découper l'index binaire selon les X premières lettres des mots
	 * @param x
	 */
	public static void splitBinaryIndex(int x) {
		FileInputStream fis;
		BufferedInputStream bis;
		DataInputStream dis;
		try {
			File fIndex = new File(index_dir + "index" + Common.extIDX);
			fis = new FileInputStream(fIndex);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			String word1 = dis.readUTF();
			int df = dis.readInt();
			byte[] docs = FusionIndex.getBinaryDocs(df, dis);
			String word2 = dis.readUTF();

			String firstOccLine1;
			String firstOccLine2;
			firstOccLine1 = Common.firstOcc(word1, x);
			firstOccLine2 = Common.firstOcc(word2, x);
			
			String occName = index_dir + firstOccLine1 + Common.extIDX;
			File fOcc = new File(occName);
			FileOutputStream fos = new FileOutputStream(fOcc);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			DataOutputStream dos = new DataOutputStream(bos);		

			boolean EOF = false;
			while (!EOF) {
				if (firstOccLine1.equals(firstOccLine2)) {
					if (df > 1) {
						dos.writeUTF(word1);
						dos.writeInt(df);
						dos.write(docs);
					}
				} else {
					if (df > 1) {
						dos.writeUTF(word1);
						dos.writeInt(df);
						dos.write(docs);
						dos.close();
					}
					dos.close();
					if (fOcc.length() == 0) fOcc.delete();
					occName = index_dir + firstOccLine2 + Common.extIDX;
					fOcc = new File(occName);
					fos = new FileOutputStream(fOcc);
					bos = new BufferedOutputStream(fos);
					dos = new DataOutputStream(fos);	
				}
				word1 = word2;
				df = dis.readInt();
				docs = FusionIndex.getBinaryDocs(df, dis);
				if (dis.available() > 0){
					word2 = dis.readUTF();
					firstOccLine1 = Common.firstOcc(word1, x);
					firstOccLine2 = Common.firstOcc(word2, x);
				}
				else EOF = true;	
			}
			dos.writeUTF(word1);
			dos.writeInt(df);
			dos.write(docs);
			dos.close();
			dis.close();
			fIndex.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * permet de découper l'index selon les X premières lettres des mots
	 * @param x
	 */
	public static void splitIndex(int x) {
		FileReader fr;
		FileWriter fw;
		try {
			File fIndex = new File(index_dir + "index" + Common.extIDX);
			fr = new FileReader(fIndex);
			BufferedReader br = new BufferedReader(fr);

			String line1 = br.readLine();
			String line2 = br.readLine();

			String firstOccLine1;
			String firstOccLine2;
			firstOccLine1 = Common.firstOcc(line1, x);
			firstOccLine2 = Common.firstOcc(line2, x);
			
			String occName = index_dir + firstOccLine1 + Common.extIDX;
			File fOcc = new File(occName);
			fw = new FileWriter(fOcc, true);
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
					if (fOcc.length() == 0) fOcc.delete();
					occName = index_dir + firstOccLine2 + Common.extIDX;
					fOcc = new File(occName);
					fw = new FileWriter(fOcc, true);
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		int compteur = 0;
		FileReader fr;

		try {
			fr = new FileReader(new File(Common.DIRRSC + corpus));
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				try {
					HashMap<String, Short> tf_doc;

					String[] doc = line.split("\t");
					int hash_doc = Integer.parseInt((doc[0]));
					tf_doc = getTermFrequencies(doc[1], normalizer,
							stopwords);
					for (Map.Entry<String, Short> word : tf_doc.entrySet()) {
						String w = word.getKey();
						if (!Common.isEmptyWord(w)) {
							TreeMap<Integer, Short> listDocs = index.get(w);
							if (listDocs != null) {
								// le mot existe dans l'index, on l'ajoute dans
								// la liste des documents
								listDocs.put(hash_doc, word.getValue());
								index.put(w, listDocs);
							} else {
								TreeMap<Integer, Short> list_docs = new TreeMap<>();
								list_docs.put(hash_doc, word.getValue());
								index.put(w, list_docs);
							}
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				if (compteur == this.reached) {
					compteur = 0;
					saveTempIndex();
				}
				line=br.readLine();
				compteur++;
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	
		saveTempIndex();
		fusionIndexes(this.tmp_idx, this.name_idx);
		System.gc();
	}

	private void saveTempIndex() {
		try {
			String out_idx = index_dir + this.name_idx + this.cur_index
					+ Common.extIDX;
			this.tmp_idx.add(out_idx);
			saveInvertedBinaryFile(index, new File(out_idx));
			this.index.clear();
			this.cur_index++;
			System.gc();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
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
	 * permet de sauvegarder un index temporaire dans un fichier binaire.
	 * Les données sont stockées de la façon suivante :
	 * String+Int(taille)+Int(doc1)+Int(tf1)+Float(poids1)+...+Int(docN)+Int(tfN)+Int(poidsN)
	 * @param invertedFile l'index en mémoire
	 * @param outFile un fichier d'index qui va recevoir les données
	 * @throws IOException
	 */
	private static void saveInvertedBinaryFile(
			TreeMap<String, TreeMap<Integer,Short>> invertedFile, File outFile)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(outFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		DataOutputStream dos = new DataOutputStream(bos);
		//parcours toutes les entrées du TreeMap
		for (Entry<String, TreeMap<Integer, Short>> word : invertedFile.entrySet()) {
			//récupère le mot
			String word_ = word.getKey();
			//écrit le mot dans le fichier binaire
			dos.writeUTF(word_);
			//récupère la liste des documents
			TreeMap<Integer,Short> fileList = word.getValue();
			
			//écrit le nombre de documents de la liste
			dos.writeInt(fileList.size());
			
			for (Map.Entry<Integer, Short> doc : fileList.entrySet()) {
				//on écrit le docID
				dos.writeInt(doc.getKey());
				//on écrit le tf
				dos.writeShort(doc.getValue());
			}
		}
		dos.close();
	}
	
	/**
	 * Une méthode renvoyant le nombre d'occurrences
	 * de chaque mot dans un fichier.
	 * @param fileName le fichier à analyser
	 * @param normalizer la classe de normalisation utilisée
	 * @param removeStopWords
	 * @throws IOException
	 */
	private static HashMap<String, Short> getTermFrequencies(String fileName, Normalizer normalizer, boolean removeStopWords) throws IOException {
		// Création de la table des mots
		HashMap<String, Short> hits = new HashMap<String, Short>();
		
		// Appel de la méthode de normalisation
		ArrayList<String> words = normalizer.normalize(new File(fileName), removeStopWords);
		Short number;
		// Pour chaque mot de la liste, on remplit un dictionnaire
		// du nombre d'occurrences pour ce mot
		for (String word : words) {
			word = word.toLowerCase();
			// on récupère le nombre d'occurrences pour ce mot
			number = hits.get(word);
			// Si ce mot n'était pas encore présent dans le dictionnaire,
			// on l'ajoute (nombre d'occurrences = 1)
			if (number == null) {
				hits.put(word, (short) 1);
			}
			// Sinon, on incrémente le nombre d'occurrence
			else {
				hits.put(word, ++number);
			}
		}
		return hits;
	}

	/**
	 * permet de fusionner tous les index temporaires de la LinkedList passée
	 * en paramètre en un seul fichier d'index. Chaque entrée de la LinkedList
	 * est un chemin vers un index temporaire
	 * 
	 * @param tmpIndexes
	 *            est un LinkedList contenant des chemins vers des index
	 *            temporaires
	 * @param nameIndex
	 *            correspond au prefix pour les noms des fichiers d'index
	 *            temporaire
	 */
	private static void fusionIndexes(LinkedList<String> tmpIndexes,
			String nameIndex) {
		// compteur pour gérer les index temporaires
		int counter = tmpIndexes.size();
		// tant qu'il reste au moins 2 index temporaires
		while (tmpIndexes.size() > 1) {
			// on dépile 2 index temporaires
			String outIndex1 = tmpIndexes.pollFirst();
			String outIndex2 = tmpIndexes.pollFirst();
			// on prépare les deux index
			File f1 = new File(outIndex1);
			File f2 = new File(outIndex2);
			// on prépare un nouvelle index temporaire fusion de f1 et f2
			String outIndexMerge = index_dir + nameIndex + counter
					+ Common.extIDX;
			counter++;
			// on fusionne les deux index temporaires
			File fMerge = new File(outIndexMerge);
			try {
				FusionIndex.mergeInvertedBinaryFiles(f1, f2, fMerge);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// on ajoute le nouvel index fusionné à la fin de la liste des
			// index temporaires
			tmpIndexes.addLast(outIndexMerge);
			// on supprime les 2 fichiers d'index temporaire
			f1.delete();
			f2.delete();
		}
		// on renomme à présent l'index temporaire résultat de la fusion de tous
		// les index temporaires
		String tmpIndex = tmpIndexes.poll();
		File f = new File(tmpIndex);
		String outIndex = index_dir + nameIndex + Common.extIDX;
		f.renameTo(new File(outIndex));
	}

	public void start() {
		th.start();
	}

	public void join() {
		try {
			th.join();
		} catch (InterruptedException e) {
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
