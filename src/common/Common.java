package common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import main.Main;

/**
 * Classe contenant des méthodes communes pour les différents package
 */

public class Common {
	
	//extension d'un fichier temporaire
	public static String extTMP = ".tmp";
	//extension de l'index
	public static String extIDX = ".idx";
	//extension du fichier poids
	public static String extWEIGHT = ".wgt";	
	//extension du fichier poids
	public static String extCORPUSLIST = ".corpus";	
	//emplacement des différents index
	
	

	public static String FICEMPTYWORD = null;
	
	public static String DIRRSC = null;
	
	public static String FICINI = "./config.ini";
	
	public static String DIRINDEX = null;
	public static String stemmername="FrenchStemmer";
	public static String tokenizername="FrenchTokenizer";
	public static String DIRSTEM = null;
	public static String DIRTOKEN = null;
	//emplacement du corpus
	public static String DIRCORPUS = null;
	
	public static ArrayList<String> emptyWords;
	
	private static void unloadEmptyWords(){
		if (!(emptyWords == null))
			emptyWords.clear();
	}

	/**
	 *  Fonction qui permet de charger la liste des mots vides
	 * @throws IOException 
	 */
	
	public static void load_data() throws IOException{
		

		FileReader fr = new FileReader(new File(Common.FICINI));
		BufferedReader br = new BufferedReader(fr);
		String line =br.readLine();
		Main.nb_doc=Integer.parseInt(line);
		line=br.readLine();
		Main.nb_thread=Integer.parseInt(line);
		line=br.readLine();
		Common.DIRCORPUS=line;
		line=br.readLine();
		Common.FICEMPTYWORD=line;
		line=br.readLine();
		Common.DIRRSC=line;
		br.close();
		
		DIRINDEX= DIRRSC + "index/";
		DIRSTEM = Common.DIRINDEX + stemmername +"/";
		DIRTOKEN = Common.DIRINDEX + tokenizername +"/";
		
	}
	
	public static void loadEmptyWords() {
		unloadEmptyWords();
		File f;
		FileReader rd = null;
		BufferedReader br = null;
		String line;
		ArrayList<String> stop = null;
		try {
			stop = new ArrayList<>();
			f = new File(FICEMPTYWORD);
			rd = new FileReader(f);
			br = new BufferedReader(rd);
			while ((line = br.readLine()) != null) {
				stop.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				rd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		emptyWords = stop;
	}
	
	/**
	 * retourne les premières lettres d'un mot de la ligne d'index 
	 * vérifie si certains mots ont une taille inférieure à x
	 * 
	 * @param line
	 * @param x
	 * @return
	 */
	public static String firstOcc(String line, int x) {
		if (line == null)
			return null;
		String word = line.split("\t")[0];
		if (word.length() < x)
			return word;
		else
			return word.substring(0, x);
	}

	/**
	 * Fonction qui permet de renvoyer un Map trié par valeur
	 * @param mp
	 * @return
	 */
	public static SortedSet<Entry<String, Float>> sortMap(Map mp) {
		TreeSet sortedMap = new TreeSet<Map.Entry<String, Float>>(
				new Comparator<Map.Entry<String, Float>>() {
					@Override
					public int compare(Map.Entry<String, Float> e1,
							Map.Entry<String, Float> e2) {
						int comp = Float.compare(e1.getValue(), e2.getValue());
						if (comp == 0)
							return 0;
						else if (comp > 0)
							return -1;
						else
							return 1;

					}
				});
		sortedMap.addAll(mp.entrySet());
		mp.clear();
		System.gc();
		return sortedMap;
	}

	/**
	 *  Fonction qui renvoie vrai si le mot en paramètre est un mot vide
	 * @param word
	 * @return
	 */
	public static boolean isEmptyWord(String word)
	{
		String regexp = "[a-zA-Zéèçà0-9]+[-]?[a-zA-Zéèçà0-9]*";
	    return !word.toLowerCase().matches(regexp) || Common.emptyWords.contains(word.toLowerCase());
	}

	/**
	 *  Fonction qui met la liste des fichiers contenus dans un répertoire en fonction de leur extension
	 *  dans la variable listFiles
	 */
	public static void getDirectory(File f, TreeMap<Integer,String> listFiles, final String ext, int max_entries) 
			throws IOException {
		
		if (max_entries != -1 && listFiles.size() >= max_entries)
			return;
		// Liste des fichiers du répertoire
		// ajouter un filtre (FileNameFilter) sur les noms
		// des fichiers si nécessaire
		File[] ltFiles=null;
		ltFiles = f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				//filtre les fichiers en ignorant les fichiers ne commençant pas par ext
				if (!pathname.isDirectory() && !pathname.getName().endsWith(ext))
					return false;
				return true;
			}
		});

		for (File file_ : ltFiles) {
			if(file_.isDirectory())
					getDirectory(file_, listFiles,ext,max_entries);
			else{
				listFiles.put(listFiles.size(),file_.getCanonicalPath());
			}	
		}
	}
	
	/**
	 * écrit la liste des documents dans un fichier .corpus.
	 * permet de sauvegarder le dictionnaire inversé, permettant de retrouver
	 * un fichier à partir de son identifiant numérique.
	 */
	public static void writeDirectory(TreeMap<Integer, String> h, int nb_th,int nb_doc){
		int modulo=nb_doc/nb_th;
		
		try {
			FileWriter fwall = new FileWriter(new File(DIRRSC+"html"+extCORPUSLIST));
			BufferedWriter bwall = new BufferedWriter(fwall);
			
				for(int j=0;j<nb_th;j++){
					
					FileWriter fw = new FileWriter(new File(DIRRSC+j+extCORPUSLIST));
					BufferedWriter bw = new BufferedWriter(fw);
					
					while(h.size()>0 && h.firstKey()/modulo==j){
						
							bw.write(h.firstKey()+"\t"+h.get(h.firstKey()));
							bw.newLine();
							bwall.write(h.get(h.firstKey()).substring(0, h.get(h.firstKey()).length()-3)+"html");
							bwall.newLine();
							h.remove(h.firstKey());
					}
					bw.close();
					fw.close();
				}
				bwall.close();
			
			}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * permet de récupérer la liste des fichiers du corpus à partir d'un fichier .corpus 
	 */
	public static TreeMap<Integer, String[]> readDirectory(){
		TreeMap<Integer,String[]> listFiles = new TreeMap<>();
		try {
			FileReader fr = new FileReader(new File(DIRRSC+"corpus.txt"));
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null){
				String[] doc = line.split("\t");
				String[] value = new String[2];
				value[0] = doc[1];
				value[1] = doc[2];
				listFiles.put(Integer.parseInt(doc[0]), value);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listFiles;
	}
	
	/**
	 * cherche un mot dans un index de façon séquentielle
	 * @param f fichier d'index
	 * @param word mot à rechercher
	 * @return la ligne de l'index avec les docs, les poids, les tfs, et le df
	 * @throws IOException
	 */
	public static String sequentialSearch(File f, String word) throws IOException{
		FileReader fr;
		fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		boolean found = false;
		while (!found && line != null){
			String word_ = line.split("\t")[0];
			if (word_.equals(word)){
				found = true;
			}
			line = br.readLine();
		}
		br.close();
		if (found)
			return line;
		else return "";
	}
	
	/**
	 * cherche un mot dans un index (fichier binaire) de façon séquentielle
	 * @param f fichier d'index
	 * @param word mot à rechercher
	 * @return la ligne de l'index avec les docs, les poids, les tfs, et le df
	 * @throws IOException
	 */
	public static String sequentialBinarySearch(File f, String word) throws IOException{
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		DataInputStream dis = new DataInputStream(bis);
		
		boolean eof = false;
		boolean found = false;
		String line = "";
		StringBuilder stb = new StringBuilder();
		String word_ = dis.readUTF();
		while (!eof && !found){
			if (word_.equals(word)){
				//on écrit le mot
				stb.append(word_+"\t");
				//on lit le df
				int df = dis.readInt();
				
				for (int i = 0; i < df ; i++){
					int docID = dis.readInt();
					float weight = dis.readFloat();
					stb.append(docID);
					stb.append(":");
					stb.append(weight);
					if (i<df-1)
						stb.append(",");
				}
				line = stb.toString();
				found = true;
			}
			else {
				//fois 8 pour inclure le doc et le poids
				int size = dis.readInt() * 8;
				//on lit la ligne de documents
				dis.skipBytes(size);
			}
				
			if (dis.available() > 0)
				word_ = dis.readUTF();
			else  eof = true;
		}
		bis.close();
		if (found)
			return line;
		else return "";
	}
	
	public static boolean clearDiskSpace(boolean index, boolean corpuslist ){
		//permet de supprimer les anciens index
		if(index){
			File [] list_index = new File(Common.DIRINDEX).listFiles();		
			if (list_index != null){
				for (File dir : list_index) {
					System.out.println(dir);
					for (File file : dir.listFiles()) {
						if(!file.delete()) return false;
					}
					if(!dir.delete()) return false;
				}
			}
			File ind = new File(Common.DIRINDEX);
			ind.delete();
		}
		//permet de supprimer les listes .corpus
		if(corpuslist){
			File fList[] = new File(Common.DIRRSC).listFiles();
			for (int i = 0; i < fList.length; i++) {
			    File fc = fList[i];
			    if (fc.getName().endsWith(Common.extCORPUSLIST)) {
			        if(!fc.delete()) return false;
			    }
			}
		}
		return true;
		
	}


}


