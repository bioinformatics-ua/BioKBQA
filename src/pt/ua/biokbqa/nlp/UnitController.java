package pt.ua.biokbqa.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pt.ua.biokbqa.controller.StanfordNLPConnector;

public class UnitController {
	private Map<String, IUnitLanguage> languageToHandler;

	public UnitController() {
		languageToHandler = new HashMap<>();
	}

	public String normalizeNumbers(final String langDescriptor, final String question) {
		if (languageToHandler.keySet().contains(langDescriptor)) {
			return languageToHandler.get(langDescriptor).convert(question);
		}
		System.out.println("Failed to to normalize numbers and units - No implementation for given language");
		return question;
	}

	public Map<String, IUnitLanguage> getLanguageToHandler() {
		return languageToHandler;
	}

	public void setLanguageToHandler(final Map<String, IUnitLanguage> languageToHandler) {
		this.languageToHandler = languageToHandler;
	}

	public void instantiateEnglish(final StanfordNLPConnector stanford) {
		languageToHandler.put("en", new UnitEnglish(stanford));
	}

	public static List<List<String>> loadTabSplit(final File file) {
		List<List<String>> ret = new ArrayList<>();
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.out.println("Could not load number conversion rules - File not fond" + file.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		try {
			String line = bufferedReader.readLine();
			while (line != null) {
				if (line.startsWith("//")) {
					line = bufferedReader.readLine();
					continue;
				}
				ret.add(new ArrayList<>(Arrays.asList(line.split("\t"))));
				line = bufferedReader.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error while parsing number conversion rules " + file.getAbsolutePath());
		}
		try {
			bufferedReader.close();
		} catch (IOException e) {
			System.out.println("Could not close resource " + file.getAbsolutePath());
			e.printStackTrace();
		}
		return ret;
	}
}
