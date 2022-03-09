package pt.ua.biokbqa.data.blueprint;

import java.util.Set;
import org.apache.jena.rdf.model.RDFNode;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class Answer implements Comparable<Answer> {
	public Set<RDFNode> answerSet;
	public SPARQLQuery query;
	public Double score = 1.0;
	public String question;
	public Integer question_id;
	public String queryString;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answerSet == null) ? 0 : answerSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Answer other = (Answer) obj;
		if (answerSet == null) {
			if (other.answerSet != null)
				return false;
		} else if (!answerSet.equals(other.answerSet))
			return false;
		return true;
	}

	@Override
	public int compareTo(final Answer a) {
		return Double.compare(this.score, a.score);
	}

	@Override
	public String toString() {
		return "Answer [answerSet=" + answerSet + ", score=" + score + ", queryString=" + queryString + "]";
	}

	public SPARQLQuery toSPARQLQuery() {
		return null;
	}
}