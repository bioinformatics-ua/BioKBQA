package pt.ua.biokbqa.pruner;

import java.util.Map;
import java.util.Set;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class PredicatesPerVariableEdge implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, KBQAQuestion q) {
		Set<SPARQLQuery> returnSet = Sets.newHashSet();
		for (SPARQLQuery sparqlQuery : queryStrings) {
			Map<String, Set<String>> predicatesPerEdge = Maps.newHashMap();
			String[] split = new String[3];
			boolean flag = true;
			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				String key = "" + split[0] + split[2];
				if (split[0].startsWith("?") && !split[1].startsWith("?") && split[2].startsWith("?")) {
					if (predicatesPerEdge.containsKey(key)) {
						Set<String> set = predicatesPerEdge.get(key);
						set.add(split[1]);
						predicatesPerEdge.put(key, set);
					} else {
						predicatesPerEdge.put(key, Sets.newHashSet(split[1]));
					}
				}
			}
			for (String key : predicatesPerEdge.keySet()) {
				if (predicatesPerEdge.get(key).size() > 1) {
					flag = false;
				}
			}
			if (flag) {
				returnSet.add(sparqlQuery);
			}
		}
		return returnSet;
	}
}
