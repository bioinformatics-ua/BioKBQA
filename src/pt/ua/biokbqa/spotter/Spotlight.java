package pt.ua.biokbqa.spotter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pt.ua.biokbqa.data.blueprint.Entity;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.utils.URLs;

public class Spotlight extends ASpotter {
	private String requestURL = URLs.SpotlightURL;
	private String confidence = "0.65";
	private String support = "20";

	private String doTASK(final String inputText) throws Exception {
		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&confidence=" + confidence;
		urlParameters += "&support=" + support;
		return requestPOST(urlParameters, requestURL);
	}

	@Override
	public Map<String, List<Entity>> getEntities(final String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<>();
		try {
			String foxJSONOutput = doTASK(question);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(foxJSONOutput);
			JSONArray resources = (JSONArray) jsonObject.get("Resources");
			if (resources != null) {
				ArrayList<Entity> tmpList = new ArrayList<>();
				for (Object res : resources.toArray()) {
					JSONObject next = (JSONObject) res;
					Entity ent = new Entity();
					// ent.setOffset(Integer.valueOf((String)
					// next.get("@offset")));
					ent.label = (String) next.get("@surfaceForm");
					String uri = ((String) next.get("@URI")).replaceAll(",", "%2C");
					ent.uris.add(new ResourceImpl(uri));
					for (String type : ((String) next.get("@types")).split(",")) {
						ent.posTypesAndCategories.add(new ResourceImpl(type));
					}
					tmpList.add(ent);
				}
				tmp.put("en", tmpList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmp;
	}

	public String getConfidence() {
		return confidence;
	}

	public void setConfidence(double i) {
		this.confidence = String.valueOf(i);
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}

	public static void main(final String args[]) {
		KBQAQuestion q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en",
				"Who was vice president under the president who approved the use of atomic weapons against Japan during World War II?");
		ASpotter spotter = new Spotlight();
		for (double i = 0; i <= 1.0; i += 0.05) {
			((Spotlight) spotter).setConfidence(i);
			System.out.println("Confidence: " + ((Spotlight) spotter).getConfidence());
			q.setLanguageToNamedEntites(spotter.getEntities(q.getLanguageToQuestion().get("en")));
			for (String key : q.getLanguageToNamedEntites().keySet()) {
				System.out.println(key);
				for (Entity entity : q.getLanguageToNamedEntites().get(key)) {
					System.out.println("\t" + entity.label + " ->" + entity.type);
					for (Resource r : entity.posTypesAndCategories) {
						System.out.println("\t\tpos: " + r);
					}
					for (Resource r : entity.uris) {
						System.out.println("\t\turi: " + r);
					}
				}
			}
		}
	}
}
