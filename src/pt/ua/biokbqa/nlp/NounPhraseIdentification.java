package pt.ua.biokbqa.nlp;

import java.util.List;
import java.util.Map;
import com.google.common.base.Joiner;
import pt.ua.biokbqa.benchmark.Dataset;
import pt.ua.biokbqa.benchmark.DataLoader;
import pt.ua.biokbqa.controller.StanfordNLPConnector;
import pt.ua.biokbqa.data.blueprint.Entity;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.data.blueprint.KBQAQuestionFactory;
import pt.ua.biokbqa.data.blueprint.Question;
import pt.ua.biokbqa.spotter.Spotlight;

public class NounPhraseIdentification {

	public static void main(String[] args) {
		Spotlight nerdModule = new Spotlight();
		List<Question> loadedQuestions = DataLoader.load(Dataset.DATASET);
		List<KBQAQuestion> questionsStanford = KBQAQuestionFactory.createInstances(loadedQuestions);
		StanfordNLPConnector connector = new StanfordNLPConnector();
		for (KBQAQuestion currentQuestion : questionsStanford) {
			System.out.println(currentQuestion.getLanguageToQuestion().get("en"));
			currentQuestion.setLanguageToNamedEntites(
					nerdModule.getEntities(currentQuestion.getLanguageToQuestion().get("en")));
			connector.combineSequences(currentQuestion);
			Map<String, List<Entity>> languageToNounPhrases = currentQuestion.getLanguageToNounPhrases();
			if (languageToNounPhrases != null && !languageToNounPhrases.isEmpty()) {
				System.out.println("Stanford:" + Joiner.on(", ").skipNulls().join(languageToNounPhrases.get("en")));
			}
		}
		List<KBQAQuestion> questionsClear = KBQAQuestionFactory.createInstances(loadedQuestions);
		for (KBQAQuestion currentQuestion : questionsClear) {
			System.out.println(currentQuestion.getLanguageToQuestion().get("en"));
			currentQuestion.setLanguageToNamedEntites(
					nerdModule.getEntities(currentQuestion.getLanguageToQuestion().get("en")));
			SentenceToSequence.combineSequences(currentQuestion);
			Map<String, List<Entity>> languageToNounPhrases = currentQuestion.getLanguageToNounPhrases();
			if (languageToNounPhrases != null && !languageToNounPhrases.isEmpty()) {
				System.out.println("Clear:" + Joiner.on(", ").skipNulls().join(languageToNounPhrases.get("en")));
			}
		}
	}
}
