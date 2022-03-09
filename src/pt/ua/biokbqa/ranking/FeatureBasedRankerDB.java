package pt.ua.biokbqa.ranking;

import java.io.File;
import java.util.Set;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.cache.StorageHelper;
import pt.ua.biokbqa.data.blueprint.Question;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class FeatureBasedRankerDB {

	public FeatureBasedRankerDB() {
	}

	public Set<SPARQLQuery> readRankings() {
		Set<SPARQLQuery> set = Sets.newHashSet();
		for (File f : new File("c:/ranking/").listFiles()) {
			set.add((SPARQLQuery) StorageHelper.readFromFileSavely(f.toString()));
		}
		return set;

	}

	public void store(Question q, Set<SPARQLQuery> queries) {
		for (SPARQLQuery query : queries) {
			int hash = query.hashCode();
			String serializedFileName = getFileName(hash);
			StorageHelper.storeToFileSavely(query, serializedFileName);
		}
	}

	private String getFileName(int hash) {
		String serializedFileName = "c:/ranking/" + hash + ".question";
		return serializedFileName;
	}
}
