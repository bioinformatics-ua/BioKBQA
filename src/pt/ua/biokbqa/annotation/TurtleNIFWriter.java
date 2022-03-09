package pt.ua.biokbqa.annotation;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

public class TurtleNIFWriter extends AbstractNIFWriter {
	private static final String HTTP_CONTENT_TYPE = "application/x-turtle";
	private static final String LANGUAGE = "TTL";

	public TurtleNIFWriter() {
		super(HTTP_CONTENT_TYPE, LANGUAGE);
	}

	@Override
	public String writeNIF(List<Document> paramList) {
		return null;
	}

	@Override
	public void writeNIF(List<Document> paramList, Writer paramWriter) {
	}

	@Override
	public void writeNIF(List<Document> paramList, OutputStream paramOutputStream) {
	}
}
