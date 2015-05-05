package edu.upenn.cis455.ui;

import java.util.ArrayList;
import java.util.HashMap;

import edu.upenn.cis455.storage.Term;

public class UrlRanking {
	
	private String url;

	public UrlRanking(String u) {
		url = u;
	}
	
	/** Maps a Term to a tfIdf score for this url*/
	private HashMap<Term, Double> tfIdf;
	/** Stores the pageRank score for this url*/
	private double pageRank;
	/** Maps a Term to a list of locations of the term for this url*/
	private HashMap<Term, ArrayList<Integer>> locations;
	
	public void addTfIdfScore(Term t, Double score) {
		tfIdf.put(t, score);
	}
	
	public void setPageRank(Double score) {
		pageRank = score;
	}
	
	public void setLocations(HashMap<Term, ArrayList<Integer>> locs) {
		locations = locs;
	}
	
	public double calculateCosSim(ArrayList<Term> query) {
		// TODO unimplemented
		return 0.0;
	}
	
	public double getPageRank() {
		return pageRank;
	}
	
	public double calculateProximity() {
		// TODO unimplemented
		return 0.0;
	}
	
	public double calculateHypeScore() {
		// TODO unimplemented
		return 0.0;
	}
	
}
