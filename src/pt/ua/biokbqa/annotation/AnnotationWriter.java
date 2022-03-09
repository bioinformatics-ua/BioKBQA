package pt.ua.biokbqa.annotation;

// import java.util.Set;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public class AnnotationWriter {
	public void writeMarkingToModel(Model nifModel, Resource documentResource, String text, String documentURI,
			Marking marking, int markingId) {
		if ((marking instanceof Span))
			addSpan(nifModel, documentResource, text, documentURI, (Span) marking);
		else if ((marking instanceof Meaning))
			addAnnotation(nifModel, documentResource, documentURI, (Annotation) marking, markingId);
	}

	public void addAnnotation(Model nifModel, Resource documentAsResource, String documentURI, Annotation annotation,
			int annotationId) {
		StringBuilder uriBuilder = new StringBuilder();
		uriBuilder.append(documentURI);
		uriBuilder.append("#annotation");
		uriBuilder.append(annotationId);
		Resource annotationAsResource = nifModel.createResource(uriBuilder.toString());
		nifModel.add(annotationAsResource, RDF.type, NIF.Annotation);
		nifModel.add(documentAsResource, NIF.topic, annotationAsResource);
		/*
		for (String meainingUri : annotation.getUris()) {
			nifModel.add(annotationAsResource, ITSRDF.taIdentRef, nifModel.createResource(meainingUri));
		}
		*/
		if ((annotation instanceof ScoredAnnotation))
			nifModel.add(annotationAsResource, NIF.confidence,
					Double.toString(((ScoredAnnotation) annotation).getConfidence()), XSDDatatype.XSDstring);
	}

	public void addSpan(Model nifModel, Resource documentAsResource, String text, String documentURI, Span span) {
		int startInJavaText = span.getStartPosition();
		int endInJavaText = startInJavaText + span.getLength();
		int start = text.codePointCount(0, startInJavaText);
		int end = start + text.codePointCount(startInJavaText, endInJavaText);
		String spanUri = NIFUriHelper.getNifUri(documentURI, start, end);
		Resource spanAsResource = nifModel.createResource(spanUri);
		nifModel.add(spanAsResource, RDF.type, NIF.Phrase);
		nifModel.add(spanAsResource, RDF.type, NIF.String);
		nifModel.add(spanAsResource, RDF.type, NIF.RFC5147String);
		nifModel.add(spanAsResource, NIF.anchorOf,
				nifModel.createTypedLiteral(text.substring(startInJavaText, endInJavaText), XSDDatatype.XSDstring));
		nifModel.add(spanAsResource, NIF.beginIndex,
				nifModel.createTypedLiteral(Integer.valueOf(start), XSDDatatype.XSDnonNegativeInteger));
		nifModel.add(spanAsResource, NIF.endIndex,
				nifModel.createTypedLiteral(Integer.valueOf(end), XSDDatatype.XSDnonNegativeInteger));
		nifModel.add(spanAsResource, NIF.referenceContext, documentAsResource);
		/*
		if ((span instanceof Meaning)) {
			for (String meainingUri : ((Meaning) span).getUris()) {
				nifModel.add(spanAsResource, ITSRDF.taIdentRef, nifModel.createResource(meainingUri));
			}
		}
		*/
		if ((span instanceof ScoredMarking)) {
			nifModel.add(spanAsResource, ITSRDF.taConfidence, nifModel
					.createTypedLiteral(Double.valueOf(((ScoredMarking) span).getConfidence()), XSDDatatype.XSDdouble));
		}
		/*
		if ((span instanceof TypedMarking)) {
			Set types = ((TypedNamedEntity) span).getTypes();
			for (String type : types)
				nifModel.add(spanAsResource, ITSRDF.taClassRef, nifModel.createResource(type));
		}
		*/
	}
}
