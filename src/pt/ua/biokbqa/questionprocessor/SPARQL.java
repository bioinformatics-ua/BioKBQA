package pt.ua.biokbqa.questionprocessor;

import java.util.Set;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.benchmark.Evaluator;
import pt.ua.biokbqa.utils.URLs;

public class SPARQL {
	public QueryExecutionFactoryHttp qef;

	public SPARQL() {
		try {
			qef = new QueryExecutionFactoryHttp(URLs.DBpediaURL, "http://dbpedia.org/");
		} catch (RuntimeException e) {
			System.exit(0);
		}
	}

	public Set<RDFNode> sparql(final String query) {
		Set<RDFNode> set = Sets.newHashSet();
		try {
			QueryExecution qe = qef.createQueryExecution(query);
			if (qe != null && query.toString() != null) {
				if (Evaluator.isAskType(query)) {
					set.add(new ResourceImpl(String.valueOf(qe.execAsk())));
				} else {
					ResultSet results = qe.execSelect();
					while (results.hasNext()) {
						set.add(results.next().get("proj"));
					}
				}
			}
		} catch (Exception e) {
			System.out.println(query.toString());
			e.printStackTrace();
		}
		return set;
	}

	public static void main(final String args[]) {
		SPARQL sqb = new SPARQL();
		SPARQLQuery query = new SPARQLQuery();
		query.addConstraint("?proj a <http://dbpedia.org/ontology/Cleric>.");
		for (String q : query.generateQueries()) {
			Set<RDFNode> set = sqb.sparql(q);
			for (RDFNode item : set) {
				System.out.println(item);
			}
		}
	}
}
