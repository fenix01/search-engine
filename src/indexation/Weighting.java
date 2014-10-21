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
	public static HashMap<String, Double> readWgtFile(File fileName)
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
				weight.put(list[0], Double.parseDouble(list[1]));
			}
		} finally {
			bf.close();
			rd.close();
		}
		return weight;

	}

}
