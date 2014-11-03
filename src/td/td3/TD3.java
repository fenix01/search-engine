package td.td3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;

public class TD3 {

	private static String DIRNAME = ".\\lemonde\\";

	private static String STOPWORDS_FILENAME = ".\\stop.txt";

	private static String OUTDIR = ".\\inverted\\";

//	public static TreeMap<String, TreeMap<Integer,Integer>> getInvertedFile(
//			String dirName, Normalizer normalizer, boolean removeStopWords)
//			throws IOException {
//		TreeMap<String, TreeMap<Integer,Integer>> invertedFile = new TreeMap<>();
//
//		String[] fileNames = new File(dirName).list();
//		for (String fileName : fileNames) {
//
//			// Appel de la méthode de normalisation
//			ArrayList<String> words = normalizer.normalize(new File(dirName
//					+ fileName), removeStopWords);
//			// parcours la liste des mots
//			for (String word : words) {
//
//				TreeSet<String> filesList = null;
//				// récupère la liste des fichiers pour le mot word
//				if (invertedFile.containsKey(word)) {
//					filesList = invertedFile.get(word);
//				} else {
//					filesList = new TreeSet<>();
//					invertedFile.put(word, filesList);
//				}
//				invertedFile.get(word).add(fileName);
//			}
//		}
//		return invertedFile;
//	}

	public static void saveInvertedFile(
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

	public static TreeMap<String, TreeMap<String, Integer>> getInvertedFileWithWeights(
			String dirName, Normalizer normalizer, boolean removeStopWords)
			throws IOException {
		TreeMap<String, TreeMap<String, Integer>> invertedFileWithWeights = new TreeMap<>();

		String[] fileNames = new File(dirName).list();
		for (String fileName : fileNames) {

			HashMap<String, Integer> hits = td.td2.TD2.getTermFrequencies(
					dirName + fileName, normalizer, removeStopWords);

			// parcours la liste des mots
			for (Map.Entry<String, Integer> word : hits.entrySet()) {

				TreeMap<String, Integer> filesList = null;
				// récupère la liste des fichiers pour le mot word
				if (invertedFileWithWeights.containsKey(word.getKey())) {
					filesList = invertedFileWithWeights.get(word.getKey());
				} else {
					filesList = new TreeMap<>();
					invertedFileWithWeights.put(word.getKey(), filesList);
				}
				invertedFileWithWeights.get(word.getKey()).put(fileName, word.getValue());
			}
		}
		return invertedFileWithWeights;

	}
	
	public static void saveInvertedFileWithWeights(
			TreeMap<String, TreeMap<String,Integer>> invertedFile, File outFile)
			throws IOException {
		FileWriter fw = new FileWriter(outFile);
		for (Map.Entry<String, TreeMap<String,Integer>> word : invertedFile.entrySet()) {
			String word_ = word.getKey();
			TreeMap<String,Integer> fileList = word.getValue();
			String line = word_ + "\t" + fileList.size() + "\t";
			String files = "";
			int i = 0;
			for (Map.Entry<String, Integer> filename : fileList.entrySet()) {
				if (i==0)
					files += filename.getKey() + ":" + filename.getValue();
				else
					files += "," + filename.getKey() + ":" + filename.getValue();
				i++;
			}
			line += files + "\n";
			fw.write(line);
		}
		fw.close();
	}

	public static void main(String[] args) {
//		try {
//			Normalizer stemmer = new FrenchStemmer(STOPWORDS_FILENAME);
//			Normalizer tokenizer = new FrenchTokenizer(STOPWORDS_FILENAME);
//			Normalizer[] normalizers = { tokenizer, stemmer };
//			
//			String normstr = "tokenizer";
//			
//			for (Normalizer normalizer : normalizers) {
//				System.out.println("  getInvertedFile avec les stop words");
//				
//				TreeMap<String, TreeSet<String>> invertedFile = getInvertedFile(
//						DIRNAME, normalizer, true);
//				
//				System.out.println(invertedFile.size());
//				
//				System.out.println("  saveInvertedFile");
//				
//				saveInvertedFile(invertedFile,
//						new File(OUTDIR + normstr));
//				
//				System.out.println("-----------------------------------------");
//				System.out.println("  getInvertedFileWithWeights avec les stop words");
//				TreeMap<String, TreeMap<String, Integer>> invertedFileWithWeights = getInvertedFileWithWeights(
//						DIRNAME, normalizer, true);
//				System.out.println(invertedFileWithWeights.size());
//				System.out.println("  saveInvertedFileWithWeights");
//				saveInvertedFileWithWeights(invertedFileWithWeights, new File(OUTDIR + normstr + "withWeights"));
//				System.out.println("-----------------------------------------");
//				
//				normstr = "stemmer";
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
