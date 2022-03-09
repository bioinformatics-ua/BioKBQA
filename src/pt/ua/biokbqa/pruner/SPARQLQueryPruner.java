package pt.ua.biokbqa.pruner;

import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQL;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class SPARQLQueryPruner implements ISPARQLQueryPruner {
	private DisjointnessBasedQueryFilter disjointness;
	private BGPisConnected BGPisConnected;
	private CyclicTriple cyclicTriple;
	private HasBoundVariables hasBoundVariables;
	private TextFilterOverVariables textFilterOverVariables;
	private UnboundTriple unboundTriple;
	private UnderDefinedQueries underdefined;
	private PredicatesPerVariableEdge predicatesPerVariableEdge;
	private NumberOfTypesPerVariable numberOfTypesPerVariable;
	private ContainsProjVariable containsProjVariable;
	private ContainsTooManyNodesAsTextLookUp containsTooManyNodesAsTextLookUp;
	private TypeMismatch typemismatch;

	public SPARQLQueryPruner(SPARQL sparql) {
		// this.disjointness = new DisjointnessBasedQueryFilter(sparql.qef);
		this.BGPisConnected = new BGPisConnected();
		this.cyclicTriple = new CyclicTriple();
		this.hasBoundVariables = new HasBoundVariables();
		this.textFilterOverVariables = new TextFilterOverVariables();
		this.unboundTriple = new UnboundTriple();
		this.underdefined = new UnderDefinedQueries();
		this.predicatesPerVariableEdge = new PredicatesPerVariableEdge();
		this.numberOfTypesPerVariable = new NumberOfTypesPerVariable();
		this.containsProjVariable = new ContainsProjVariable();
		this.containsTooManyNodesAsTextLookUp = new ContainsTooManyNodesAsTextLookUp();
		// this.typemismatch = new TypeMismatch(sparql.qef);
	}

	@SuppressWarnings("unchecked")
	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queries, KBQAQuestion q) {
		Set<SPARQLQuery> returnedQueries = null;
		JSONArray document = new JSONArray();
		JSONObject tmp = new JSONObject();
		tmp.put("label", "Number of Queries before pruning");
		tmp.put("value", queries.size());
		document.add(tmp);
		returnedQueries = underdefined.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "Underdefined pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = containsProjVariable.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing no project variable pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = containsTooManyNodesAsTextLookUp.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing too many nodes as text lookup pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = predicatesPerVariableEdge.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with more than one predicate between the same variables pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = numberOfTypesPerVariable.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with more than one type per variable pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = BGPisConnected.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without connected BGP pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = cyclicTriple.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing cycic triple pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = hasBoundVariables.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without bound variables pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = textFilterOverVariables.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without text filter over existing variables pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = unboundTriple.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with unbound triples pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		returnedQueries = typemismatch.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with mismatching types pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		try {
			returnedQueries = disjointness.prune(queries, q);
			tmp = new JSONObject();
			tmp.put("label", "SPARQL queries with disjoint classes pruned");
			tmp.put("value", (queries.size() - returnedQueries.size()));
			tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
			document.add(tmp);
			queries = returnedQueries;
		} catch (Exception e) {
			System.out.println("Cannot prune based on disjointness due to unavailable endpoint");
			e.printStackTrace();
		}
		tmp = new JSONObject();
		tmp.put("label", "Number of Queries after really short pruning");
		tmp.put("value", queries.size());
		tmp.put("queries", queries2json(queries));
		document.add(tmp);
		q.setPruning_messages(document);
		System.out.println(document.toJSONString());
		return queries;
	}

	private static Set<SPARQLQuery> queriesDiff(Set<SPARQLQuery> originalQueries, Set<SPARQLQuery> modifiedQueries) {
		Set<SPARQLQuery> result = new HashSet<>(originalQueries);
		result.removeAll(modifiedQueries);
		return result;
	}

	@SuppressWarnings("unchecked")
	private static JSONArray queries2json(Set<SPARQLQuery> queries) {
		JSONArray jsonQueries = new JSONArray();
		for (SPARQLQuery query : queries) {
			JSONObject jsonQuery = new JSONObject();
			jsonQuery.put("query", query.toString());
			jsonQueries.add(jsonQuery);
		}
		return jsonQueries;
	}
}
