package edu.upenn.cis455.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import edu.upenn.cis455.storage.Term;

public class UrlRanking {
	
	private String url;
	private int queryLength;

	public UrlRanking(String u) {
		url = u;
		numTerms = 0;
		titleBonus = 0;
		// defaults
		cosSimWeight = 0.25;
		pageRankWeight = 0.25;
		proximityWeight = 0.25;
		numTermsWeight = 0.25;
		terms = new HashSet<Term>();
		tfIdf = new HashMap<Term, Double>();
	}
	
	/** Maps a Term to a tfIdf score for this url*/
	private HashMap<Term, Double> tfIdf;
	/** Stores the pageRank score for this url*/
	private double pageRank;
	/** Maps a Term to a list of locations of the term for this url*/
	private HashSet<Term> terms;
	/** Stores the proximity for this url*/
	private double proximity;
	/** Stores the number of terms in the query that appear on the page*/
	private int numTerms;
	/** Stores the cosine similarity for this url with the query*/
	private double cosSim;
	
	
	public String getUrl() {
		return url;
	}
	
	public void addTfIdfScore(Term t, Double score) {
		numTerms++;
		terms.add(t);
		tfIdf.put(t, score);
	}
	
	public void setPageRank(Double score) {
		pageRank = score;
	}
	
	public void calculateCosSim(ArrayList<Term> query) {
		queryLength = query.size();
		double unitVect = 1.0 / queryLength;
		double dotProduct = 0.0;
		for (Term t : query) {
			if (tfIdf.containsKey(t)) {
				dotProduct += (unitVect * tfIdf.get(t));
			}
		}
		cosSim = dotProduct;
	}
	
	public double getPageRank() {
		return pageRank;
	}

	public void calculateProximity() {
		if (numTerms < 2) {
			System.out.println("numTerms is 1 on url: "+url);
			proximity = 10000;
			return;
		}
		ArrayList<Double> averages = new ArrayList<Double>();
		for (Term t : terms) {
			int sum = 0;
			LinkedList<Integer> locs = t.getLocations(url);
			for (Integer i : locs) {
				sum += i;
			}
			averages.add((double) sum / (double) locs.size());
		}
		for (int i = 0; i < averages.size(); i++) {
			for (int j = 0; j < averages.size(); j++) {
				if (i != j) {
					proximity += (Math.abs(averages.get(i) - averages.get(j)));
				}
			}
		}
		if(proximity == 0 || proximity < numTerms)
			proximity = numTerms;
	}
	
	public double getProximity() {
		return proximity;
	}
	
	public double calculateHypeScore() {
		// TODO Tune this.
		System.out.println("url is"+url+" cossim is: "+cosSim+" pageRank is: "+pageRank+"numTerms is: "+numTerms+" proximity is :"+proximity+" queryLength is: "+queryLength);
		return (cosSimWeight * cosSim) + (pageRankWeight * pageRank)
				+ (proximityWeight * (numTerms / proximity))
				+ (numTermsWeight * (numTerms / (double) queryLength))
				+ ((double) titleBonus / (double) numTerms);
	}

	private double cosSimWeight;
	private double pageRankWeight;
	private double proximityWeight;
	private double numTermsWeight;
	
	private double titleBonus;
	
	public void setWeights(double cossim, double pr, double prox, double numWords) {
		cosSimWeight = cossim;
		pageRankWeight = pr;
		proximityWeight = prox;
		numTermsWeight = numWords;
	}

	public void incrementTitleBonus(int matches) {
		titleBonus += matches;
	}
	
}
