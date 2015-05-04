package edu.upenn.cis455.ui;

import java.io.Serializable;
import java.util.Comparator;

public class ResultsComparator implements Comparator<Result>, Serializable {
	@Override
	public int compare(Result o1, Result o2) {
		// TODO Auto-generated method stub
		if(o1.getScore()>o2.getScore())
			return -1;
		else if(o1.getScore()<o2.getScore())
			return 1;
		else
			return 0;
	}
}