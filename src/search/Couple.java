package search;

/**
 * classe java permettant de stocker un couple d'entier,double
 * @author Yohann
 *
 */
public class Couple {
	
	private int docID;
	private double weight;
	
	public int getDocID() {
		return docID;
	}

	public double getWeight() {
		return weight;
	}

	Couple(int docID, double weight){
		this.docID = docID;
		this.weight = weight;
	}

}
