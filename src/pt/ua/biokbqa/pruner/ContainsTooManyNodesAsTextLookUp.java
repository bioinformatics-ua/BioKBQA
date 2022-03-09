package pt.ua.biokbqa.pruner;

import java.util.Set;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class ContainsTooManyNodesAsTextLookUp implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, KBQAQuestion q) {
		Set<SPARQLQuery> returnList = Sets.newHashSet();
		for (SPARQLQuery query : queryStrings) {
			for (String variable : query.textMapFromVariableToCombinedNNExactMatchToken.keySet()) {
				if (query.textMapFromVariableToCombinedNNExactMatchToken.get(variable).size() <= 2) {
					returnList.add(query);
				}
			}
		}
		return returnList;
	}
}
