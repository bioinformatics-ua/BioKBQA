package pt.ua.biokbqa.pruner;

import java.util.Set;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class UnboundTriple implements ISPARQLQueryPruner {

	private int maximalUnboundTriplePatterns = 1;

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, KBQAQuestion q) {
		Set<SPARQLQuery> returnSet = Sets.newHashSet();
		for (SPARQLQuery sparqlQuery : queryStrings) {
			int numberOfUnboundTriplePattern = 0;
			String[] split = new String[3];
			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				if (split[0].startsWith("?") && split[1].startsWith("?") && split[2].startsWith("?")) {
					numberOfUnboundTriplePattern++;
				}
			}
			if (numberOfUnboundTriplePattern <= maximalUnboundTriplePatterns) {
				returnSet.add(sparqlQuery);
			}
		}
		return returnSet;
	}
}
