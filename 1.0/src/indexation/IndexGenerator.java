package indexation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;

import common.Common;

public class IndexGenerator {

	// index du tokenizer
	public static HashMap<String, Directory> idx_tokenizer;
	// index du stemmer
	public static HashMap<String, Directory> idx_stemmer;
	// liste des fichiers du corpus, string[0] le chemin, string[1] le nom de
	// fichier
	public static HashMap<Integer, String[]> dirCorpus;
	// liste des fichiers de poids pour le tokenizer
	public static ArrayList<String[]> dirWgtToken;
	// liste des fichiers de poids pour le stemmer
	public static ArrayList<String[]> dirWgtStem;

	public static String getFileName(Integer fileID) {
		return dirCorpus.get(fileID)[1];
	}
	
	private void checkDir(){
		File idx1 = new File("./rsc/index/html");
		idx1.mkdirs();
		File idx2 = new File("./rsc/index/html");
		idx2.mkdirs();
		File w1 = new File("./rsc/weight/html/stemmer");
		w1.mkdirs();
		File w2 = new File("./rsc/weight/html/tokenizer");
		w2.mkdirs();	
		File w3 = new File("./rsc/weight/texte/stemmer");
		w3.mkdirs();
		File w4 = new File("./rsc/weight/texte/tokenizer");
		w4.mkdirs();
	}

	public HashMap<String, Directory> readIndex(File fileName)
			throws IOException {
		FileInputStream fr = null;
		InputStreamReader str = null;
		BufferedReader br = null;
		HashMap<String, Directory> idx = null;
		try {
			idx = new HashMap<>();
			fr = new FileInputStream(fileName);
			str = new InputStreamReader(fr, "UTF-8");
			br = new BufferedReader(str);
			String line;
			while ((line = br.readLine()) != null) {
				String[] list = line.split("\t");
				Directory dir = new Directory();
				for (int i = 1; i <= list.length - 1; i += 2) {
					Integer key = Integer.valueOf(list[i]);
					Integer value = Integer.valueOf(list[i + 1]);
					dir.getRefs().put(key, value);
				}
				idx.put(list[0], dir);
			}
		} finally {
			br.close();
			str.close();
			fr.close();
		}
		return idx;

	}

	public void writeIndex(File fileName, HashMap<String, Directory> idx)
			throws IOException {
		FileOutputStream fw = null;
		OutputStreamWriter stw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileOutputStream(fileName);
			stw = new OutputStreamWriter(fw, "UTF-8");
			bw = new BufferedWriter(stw);
			for (Map.Entry<String, Directory> word : idx.entrySet()) {
				int capacity = word.getValue().df() * 13 + 30;
				StringBuilder stb = new StringBuilder(capacity);
				stb.append(word.getKey());
				for (Map.Entry<Integer, Integer> file : word.getValue()
						.getRefs().entrySet()) {
					stb.append("\t");
					stb.append(file.getKey());
					stb.append("\t");
					stb.append(file.getValue());
				}
				bw.write(stb.toString());
				bw.newLine();
			}
		} finally {
			bw.close();
			stw.close();
			fw.close();
		}
	}

	// créer un index mot->liste des fichiers s'y rapportant
	// prend en paramètre un répertoire le nom de l'index et le type de
	// normalisation
	public void buildIndex(File index, Normalizer normalizer) {
		// Création de la table des mots
		HashMap<String, Directory> words = new HashMap<String, Directory>();
		for (Entry<Integer, String[]> file : dirCorpus.entrySet()) {
			Integer fileID = file.getKey();
			String filePath = file.getValue()[0];
			try {
				ArrayList<String> ltWords;
				if (Common.Choice)
					ltWords = normalizer.normalize(new File(filePath));
				else {
					HtmlParser parser = new HtmlParser();
					parser.parseText(new File(filePath));
					ltWords = normalizer.normalize(parser.getFullTextTags());
				}
				for (String word : ltWords) {
					String wordLC = word.toLowerCase();
					if (!Common.isEmptyWord(wordLC)) {
						if (words.get(wordLC) == null) {
							words.put(wordLC, new Directory());
						}
						if (!words.get(wordLC).getRefs().containsKey(fileID)) {
							words.get(wordLC).getRefs().put(fileID, 1);
						} else {
							words.get(wordLC)
									.getRefs()
									.put(fileID,
											words.get(wordLC).getRefs()
													.get(fileID) + 1);
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			writeIndex(index, words);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void unload() {
		if (!(dirCorpus == null))
		if (!dirCorpus.isEmpty())
			dirCorpus.clear();
		
		if (!(dirCorpus == null))
		if (!dirWgtStem.isEmpty())
			dirWgtStem.clear();
		
		if (!(dirWgtToken == null))
		if (!dirWgtToken.isEmpty())
			dirWgtToken.clear();
		
		if (!(idx_stemmer == null))
		if (!idx_stemmer.isEmpty())
			idx_stemmer.clear();
		
		if (!(idx_tokenizer == null))
		if (!idx_tokenizer.isEmpty())
			idx_tokenizer.clear();
		
		Common.DIRRSC = "./rsc/";
		Common.CONTENTTYP = (Common.Choice) ? "texte/" : "html/";
		Common.EXTCORPUS = (Common.Choice) ? ".txt" : ".html";
		Common.DIRCORPUS = Common.DIRRSC + "corpus/" + Common.CONTENTTYP;
		Common.IDX_STEMMER = Common.DIRRSC + "index/" + Common.CONTENTTYP
				+ "troll_index_stemmer.dat";
		Common.IDX_TOKENIZER = Common.DIRRSC + "index/" + Common.CONTENTTYP
				+ "troll_index_tokenizer.dat";
		Common.DIRWEIGTH_STEMMER = Common.DIRRSC + "weight/" + Common.CONTENTTYP
				+ "stemmer/";
		Common.DIRWEIGTH_TOKENIZER = Common.DIRRSC + "weight/" + Common.CONTENTTYP
				+ "tokenizer/";
	}

	// méthode permettant de créer si nécessaire les indexs, les fichiers de
	// poids et de charger les indexs en mémoire
	public void generate() {
		checkDir();
		unload();
		Common.loadEmptyWords();
		dirCorpus = Common.buildDirectory(new File(Common.DIRCORPUS),
				Common.EXTCORPUS);
		dirWgtToken = Common.buildDirectory2(new File(
				Common.DIRWEIGTH_TOKENIZER), ".poids");
		dirWgtStem = Common.buildDirectory2(
				new File(Common.DIRWEIGTH_TOKENIZER), ".poids");

		File f_tokenizer = new File(Common.IDX_TOKENIZER);
		File f_stemmer = new File(Common.IDX_STEMMER);

		// génère l'index du tokenizer s'il n'existe pas
		if (!f_tokenizer.exists())
			buildIndex(new File(Common.IDX_TOKENIZER), new FrenchTokenizer());
		// génère l'index du stemmer s'il n'existe pas
		if (!f_stemmer.exists())
			buildIndex(new File(Common.IDX_STEMMER), new FrenchStemmer());

		// charge les indexs en mémoire
		try {
			idx_tokenizer = readIndex(new File(Common.IDX_TOKENIZER));
			idx_stemmer = readIndex(new File(Common.IDX_STEMMER));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// génère les fichiers de poids du tokenizer si nécessaire
		Weighting weight = new Weighting();
		if (dirWgtToken.size() < dirCorpus.size())
			weight.buildWeightFiles(Common.DIRWEIGTH_TOKENIZER,
					idx_tokenizer, new FrenchTokenizer());

		// génère les fichiers de poids du stemmer si nécessaire
		if (dirWgtStem.size() < dirCorpus.size())
			weight.buildWeightFiles(Common.DIRWEIGTH_STEMMER, idx_stemmer,
					new FrenchStemmer());
	}

}
