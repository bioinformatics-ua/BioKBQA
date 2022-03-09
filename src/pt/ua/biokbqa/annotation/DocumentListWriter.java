package pt.ua.biokbqa.annotation;

import java.util.List;
import org.apache.jena.rdf.model.Model;

public class DocumentListWriter {
	private DocumentWriter documentWriter = new DocumentWriter();

	public void writeDocumentsToModel(Model nifModel, List<Document> documents) {
		for (Document document : documents)
			this.documentWriter.writeDocumentToModel(nifModel, document);
	}
}
