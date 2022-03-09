package pt.ua.biokbqa.pruner;

import java.util.Set;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class NumberOfTypesPerVariable implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, KBQAQuestion q) {
		Set<SPARQLQuery> returnSet = Sets.newHashSet();
		for (SPARQLQuery sparqlQuery : queryStrings) {
			String[] split = new String[3];
			Set<String> variableWithType = Sets.newHashSet();
			boolean flag = true;
			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				if (split[0].startsWith("?") && split[1].equals("a")) {
					if (variableWithType.contains(split[0])) {
						flag = false;
					} else {
						variableWithType.add(split[0]);
					}
				}
			}
			if (flag) {
				returnSet.add(sparqlQuery);
			}
		}
		return returnSet;
	}
}
