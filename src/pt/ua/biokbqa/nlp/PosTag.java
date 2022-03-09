package pt.ua.biokbqa.nlp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pt.ua.biokbqa.data.blueprint.Question;
import edu.stanford.nlp.pipeline.Annotation;
import pt.ua.biokbqa.benchmark.Dataset;
import pt.ua.biokbqa.benchmark.DataLoader;
import pt.ua.biokbqa.controller.StanfordNLPConnector;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.data.blueprint.KBQAQuestionFactory;

public class PosTag { // Using Stanford NLP or Core NLP

	static StanfordNLPConnector stanford;
	static List<KBQAQuestion> questionsStanford;
	static List<KBQAQuestion> questionsClear;

	public static void main(String[] args) {
		List<Question> loadedQuestions = DataLoader.load(Dataset.DATASET);
		questionsStanford = KBQAQuestionFactory.createInstances(loadedQuestions);
		questionsClear = KBQAQuestionFactory.createInstances(loadedQuestions);
		stanford = new StanfordNLPConnector();
		boolean testPass = true;
		StringBuilder outputStr = new StringBuilder();
		Map<String, String> mismatched = new HashMap<String, String>();
		Map<String, Integer> mismatchCnt = new HashMap<String, Integer>();
		Map<String, Integer> stanTotalCnt = new HashMap<String, Integer>();
		Map<String, Integer> coreTotalCnt = new HashMap<String, Integer>();
		for (KBQAQuestion currentQuestion : questionsStanford) {
			Map<String, String> core = SentenceToSequence.generatePOSTags(currentQuestion);
			Annotation doc = stanford.runAnnotation(currentQuestion);
			Map<String, String> stanPos = stanford.generatePOSTags(doc);
			for (Map.Entry<String, String> e : stanPos.entrySet()) {
				stanTotalCnt.putIfAbsent(e.getValue(), 0);
				stanTotalCnt.put(e.getValue(), stanTotalCnt.get(e.getValue()) + 1);
				String stanKey = e.getKey();
				String stanVal = e.getValue();
				String coreVal = core.get(stanKey);
				if (coreVal == null) {
					coreVal = "null";
				}
				if (stanVal == null) {
					stanVal = "null";
				}
				coreTotalCnt.putIfAbsent(coreVal, 0);
				coreTotalCnt.put(coreVal, coreTotalCnt.get(coreVal) + 1);
				if (!stanVal.equals(coreVal)) {
					mismatched.putIfAbsent(stanVal, coreVal);
					String mismatchLabel = stanVal + "-" + coreVal;
					mismatchCnt.putIfAbsent(mismatchLabel, 0);
					mismatchCnt.put(mismatchLabel, mismatchCnt.get(mismatchLabel) + 1);
					outputStr.append("Differing POS Tags for node '" + stanKey + "' (Stanf.|Core):" + stanVal + " | "
							+ coreVal + "\n\n" + "");
				}
			}
			if (!core.equals(stanPos)) {
				outputStr.append("CLEAR |  " + core.toString() + "\n");
				outputStr.append("STAN  |  " + stanPos.toString() + "\n");
				testPass = false;
			}
		}
		System.out.println(outputStr.toString());
		System.out.println("Discrepancies between POS-Tags (Stan = Clear):");
		System.out.println(mismatched.toString());
		System.out.println("Discrepancy count (Stan-Clear):");
		System.out.println(mismatchCnt.toString());
		System.out.println("Discrepancies/Total Occurrances of POS  in Stanford and CoreNLP");
		for (String mismatchedPOS : mismatchCnt.keySet()) {
			String stanKey = mismatchedPOS.split("-")[0];
			String coreKey = mismatchedPOS.split("-")[1];
			Integer totalStanOcc = stanTotalCnt.get(stanKey);
			Integer totalCoreOcc = coreTotalCnt.get(coreKey);
			Integer totalStanOcc2 = stanTotalCnt.get(coreKey);
			Integer totalCoreOcc2 = coreTotalCnt.get(stanKey);
			System.out.println("[" + mismatchedPOS + "]:" + mismatchCnt.get(mismatchedPOS).toString());
			System.out.println(
					"[" + stanKey + "]/[" + coreKey + "] (StanfordNLP): " + totalStanOcc + "/" + totalStanOcc2);
			System.out.println("[" + stanKey + "]/[" + coreKey + "] (CoreNLP): " + totalCoreOcc2 + "/" + totalCoreOcc);
		}
		System.out.println("testPass = " + testPass);
	}
}
