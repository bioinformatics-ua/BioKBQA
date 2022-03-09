package pt.ua.biokbqa.nlp;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import com.google.common.base.Joiner;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import pt.ua.biokbqa.controller.StanfordNLPConnector;

public class UnitEnglish implements IUnitLanguage {
	HashMap<String, ImmutablePair<Double, String>> identifierToUnit;
	HashMap<String, Double> identifierToMultiplier;
	StanfordNLPConnector stanford;

	public UnitEnglish(final StanfordNLPConnector stanford) {
		this.stanford = stanford;
		identifierToMultiplier = new HashMap<>();
		identifierToUnit = new HashMap<>();
		loadResource();
	}

	@SuppressWarnings("unused")
	@Override
	public String convert(final String q) {
		String out = "";
		if (q == null || q.isEmpty()) {
			return out;
		}
		if (q.toLowerCase().matches("(.*)(\\s)(one)([\\p{Punct}\\s])(.*)")) {
			Annotation document = stanford.runAnnotation(q);
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			CoreMap sentenc = sentences.get(0);
			SemanticGraph dependencies = sentenc.get(CollapsedCCProcessedDependenciesAnnotation.class);
			ArrayList<String> parseForNumber = new ArrayList<>();
			ArrayList<String> outArray = new ArrayList<>();
			boolean lastWasNumber = false;
			boolean numberContainsNmod = false;
			String stOut = "";
			for (CoreLabel token : sentenc.get(TokensAnnotation.class)) {
				String ner = token.get(NamedEntityTagAnnotation.class);
				if (ner.toLowerCase().matches("number|duration|currency")) {
					lastWasNumber = true;
					IndexedWord depWord = dependencies.getNodeByIndex(token.index());
					List<SemanticGraphEdge> depEdges = dependencies.getIncomingEdgesSorted(depWord);
					numberContainsNmod = false;
					for (SemanticGraphEdge it : depEdges) {
						if (it.getRelation().getShortName().toLowerCase().equals("nmod")) {
							numberContainsNmod = true;
							break;
						}
					}
					outArray.add(token.word());
				} else {
					if (lastWasNumber && !numberContainsNmod) {
						outArray.add(convertToBaseUnit(replaceNumerals(Joiner.on(" ").join(parseForNumber))));
						outArray.add(token.word());
						parseForNumber = new ArrayList<>();
					} else {
						outArray.addAll(parseForNumber);
						outArray.add(token.word());
					}
					lastWasNumber = false;
				}
			}
			if (lastWasNumber && numberContainsNmod) {
				outArray.addAll(parseForNumber);
			}
			if (lastWasNumber && !numberContainsNmod) {
				outArray.add(replaceNumerals(Joiner.on(" ").join(parseForNumber)));
			}
			return Joiner.on(" ").join(outArray).replaceAll("(\\s+)(\\p{Punct})(\\s*)$", "$2");

		} else {
			return convertToBaseUnit(replaceNumerals(q)).trim();
		}
	}

	private String replaceNumerals(final String replaceThis) {
		if (replaceThis == null || replaceThis.isEmpty()) {
			return "";
		}
		System.out.println("Replacing numerals on :" + replaceThis);
		String out = "";
		Double val = 1.0;
		Double lastNumber = 0.0;
		String withWhitespace = this.insertWhitespacebeforePunctuation(replaceThis);
		withWhitespace = withWhitespace.replaceAll("(\\p{Sc})(\\d+)", "$1 $2");
		ArrayList<String> split = new ArrayList<>(Arrays.asList(withWhitespace.split(" ")));
		ArrayList<String> cleaned = split;
		boolean lastWasNumber = false;
		for (int i = 0; i < cleaned.size(); i++) {
			String it = cleaned.get(i);
			Double parsedNumber = parseWord(it);
			if (parsedNumber == null && !lastWasNumber) {
				out += it + " ";
				continue;
			}
			if (parsedNumber == null && lastWasNumber) {
				if (it.equals("and")) {
					continue;
				}
				lastWasNumber = false;
				out = prettyAppendDouble(out, val) + it + " ";
				continue;
			}
			if (parsedNumber != null) {
				if (lastWasNumber) {
					if (parsedNumber < lastNumber) {
						val += parsedNumber;
					} else {
						val *= parsedNumber;
					}
					lastNumber = parsedNumber;

				} else {
					val = parsedNumber;
					lastNumber = parsedNumber;
					lastWasNumber = true;
				}
				if (i == cleaned.size() - 1) {
					out = prettyAppendDouble(out, val);
				}
			}
		}
		return out.trim();
	}

	private String convertToBaseUnit(final String str) {
		String out = "";
		if (str == null || str.isEmpty()) {
			return out;
		}
		System.out.println("converting base units for: " + str);
		String withWhitespace = this.insertWhitespacebeforePunctuation(str);
		ArrayList<String> split = new ArrayList<>(Arrays.asList(withWhitespace.split(" ")));
		Double lastIt = null;
		for (String it : split) {
			if (lastIt != null) {
				if (identifierToUnit.containsKey(it)) {
					out += lastIt * identifierToUnit.get(it).getLeft() + " " + identifierToUnit.get(it).getRight()
							+ " ";
				} else {
					out = prettyAppendDouble(out, lastIt) + it + " ";
				}
				lastIt = null;
			} else {
				try {
					lastIt = Double.parseDouble(it);
				} catch (NumberFormatException e) {
					out += it + " ";
				}
			}
		}
		if (lastIt != null) {
			out = prettyAppendDouble(out, lastIt);
		}
		return out.replaceAll("(\\s+)(\\p{Punct})(\\s*)$", "$2");
	}

	private String insertWhitespacebeforePunctuation(final String input) {
		return input.replaceAll("(\\w)([\\.?!])(\\s*)$", "$1 $2");
	}

	private String prettyAppendDouble(final String out, final Double val) {
		if ((val == Math.floor(val)) && !Double.isInfinite(val)) {
			return out + val.intValue() + " ";

		} else {
			DecimalFormat df = new DecimalFormat("#");
			return out + df.format(val);
		}
	}

	private Double parseWord(final String s) {
		Double out = null;
		try {
			out = Double.parseDouble(s);
		} catch (NumberFormatException e) {
			out = identifierToMultiplier.get(s);
		}
		return out;
	}

	@SuppressWarnings("unused")
	private void loadResource() {
		System.out.println("Loading number conversion rules for english");
		final ClassLoader classLoader = getClass().getClassLoader();
		java.nio.file.Path currentRelativePath = java.nio.file.Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + s);
		System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		final File file = new File("resources/units.txt");
		List<List<String>> data = UnitController.loadTabSplit(file);
		if (data == null || data.isEmpty()) {
			return;
		}
		for (List<String> line : data) {
			try {
				if (line.size() == 3) {
					identifierToUnit.put(line.get(0), new ImmutablePair<>(new Double(line.get(1)), line.get(2)));
				} else {
					identifierToMultiplier.put(line.get(0), new Double(line.get(1)));
				}

			} catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e) {
				System.out
						.println("Could not parse line " + data.indexOf(line) + "from file " + file.getAbsolutePath());
			}
		}
	}

	public static void main(final String[] args) {
		UnitEnglish ue = new UnitEnglish(null);
		String q = "€60 thousand and $45";
		System.out.println("Start conversion");
		System.out.println(ue.convert(q));
	}
}
