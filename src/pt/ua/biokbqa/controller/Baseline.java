package pt.ua.biokbqa.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.rdf.model.RDFNode;
import com.google.common.collect.Maps;
import pt.ua.biokbqa.benchmark.Evaluator;
import pt.ua.biokbqa.benchmark.DataLoader;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.data.blueprint.KBQAQuestionFactory;
import pt.ua.biokbqa.benchmark.Dataset;

public class Baseline {
	String dataset;

	void run(final Dataset dataset) throws IOException {
		List<KBQAQuestion> questions = KBQAQuestionFactory.createInstances(DataLoader.load(dataset));
		double overallf = 0;
		double overallp = 0;
		double overallr = 0;
		double counter = 0;
		for (KBQAQuestion q : questions) {
			if (q.getAnswerType().equals("resource")) {
				if (q.getOnlydbo()) {
					if (!q.getAggregation()) {
						Map<String, Set<RDFNode>> answer = calculateSPARQLRepresentation(q);
						double fmax = 0;
						double pmax = 0;
						double rmax = 0;
						for (String query : answer.keySet()) {
							Set<RDFNode> systemAnswers = answer.get(query);
							double precision = Evaluator.precision(systemAnswers, q);
							double recall = Evaluator.recall(systemAnswers, q);
							double fMeasure = Evaluator.fMeasure(systemAnswers, q);
							if (fMeasure > fmax) {
								System.out.println(query.substring(0, Math.min(1000, query.length())));
								System.out.println("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
								fmax = fMeasure;
								pmax = precision;
								rmax = recall;
							}
						}
						overallf += fmax;
						overallp += pmax;
						overallr += rmax;
						counter++;
					}
				}
			}
		}
		System.out.println("Average P=" + overallp / counter + " R=" + overallr / counter + " F=" + overallf / counter
				+ " Counter=" + counter);
	}

	public Map<String, Set<RDFNode>> calculateSPARQLRepresentation(final KBQAQuestion q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		return answer;
	}
}
