package pt.ua.biokbqa.questionprocessor;

import pt.ua.biokbqa.utils.URLs;

public class DBpediaEndpoint {

	public static void main(String args[]) {
		String queryStr = "";
		queryStr = "SELECT ?prop ?place WHERE {<http://dbpedia.org/resource/%C3%84lvdalen> ?prop ?place .}";
		org.apache.jena.query.Query query = org.apache.jena.query.QueryFactory.create(queryStr);
		String dbpedia = URLs.DBpediaURL;
		try {
			// test local instance
			java.net.URL url = new java.net.URL("http://localhost:3030/$/stats/ds");
			java.net.HttpURLConnection huc = (java.net.HttpURLConnection) url.openConnection();
			if (huc.getResponseCode() == java.net.HttpURLConnection.HTTP_OK) {
				dbpedia = URLs.DBpediaLocalInstanceURL;
			}
		} catch (Exception e) {
		}
		org.apache.jena.sparql.engine.http.QueryEngineHTTP qe = new org.apache.jena.sparql.engine.http.QueryEngineHTTP(
				dbpedia, query);
		qe.addParam("timeout", "10000");
		try {
			org.apache.jena.query.ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				org.apache.jena.query.QuerySolution s = rs.nextSolution();
				System.out.println(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			qe.close();
		}
	}
}
