package pt.ua.biokbqa.ranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.rdf.model.RDFNode;
import com.google.common.collect.Maps;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.data.blueprint.Question;
import pt.ua.biokbqa.questionprocessor.SPARQLQuery;

public class FeatureBasedRanker implements Ranking {
	public enum Feature {
		PREDICATES, PATTERN, NR_OF_CONSTRAINTS, NR_OF_TYPES, NR_OF_TERMS
	}
	private FeatureBasedRankerDB db = new FeatureBasedRankerDB();
	private Map<String, Double> vec;
	private Collection<Feature> features;

	public void learn(final Question q, final Set<SPARQLQuery> queries) {
		db.store(q, queries);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Answer> rank(final List<Answer> answers, final KBQAQuestion q) {
		Map<Answer, Double> buckets = Maps.newHashMap();
		for (Answer answer : answers) {
			Map<String, Double> calculateRanking = calculateRanking(answer.query);
			double distance = cosinus(calculateRanking, vec);
			answer.score = distance;
			buckets.put(answer, answer.score);
		}
		List tmplist = new LinkedList(buckets.entrySet());
		Collections.sort(tmplist, new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		List list = new ArrayList<Set<RDFNode>>();
		for (Iterator it = tmplist.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			list.add(entry.getKey());
		}
		return list;
	}

	public void setFeatures(final Collection<Feature> features) {
		this.features = features;
	}

	public void train() {
		Set<SPARQLQuery> queries = db.readRankings();
		this.vec = Maps.newHashMap();
		for (SPARQLQuery q : queries) {
			Map<String, Double> tmp = calculateRanking(q);
			for (String key : tmp.keySet()) {
				System.out.println(key);
				if (vec.containsKey(key)) {
					vec.put(key, tmp.get(key) + vec.get(key));
				} else {
					vec.put(key, tmp.get(key));
				}
			}
		}
		for (String key : vec.keySet()) {
			vec.put(key, vec.get(key) / queries.size());
		}

	}

	private void addOneToMapAtKey(final Map<String, Double> map, final String key) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + 1.0);
		} else {
			map.put(key, 1.0);
		}
	}

	private Map<String, Double> calculateRanking(final SPARQLQuery q) {
		Collections.sort(q.constraintTriples);
		Map<String, Double> featureValues = Maps.newHashMap();
		System.out.println("evaluating: " + q.toString());
		for (Feature feature : features) {
			System.out.println("feature:");
			System.out.println(feature);
			switch (feature) {
			case PREDICATES:
				featureValues.putAll(usedPredicates(q));
				break;
			case PATTERN:
				featureValues.putAll(usedPattern(q));
				break;
			case NR_OF_CONSTRAINTS:
				featureValues.put("feature:numberOfConstraints", numberOfConstraints(q));
				break;
			case NR_OF_TERMS:
				featureValues.put("feature:numberOfTermsInTextQuery", numberOfTermsInTextQuery(q));
				break;
			case NR_OF_TYPES:
				featureValues.put("feature:numberOfTypes", numberOfTypes(q));
				break;
			default:
				break;
			}
		}
		return featureValues;
	}

	private double cosinus(final Map<String, Double> calculateRanking, final Map<String, Double> goldVector) {
		double dotProduct = 0;
		for (String key : goldVector.keySet()) {
			if (calculateRanking.containsKey(key)) {
				dotProduct += goldVector.get(key) * calculateRanking.get(key);
			}
		}
		double magnitude_A = 0;
		for (String key : goldVector.keySet()) {
			magnitude_A += Math.sqrt(goldVector.get(key) * goldVector.get(key));
		}
		double magnitude_B = 0;
		for (String key : calculateRanking.keySet()) {
			magnitude_B += Math.sqrt(calculateRanking.get(key) * calculateRanking.get(key));
		}
		return dotProduct / (magnitude_A * magnitude_B);
	}

	private double numberOfConstraints(final SPARQLQuery query) {
		return query.constraintTriples.size();
	}

	private Double numberOfTermsInTextQuery(final SPARQLQuery q) {
		for (String key : q.textMapFromVariableToSingleFuzzyToken.keySet()) {
			return (double) q.textMapFromVariableToSingleFuzzyToken.get(key).size();
		}
		return 0.0;
	}

	private Double numberOfTypes(final SPARQLQuery q) {
		String[] split = new String[3];
		double numberOfTypes = 0;
		for (String triple : q.constraintTriples) {
			split = triple.split(" ");
			if (split[1].equals("a")) {
				numberOfTypes++;
			}
		}
		return numberOfTypes;
	}

	private Map<String, Double> usedPattern(final SPARQLQuery q) {
		Map<String, Double> map = Maps.newHashMap();
		String[] split = new String[3];
		String textNode = null;
		for (String var : q.textMapFromVariableToCombinedNNExactMatchToken.keySet()) {
			textNode = var;
		}
		List<String> constraintTriples = q.constraintTriples;
		for (String triple : constraintTriples) {
			triple = triple.replaceAll("\\s+", " ");
			split = triple.split(" ");
			String subject = split[0];
			String predicate = split[1];
			String object = split[2];
			if (subject.equals(textNode) && predicate.startsWith("?") && object.startsWith("?")) {
				String key = "textNode_?var_?var";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && !predicate.startsWith("?") && object.startsWith("?")) {
				String key = "textNode_bound_?var";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && predicate.startsWith("?") && !object.startsWith("?")) {
				String key = "textNode_?var_bound";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && !predicate.startsWith("?") && !object.startsWith("?")) {
				String key = "textNode_bound_bound";
				addOneToMapAtKey(map, key);
			} else if (object.equals(textNode) && predicate.startsWith("?") && subject.startsWith("?")) {
				String key = "?var_?var_textNode";
				addOneToMapAtKey(map, key);
			} else if (object.equals(textNode) && !predicate.startsWith("?") && subject.startsWith("?")) {
				String key = "?var_bound_textNode";
				addOneToMapAtKey(map, key);
			} else if (object.equals(textNode) && predicate.startsWith("?") && !subject.startsWith("?")) {
				String key = "bound_?var_textNode";
				addOneToMapAtKey(map, key);
			} else if (object.equals(textNode) && !predicate.startsWith("?") && !subject.startsWith("?")) {
				String key = "bound_bound_textNode";
				addOneToMapAtKey(map, key);
			}
		}
		return map;
	}

	private Map<String, Double> usedPredicates(final SPARQLQuery q) {
		Map<String, Double> map = Maps.newHashMap();
		String[] split = new String[3];
		for (String triple : q.constraintTriples) {
			triple = triple.replaceAll("\\s+", " ");
			split = triple.split(" ");
			if (map.containsKey(split[1])) {
				double tmp = map.get(split[1]);
				map.put(split[1], tmp + 1);
			} else {
				map.put(split[1], 1.0);
			}
		}
		return map;
	}
}
