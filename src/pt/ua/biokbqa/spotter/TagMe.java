package pt.ua.biokbqa.spotter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
import pt.ua.biokbqa.conf.ConfLoader;
import pt.ua.biokbqa.data.blueprint.Entity;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.utils.URLs;

public class TagMe extends ASpotter {

	private String requestURL = URLs.TagMeURL;
	private String key = ConfLoader.tagmekey;
	private String lang = "en";
	private String include_all_spots = "true";
	private String include_categories = "true";

	private String doTASK(final String inputText) throws MalformedURLException, IOException, ProtocolException {
		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&key=" + key;
		urlParameters += "&lang=" + lang;
		urlParameters += "&include_all_spots=" + include_all_spots;
		urlParameters += "&include_categories=" + include_categories;
		return requestPOST(urlParameters, requestURL);
	}

	@Override
	public Map<String, List<Entity>> getEntities(final String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<>();
		try {
			String foxJSONOutput = doTASK(question);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(foxJSONOutput);
			JSONArray resources = (JSONArray) jsonObject.get("annotations");
			ArrayList<Entity> tmpList = new ArrayList<>();
			for (Object res : resources.toArray()) {
				JSONObject next = (JSONObject) res;
				Entity ent = new Entity();
				ent.label = ((String) next.get("spot"));
				ent.uris.add(new ResourceImpl(((String) next.get("title")).replaceAll(",", "%2C")));
				JSONArray types = (JSONArray) next.get("dbpedia_categories");
				if (types != null) {
					for (Object type : types) {
						ent.posTypesAndCategories.add(new ResourceImpl((String) type));
					}
				}
				tmpList.add(ent);
			}
			String baseURI = "http://dbpedia.org/resource/";
			for (Entity entity : tmpList) {
				Resource resource = entity.uris.get(0);
				if (resource.getURI() != null) {
					ResourceImpl e = new ResourceImpl(baseURI + resource.getURI().replace(" ", "_"));
					entity.uris.add(e);
					entity.uris.remove(0);
				}
			}
			tmp.put("en", tmpList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmp;
	}

	public static void main(final String args[]) {
		KBQAQuestion q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en", "Which buildings in art deco style did Shreve, Lamb and Harmon design?");
		ASpotter fox = new TagMe();
		q.setLanguageToNamedEntites(fox.getEntities(q.getLanguageToQuestion().get("en")));
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
