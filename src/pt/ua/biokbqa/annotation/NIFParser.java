package pt.ua.biokbqa.annotation;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import org.apache.jena.rdf.model.Model;

public abstract interface NIFParser {
	public abstract List<Document> parseNIF(String paramString);
	public abstract List<Document> parseNIF(Reader paramReader);
	public abstract List<Document> parseNIF(InputStream paramInputStream);
	public abstract List<Document> parseNIF(String paramString, Model paramModel);
	public abstract List<Document> parseNIF(Reader paramReader, Model paramModel);
	public abstract List<Document> parseNIF(InputStream paramInputStream, Model paramModel);
	public abstract String getHttpContentType();
}
