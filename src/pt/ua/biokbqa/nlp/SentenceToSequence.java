package pt.ua.biokbqa.nlp;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pt.ua.biokbqa.data.blueprint.Entity;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;

public class SentenceToSequence {
	private static String language = AbstractReader.LANG_EN;
	static AbstractTokenizer tokenizer = NLPGetter.getTokenizer(language);

	public static void combineSequences(final KBQAQuestion q) {
		String sentence = q.getLanguageToQuestion().get("en");
		List<String> tokens = tokenizer.getTokens(sentence);
		System.out.println(tokens.toString());
		Map<String, String> label2pos = generatePOSTags(q);
		runPhraseCombination(q, tokens, label2pos);
	}

	public static Map<String, String> generatePOSTags(final KBQAQuestion q) {
		ParseTree parse = new ParseTree();
		DEPTree tree = parse.process(q);
		Map<String, String> label2pos = Maps.newHashMap();
		Stack<DEPNode> stack = new Stack<>();
		stack.push(tree.getFirstRoot());
		while (!stack.isEmpty()) {
			DEPNode tmp = stack.pop();
			label2pos.put(tmp.form, tmp.pos);
			for (DEPNode child : tmp.getDependentNodeList()) {
				stack.push(child);
			}
		}
		return label2pos;
	}

	public static void runPhraseCombination(final KBQAQuestion q, final List<String> tokens,
			final Map<String, String> label2pos) {
		List<String> subsequence = Lists.newArrayList();
		for (int tcounter = 0; tcounter < tokens.size(); tcounter++) {
			String token = tokens.get(tcounter);
			String pos = label2pos.get(token);
			String nextPos = tcounter + 1 == tokens.size() ? null : label2pos.get(tokens.get(tcounter + 1));
			String lastPos = tcounter == 0 ? null : label2pos.get(tokens.get(tcounter - 1));
			if (subsequence.isEmpty() && null != pos && pos.matches("CD|JJ|NN(.)*|RB(.)*")) {
				subsequence.add(token);
			} else if (!subsequence.isEmpty() && null != pos && tcounter + 1 < tokens.size() && null != nextPos
					&& pos.matches("IN") && !token.matches("of") && nextPos.matches("(W)?DT|NNP(S)?")) {
				if (subsequence.size() > 1) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			} else if (!subsequence.isEmpty() && null != pos && null != lastPos && lastPos.matches("NNS")
					&& pos.matches("NNP(S)?")) {
				if (subsequence.size() > 2) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			} else if (!subsequence.isEmpty() && !lastPos.matches("JJ|HYPH")
					&& (null == pos || pos.matches("VB(.)*|\\.|WDT") || (pos.matches("IN") && nextPos == null)
							|| (pos.matches("IN") && nextPos.matches("DT")))) {
				if (subsequence.size() > 1) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			} else if (!subsequence.isEmpty() && null != pos && pos.matches("NN(.)*|RB|CD|CC|JJ|DT|IN|PRP|HYPH|VBN")) {
				subsequence.add(token);
			} else {
				subsequence = Lists.newArrayList();
			}
		}
	}

	public static void transformTree(final List<String> subsequence, final KBQAQuestion q) {
		String combinedNN = Joiner.on(" ").join(subsequence);
		String combinedURI = "http://aksw.org/combinedNN/" + Joiner.on("_").join(subsequence);
		Entity tmpEntity = new Entity();
		tmpEntity.label = combinedNN;
		tmpEntity.uris.add(new ResourceImpl(combinedURI));
		List<Entity> nounphrases = q.getLanguageToNounPhrases().get("en");
		if (null == nounphrases) {
			nounphrases = Lists.newArrayList();
		}
		nounphrases.add(tmpEntity);
		q.getLanguageToNounPhrases().put("en", nounphrases);
	}

	public static void main(final String args[]) {
		KBQAQuestion q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en",
				"Who was vice-president under the president who authorized atomic weapons against Japan during World War II?");
		SentenceToSequence.combineSequences(q);
		System.out.println(q.getLanguageToNounPhrases().get("en"));
		q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en",
				"Who plays Phileas Fogg in the adaptation of Around the World in 80 Days directed by Buzz Kulik?");
		SentenceToSequence.combineSequences(q);
		System.out.println(q.getLanguageToNounPhrases().get("en"));
		q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en", "Which recipients of the Victoria Cross died in the Battle of Arnhem?");
		SentenceToSequence.combineSequences(q);
		System.out.println("LANGUAGETONOUNPHRASES");
		System.out.println(q.getLanguageToNounPhrases().get("en"));
	}
}
