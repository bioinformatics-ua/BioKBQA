package pt.ua.biokbqa.controller;

import java.util.List;
import pt.ua.biokbqa.benchmark.DataLoader;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.data.blueprint.KBQAQuestionFactory;
import pt.ua.biokbqa.benchmark.Dataset;

public class QueryTypeClassifier {

	public Boolean isASKQuery(String question) {
		return question.startsWith("Are ") || question.startsWith("Did ") || question.startsWith("Do ")
				|| question.startsWith("Does ") || question.startsWith("Is ") || question.startsWith("Was ");
	}

	@SuppressWarnings("static-access")
	public static void main(String args[]) {
		System.out.println("Test QueryType classification ...");
		DataLoader datasetLoader = new DataLoader();
		QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();
		System.out.println("Run queries through components ...");
		for (Dataset d : Dataset.values()) {
			System.out.println("Load data file: " + d);
			List<KBQAQuestion> questions = KBQAQuestionFactory.createInstances(datasetLoader.load(d));
			int counter = 0;
			int counterASK = 0;
			int counterClassifiedWrong = 0;
			for (KBQAQuestion q : questions) {
				// classify query type
				q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
				System.out.println("Question ID=" + q.getId() + ": isASK=" + q.getIsClassifiedAsASKQuery() + " - "
						+ q.getLanguageToQuestion().get("en"));
				if (q.getIsClassifiedAsASKQuery()) {
					++counterASK;
				}
				++counter;
				if (q.getIsClassifiedAsASKQuery().booleanValue() != q.getLoadedAsASKQuery().booleanValue()) {
					System.out.println("Expected ASK query classification: " + q.getLoadedAsASKQuery() + ", got: "
							+ q.getIsClassifiedAsASKQuery() + ", for: " + q.getLanguageToQuestion().get("en"));
					++counterClassifiedWrong;
				}
			}
			System.out.println("Classified " + counterClassifiedWrong + " wrong from " + counter + " queries. ("
					+ counterASK + " are ASK)");
		}
	}
}
