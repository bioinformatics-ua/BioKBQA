package pt.ua.biokbqa.annotation;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class ITSRDF {
	protected static final String uri = "http://www.w3.org/2005/11/its/rdf#";
	public static final Property taIdentRef = property("taIdentRef");
	public static final Property taSource = property("taSource");
	public static final Property taConfidence = property("taConfidence");

	public static String getURI() {
		return uri;
	}

	protected static final Resource resource(String local) {
		return ResourceFactory.createResource(uri + local);
	}

	protected static final Property property(String local) {
		return ResourceFactory.createProperty(uri, local);
	}
}
