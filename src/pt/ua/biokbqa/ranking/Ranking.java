package pt.ua.biokbqa.ranking;

import java.util.List;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;

public interface Ranking {
	public List<Answer> rank(List<Answer> answers, KBQAQuestion q);
}
