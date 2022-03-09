package pt.ua.biokbqa.benchmark;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pt.ua.biokbqa.data.blueprint.Question;

public class DataLoader {

	private static InputStream getInputStream(final Dataset set) {
		try {
			URL url = mapDatasetToPath(set);
			return url.openStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static URL mapDatasetToPath(final Dataset set) {
		Class<?> loadingAnchor = null;
		try {
			loadingAnchor = Class.forName("pt.ua.biokbqa.benchmark.DataLoader");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		switch (set) {
		case DATASET:
			return loadingAnchor.getResource("/resources/dataset.json");
		default:
			break;

		}
		return null;
	}

	@SuppressWarnings({ "unused" })
	public static List<Question> load(final Dataset data) {
		try {
			InputStream is = null;
			is = getInputStream(data);
			if (is == null) {
				return null;
			}
			List<Question> out = null;
			if (is.available() > 0) {
				List<Question> hybrid;
				List<Question> loadedQ;
				switch (data) {
				case DATASET:
					out = loadJSON(is);
					break;
				default:
					break;
				}
				is.close();
				return out;
			} else {
				throw new Exception();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<Question> loadXML(final InputStream file) {
		List<Question> questions = new ArrayList<>();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc;
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			for (int i = 0; i < questionNodes.getLength(); i++) {
				Question question = new Question();
				Element questionNode = (Element) questionNodes.item(i);
				question.setId(Integer.valueOf(questionNode.getAttribute("id")));
				question.setAnswerType(questionNode.getAttribute("answertype"));
				question.setAggregation(Boolean.valueOf(questionNode.getAttribute("aggregation")));
				question.setOnlydbo(Boolean.valueOf(questionNode.getAttribute("onlydbo")));
				// question.setHybrid(Boolean.valueOf(questionNode.getAttribute("hybrid")));
				NodeList nlrs = questionNode.getElementsByTagName("string");
				for (int j = 0; j < nlrs.getLength(); j++) {
					String lang = ((Element) nlrs.item(j)).getAttribute("lang");
					question.getLanguageToQuestion().put(lang, ((Element) nlrs.item(j)).getTextContent().trim());
				}
				NodeList keywords = questionNode.getElementsByTagName("keywords");
				for (int j = 0; j < keywords.getLength(); j++) {
					String lang = ((Element) keywords.item(j)).getAttribute("lang");
					question.getLanguageToKeywords().put(lang,
							Arrays.asList(((Element) keywords.item(j)).getTextContent().trim().split(", ")));
				}
				Element element = (Element) questionNode.getElementsByTagName("pseudoquery").item(0);
				if (element != null && element.hasChildNodes()) {
					NodeList childNodes = element.getChildNodes();
					Node item = childNodes.item(0);
					question.setPseudoSparqlQuery(item.getNodeValue().trim());
				}
				element = (Element) questionNode.getElementsByTagName("query").item(0);
				if (element != null && element.hasChildNodes()) {
					NodeList childNodes = element.getChildNodes();
					Node item = childNodes.item(0);
					question.setSparqlQuery(item.getNodeValue().trim());
				}
				if (question.getPseudoSparqlQuery() != null) {
					question.setOutOfScope(question.getPseudoSparqlQuery().toUpperCase().contains("OUT OF SCOPE"));
				}
				if (question.getSparqlQuery() != null) {
					question.setOutOfScope(question.getSparqlQuery().toUpperCase().contains("OUT OF SCOPE"));
				}
				NodeList answers = questionNode.getElementsByTagName("answers");
				HashSet<String> set = new HashSet<>();
				for (int j = 0; j < answers.getLength(); j++) {
					NodeList answer = ((Element) answers.item(j)).getElementsByTagName("answer");
					for (int k = 0; k < answer.getLength(); k++) {
						String answerString = ((Element) answer.item(k)).getTextContent();
						set.add(answerString.trim());
					}
				}
				question.setGoldenAnswers(set);
				questions.add(question);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return questions;
	}

	public static List<Question> loadJSON(final InputStream file) {
		List<Question> output = new ArrayList<>();
		try {
			JsonReader jsonReader = Json.createReader(file);
			JsonObject mainJsonObject = jsonReader.readObject();
			JsonArray jArray = mainJsonObject.getJsonArray("questions");
			String attributes[] = { "id", "aggregation", "answertype", "onlydbo", "hybrid" };
			for (JsonValue questionJsonObj : jArray) {
				JsonObject listObj = (JsonObject) questionJsonObj;
				Question q = new Question();
				for (String attr : attributes) {
					if (listObj.containsKey(attr)) {
						String val = listObj.get(attr).toString().replace("\"", "");
						q.setValue(attr, val);
					}
				}
				output.add(q);
				JsonArray questionArray = listObj.getJsonArray("question");
				for (JsonValue questionVal : questionArray) {
					JsonObject questionObj = (JsonObject) questionVal;
					String lang = questionObj.getString("language");
					q.getLanguageToQuestion().put(lang, questionObj.getString("string").trim());
					if (questionObj.containsKey("keywords")) {
						List<String> keywords = Arrays.asList(questionObj.getString("keywords").split(","));
						q.getLanguageToKeywords().put(lang, keywords);
					}
				}
				JsonObject query = (JsonObject) listObj.get("query");
				if (query.containsKey("sparql")) {
					String strQuery = query.getString("sparql").trim();
					q.setSparqlQuery(strQuery);
				}
				if (query.containsKey("pseudo")) {
					String strQuery = query.getString("pseudo").trim();
					q.setPseudoSparqlQuery(strQuery);
				}
				JsonArray answerList = listObj.getJsonArray("answers");
				if (!answerList.isEmpty()) {
					JsonObject answerListHead = answerList.getJsonObject(0);
					JsonObject headObject = answerListHead.getJsonObject("head");
					JsonArray vars = headObject.getJsonArray("vars");
					Set<String> answers = new HashSet<>();
					if (!answerList.isEmpty()) {
						JsonObject answerObject = answerList.getJsonObject(0);
						if (answerObject.containsKey("boolean")) {
							answers.add(answerObject.get("boolean").toString());
						}
						if (answerObject.containsKey("results")) {
							JsonObject resultObject = answerObject.getJsonObject("results");
							JsonArray bindingsList = resultObject.getJsonArray("bindings");
							for (JsonValue bind : bindingsList) {
								JsonObject bindObj = (JsonObject) bind;
								for (JsonValue varName : vars) {
									String var = varName.toString().replaceAll("\"", "");
									if (bindObj.containsKey(var)) {
										JsonObject j = bindObj.getJsonObject(var);
										answers.add(j.getString("value").trim());
									}
								}
							}
						}
						q.setGoldenAnswers(answers);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean printHappend = false;
		String message = "";
		List<Question> emptyQuestions = new ArrayList<>();
		for (Question k : output) {
			if (k.getGoldenAnswers().isEmpty()) {
				emptyQuestions.add(k);
				if (!printHappend) {
					message += "Following Questions (id) have no attached answers: ";
					printHappend = true;
				}
				message += k.getId() + ", ";
			}
		}
		if (printHappend) {
			System.out.println(message + " and will be removed");
		}
		output.removeAll(emptyQuestions);
		return output;
	}

	public static List<Question> loadNLQ(final InputStream file) {
		List<Question> output = new ArrayList<>();
		HashMap<Integer, ArrayList<JsonObject>> idToQuestion = new HashMap<>();
		try {
			if (file.available() > 0) {
				JsonReader jsonReader = Json.createReader(file);
				JsonArray mainJsonArray = jsonReader.readArray();
				for (JsonValue currentJsonValue : mainJsonArray) {
					JsonObject currentObject = (JsonObject) currentJsonValue;
					try {
						Integer id = Integer.parseInt(currentObject.getString("id"));
						if (idToQuestion.containsKey(id)) {
							idToQuestion.get(id).add(currentObject);
						} else {
							ArrayList<JsonObject> jArray = new ArrayList<>();
							jArray.add(currentObject);
							idToQuestion.put(id, jArray);
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Integer i : idToQuestion.keySet()) {
			Question q = new Question();
			for (JsonObject currentJsonObject : idToQuestion.get(i)) {
				q.setValue("id", currentJsonObject.getString("id"));
				String lang = currentJsonObject.getString("lang");
				String questiion = currentJsonObject.getString("question");
				String answer = currentJsonObject.getString("answer");
				String sparql = currentJsonObject.getString("sparql");
				q.getLanguageToQuestion().put(lang, questiion);
				q.setSparqlQuery(lang, sparql);
				Set<String> answ = new HashSet<>();
				answ.add(answer);
				q.setGoldenAnswers(lang, answ);
			}
			output.add(q);
		}
		return output;
	}
}
