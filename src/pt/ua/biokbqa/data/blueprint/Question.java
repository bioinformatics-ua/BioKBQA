package pt.ua.biokbqa.data.blueprint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pt.ua.biokbqa.utils.Utils;

public class Question {
	private Integer id;
	private String answerType;
	private String pseudoSparqlQuery;
	private Map<String, String> sparqlQuery;
	private Boolean aggregation;
	private Boolean onlydbo;
	private Boolean outOfScope;
	// private Boolean hybrid;
	private Map<String, String> languageToQuestion;
	private Map<String, List<String>> languageToKeywords;
	private Map<String, Set<String>> goldenAnswers;

	public Question() {
		HashSet<String> ga = Utils.newHashSet();
		goldenAnswers = new HashMap<String, Set<String>>();
		goldenAnswers.put("en", ga);
		sparqlQuery = new HashMap<String, String>();
		languageToQuestion = Utils.newLinkedHashMap();
		languageToKeywords = Utils.newLinkedHashMap();
	}

	@Override
	public String toString() {
		return "Question [id=" + id + ", answerType=" + answerType + ", aggregation=" + aggregation + ", onlydbo="
				+ onlydbo + ", outOfScope=" + outOfScope
				// + ", hybrid=" + hybrid
				+ ", pseudoSparqlQuery=" + pseudoSparqlQuery + ", sparqlQuery=" + sparqlQuery + ", languageToQuestion="
				+ languageToQuestion + ", languageToKeywords=" + languageToKeywords + ", goldenAnswers=" + goldenAnswers
				+ "]";
	}

	public void setValue(String valDescriptor, String val) {
		valDescriptor = valDescriptor.toLowerCase();
		switch (valDescriptor) {
		case "id":
			this.id = Integer.parseInt(val);
			break;
		case "answertype":
			this.answerType = val;
			break;
		case "aggregation":
			this.aggregation = Boolean.parseBoolean(val);
			break;
		case "onlydbo":
			this.onlydbo = Boolean.parseBoolean(val);
			break;
		// case "hybrid":
		// this.hybrid = Boolean.parseBoolean(val);
		// break;
		default:
			;
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAnswerType() {
		return answerType;
	}

	public void setAnswerType(String answerType) {
		this.answerType = answerType;
	}

	public String getPseudoSparqlQuery() {
		return pseudoSparqlQuery;
	}

	public void setPseudoSparqlQuery(String pseudoSparqlQuery) {
		this.pseudoSparqlQuery = pseudoSparqlQuery;
	}

	public String getSparqlQuery() {
		return sparqlQuery.get("en");
	}

	public String getSparqlQuery(String lang) {
		return sparqlQuery.get(lang);
	}

	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery.put("en", sparqlQuery);
	}

	public void setSparqlQuery(String lang, String sparqlQuery) {
		this.sparqlQuery.put(lang, sparqlQuery);
	}

	public Boolean getAggregation() {
		return aggregation;
	}

	public void setAggregation(Boolean aggregation) {
		this.aggregation = aggregation;
	}

	public Boolean getOnlydbo() {
		return onlydbo;
	}

	public void setOnlydbo(Boolean onlydbo) {
		this.onlydbo = onlydbo;
	}

	public Boolean getOutOfScope() {
		return outOfScope;
	}

	public void setOutOfScope(Boolean outOfScope) {
		this.outOfScope = outOfScope;
	}

	// public Boolean getHybrid() {
	// return hybrid;
	// }

	// public void setHybrid(Boolean hybrid) {
	// this.hybrid = hybrid;
	// }

	public Map<String, String> getLanguageToQuestion() {
		return languageToQuestion;
	}

	public void setLanguageToQuestion(Map<String, String> languageToQuestion) {
		this.languageToQuestion = languageToQuestion;
	}

	public Map<String, List<String>> getLanguageToKeywords() {
		return languageToKeywords;
	}

	public void setLanguageToKeywords(Map<String, List<String>> languageToKeywords) {
		this.languageToKeywords = languageToKeywords;
	}

	public Set<String> getGoldenAnswers() {
		return goldenAnswers.get("en");
	}

	public Set<String> getGoldenAnswers(String lang) {
		return goldenAnswers.get(lang);
	}

	public void setGoldenAnswers(Set<String> goldenAnswers) {
		this.goldenAnswers.put("en", goldenAnswers);
	}

	public void setGoldenAnswers(String lang, Set<String> goldenAnswers) {
		this.goldenAnswers.put(lang, goldenAnswers);
	}
}
