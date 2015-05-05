package edu.upenn.cis455.ui;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import edu.upenn.cis455.indexer.DocInfo;
import edu.upenn.cis455.storage.DBWrapperIndexer;
import edu.upenn.cis455.storage.PageRank;
import edu.upenn.cis455.storage.Term;

public class IndexServlet extends HttpServlet {
	 @Override
	  public void init(ServletConfig servletConfig)
	  {
		AmazonS3 s3;
		String bucketName;
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (/home/cis455/.aws/credentials), and is in valid format.",
					e);
		}

		s3 = new AmazonS3Client(credentials);
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		s3.setRegion(usEast1);

		bucketName = "for.indexer";
		System.out.println("Listing objects");
		ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix("UrlS3batch:"));
		for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) 
		{
			String key = objectSummary.getKey();
			System.out.println("Getting object: " + key);
			String fileName = key;
			S3Object object = s3.getObject(new GetObjectRequest(bucketName, fileName));
			InputStream input = object.getObjectContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input));
			
			String line;
			try {
				while ((line = reader.readLine())!=null) 
				{
					String [] tokens = line.split("\t");
					if(tokens.length<=1)
					{
						System.out.println("skipping badly formatted line: "+line);
						continue;
					}
					String url = tokens[0];
					Double pageRankScore = Double.valueOf((String)tokens[1]);
					DBWrapperIndexer.putPageRank(url, pageRankScore);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			
		}
	  }
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
	private HashMap<String, UrlRanking> rankings;
	
	private ArrayList<Result> getResults(String originalQuery)
	{
		rankings = new HashMap<String, UrlRanking>();
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
			for (String url : urlToTFs.keySet()) {
				rankings.put(url, new UrlRanking(url));
				if (urlToTerms.containsKey(url)) {
					// if the url is in the hashmap of Url to Terms in the
					// search query in that document
					// then get the value for the hashmap, add this term to the
					// arraylist and put it back in
					// the hashmap
					ArrayList<Term> terms = urlToTerms.get(url);
					terms.add(curr);
					urlToTerms.put(url, terms);
				} else {
					// looking at all urls and their TFs associated with one
					// term
					// if the url is not in the mapping from url to the Terms in
					// the search query then add it to the hashmap
					ArrayList<Term> terms = new ArrayList<Term>();
					terms.add(curr);
					urlToTerms.put(url, terms);
				}
				calculateTfIdf(curr, url, urlToTFs);
			}
		}
		ArrayList<Result> results = new ArrayList<Result>();
		//looking at all urls that contain a term, if a url contains more than one term then 
		//boost its ranking by a factor of 5000, so 2 search terms adds 5000, 3 adds 10000, 4 adds 15000, etc.
		corpusSize = urlToTerms.keySet().size();
		for (String url : urlToTerms.keySet()) {
			double score = 0;
			ArrayList<Term> terms = urlToTerms.get(url);
			fetchPageRank(url);
			calculateProximity(terms, url);
			if (terms.size() > 1) {
				score += 5000 * terms.size() - 1;
			}
			//score = rankings.get(url).calculateHypeScore();
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

	private void calculateProximity(ArrayList<Term> terms, String url) {
		// TODO Fix this bug.
		HashMap<Term, LinkedList<Integer>> locations = new HashMap<Term, LinkedList<Integer>>();
		UrlRanking temp = rankings.get(url);
		for (Term t : terms) {
			locations.put(t, t.getLocations(url));
		}
		temp.setLocations(locations);
	}

	private void fetchPageRank(String url) {
		// TODO Fix this and make sure on startup
		// we're downloading all the PageRank data
		PageRank pRank = DBWrapperIndexer.getPageRank(url);
		UrlRanking temp = rankings.get(url);
		temp.setPageRank(pRank.getPageRankScore());
		rankings.put(url, temp);
	}
	

	/**
	 * Calculates the cosine similarity of one URL with the search query.
	 * @param term
	 * @param url
	 * @param urlToTFs
	 */
	private void calculateTfIdf(Term term, String url,
			HashMap<String, Double> urlToTFs) {
		double tf = term.getTermFrequency(url);
		double idf = (double) corpusSize / (double) urlToTFs.keySet().size();
		UrlRanking temp = rankings.get(term);
		temp.addTfIdfScore(term, (double) tf * idf); 
		rankings.put(url, temp);
	}

	public void destroy() {
	
	}
}
