package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

//Classe qui contient des méthodes communes pour les différentes package

public class Common {

	public static Boolean Choice = false;
	//false = version html
	//true = version texte
	public static String DIRRSC = "./rsc/";
	public static String CONTENTTYP = (Choice) ? "texte/" : "html/";
	public static String EXTCORPUS = (Choice) ? ".txt" : ".html";
	public static String DIRCORPUS = DIRRSC + "corpus/" + CONTENTTYP;
	public static String IDX_STEMMER = DIRRSC + "index/" + CONTENTTYP
			+ "troll_index_stemmer.dat";
	public static String IDX_TOKENIZER = DIRRSC + "index/" + CONTENTTYP
			+ "troll_index_tokenizer.dat";
	public static String DIRWEIGTH_STEMMER = DIRRSC + "weight/" + CONTENTTYP
			+ "stemmer/";
	public static String DIRWEIGTH_TOKENIZER = DIRRSC + "weight/" + CONTENTTYP
			+ "tokenizer/";
	public static ArrayList<String> emptyWords;

	public static String aListToString(ArrayList<String> aList) {
		StringBuilder stb = new StringBuilder();
		for (String word : aList) {
			stb.append(word);
			stb.append(" ");
		}
		return stb.toString();
	}
	
	private static void unload(){
		if (!(emptyWords == null))
		if (!emptyWords.isEmpty())
			emptyWords.clear();
	}

	// Fonction qui permet de charger la liste des mots vides
	public static void loadEmptyWords() {
		unload();
		File f;
		FileReader rd = null;
		BufferedReader br = null;
		String line;
		ArrayList<String> stop = null;
		try {
			stop = new ArrayList<>();
			f = new File("./rsc/stop.txt");
			rd = new FileReader(f);
			br = new BufferedReader(rd);
			while ((line = br.readLine()) != null) {
				stop.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
				rd.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		emptyWords = stop;
	}

	// Fonction qui permet de renvoyer un Map trié par les valeurs
	public static SortedSet<Entry<Integer, Double>> sortMap(Map mp) {
		TreeSet sortedMap = new TreeSet<Map.Entry<Integer, Double>>(
				new Comparator<Map.Entry<Integer, Double>>() {
					@Override
					public int compare(Map.Entry<Integer, Double> e1,
							Map.Entry<Integer, Double> e2) {
						int comp = Double.compare(e1.getValue(), e2.getValue());
						if (comp == 0)
							return 0;
						else if (comp > 0)
							return -1;
						else
							return 1;

					}
				});
		sortedMap.addAll(mp.entrySet());
		return sortedMap;
	}

	// Fonction qui renvoit vrai si le mot en paramètre est un mot vide
	public static boolean isEmptyWord(String word)
	{
		String regexp = "[a-zA-Zéèçà0-9]*";
	    return !word.toLowerCase().matches(regexp) || Common.emptyWords.contains(word.toLowerCase());
	}

	// Fonction qui renvoit la liste des fichiers contenus dans un répertoire en
	// fonction de son extension
	public static void getDirectory(File dirName,
			ArrayList<String[]> listFiles, final String extension) {
		File[] ltFiles = null;
		// Liste des fichiers du répertoire
		// ajouter un filtre (FileNameFilter) sur les noms
		// des fichiers si nécessaire
		ltFiles = dirName.listFiles();
		for (File file : ltFiles) {
			if (file.isDirectory())
				getDirectory(file, listFiles, extension);
			else if (file.getName().endsWith(extension)) {
				String[] fileStr = new String[2];
				fileStr[0] = file.getAbsolutePath();
				fileStr[1] = file.getName();
				listFiles.add(fileStr);
			}
		}
	}

	// permet de générer une liste de fichiers
	public static HashMap<Integer, String[]> buildDirectory(File directory,
			final String extension) {
		HashMap<Integer, String[]> ltFiles = new HashMap<>();
		ArrayList<String[]> fileNames = new ArrayList<>();
		getDirectory(directory, fileNames, extension);
		for (String[] fileName : fileNames) {
			ltFiles.put(fileName[1].hashCode(), fileName);
		}
		return ltFiles;
	}

	// permet de générer une liste de fichiers
	public static ArrayList<String[]> buildDirectory2(File directory,
			final String extension) {
		ArrayList<String[]> ltFiles = new ArrayList<>();
		getDirectory(directory, ltFiles, extension);
		return ltFiles;
	}
	
	public static void writeRequest(String content) throws IOException{
		File f = new File(DIRRSC+"request.htm");
		if (f.exists())
			f.delete();
		FileWriter fw = new FileWriter(f);
		fw.write(content);
		fw.close();
	}

}
