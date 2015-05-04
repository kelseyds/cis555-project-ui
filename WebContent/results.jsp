<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page isELIgnored="false"%>
<%@ page import="edu.upenn.cis455.ui.Result" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html>
<html>
	<head>
	 	<link rel="stylesheet" href="styles/styles.css" type="text/css" media="screen">
	    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	    <title>Hype Engine UI</title>
	   
	</head>
	<body>
		<center>
	 		<h1>Hype Engine</h1>
	 	</center>
	    <div id="content" class="container">
			<!-- HTML for SEARCH BAR -->
			<div id="tfheader">
				<form id="tfnewsearch" method="get" action="results">
				        <input type="text" class="tftextinput" name="query" maxlength="240"><input type="submit" value="Get Hyped" class="tfbutton">
				</form>
				<div class="tfclear"></div>
			</div>
			<div id="searchedfor">
				<h3>Searched For: <c:out value="${param.query}"/></h3>
			</div>
			<div id="resultslist">
				<dl>
				<c:forEach items="${resultsList}" var="result">
					<div class="result">
						<dt><a href="${result.url}"> <c:out value="${result.title}"/></a></dt>
						<dd class="description">
							<c:set var="description" value="${result.description}"/>
							<%-- <c:set var="queryTerms" value="${fn:split(description, ' ')}" />
							
							<c:forEach items="${queryTerms}" var="term">
								<c:set var="description" value="${fn:replace(description, 
                                '${term}', '<strong>${term}</strong>')}" />
							</c:forEach>--%>
						<p>${description}</p></dd> 
					    <dd class="url"><c:out value="url: ${result.url}"/></dd>
				    </div>
				</c:forEach>
				</dl>
			</div>
		</div>
	</body>
</html>
	