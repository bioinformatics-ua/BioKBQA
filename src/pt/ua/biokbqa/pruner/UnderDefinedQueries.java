package pt.ua.biokbqa.pruner;

import java.util.Set;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class UnderDefinedQueries implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, KBQAQuestion q) {
		Set<SPARQLQuery> returnSet = Sets.newHashSet();
		for (SPARQLQuery sparqlQuery : queryStrings) {
			if (sparqlQuery.constraintTriples.isEmpty()
					&& sparqlQuery.textMapFromVariableToCombinedNNExactMatchToken.isEmpty()) {
				continue;
			}
			String[] split = new String[3];
			boolean containsOnlyUnboundTriple = true;
			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				if (!split[0].startsWith("?") || !split[1].startsWith("?") || !split[2].startsWith("?")) {
					containsOnlyUnboundTriple = false;
				}
			}
			boolean containsOnlyTypeDefinitions = true;
			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				if (!split[1].equals("a")) {
					containsOnlyTypeDefinitions = false;
				}
			}
			boolean wellDefinedTextFilter = false;
			for (String key : sparqlQuery.textMapFromVariableToSingleFuzzyToken.keySet()) {
				if (sparqlQuery.textMapFromVariableToSingleFuzzyToken.get(key).size() >= 2) {
					wellDefinedTextFilter = true;
				}
			}
			if ((!containsOnlyUnboundTriple && !containsOnlyTypeDefinitions)
					|| (containsOnlyTypeDefinitions && wellDefinedTextFilter)) {
				returnSet.add(sparqlQuery);
			}
		}
		return returnSet;
	}
}
