package pt.ua.biokbqa.data.blueprint;

import java.util.ArrayList;
import java.util.List;
import pt.ua.biokbqa.benchmark.Evaluator;

public class KBQAQuestionFactory {

	public static KBQAQuestion createInstance(Question q) {
		KBQAQuestion hq = new KBQAQuestion();
		hq.setId(q.getId());
		hq.setAnswerType(q.getAnswerType());
		hq.setPseudoSparqlQuery(q.getPseudoSparqlQuery());
		hq.setSparqlQuery(q.getSparqlQuery());
		hq.setAggregation(Boolean.TRUE.equals(q.getAggregation()));
		hq.setOnlydbo(Boolean.TRUE.equals(q.getOnlydbo()));
		hq.setOutOfScope(Boolean.TRUE.equals(q.getOutOfScope()));
		// hq.setHybrid(Boolean.TRUE.equals(q.getHybrid()));
		boolean b = Evaluator.isAskType(q.getSparqlQuery());
		b |= Evaluator.isAskType(q.getPseudoSparqlQuery());
		hq.setLoadedAsASKQuery(b);
		hq.setLanguageToQuestion(q.getLanguageToQuestion());
		hq.setLanguageToKeywords(q.getLanguageToKeywords());
		hq.setGoldenAnswers(q.getGoldenAnswers());
		return hq;
	}

	public static List<KBQAQuestion> createInstances(List<Question> qList) {
		ArrayList<KBQAQuestion> hq = new ArrayList<KBQAQuestion>();
		for (Question q : qList) {
			hq.add(KBQAQuestionFactory.createInstance(q));
		}
		return hq;
	}
}
