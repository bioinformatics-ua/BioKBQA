package pt.ua.biokbqa.data.blueprint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.ua.biokbqa.nlp.MutableTree;
import pt.ua.biokbqa.utils.JSONStatusBuilder;

public class KBQAQuestion extends Question implements Serializable {
	private static final long serialVersionUID = 1L;
	private int cardinality;
	private Boolean isClassifiedAsASKQuery;
	private Boolean loadedAsASKQuery;
	private MutableTree tree;
	private Map<String, List<Entity>> languageToNamedEntites = new LinkedHashMap<String, List<Entity>>();
	private Map<String, List<Entity>> languageToNounPhrases = new LinkedHashMap<String, List<Entity>>();
	private Map<String, List<Entity>> goldEntites = new HashMap<String, List<Entity>>();
	private UUID UUID;
	private List<Answer> finalAnswer;
	private JSONObject tree_full;
	private JSONObject tree_pruned;
	private JSONObject tree_final;
	private JSONArray pruning_messages = new JSONArray();

	public KBQAQuestion() {
		goldEntites.put("en", new ArrayList<Entity>());
	}

	public String toString() {
		String output = "";
		for (Map.Entry<String, String> entry : getLanguageToQuestion().entrySet()) {
			output += "Question: " + entry.getValue() + "\n";
		}
		output += "Answers: " + StringUtils.join(getGoldenAnswers(), ", ") + "\n";
		return output;
	}

	public String getJSONStatus() {
		JSONObject sb = JSONStatusBuilder.status(this);
		return sb.toJSONString();
	}

	public JSONObject getTree_full() {
		return tree_full;
	}

	public void setTree_full(JSONObject tree_full) {
		this.tree_full = tree_full;
	}

	public Boolean getIsClassifiedAsASKQuery() {
		return isClassifiedAsASKQuery;
	}

	public void setIsClassifiedAsASKQuery(Boolean isClassifiedAsASKQuery) {
		this.isClassifiedAsASKQuery = isClassifiedAsASKQuery;
	}

	public Boolean getLoadedAsASKQuery() {
		return loadedAsASKQuery;
	}

	public void setLoadedAsASKQuery(Boolean loadedAsASKQuery) {
		this.loadedAsASKQuery = loadedAsASKQuery;
	}

	public Map<String, List<Entity>> getLanguageToNamedEntites() {
		return languageToNamedEntites;
	}

	public void setLanguageToNamedEntites(Map<String, List<Entity>> languageToNamedEntites) {
		this.languageToNamedEntites = languageToNamedEntites;
	}

	public Map<String, List<Entity>> getLanguageToNounPhrases() {
		return languageToNounPhrases;
	}

	public void setLanguageToNounPhrases(Map<String, List<Entity>> languageToNounPhrases) {
		this.languageToNounPhrases = languageToNounPhrases;
	}

	public MutableTree getTree() {
		return tree;
	}

	public void setTree(MutableTree tree) {
		this.tree = tree;
	}

	public JSONObject getTree_final() {
		return tree_final;
	}

	public void setTree_final(JSONObject tree_final) {
		this.tree_final = tree_final;
	}

	public JSONObject getTree_pruned() {
		return tree_pruned;
	}

	public void setTree_pruned(JSONObject tree_pruned) {
		this.tree_pruned = tree_pruned;
	}

	public JSONArray getPruning_messages() {
		return pruning_messages;
	}

	public void setPruning_messages(JSONArray pruning_messages) {
		this.pruning_messages = pruning_messages;
	}

	public List<Answer> getFinalAnswer() {
		return finalAnswer;
	}

	public void setFinalAnswer(List<Answer> finalAnswer) {
		this.finalAnswer = finalAnswer;
	}

	public UUID getUUID() {
		return UUID;
	}

	public void setUUID(UUID uUID) {
		UUID = uUID;
	}

	public int getCardinality() {
		return cardinality;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	public boolean checkSuitabillity() {
		return (this.getAnswerType().matches("resource||boolean") & this.getOnlydbo() & !this.getAggregation());
	}
}
