package pt.ua.biokbqa.pruner;

import java.util.Set;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class TextFilterOverVariables implements ISPARQLQueryPruner {
	private int maximalVariables = 1;

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, KBQAQuestion q) {
		Set<SPARQLQuery> returnList = Sets.newHashSet();
		for (SPARQLQuery query : queryStrings) {
			if (query.textMapFromVariableToSingleFuzzyToken.size() <= maximalVariables) {
				returnList.add(query);
			}
		}
		return returnList;
	}
}
