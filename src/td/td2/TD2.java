package td.td2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;

/**
 * TD 2
 * @author xtannier
 *
 */
public class TD2 {
	
	/**
	 * Le répertoire du corpus
	 */
	private static String DIRNAME = "/home/xtannier/data/lemonde/";
	/**
	 * Le fichier contenant les mots vides
	 */
	private static String STOPWORDS_FILENAME = "frenchST.txt";

	
	/**
	 * Une méthode renvoyant le nombre d'occurrences
	 * de chaque mot dans un fichier.
	 * @param fileName le fichier à analyser
	 * @param normalizer la classe de normalisation utilisée
	 * @param removeStopWords
	 * @throws IOException
	 */
	public static HashMap<String, Integer> getTermFrequencies(String fileName, Normalizer normalizer, boolean removeStopWords) throws IOException {
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
	 * exo 2.1 : Calcule le df, c'est-à-dire le nombre de documents
	 * pour chaque mot apparaissant dans la collection. Le mot
	 * "à" doit ainsi apparaître dans 88 documents, le mot
	 * "ministère" dans 4 documents.
	 * @param dirName
	 * @param normalizer
	 * @param removeStopWords
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, Integer> getDocumentFrequency(String dirName, Normalizer normalizer, boolean removeStopWords) throws IOException {
		File dir = new File(dirName);
		HashMap<String, Integer> hits = new HashMap<String, Integer>();
		ArrayList<String> wordsInFile;
		ArrayList<String> words;
		String wordLC;
		if (dir.isDirectory()) {
			// Liste des fichiers du répertoire
			// ajouter un filtre (FileNameFilter) sur les noms
			// des fichiers si nécessaire
			String[] fileNames = dir.list();
			
			Integer number;
			for (String fileName : fileNames) {
				System.err.println("Analyse du fichier " + fileName);
				// Les mots présents dans ce document
				wordsInFile = new ArrayList<String>();
				// Appel de la méthode de normalisation
				words = normalizer.normalize(new File(dirName + File.separator + fileName), removeStopWords);
				// Pour chaque mot de la liste, on remplit un dictionnaire
				// du nombre d'occurrences pour ce mot
				for (String word : words) {
					wordLC = word;
					wordLC = wordLC.toLowerCase();
					// si le mot n'a pas déjà été trouvé dans ce document :
					if (!wordsInFile.contains(wordLC)) {
						number = hits.get(wordLC);
						// Si ce mot n'était pas encore présent dans le dictionnaire,
						// on l'ajoute (nombre d'occurrences = 1)
						if (number == null) {
							hits.put(wordLC, 1);
						}
						// Sinon, on incrémente le nombre d'occurrence
						else {
							hits.put(wordLC, number+1);
						}
						wordsInFile.add(wordLC);
					}
				}
			}
		}

		// Affichage du résultat (avec la fréquence)	
//		for (Map.Entry<String, Integer> hit : hits.entrySet()) {
//			System.out.println(hit.getKey() + "\t" + hit.getValue());
//		}
		return hits;
	}
	
	/**
	 * exo 2.4 : Calcule le tf.idf des mots d'un fichier en fonction
	 * des df déjà calculés, du nombre de documents et de
	 * la méthode de normalisation.
	 * @param fileName
	 * @param dfs
	 * @param documentNumber
	 * @param normalizer
	 * @param removeStopWords
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, Double> getTfIdf(String fileName, HashMap<String, Integer> dfs, int documentNumber, Normalizer normalizer, boolean removeStopWords) throws IOException {
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
		
		Integer tf;
		Double tfIdf;
		String word;
		HashMap<String, Double> tfIdfs = new HashMap<String, Double>();

		// Calcul des tf.idf
		for (Map.Entry<String, Integer> hit : hits.entrySet()) {
			tf = hit.getValue();
			word = hit.getKey();
			tfIdf = (double)tf * Math.log((double)documentNumber / (double)dfs.get(word));
			tfIdfs.put(word, tfIdf);
		}
		return tfIdfs;
	}
	
	/**
	 * exo 2.5 : Crée, pour chaque fichier d'un répertoire, un nouveau
	 * fichier contenant les poids de chaque mot. Ce fichier prendra
	 * la forme de deux colonnes (mot et poids) séparées par une tabulation.
	 * Les mots devront être placés par ordre alphabétique.
	 * Les nouveaux fichiers auront pour extension .poids
	 * et seront écrits dans le répertoire outDirName.
	 * @param inDirName
	 * @param outDirName
	 * @param normalizer
	 * @param removeStopWords
	 * @throws IOException
	 */
	private static void getWeightFiles(String inDirName, String outDirName, Normalizer normalizer, boolean removeStopWords) throws IOException {
		// calcul des dfs
		HashMap<String, Integer> dfs = getDocumentFrequency(inDirName, normalizer, removeStopWords);
		// Nombre de documents
		String[] fileNames = new File(inDirName).list();
		int documentNumber = fileNames.length;
		// TfIdfs 
		for (String fileName : fileNames) {
			HashMap<String, Double> tfIdfs = getTfIdf(inDirName + File.separator + fileName, dfs, documentNumber, normalizer, removeStopWords);
			TreeSet<String> words = new TreeSet<String>(tfIdfs.keySet());
			// on écrit dans un fichier
			try {
				if (!(new File(outDirName)).exists()) {
					(new File(outDirName)).mkdirs();
				}
				FileWriter fw = new FileWriter (outDirName + File.separator + fileName + ".poids");
				BufferedWriter bw = new BufferedWriter (fw);
				PrintWriter out = new PrintWriter (bw);
				// Ecriture des mots
				for (String word : words) {
					out.println(word + "\t" + tfIdfs.get(word)); 
				}
				out.close();
			}
			catch (Exception e){
				System.out.println(e.toString());
			}		
		}
	}


	/**
	 * Main, appels de toutes les méthodes des exercices du TD1. 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String outDir = "/home/xtannier/tmp/sorties";
			Normalizer stemmer = new FrenchStemmer(STOPWORDS_FILENAME);
			Normalizer tokenizer = new FrenchTokenizer(STOPWORDS_FILENAME);
			Normalizer[] normalizers = {tokenizer, stemmer};
			for (Normalizer normalizer : normalizers) {
				System.out.println("Normalisation avec " + normalizer.getClass().getName());
				System.out.println("  GetDocumentFrequency sans les stop words");
				System.out.println(getDocumentFrequency(DIRNAME, normalizer, false).size());
				System.out.println("  GetDocumentFrequency avec les stop words");
				System.out.println(getDocumentFrequency(DIRNAME, normalizer, true).size());
				System.out.println("  GetWeightFiles sans les stop words");
				getWeightFiles(DIRNAME, outDir + "/" + normalizer.getClass().getName() + "_noSW", normalizer, true);
				System.out.println("  GetWeightFiles avec les stop words");
				getWeightFiles(DIRNAME, outDir + "/" + normalizer.getClass().getName(), normalizer, false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
