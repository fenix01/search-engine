package main;

import indexation.TaskIndexing;
import indexation.Weighting;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

import tools.FrenchStemmer;
import tools.Normalizer;
import common.Common;

public class Main {

	public static void main(String[] args) throws IOException {
		// liste des fichiers du corpus
		HashMap<Integer, String[]> h = null;
		File fRevCorpus = new File(Common.DIRRSC + "corpus.txt");
		File fCorpus = new File(Common.DIRCORPUS);

		//permet de charger les mots vides
		Common.loadEmptyWords();
		
		boolean stop = false;
		int command;
		Scanner sc = new Scanner(System.in);
		while (!stop) {
			System.out.println("--Bienvenue dans notre moteur de recherche--");
			System.out.println("Choisissez une commande à effectuer");
			System.out.println("lister les fichiers du corpus : tapez 1");
			System.out.println("lire la liste des fichiers du corpus : tapez 2");
			System.out.println("Indexer le corpus : tapez 3");
			System.out.println("Parcours les indexes pour inclure le poids,"+
			"et générer le fichier somme des poids : tapez 4");
			System.out.println("Supprimer les indexes, le fichier poids, et le fichier corpus : tapez 8");
			System.out.println("quitter : tapez 9");
			command = sc.nextInt();
			switch (command) {
			//lister les fichiers du corpus
			case 1:
				System.out.println("Nombre de fichiers à indexer");
				int nbrDocs = sc.nextInt();
				
				h = new HashMap<>();
				Common.getDirectory(fCorpus, h, ".txt", nbrDocs);
				Common.writeDirectory(h);
				break;
			//lire la liste des fichiers du corpus
			case 2:
				if (!fRevCorpus.exists())
					System.out.println("Attention le fichier n'existe pas, appelez la commande 1!");
				else {
					h = Common.readDirectory();
				}
				break;
			//Indexer le corpus
			case 3:
				//on créé 2 normalizer pour les deux threads
				Normalizer stemmer = new FrenchStemmer(common.Common.DIRRSC
						+ "stop.txt");
				Normalizer stemmer2 = new FrenchStemmer(common.Common.DIRRSC
						+ "stop.txt");
				// Normalizer stemmer3 = new
				// FrenchStemmer(common.Common.DIRRSC+"stop.txt");
				// Normalizer stemmer4 = new
				// FrenchStemmer(common.Common.DIRRSC+"stop.txt");
				
				//permet de démarrer 2 threads qui vont effectuer de la fusion
				TaskIndexing ti1 = new TaskIndexing(h, 0, h.size() / 2, stemmer, true,
						"index0", 5000);
				TaskIndexing ti2 = new TaskIndexing(h, h.size() / 2, h.size(),
						stemmer2, true, "index1", 5000);
				// TaskIndexing ti3 = new
				// TaskIndexing(h,documents/2,(3*documents)/4,stemmer3,true,
				// "index2",2500);
				// TaskIndexing ti4 = new
				// TaskIndexing(h,(3*documents)/4,h.size(),stemmer4,true,
				// "index3",2500);
				
				ti1.start();
				ti2.start();
				// ti3.start();
				// ti4.start();
				ti1.join();
				ti2.join();
				
				//fusionne les indexes créé par les 2 threads
				TaskIndexing.fusionThreadsIndexes(2);
				//découper l'index en plusieurs index
				TaskIndexing.splitIndex(2);
				break;
			case 4:
				File fIndexes = new File(Common.DIRINDEX);
				HashMap<Integer, String[]> indexes = new HashMap<>();
				TreeMap<Integer, Double> sum_weights = new TreeMap<>();
				Common.getDirectory(fIndexes, indexes, ".idx", -1);
				Weighting weight_1 = new Weighting(sum_weights, indexes, 0,
						indexes.size() / 2, h.size());
				Weighting weight_2 = new Weighting(sum_weights, indexes,
						indexes.size() / 2, indexes.size(), h.size());
				
				weight_1.start();
				weight_2.start();
				
				weight_1.join();
				weight_2.join();
				Weighting.saveWeights(Common.DIRINDEX + "docWeight"+Common.extWEIGHT, sum_weights);
				break;
			case 8:
				//permet de supprimer les anciens indexes, et de créer le répertoire
				File index_dir = new File(Common.DIRINDEX);
				index_dir.mkdir();
				for (File file : index_dir.listFiles()) {
					file.delete();
				}
				fRevCorpus.delete();
				break;
			case 9:
				stop = true;
				break;
			}
		}

	



		

	}

}
