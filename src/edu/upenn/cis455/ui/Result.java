package edu.upenn.cis455.ui;

public class Result {

	private String url;
	private String title;
	private String description;
	private String modifiedDate;
	private double score;
	
	public Result()
	{
		url="";
		title="";
		description="";
		modifiedDate="";
		score = 0;
	}
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String descrip)
	{
		this.description = descrip;
	}
	public String getDate()
	{
		return modifiedDate;
	}
	public void setDate(String date)
	{
		this.modifiedDate = date;
	}
	public double getScore()
	{
		return score;
	}
	public void setScore(double score)
	{
		this.score = score;
	}
}
