package pt.ua.biokbqa.utils;

import java.util.Stack;
import org.apache.commons.codec.binary.Base64;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.rdf.model.RDFNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.ua.biokbqa.data.blueprint.Entity;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.nlp.MutableTree;
import pt.ua.biokbqa.nlp.MutableTreeNode;

public class JSONStatusBuilder {

	@SuppressWarnings("unchecked")
	public static JSONObject status(final KBQAQuestion question) {
		JSONObject document = new JSONObject();
		document.put("input_question", question.getLanguageToQuestion().get("en"));
		if (!question.getLanguageToNamedEntites().isEmpty()) {
			JSONArray tmp = new JSONArray();
			for (Entity key : question.getLanguageToNamedEntites().get("en")) {
				JSONObject tmpobj = new JSONObject();
				tmpobj.put("key", key.label);
				tmpobj.put("value", new JsonString(key.uris.get(0).getURI()));
				tmp.add(tmpobj);
			}
			document.put("named_entities", tmp);
		}
		if (!question.getLanguageToNounPhrases().isEmpty()) {
			JSONArray tmp = new JSONArray();
			for (Entity key : question.getLanguageToNounPhrases().get("en")) {
				JSONObject tmpobj = new JSONObject();
				tmpobj.put("key", key.label);
				tmpobj.put("value", new JsonString(key.uris.get(0).getURI()));
				tmp.add(tmpobj);
			}
			document.put("combined_nouns", tmp);
		}
		if (question.getTree() != null) {
			Stack<MutableTreeNode> stack = new Stack<>();
			stack.push(question.getTree().getRoot());
			JSONArray tmp = new JSONArray();
			while (!stack.isEmpty()) {
				MutableTreeNode node = stack.pop();
				JSONObject tmpobj = new JSONObject();
				tmpobj.put("key", node.label);
				tmpobj.put("POS", node.posTag);
				tmp.add(tmpobj);
				for (MutableTreeNode child : node.children) {
					stack.push(child);
				}
			}
			document.put("POS_tags", tmp);
		}
		if (question.getTree_full() != null) {
			document.put("tree_full", question.getTree_full());
		}
		if (question.getTree_pruned() != null) {
			document.put("tree_pruned", question.getTree_pruned());
		}
		if (question.getTree_final() != null) {
			document.put("tree_final", question.getTree_final());
		}
		if (question.getTree() != null) {
			Stack<MutableTreeNode> stack = new Stack<>();
			stack.push(question.getTree().getRoot());
			JSONArray tmp = new JSONArray();
			while (!stack.isEmpty()) {
				MutableTreeNode node = stack.pop();
				if (!node.getAnnotations().isEmpty()) {
					JSONObject tmpobj = new JSONObject();
					tmpobj.put("label", node.label);
					JSONArray tmpArray = new JSONArray();
					tmpArray.addAll(node.getAnnotations());
					tmpobj.put("annotations", tmpArray);
					tmp.add(tmpobj);
				}
				for (MutableTreeNode child : node.children) {
					stack.push(child);
				}
			}
			document.put("annotation", tmp);
		}
		if (!question.getPruning_messages().isEmpty()) {
			document.put("pruning_messages", question.getPruning_messages());
		}
		if (question.getFinalAnswer() != null && !question.getFinalAnswer().isEmpty()) {
			document.put("final_sparql_base64",
					new String(Base64.encodeBase64(question.getFinalAnswer().get(0).queryString.getBytes())));
			JSONArray array = new JSONArray();
			for (RDFNode answer : question.getFinalAnswer().get(0).answerSet) {
				array.add(AnswerBox.buildAnswerBoxFeatures(answer.asResource().getURI()));
			}
			document.put("answer", array);
		}
		return document;
	}

	public static JSONObject treeToJSON(final MutableTree tree) {
		if (tree != null) {
			JSONObject document = recursiveNodeLabel(tree.getRoot(), new JSONObject());
			return document;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static JSONObject recursiveNodeLabel(final MutableTreeNode node, final JSONObject document) {
		document.put("label", node.label);
		if (!node.children.isEmpty()) {
			JSONArray tmp = new JSONArray();
			for (MutableTreeNode child : node.children) {
				JSONObject object = new JSONObject();
				tmp.add(recursiveNodeLabel(child, object));
			}
			document.put("children", tmp);
		}
		return document;
	}
}
