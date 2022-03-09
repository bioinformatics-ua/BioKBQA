package pt.ua.biokbqa.pruner;

import java.util.Set;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public interface ISPARQLQueryPruner {
	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queries, KBQAQuestion q);
}
