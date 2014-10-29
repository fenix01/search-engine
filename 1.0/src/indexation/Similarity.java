package indexation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import tools.Normalizer;

import common.Common;

public class Similarity {

	//liste des fichiers qui se rapportent à la requête
	private HashMap<String, ArrayList<Integer>> listFiles;
	
	//liste des fichiers qui ont un degré de similarité élevé pour la requête
	private HashMap<Integer, Double> rateFiles;
	
	//pourcentage en deça duquel les fichiers sont éjectés des résultats
	public double percentGarbage;

	public int getCountResult() {
		return rateFiles.size();
	}

	public Similarity() {
		listFiles = new HashMap<>();
		rateFiles = new HashMap<>();
		percentGarbage = 0.10;
	}

	private void clear() {
		if (listFiles != null)
			if (!listFiles.isEmpty())
				listFiles.clear();
	}

	// Fonction qui renvoit à partir d'un mot de l'index les fichiers poids qui
	// s'y
	// rapportent
	private ArrayList<Integer> getWordFiles(String word,
			HashMap<String, Directory> idx) {
		ArrayList<Integer> result = new ArrayList<>();
		if (idx.containsKey(word)) {
			for (Map.Entry<Integer, Integer> file : idx.get(word).getRefs()
					.entrySet()) {
				result.add(file.getKey());
			}
		}
		return result;
	}

	// De chaque mot de la requête une liste des fichiers qui le contient est
	// produite.
	// Pour minimiser le nombre de fois passer dans chaque fichier poids on
	// calcul l'intersection
	// entre chaque liste pour voir si les mots de la requête n'apparaissent pas
	// dans les mêmes fichiers poids
	// renvoit l'intersection
	private void minimizeWgtFileAnalyze(
			HashMap<String, ArrayList<Integer>> list, String ref) {
		ArrayList<Integer> intersect = new ArrayList<>(list.get(ref));
		for (Map.Entry<String, ArrayList<Integer>> childList : list.entrySet()) {
			if (!ref.equals(childList.getKey())) {
				intersect.retainAll(childList.getValue());
				childList.getValue().removeAll(intersect);
			}
		}
		for (Map.Entry<String, ArrayList<Integer>> childList : list.entrySet()) {
			childList.getValue().removeAll(intersect);
		}
		// indique la liste des fichiers
		// contenant tous les mots de la requête
		list.put("@ll", intersect);
		// La première liste contient l'intersection de toutes les listes et la
		// dernière et une sauvegarde de la première.
	}

	private ArrayList<String> removeEmptyWords(ArrayList<String> request) {
		ArrayList<String> new_req = new ArrayList<>();
		for (String word : request) {
			if (!word.startsWith("+"))
				if ((!Common.isEmptyWord(word)) && !new_req.contains(word))
					new_req.add(word);
		}
		return new_req;
	}

	// Normalise la requête et supprime les mots vides
	private ArrayList<String> normalizeRequest(String request,
			Normalizer normalize) throws IOException {
		ArrayList<String> norm = new ArrayList<>();
		String requestLC = request.toLowerCase();
		ArrayList<String> split_req = new ArrayList<>(Arrays.asList(requestLC
				.split(" ")));
		ArrayList<String> split_req2 = removeEmptyWords(split_req);
		for (String word : split_req2) {
			ArrayList<String> normWord = normalize.normalize(word);
			norm.addAll(normWord);
		}
		return norm;

	}

	//calcul la similarité entre la requête et un un fichier poids
	private double buildSimilarity(String request, File fileName,
			HashMap<String, Directory> idx, Normalizer normalizer)
			throws IOException, ClassNotFoundException {
		Weighting weight = new Weighting();
		HashMap<String, Double> weight1 = weight.getTfIdf(request, idx,
				normalizer);
		HashMap<String, Double> weight2 = weight.readWgtFile(new File(fileName
				+ ".poids"));
		double sum = 0;

		double f1 = 0, f2 = 0, result = 0;
		for (String key : weight1.keySet()) {
			if (weight2.containsKey(key))
				sum += weight1.get(key) * weight2.get(key);
			f1 += weight1.get(key) * weight1.get(key);
		}

		for (String key : weight2.keySet()) {
			f2 += weight2.get(key) * weight2.get(key);
		}
		result = sum / (Math.sqrt(f1) * Math.sqrt(f2));

		return result;
	}

	//Calcul la similarité avec tous les fichiers poids correspondants à la requête
	public HashMap<Integer, Double> computeRequest(String dirName,
			String request, HashMap<String, Directory> idx, Normalizer normalize)
			throws IOException, ClassNotFoundException {
		clear();
		ArrayList<String> norm_areq = normalizeRequest(request, normalize);
		String norm_req = Common.aListToString(norm_areq);

		for (String word : norm_areq) {
			if (!listFiles.containsKey(word)) {
				ArrayList<Integer> files = getWordFiles(word, idx);
				listFiles.put(word, files);
			}
		}
		// On calcule l'intersection uniquement s'il y a plus d'un mot
		if (norm_areq.size() > 1) {
			// contient la liste des documents contenant tous les mots
			minimizeWgtFileAnalyze(listFiles, norm_areq.get(0));
		}
		for (Map.Entry<String, ArrayList<Integer>> childList : listFiles
				.entrySet()) {
			// Parcours les fichiers contenant un des mots de la requête
			for (Integer fileID : childList.getValue()) {
				String fileName = IndexGenerator.getFileName(fileID);
				double similarity = buildSimilarity(norm_req, new File(dirName
						+ fileName), idx, normalize);
				if (Double.compare(similarity, percentGarbage) > 0)
					rateFiles.put(fileID, similarity);
			}
		}
		return rateFiles;
	}
}
