package pt.ua.biokbqa.annotation;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public abstract class AbstractNIFWriter implements NIFWriter {
	private String httpContentType;
	private String language;
	private DocumentListWriter writer = new DocumentListWriter();

	public AbstractNIFWriter(String httpContentType, String language) {
		this.httpContentType = httpContentType;
		this.language = language;
	}

	protected Model createNIFModel(List<Document> document) {
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		this.writer.writeDocumentsToModel(nifModel, document);
		return nifModel;
	}

	public String getHttpContentType() {
		return this.httpContentType;
	}

	public String writeNIF(List<Document> document) {
		StringWriter writer = new StringWriter();
		writeNIF(document, writer);
		return writer.toString();
	}

	public void writeNIF(List<Document> document, OutputStream os) {
		Model nifModel = createNIFModel(document);
		nifModel.write(os, this.language);
	}

	public void writeNIF(List<Document> document, Writer writer) {
		Model nifModel = createNIFModel(document);
		nifModel.write(writer, this.language);
	}
}
