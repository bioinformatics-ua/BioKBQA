package pt.ua.biokbqa.questionprocessor;

import java.util.List;
import pt.ua.biokbqa.controller.AbstractPipeline;
import pt.ua.biokbqa.controller.PipelineStanford;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.ranking.BucketRanker;

public class QueryExecutor {
	private AbstractPipeline pipeline = new PipelineStanford();

	public String runPipelineSingleAnswer(String question) {
		KBQAQuestion q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en", question);
		// System.out.println("---------- 1:\n" + q.toString());
		List<Answer> answers = pipeline.getAnswersToQuestion(q);
		// System.out.println("---------- 2:\n" + answers.size());
		BucketRanker bucket_ranker = new BucketRanker();
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		q.setFinalAnswer(rankedAnswer);
		// System.out.println("---------- 3:\n" + q.toString());
		return q.toString();
	}

	public List<Answer> runPipeline(String question) {
		AbstractPipeline pipeline = new PipelineStanford();
		KBQAQuestion q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en", question);
		List<Answer> answers = pipeline.getAnswersToQuestion(q);
		BucketRanker bucket_ranker = new BucketRanker();
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		q.setFinalAnswer(rankedAnswer);
		return rankedAnswer;
	}

	public static void main(String args[]) {
		String question;
		question = "Who killed John Lennon?";
		// question = "Who is the youngest Pulitzer Prize winner?";
		// question = "Where was the assassin of Martin Luther King born?";
		// question = "Which anti-apartheid activist was born in Mvezo?";
		// question = "";
		QueryExecutor queryExecutor = new QueryExecutor();
		// String ans = queryExecutor.runPipelineSingleAnswer(question);
		// System.out.println(ans);
		List<Answer> answer = queryExecutor.runPipeline(question);
		System.out.println(answer);
	}
}
