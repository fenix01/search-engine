package search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import common.Common;
import tools.Normalizer;

public class Request {
	
	private ArrayList<String> request;
	
	public Request(String req, Normalizer norm){
		//permet de normaliser la requête
		this.request = norm.normalize(req);
	}
	
	/**
	 * permet de rechercher les documents pour la requête donnée
	 */
	public void search(){
		//contient pour chaque mot sa liste de documents
		LinkedList<ArrayList<Couple>> ltRequest = new LinkedList<>(); 
		for (String word : request){
			ArrayList<Couple> ltDocs;
			ltDocs = extractWordFromIndex(word);
			if (ltDocs != null)
				ltRequest.add(ltDocs);
		}
		
		//il faut à présent calculer la similarité entre la requête et la liste des docs extraits
		ArrayList<Couple> ltFusion = fusion(ltRequest);
		
	}
	
	/**
	 * extrait un mot de l'index est retourne la liste des documents et le poids
	 * @param word
	 * @return HashMap dont l'ordre d'insertion est conserver (docID,poids)
	 */
	private ArrayList<Couple> extractWordFromIndex(String word) {
		ArrayList<Couple> ltDocs = null;
		String firstOcc = Common.firstOcc(word, 2);
		File fIndex = new File(Common.DIRINDEX + firstOcc + Common.extIDX);
		String line = null;
		try {
			line = Common.sequentialSearch(fIndex, word);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!line.equals("")){
			String[] lineIndex = line.split("\t");
			String[] docs = lineIndex[2].split(","); 
			
			ltDocs = new ArrayList<Couple>(docs.length);
			for (String doc : docs){
				String[] docEl = doc.split(":");
				Couple cp = new Couple(Integer.parseInt(docEl[0]));
				cp.addWord(word , Double.parseDouble(docEl[2]));
				ltDocs.add(cp);
				
			}
		}
		return ltDocs;
	}

	/**
	 * permet d'appliquer l'algo de fusion sur X listes de documents.
	 * Chaque liste de documents est associé à un mot clé de la requête
	 * @param ltRequest prend une liste de liste de documents
	 * @return une liste de documents contenant l'intersection de toutes les listes
	 */
	private ArrayList<Couple> fusion(LinkedList<ArrayList<Couple>> ltRequest){
		//liste finale de documents
		ArrayList<Couple> ltDocs = new ArrayList<>();
		// tant qu'il reste au moins 2 listes de documents à parcourir
		while (ltRequest.size() > 1) {
			int l1 = 0,l2 = 0;
			//on dépile deux listes de documents
			ArrayList<Couple> ltDoc1 = ltRequest.pollFirst();
			ArrayList<Couple> ltDoc2 = ltRequest.pollFirst();
			//on applique l'algo de fusion
			while (l1 != ltDoc1.size() && l2 != ltDoc2.size()){
				//on dépile deux documents
				Couple c1 = ltDoc1.get(l1);
				Couple c2 = ltDoc2.get(l2);
				//on compare si leurs ids est pareil
				if (c1.getDocID() == c2.getDocID()){
					ltDocs.add(c1);
					c1.fusion(c2);
					l1++;
					l2++;
				}
				else if (c1.getDocID() < c2.getDocID())
					l1++;
				else l2++;
			}
		}
		return ltDocs;
		
	}

}
