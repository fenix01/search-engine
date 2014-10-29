package indexation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import common.Common;

import tools.Normalizer;

public class Weighting {

	// écrit un fichier poids
	public void writeWgtFile(File fileName,
			HashMap<String, Double> wgtFile) throws IOException {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(fileName);
			bw = new BufferedWriter(fw);
			for (Map.Entry<String, Double> word : wgtFile.entrySet()) {
				StringBuilder stb = new StringBuilder(word.getKey());
				stb.append("\t");
				stb.append(word.getValue());
				bw.write(stb.toString());
				bw.newLine();
			}
		} finally {
			bw.close();
			fw.close();
		}
	}

	// charge un fichier poids
	public HashMap<String, Double> readWgtFile(File fileName)
			throws IOException {
		FileReader rd = null;
		BufferedReader bf = null;
		HashMap<String, Double> weight = null;
		try {
			weight = new HashMap<>();
			rd = new FileReader(fileName);
			bf = new BufferedReader(rd);
			String line;
			while ((line = bf.readLine()) != null) {
				String[] list = line.split("\t");
				weight.put(list[0], Double.valueOf(list[1]));
			}
		} finally {
			bf.close();
			rd.close();
		}
		return weight;

	}

	// Calcule du tf idf à partir d'une chaîne de caractère
	public HashMap<String, Double> getTfIdf(String str,
			HashMap<String, Directory> idx, Normalizer normalizer)
			throws IOException {
		HashMap<String, Double> tf_idf = new HashMap<>();
		for (String word : normalizer.normalize(str)) {
			String wordLC = word.toLowerCase();
			if (!Common.isEmptyWord(wordLC)) {
				double wordWeight;
				if (!idx.containsKey(wordLC)) {
					wordWeight = 0.1;
				} else {
					int tf = 5;
					int df = idx.get(wordLC).df();
					wordWeight = tf
							* Math.log((double) IndexGenerator.dirCorpus.size()
									/ (df*0.5));
				}
				tf_idf.put(wordLC, wordWeight);
			}

		}
		return tf_idf;

	}

	//méthode qui renvoit le tfidf d'un mot du fichier en question en fonction du coefficient
	// le coefficient varie uniquement pour la version html
	//le coef est par exemple plus élevé pour le titre que pour un mot d'un paragraphe
	private double computeTfIdf(String word, double coef, File fileName,
			HashMap<String, Directory> idx) {
		double wordWeight;
		if (!idx.containsKey(word)) {
			wordWeight = 0.1;
		} else {
			int fileID = fileName.getName().hashCode();
			int tf = idx.get(word).getRefs().get(fileID);
			int df = idx.get(word).df();
			wordWeight = tf * coef * Math.log((double) IndexGenerator.dirCorpus.size() / (df*0.1*coef));
		}
		return wordWeight;
	}

	// Calcule du tf idf à partir d'un fichier
	private HashMap<String, Double> getTfIdf(File fileName,
			HashMap<String, Directory> idx, Normalizer normalizer)
			throws IOException {
		HashMap<String, Double> tf_idf = new HashMap<>();
		ArrayList<String> ltWords = null;
		if (Common.Choice){
			//tfIdf pour la version texte
			ltWords = normalizer.normalize(fileName);
			for (String word : ltWords) {
				String wordLC = word.toLowerCase();
				if (!Common.isEmptyWord(wordLC)){
					double tfidfW = computeTfIdf(wordLC,1.0, fileName, idx);
					tf_idf.put(wordLC, tfidfW);
				}
			}
		}
		else {
			//TfIdf pour la version html
			HtmlParser parser = new HtmlParser();
			parser.parseText(fileName);
			for (Entry<String, String> textTag : parser.getTextTags()
					.entrySet()) {
				ltWords = normalizer.normalize(textTag.getValue());
				for (String word : ltWords) {
					String wordLC = word.toLowerCase();
					if (!Common.isEmptyWord(wordLC)){
						double coef = parser.getCoef(textTag.getKey());
						double tfidfW = computeTfIdf(wordLC,coef, fileName, idx);
						tf_idf.put(wordLC, tfidfW);
					}
				}
				ltWords.clear();
			}
			
		}
		return tf_idf;
	}

	// Fonction qui construit les fichiers de poids
	public void buildWeightFiles(String outDirName,
			HashMap<String, Directory> idx, Normalizer normalizer) {
		for (Entry<Integer, String[]> file : IndexGenerator.dirCorpus
				.entrySet()) {
			HashMap<String, Double> tf_idf = null;
			try {
				Integer fileID = file.getKey();
				String fileName = IndexGenerator.getFileName(fileID);
				String filePath = file.getValue()[0];
				tf_idf = getTfIdf(new File(filePath), idx, normalizer);
				writeWgtFile(new File(outDirName + fileName + ".poids"),
						(HashMap<String, Double>) tf_idf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
