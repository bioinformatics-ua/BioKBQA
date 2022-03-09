package pt.ua.biokbqa.pruner;

import java.util.Set;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class HasBoundVariables implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, KBQAQuestion q) {
		Set<SPARQLQuery> returnList = Sets.newHashSet();
		for (SPARQLQuery queryString : queryStrings) {
			boolean flag = true;
			for (String triple : queryString.constraintTriples) {
				if (triple.contains("http")) {
					flag = true;
				}
			}
			// if (queryString.filter.isEmpty()) {
			// flag = false;
			// }
			if (flag) {
				returnList.add(queryString);
			}
		}
		return returnList;
	}
}
