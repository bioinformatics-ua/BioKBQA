package pt.ua.biokbqa.utils;

import java.util.Set;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.json.simple.JSONObject;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.questionprocessor.QueryExecutionFactoryHttp;

public class AnswerBox {
	private static QueryExecutionFactoryHttp qef;

	static {
		try {
			qef = new QueryExecutionFactoryHttp(URLs.DBpediaURL);
		} catch (Exception e) {
			System.out.println("Could not create SPARQL interface!");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONObject buildAnswerBoxFeatures(final String uri) {
		JSONObject document = new JSONObject();
		document.put("URI", new JsonString(uri));
		@SuppressWarnings("unused")
		Set<RDFNode> set = Sets.newHashSet();
		try {
			String query = "select ?thumbnail ?abstract ?comment ?label" + "where {" + "<" + uri
					+ "> <http://dbpedia.org/ontology/thumbnail> ?thumbnail;"
					+ "<http://dbpedia.org/ontology/abstract> ?abstract;"
					+ "<http://www.w3.org/2000/01/rdf-schema#label> ?label;"
					+ "<http://www.w3.org/2000/01/rdf-schema#comment> ?comment."
					+ "FILTER(langMatches(lang(?abstract), \"EN\") &&"
					+ "          langMatches(lang(?label), \"EN\") &&"
					+ "          langMatches(lang(?comment), \"EN\"))" + "}";
			QueryExecution qe = qef.createQueryExecution(query);
			if (qe != null && query.toString() != null) {
				ResultSet results = qe.execSelect();
				while (results.hasNext()) {
					QuerySolution next = results.next();
					RDFNode thumbnail = next.get("thumbnail");
					RDFNode abstractLiteral = next.get("abstract");
					RDFNode commentLiteral = next.get("comment");
					RDFNode labelLiteral = next.get("label");
					if (thumbnail != null) {
						document.put("thumbnail", new JsonString(thumbnail.asResource().getURI()));
					}
					if (abstractLiteral != null) {
						document.put("abstract", new JsonString(abstractLiteral.asLiteral().getString()));
					}
					if (commentLiteral != null) {
						document.put("comment", new JsonString(commentLiteral.asLiteral().getString()));
					}
					if (labelLiteral != null) {
						document.put("label", new JsonString(labelLiteral.asLiteral().getString()));
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Cannot ask DBpedia for verbose description of " + uri);
			e.printStackTrace();
		}
		return document;
	}
}
