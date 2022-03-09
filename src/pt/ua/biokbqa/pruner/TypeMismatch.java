package pt.ua.biokbqa.pruner;

import java.util.HashSet;
import java.util.List;
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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class TypeMismatch implements ISPARQLQueryPruner {
	private static final ParameterizedSparqlString typeQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + "SELECT ?type WHERE {?s a ?type .}");
	private static final Set<Resource> PROPERTY_ENTITY_TYPES = Sets.newHashSet(OWL.ObjectProperty, OWL.DatatypeProperty,
			RDF.Property);
	@SuppressWarnings("unused")
	private QueryExecutionFactory qef;
	private QueryUtils queryUtils = new QueryUtils();
	private Monitor mon = MonitorFactory.getTimeMonitor("typeMismatch");
	private static final Set<String> ignoredProperties = Sets.newHashSet("http://jena.apache.org/text#query",
			"http://dbpedia.org/ontology/abstract");

	public TypeMismatch(final QueryExecutionFactory qef) {
		this.qef = qef;
	}

	@Override
	public Set<SPARQLQuery> prune(final Set<SPARQLQuery> queryStrings, final KBQAQuestion q) {
		mon.reset();
		Set<SPARQLQuery> filteredQueries = Sets.newHashSet();
		for (SPARQLQuery sparqlQuery : queryStrings) {
			if (accept(sparqlQuery)) {
				filteredQueries.add(sparqlQuery);
			}
		}
		return filteredQueries;
	}

	private Set<Resource> getEntityTypes(final String entity) {
		Set<Resource> entityTypes = new HashSet<>();
		typeQueryTemplate.setIri("s", entity);
		// String query = typeQueryTemplate.toString();
		// QueryExecution qe = qef.createQueryExecution(query);
		// ResultSet rs = qe.execSelect();
		// while (rs.hasNext()) {
		// 	QuerySolution qs = rs.next();
		// 	Resource type = qs.getResource("type");
		// 	entityTypes.add(type);
		// }
		// qe.close();
		return entityTypes;
	}

	private boolean accept(final SPARQLQuery sparqlQuery) {
		mon.start();
		try {
			Query query = QueryFactory.create(sparqlQuery.toString());
			List<Var> projectVars = query.getProjectVars();
			Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);
			for (Triple tp : triplePatterns) {
				Node predicate = tp.getPredicate();
				if (predicate.isURI() && !predicate.getNameSpace().equals(RDF.getURI())
						&& !predicate.getNameSpace().equals(RDFS.getURI())
						&& !ignoredProperties.contains(predicate.getURI())) {
					Set<Resource> entityTypes = getEntityTypes(predicate.getURI());
					if (!isProperty(entityTypes)) {
						return false;
					}
					if (entityTypes.contains(OWL.DatatypeProperty) && projectVars.contains(tp.getObject())) {
						return false;
					}
				}
			}
		} finally {
			mon.stop();
		}
		return true;
	}

	private boolean isProperty(final Set<Resource> entityTypes) {
		return !Sets.intersection(entityTypes, PROPERTY_ENTITY_TYPES).isEmpty();
	}
}
