package indexation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe permettant une fusion d'index simple et efficace *
 */

public class FusionIndex {
	
	/**
	 * Fusionne 2 fichiers d'index en conservant l'ordre établi ligne par ligne
	 * @param invertedFile1
	 * @param invertedFile2
	 * @param mergedInvertedFile le fichier résultat
	 * @throws IOException
	 */
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
				while (line1 != null && line2 != null) {
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
						//on écrit la ligne 1 dans la sortie
						brMerge.write(line1);
						brMerge.newLine();
						line1 = br1.readLine();
					}
					else {
						//on écrit la ligne 2 dans la sortie
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
	
	/**
	 * Récupère la liste des documents dans lequel un mot est présent
	 * @param df
	 * @param dis
	 * @return array of bytes
	 * @throws IOException
	 */
	public static byte[] getBinaryDocs(int df, DataInputStream dis) throws IOException{
		//fois 6 pour inclure le doc et le tf
		int size = df * 6;
		//on lit la ligne de documents
		byte[] array = new byte[size];
		dis.read(array, 0, size);
		return array;
	}
	
	private static void copyLine(String word, DataInputStream dis, DataOutputStream dos){
		try {
			//on écrit le mot
			dos.writeUTF(word);
			//on récupère le df pour accélérer la copie
			int df = dis.readInt();
			//on écrit le df et toute la ligne de documents
			dos.writeInt(df);
			copyDocs(df, dis, dos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void copyDocs(int df, DataInputStream dis, DataOutputStream dos){
		try {
			//fois 6 pour inclure le int(doc) et le short(tf)
			int size = df * 6;
			//on lit la ligne de documents
			byte[] array = new byte[size];
			dis.read(array, 0, size);
			//on écrit la ligne de documents
			dos.write(array, 0, size);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Fusionne 2 fichiers (binaires) d'index en conservant l'ordre établi ligne par ligne
	 * @param invertedFile1 fichier d'index 1
	 * @param invertedFile2 fichier d'index 2
	 * @param mergedInvertedFile fichier résultant de la fusion de index1 et index2
	 * @throws IOException
	 */
	public static void mergeInvertedBinaryFiles(File invertedFile1, File invertedFile2,
			File mergedInvertedFile) throws IOException
			{
				FileInputStream fis1 = new FileInputStream(invertedFile1);
				BufferedInputStream bis1 = new BufferedInputStream(fis1);
				DataInputStream dis1 = new DataInputStream(bis1);
				FileInputStream fis2 = new FileInputStream(invertedFile2);
				BufferedInputStream bis2 = new BufferedInputStream(fis2);
				DataInputStream dis2 = new DataInputStream(bis2);
				
				FileOutputStream fosMerge = new FileOutputStream(mergedInvertedFile);
				BufferedOutputStream bosMerge = new BufferedOutputStream(fosMerge);
				DataOutputStream dosMerge = new DataOutputStream(bosMerge);				
				
				String word1 = dis1.readUTF();
				String word2 = dis2.readUTF();
				
				boolean EOF1 = false;
				boolean EOF2 = false;
				while (!EOF1 && !EOF2){
					//si le mot est égal dans chacune des lignes
					if (word1.equals(word2)){
						
						//on fusionne : on ajoute le mot, le df, on fusionne les listes
						int df1 = 0;
						int df2 = 0;
						df1 = dis1.readInt();
						df2 = dis2.readInt();
						
						//on écrit le mot
						dosMerge.writeUTF(word1);
						//on écrit le nouveau df
						dosMerge.writeInt(df1+df2);
						
						//on écrit les documents de l'index 1
						copyDocs(df1, dis1, dosMerge);
						
						//on écrit les documents de l'index 2
						copyDocs(df2, dis2, dosMerge);
						
						//on lit les 2 nouveaux mots
						if (dis1.available() > 0)
							word1 = dis1.readUTF();
						else EOF1 = true;
						if (dis2.available() > 0)
							word2 = dis2.readUTF();
						else EOF2 = true;
						
					}
					//si le mot1 < mot2
					else if (word1.compareTo(word2) < 0){
						//on écrit la ligne 1 dans la sortie
						copyLine(word1, dis1, dosMerge);
						if (dis1.available() > 0)
							word1 = dis1.readUTF();
						else EOF1 = true;
					}
					else {
						//on écrit la ligne 2 dans la sortie
						copyLine(word2, dis2, dosMerge);
						if (dis2.available() > 0)
							word2 = dis2.readUTF();
						else EOF2 = true;
					}
				}
				
				//si on a pas fini de lire le fichier 1 on copie la fin dans la sortie
				while (!EOF1){
					//on écrit la ligne 1 dans la sortie
					copyLine(word1, dis1, dosMerge);
					if (dis1.available() > 0)
						word1 = dis1.readUTF();
					else EOF1 = true;
				}
				
				//si on a pas fini de lire le fichier 2 on copie la fin dans la sortie
				while (!EOF2){
					//on écrit la ligne 2 dans la sortie
					copyLine(word2, dis2, dosMerge);
					if (dis2.available() > 0)
						word2 = dis2.readUTF();
					else EOF2 = true;
				}
				
				dis1.close();
				dis2.close();
				dosMerge.close();
			}
	
	public static void main(String[] argv) throws IOException{
		File f1 = new File("./rsc/index/index1.ind");
		File f2 = new File("./rsc/index/index2.ind");
		File fMerge = new File("./rsc/index/indexMerge.ind");
		mergeInvertedFiles(f1, f2, fMerge);
	}

}
