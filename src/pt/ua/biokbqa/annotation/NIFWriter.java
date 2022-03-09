package pt.ua.biokbqa.annotation;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

public abstract interface NIFWriter {
	public abstract String writeNIF(List<Document> paramList);
	public abstract void writeNIF(List<Document> paramList, Writer paramWriter);
	public abstract void writeNIF(List<Document> paramList, OutputStream paramOutputStream);
	public abstract String getHttpContentType();
}
