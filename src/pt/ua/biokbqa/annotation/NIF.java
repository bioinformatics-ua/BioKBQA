package pt.ua.biokbqa.annotation;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class NIF {
	// NIF 2.0 Core Ontology
	public static final String uri = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
	public static final Resource Annotation = resource("Annotation");
	public static final Resource Context = resource("Context");
	public static final Resource Phrase = resource("Phrase");
	public static final Resource String = resource("String");
	public static final Resource RFC5147String = resource("RFC5147String");
	public static final Property anchorOf = property("anchorOf");
	public static final Property beginIndex = property("beginIndex");
	public static final Property confidence = property("confidence");
	public static final Property isString = property("isString");
	public static final Property endIndex = property("endIndex");
	public static final Property keyword = property("keyword");
	public static final Property referenceContext = property("referenceContext");
	public static final Property topic = property("topic");

	protected static final Resource resource(String local) {
		return ResourceFactory.createResource(uri + local);
	}

	protected static final Property property(String local) {
		return ResourceFactory.createProperty(uri, local);
	}

	public static String getURI() {
		return uri;
	}
}
