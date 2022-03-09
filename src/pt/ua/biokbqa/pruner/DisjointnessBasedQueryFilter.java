package pt.ua.biokbqa.pruner;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
// import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
// import org.apache.jena.query.QuerySolution;
// import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class DisjointnessBasedQueryFilter implements ISPARQLQueryPruner {

	private static final ParameterizedSparqlString domainQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> SELECT ?dom WHERE {?p rdfs:domain ?o . ?o rdfs:subClassOf* ?dom .}");
	private static final ParameterizedSparqlString rangeQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> SELECT ?ran WHERE {?p rdfs:range ?o . ?o rdfs:subClassOf* ?ran .}");
	private static final ParameterizedSparqlString superClassesQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> SELECT ?sup WHERE {?sub rdfs:subClassOf+ ?sup .}");
	@SuppressWarnings("unused")
	private QueryExecutionFactory qef;
	QueryUtils queryUtils = new QueryUtils();
	private static final Set<String> ignoredProperties = Sets.newHashSet("http://jena.apache.org/text#query",
			"http://dbpedia.org/ontology/abstract");

	public DisjointnessBasedQueryFilter(final QueryExecutionFactory qef) {
		this.qef = qef;
	}

	@Override
	public Set<SPARQLQuery> prune(final Set<SPARQLQuery> queryStrings, final KBQAQuestion q) {
		MonitorFactory.getTimeMonitor("parse").reset();
		Set<SPARQLQuery> filteredQueries = Sets.newHashSet();
		for (SPARQLQuery sparqlQuery : queryStrings) {
			if (accept(sparqlQuery)) {
				filteredQueries.add(sparqlQuery);
			}
		}
		System.out.println(MonitorFactory.getTimeMonitor("parse"));
		return filteredQueries;
	}

	private Set<Node> getDomain(final String predicate) {
		Set<Node> domains = new HashSet<>();
		domainQueryTemplate.setIri("p", predicate);
		// String query = domainQueryTemplate.toString();
		// QueryExecution qe = qef.createQueryExecution(query);
		// ResultSet rs = qe.execSelect();
		// while (rs.hasNext()) {
		// 	QuerySolution qs = rs.next();
		// 	domains.add(qs.getResource("dom").asNode());
		// }
		// qe.close();
		return domains;
	}

	private Set<Node> getRange(final String predicate) {
		Set<Node> range = new HashSet<>();
		rangeQueryTemplate.setIri("p", predicate);
		// String query = rangeQueryTemplate.toString();
		// QueryExecution qe = qef.createQueryExecution(query);
		// ResultSet rs = qe.execSelect();
		// while (rs.hasNext()) {
		// 	QuerySolution qs = rs.next();
		// 	range.add(qs.getResource("ran").asNode());
		// }
		// qe.close();
		return range;
	}

	private Set<Node> getSuperClasses(final String cls) {
		Set<Node> superClasses = new HashSet<>();
		superClassesQueryTemplate.setIri("sub", cls);
		// String query = superClassesQueryTemplate.toString();
		// QueryExecution qe = qef.createQueryExecution(query);
		// ResultSet rs = qe.execSelect();
		// while (rs.hasNext()) {
		// 	QuerySolution qs = rs.next();
		// 	superClasses.add(qs.getResource("sup").asNode());
		// }
		// qe.close();
		return superClasses;
	}

	private boolean accept(final SPARQLQuery sparqlQuery) {
		Monitor mon = MonitorFactory.getTimeMonitor("parse");
		mon.start();
		Query query = QueryFactory.create(sparqlQuery.toString());
		mon.stop();
		Set<Triple> typeTriples = queryUtils.getRDFTypeTriples(query);
		Multimap<Var, Node> var2Types = HashMultimap.create();
		for (Triple triple : typeTriples) {
			if (triple.getObject().isURI()) {
				Node type = triple.getObject();
				Var var = Var.alloc(triple.getSubject());
				var2Types.put(var, type);
				var2Types.putAll(var, getSuperClasses(type.getURI()));
			}
		}
		Set<Var> subjectVars = queryUtils.getSubjectVariables(query);
		for (Var var : subjectVars) {
			Set<Triple> outgoingTriplePatterns = queryUtils.extractOutgoingTriplePatterns(query, var);
			outgoingTriplePatterns.removeAll(typeTriples);
			Set<Node> subjectTypes = (Set<Node>) var2Types.get(var);
			if (!subjectTypes.isEmpty()) {
				for (Triple tp : outgoingTriplePatterns) {
					Node predicate = tp.getPredicate();
					if (predicate.isURI() && !ignoredProperties.contains(predicate.toString())) {
						Set<Node> domain = getDomain(predicate.getURI());
						System.out.println("Domain of " + predicate + ": " + subjectTypes);
						if (conflicts(subjectTypes, domain)) {
							System.out.println("Domain of " + predicate + " does not match types " + subjectTypes);
							return false;
						}
					}
				}
			}
		}
		Set<Var> objectVars = queryUtils.getSubjectVariables(query);
		for (Var var : objectVars) {
			Set<Triple> incomingTriplePatterns = queryUtils.extractIncomingTriplePatterns(query, var);
			Set<Node> objectTypes = (Set<Node>) var2Types.get(var);
			if (!objectTypes.isEmpty()) {
				for (Triple tp : incomingTriplePatterns) {
					Node predicate = tp.getPredicate();
					if (predicate.isURI() && !ignoredProperties.contains(predicate.toString())) {
						Set<Node> range = getRange(predicate.getURI());
						System.out.println("Range of " + predicate + ": " + objectTypes);
						if (conflicts(objectTypes, range)) {
							System.out.println("Range of " + predicate + " does not match types " + objectTypes);
							return false;
						}
					}

				}
			}
		}
		return true;
	}

	private boolean conflicts(final Set<Node> types1, final Set<Node> types2) {
		return Sets.intersection(types1, types2).isEmpty();
	}
}
