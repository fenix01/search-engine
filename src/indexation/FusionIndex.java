package indexation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FusionIndex {
	public static void mergeInvertedFiles(File invertedFile1, File invertedFile2,
			File mergedInvertedFile) throws IOException
			{
				FileReader fr1 = new FileReader(invertedFile1);
				BufferedReader br1 = new BufferedReader(fr1);
				FileReader fr2 = new FileReader(invertedFile2);
				BufferedReader br2 = new BufferedReader(fr2);
				FileWriter frMerge = new FileWriter(mergedInvertedFile);
				BufferedWriter brMerge = new BufferedWriter(frMerge);				
				
				String line1 = br1.readLine();
				String line2 = br2.readLine();
				int count =0;
				while (line1 != null && line2 != null) {
					count++;
					String[] splitted_line1 = line1.split("\t");
					String[] splitted_line2 = line2.split("\t");
					//on a dans la premi�re case de chaque tableau le mot
					if (splitted_line1[0].equals(splitted_line2[0])){
						//on fusionne : on ajoute les dfs, et on fusionne les listes
						int df = 0;
						df = Integer.parseInt(splitted_line1[1]) + Integer.parseInt(splitted_line2[1]);
						
						String docs = splitted_line1[2] + "," + splitted_line2[2];
						
						brMerge.write(splitted_line1[0]+"\t"+df+"\t"+docs);
						brMerge.newLine();
						line1 = br1.readLine();
						line2 = br2.readLine();
						
					}
					//si le mot1 < mot2
					else if (splitted_line1[0].compareTo(splitted_line2[0]) < 0){
						//on �crit la ligne 1 dans la sortie
						brMerge.write(line1);
						brMerge.newLine();
						line1 = br1.readLine();
					}
					else {
						//on �crit la ligne 2 dans la sortie
						brMerge.write(line2);
						brMerge.newLine();
						line2 = br2.readLine();
					}
				}
				
				//si on a pas fini de lire le fichier 1 on copie la fin dans la sortie
				while (line1 != null){
					brMerge.write(line1);
					brMerge.newLine();
					line1 = br1.readLine();
				}
				
				//si on a pas fini de lire le fichier 2 on copie la fin dans la sortie
				while (line2 != null){
					brMerge.write(line2);
					brMerge.newLine();
					line2 = br2.readLine();
				}
				
				br1.close();
				br2.close();
				brMerge.close();
			}
	
	public static void main(String[] argv) throws IOException{
		File f1 = new File("./rsc/index/index1.ind");
		File f2 = new File("./rsc/index/index2.ind");
		File fMerge = new File("./rsc/index/indexMerge.ind");
		mergeInvertedFiles(f1, f2, fMerge);
	}

}
