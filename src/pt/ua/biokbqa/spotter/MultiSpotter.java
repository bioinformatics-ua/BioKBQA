package pt.ua.biokbqa.spotter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pt.ua.biokbqa.data.blueprint.Entity;

// Get as many entities as possible from several spotters.
public class MultiSpotter extends ASpotter {

	private List<ASpotter> spotters;
	private Map<String, List<Entity>> totalResult;

	public MultiSpotter(ASpotter... spotters) {
		this.spotters = Arrays.asList(spotters);
	}

	@Override
	public Map<String, List<Entity>> getEntities(final String question) {
		totalResult = Maps.newConcurrentMap();
		ExecutorService tp = Executors.newFixedThreadPool(spotters.size());
		@SuppressWarnings("unused")
		CompletionService<Map<String, List<Entity>>> ecs = new ExecutorCompletionService<Map<String, List<Entity>>>(tp);
		for (final ASpotter spotter : spotters) {
			tp.submit(new Runnable() {
				@Override
				public void run() {
					Map<String, List<Entity>> tmp = spotter.getEntities(question);
					mergeResults(tmp);
				}
			});
		}
		tp.shutdown();
		try {
			tp.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return totalResult;
	}

	private synchronized void mergeResults(Map<String, List<Entity>> result) {
		for (Entry<String, List<Entity>> entry : result.entrySet()) {
			String lang = entry.getKey();
			List<Entity> newEntities = entry.getValue();
			List<Entity> existingEntities = totalResult.get(lang);
			if (existingEntities != null) {
				List<Entity> tmp = Lists.newArrayList();
				for (Entity entity1 : newEntities) {
					boolean add = true;
					for (Entity entity2 : existingEntities) {
						if (entity1.label.equals(entity2.label)) {
							add = false;
							entity2.uris.addAll(entity1.uris);
							break;
						}
					}
					if (add) {
						tmp.add(entity1);
					}
				}
				existingEntities.addAll(tmp);
			} else {
				totalResult.put(lang, newEntities);
			}
		}
	}

	// @SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		String question = "Which building owned by the crown overlook the North Sea?";
		// ASpotter fox = new Fox();
		// System.out.println(fox.getEntities(question));
		ASpotter tagMe = new TagMe();
		System.out.println(tagMe.getEntities(question));
		ASpotter spot = new Spotlight();
		System.out.println(spot.getEntities(question));
		// MultiSpotter multiSpotter = new MultiSpotter(fox, tagMe, spot);
		MultiSpotter multiSpotter = new MultiSpotter(tagMe, spot);
		System.out.println(multiSpotter.getEntities(question));
	}
}
