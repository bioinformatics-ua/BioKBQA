package pt.ua.biokbqa.questionprocessor;

import java.util.List;
import java.util.Set;
import org.json.simple.JSONObject;
import com.google.common.collect.Lists;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.pruner.SPARQLQueryPruner;

public class SPARQLQueryBuilder {
	int numberOfOverallQueriesExecuted = 0;
	private SPARQL sparql;
	private RecursiveSparqlQueryBuilder recursiveSparqlQueryBuilder;
	private SPARQLQueryPruner sparqlQueryPruner;

	public SPARQLQueryBuilder(SPARQL sparql) {
		this.sparql = sparql;
		this.recursiveSparqlQueryBuilder = new RecursiveSparqlQueryBuilder();
		this.sparqlQueryPruner = new SPARQLQueryPruner(sparql);
	}

	@SuppressWarnings("unchecked")
	public List<Answer> build(KBQAQuestion q) {
		List<Answer> answer = Lists.newArrayList();
		try {
			Set<SPARQLQuery> queryStrings = recursiveSparqlQueryBuilder.start(this, q);
			queryStrings = sparqlQueryPruner.prune(queryStrings, q);
			int cardinality = cardinality(q, queryStrings);
			JSONObject tmp = new JSONObject();
			tmp.put("label", "Cardinality of question results");
			tmp.put("value", cardinality);
			q.getPruning_messages().add(tmp);
			System.out.println("Cardinality:" + q.getLanguageToQuestion().get("en").toString() + "-> " + cardinality);
			int i = 0;
			for (SPARQLQuery query : queryStrings) {
				for (String queryString : query.generateQueries()) {
					System.out.println(
							i++ + "/" + queryStrings.size() * query.generateQueries().size() + "= " + queryString);
					Answer a = new Answer();
					a.answerSet = sparql.sparql(queryString);
					a.query = query;
					a.queryString = queryString;
					a.question_id = q.getId();
					a.question = q.getLanguageToQuestion().get("en").toString();
					if (!a.answerSet.isEmpty()) {
						answer.add(a);
					}
					numberOfOverallQueriesExecuted++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.gc();
		}
		JSONObject tmp = new JSONObject();
		tmp.put("label", "Number of sofar executed queries");
		tmp.put("value", numberOfOverallQueriesExecuted);
		q.getPruning_messages().add(tmp);
		System.out.println("Number of sofar executed queries: " + numberOfOverallQueriesExecuted);
		return answer;
	}

	private int cardinality(KBQAQuestion q, Set<SPARQLQuery> queryStrings) {
		int cardinality = q.getCardinality();
		for (SPARQLQuery s : queryStrings) {
			s.setLimit(cardinality);
		}
		return cardinality;
	}
}
