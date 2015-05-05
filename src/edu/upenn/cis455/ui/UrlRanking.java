package edu.upenn.cis455.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import edu.upenn.cis455.storage.Term;

public class UrlRanking {
	
	private String url;

	public UrlRanking(String u) {
		url = u;
		numTerms = 0;
		// defaults
		cosSimWeight = 0.25;
		pageRankWeight = 0.25;
		proximityWeight = 0.25;
		numTermsWeight = 0.25;
	}
	
	/** Maps a Term to a tfIdf score for this url*/
	private HashMap<Term, Double> tfIdf;
	/** Stores the pageRank score for this url*/
	private double pageRank;
	/** Maps a Term to a list of locations of the term for this url*/
	private HashMap<Term, LinkedList<Integer>> locations;
	/** Stores the proximity for this url*/
	private int proximity;
	/** Stores the number of terms in the query that appear on the page*/
	private int numTerms;
	private double cosSim;
	
	public void addTfIdfScore(Term t, Double score) {
		numTerms++;
		tfIdf.put(t, score);
	}
	
	public void setPageRank(Double score) {
		pageRank = score;
	}
	
	public void setLocations(HashMap<Term, LinkedList<Integer>> locs) {
		locations = locs;
		calculateProximity();
	}
	
	public void calculateCosSim(ArrayList<Term> query) {
		double dotProduct = 0.0;
		for (Term t : query) {
			if (tfIdf.containsKey(t)) {
				dotProduct += (1.0 * tfIdf.get(t));
			}
		}
		cosSim = dotProduct;
	}
	
	public double getPageRank() {
		return pageRank;
	}
	
	private void calculateProximity() {
		// TODO unimplemented
		proximity = 0;
	}
	
	public int getProximity() {
		return proximity;
	}
	
	public double calculateHypeScore() {
		return (cosSimWeight * cosSim) + (pageRankWeight * pageRank)
				+ (proximityWeight * proximity) + (numTermsWeight * numTerms);
	}
	
	private double cosSimWeight;
	private double pageRankWeight;
	private double proximityWeight;
	private double numTermsWeight;
	
	public void setWeights(double cossim, double pr, double prox, double numWords) {
		cosSimWeight = cossim;
		pageRankWeight = pr;
		proximityWeight = prox;
		numTermsWeight = numWords;
	}
	
}
