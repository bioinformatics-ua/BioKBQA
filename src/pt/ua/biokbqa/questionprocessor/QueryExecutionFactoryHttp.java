package pt.ua.biokbqa.questionprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import com.google.common.base.Joiner;

public class QueryExecutionFactoryHttp {
	private String service;

	private List<String> defaultGraphs = new ArrayList<String>();

	public QueryExecutionFactoryHttp(String service) {
		this(service, Collections.<String>emptySet());
	}

	public QueryExecutionFactoryHttp(String service, String defaultGraphName) {
		this(service, Collections.singleton(defaultGraphName));
	}

	public QueryExecutionFactoryHttp(String service, Collection<String> defaultGraphs) {
		this.service = service;
		this.defaultGraphs = new ArrayList<String>(defaultGraphs);
		Collections.sort(this.defaultGraphs);
	}

	// @Override
	public String getId() {
		return service;
	}

	// @Override
	public String getState() {
		return Joiner.on("|").join(defaultGraphs);
	}

	// @Override
	public QueryExecution createQueryExecution(String queryString) {
		QueryEngineHTTP result = new QueryEngineHTTP(service, queryString);
		result.setDefaultGraphURIs(defaultGraphs);
		return result;
	}
}
