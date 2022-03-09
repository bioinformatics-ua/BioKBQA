package pt.ua.biokbqa.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import pt.ua.biokbqa.benchmark.Evaluation;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;

public abstract class AbstractPipeline {
	abstract public List<Answer> getAnswersToQuestion(KBQAQuestion q);

	protected static void write(Set<Evaluation> evals) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("results.html"));
			bw.write("<script src=\"sorttable.js\"></script><table class=\"sortable\">");
			bw.newLine();
			bw.write(
					" <tr>     <th>id</th><th>Question</th><th>F-measure</th><th>Precision</th><th>Recall</th><th>Comment</th>  </tr>");
			for (Evaluation eval : evals) {
				bw.write(" <tr>    <td>" + eval.getId() + "</td><td>" + eval.getQuestion() + "</td><td>"
						+ eval.getFmax() + "</td><td>" + eval.getPmax() + "</td><td>" + eval.getRmax() + "</td><td>"
						+ eval.getComment() + "</td>  </tr>");
				bw.newLine();
			}
			bw.write("</table>");
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
