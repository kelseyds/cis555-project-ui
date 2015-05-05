package edu.upenn.cis455.ui;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import edu.upenn.cis455.indexer.DocInfo;
import edu.upenn.cis455.storage.DBWrapperIndexer;
import edu.upenn.cis455.storage.Term;

public class IndexServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		//response.sendRedirect("index.jsp");
		System.out.println(request.getServletPath());
		if(request.getServletPath().equals("/search"))
	    {
			response.setContentType("text/html");
			System.out.println("in index servlet get for search");
			ServletContext sc = this.getServletContext();
			RequestDispatcher rd = sc.getRequestDispatcher("/index.jsp");
			rd.include(request, response);
	    }
		else if(request.getServletPath().equals("/results"))
		{
			response.setContentType("text/html");
			System.out.println("in index servlet get for results");
			String query = (String) request.getParameter("query");
			
			ArrayList<Result> results = (ArrayList<Result>) getResults(query);
			/*
			ArrayList<Result> results = new ArrayList<Result>();
			Result newResult = new Result();
			newResult.setTitle("Test 1");
			newResult.setUrl("http://www.google.com");
			newResult.setDescription("This links to google. It's a cool website that helps you find things.");
			results.add(newResult);
			Result secondResult = new Result();
			secondResult.setTitle("Test 2");
			secondResult.setUrl("http://www.jameslovey.com");
			secondResult.setDescription("This is the personal website of James Lovey. He talks about his Community Service work and his MEAM projects.");
			results.add(secondResult);
			*/
			if(results!=null)
				request.setAttribute("resultsList",results);
			ServletContext sc = this.getServletContext();
			RequestDispatcher rd = sc.getRequestDispatcher("/results.jsp");
			rd.include(request, response);
		}
		/*else if(request.getServletPath().equals("/styles"))
		{
			System.out.println("in styles request");
			response.setContentType("text/css");
			ServletContext sc = this.getServletContext();
			RequestDispatcher rd = sc.getRequestDispatcher("/styles/styles.css");
			rd.forward(request, response);
		}*/
	}
	
	private int corpusSize;
	
	private ArrayList<Result> getResults(String originalQuery)
	{
		DBWrapperIndexer.init("/home/cis455/workspace/cis555-project/database");
		String [] originalQueryTokens = originalQuery.split(" ");
		HashMap<String, String> basicWordToSearchQueryWord = new HashMap<String, String>();
		String lowerCaseBasicSearchString ="";
		for(int i = 0; i<originalQueryTokens.length; i++)
		{
			String basicQuery = originalQueryTokens[i].replaceAll("[^A-Za-z0-9]", "").toLowerCase();
			basicWordToSearchQueryWord.put(basicQuery, originalQueryTokens[i]);
			lowerCaseBasicSearchString+=basicQuery+" ";
		}
		StringTokenizer st = new StringTokenizer(lowerCaseBasicSearchString, " ");
		ArrayList<Term> allTerms = new ArrayList<Term>();
		urlsToCosSim = new HashMap<String,Double>();
		HashMap<String, ArrayList<Term>> urlToTerms = new HashMap<String, ArrayList<Term>>();
		while (st.hasMoreElements()) {
			String nextString = (String) st.nextElement();
			System.out.println("next element: "+nextString);
			Term curr = DBWrapperIndexer.getTerm(nextString);
			System.out.println("current Term: "+curr);
			if(curr == null)
			{
				System.out.println("term searched not found in database");
				continue;
			}
			//store all Term objects associated with the search
			allTerms.add(curr);
			HashMap<String, Double> urlToTFs = curr.getUrlToTFHashMap();
			for(String url: urlToTFs.keySet())
			{
				calculateCosSim(curr, url, urlToTFs);
				if(urlToTerms.containsKey(url))
				{
					//if the url is in the hashmap of Url to Terms in the search query in that document
					//then get the value for the hashmap, add this term to the arraylist and put it back in
					// the hashmap
					ArrayList<Term> terms = urlToTerms.get(url);
					terms.add(curr);
					urlToTerms.put(url, terms);
				}
				else
				{
					//looking at all urls and their TFs associated with one term
					//if the url is not in the mapping from url to the Terms in the search query
					//then add it to the hashmap
					ArrayList<Term> terms = new ArrayList<Term>();
					terms.add(curr);
					urlToTerms.put(url, terms);
				}
			}
		}
		ArrayList<Result> results = new ArrayList<Result>();
		//looking at all urls that contain a term, if a url contains more than one term then 
		//boost its ranking by a factor of 5000, so 2 search terms adds 5000, 3 adds 10000, 4 adds 15000, etc.
		corpusSize = urlToTerms.keySet().size();
		for (String url : urlToTerms.keySet()) {
			double score = 0;
			ArrayList<Term> terms = urlToTerms.get(url);
			Double cosSim = calculateCosSim(terms, url);
			Double pageRank = fetchPageRank(url);
			Double proximityScore = calculateProximity(terms, url);
			if (terms.size() > 1) {
				score += 5000 * terms.size() - 1;
			}
			score += (0 * cosSim) + (0 * pageRank) + (0 * proximityScore);

			//score += tf;
			DocInfo docInfo = DBWrapperIndexer
					.getDocInfo(url);
			Term firstTerm = terms.get(0);

			String docText = docInfo.getDocText();
			System.out.println("docText = "+docText);
			System.out.println("term = "+firstTerm.getTerm());
			String actualWord = basicWordToSearchQueryWord.get(firstTerm.getTerm());
			Integer firstLocation = docText.toLowerCase().indexOf(actualWord.toLowerCase());
			System.out.println("first location = "+firstLocation);
			System.out.println("first term actual word: "+ actualWord);
			Integer beginIndexSubstring = firstLocation - 150;
			if(beginIndexSubstring<0)
				beginIndexSubstring = 0;
			Integer endIndexSubstring = firstLocation + 400;
			if(endIndexSubstring> docText.length())
				endIndexSubstring = docText.length();
			String substring = docText.substring(beginIndexSubstring, endIndexSubstring);
			System.out.println("substring = "+ substring);
			Integer startIndex = substring.indexOf('.')+1;
			if(startIndex == -1)
				startIndex = 0;
			else if(startIndex>150)
				startIndex=150;
			Integer endIndex = startIndex+400;
			if(endIndex>substring.length())
				endIndex = substring.length();
			String description = substring.substring(startIndex, endIndex);
			System.out.println("description: "+description);
			description = "... "+description+" ...";
			for(Term term: allTerms)
			{
				String word = term.getTerm();
				description = description.replaceAll(" (\\p{Punct})*(?i)("+word+")(\\p{Punct})* ", " $1<strong>$2</strong>$3 ");
			}
			Result result = new Result();
			result.setUrl(url);
			result.setTitle(docInfo.getTitle());
			result.setDescription(description);
			result.setScore(score);
			results.add(result);
		}
		Collections.sort(results, new ResultsComparator());
		return results;
		
	}

	private Double calculateProximity(ArrayList<Term> terms, String url) {
		// TODO Fix this bug.
		Term tmp = terms.get(0);
		tmp.getLocations(url);
		return (double) terms.size();
	}

	private Double fetchPageRank(String url) {
		// TODO Fix this and make sure on startup
		// we're downloading all the PageRank data
		return 0.85;
	}
	
	private HashMap<String, Double> urlsToCosSim;

	/**
	 * Calculates the cosine similarity of one URL with the
	 * search query.
	 * @param term - the word we're
	 * @param url
	 * @param urlToTFs
	 */
	private void calculateTfIdf(Term term, String url, HashMap<String, Double> urlToTFs) {
		ArrayList<Double> vector = new ArrayList<Double>();
		for (Term t : terms) {
			double tf = terms.get(0).getTermFrequency(url);
			for (int i = 1; i < terms.size(); i++) {
				tf *= terms.get(i).getTermFrequency(url);
			}
			// use corpusSize and calculate idf
			t.
			double idf = (double) corpusSize
					/ (double) DBWrapperIndexer.getTerm(t);
			// put scores in a vector of terms
			// and dot product with the 1 vector
			// return that number
		}
		return 1.0;
	}
	
	public void destroy() {
	
	}
}
