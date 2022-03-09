package pt.ua.biokbqa.index;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PattyRelations {
	@SuppressWarnings("deprecation")
	private static final Version LUCENE_VERSION = Version.LUCENE_46;
	private int numberOfDocsRetrievedFromIndex = 100;
	public String FIELD_NAME_URI = "uri";
	public String FIELD_NAME_OBJECT = "object";
	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private StandardAnalyzer analyzer;

	public PattyRelations() {
		try {
			File indexDir = new File("resources/puttyRelations");
			analyzer = new StandardAnalyzer(LUCENE_VERSION);
			if (!indexDir.exists()) {
				indexDir.mkdir();
				IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);
				directory = new MMapDirectory(indexDir);
				iwriter = new IndexWriter(directory, config);
				index();
			} else {
				directory = new MMapDirectory(indexDir);
			}
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void index() {
		try {
			Path currentDir = Paths.get(".");
			Path path = currentDir.resolve("resources/dbpedia-relation-paraphrases.txt");
			Stream<String> lines = Files.lines(path);
			lines.forEach(s -> lineSplitAndAddtoIndex(s));
			lines.close();
			iwriter.commit();
			iwriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addDocumentToIndex(String relation, String data) throws Exception {
		Document doc = new Document();
		doc.add(new StringField(FIELD_NAME_URI, "http://dbpedia.org/ontology/" + relation, Store.YES));
		doc.add(new TextField(FIELD_NAME_OBJECT, data, Store.YES));
		iwriter.addDocument(doc);
	}

	private void lineSplitAndAddtoIndex(String line) {
		String rel = line.split("\\t")[0];
		String dat = line.split("\\t")[1];
		try {
			addDocumentToIndex(rel, dat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashSet<String> search(String object) {
		ArrayList<String> uris = Lists.newArrayList();
		try {
			System.out.println("\t start asking index...");
			Query q = new FuzzyQuery(new Term(FIELD_NAME_OBJECT, object), 0);
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);
			isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				uris.add(hitDoc.get(FIELD_NAME_URI));
			}
			System.out.println("\t finished asking index...");
		} catch (Exception e) {
			e.printStackTrace();
		}
		HashSet<String> setUris = Sets.newHashSet(uris);
		return setUris;
	}
}
