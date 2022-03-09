package pt.ua.biokbqa.annotation;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public class DocumentWriter {
	private AnnotationWriter annotationWriter = new AnnotationWriter();

	public void writeDocumentToModel(Model nifModel, Document document) {
		String text = document.getText();
		int end = text.codePointCount(0, text.length());
		String documentUri = NIFUriHelper.getNifUri(document, end);
		Resource documentResource = nifModel.createResource(documentUri);
		nifModel.add(documentResource, RDF.type, NIF.Context);
		nifModel.add(documentResource, RDF.type, NIF.String);
		nifModel.add(documentResource, RDF.type, NIF.RFC5147String);
		nifModel.add(documentResource, NIF.isString,
				nifModel.createTypedLiteral(document.getText(), XSDDatatype.XSDstring));
		nifModel.add(documentResource, NIF.beginIndex,
				nifModel.createTypedLiteral(Integer.valueOf(0), XSDDatatype.XSDnonNegativeInteger));
		nifModel.add(documentResource, NIF.endIndex,
				nifModel.createTypedLiteral(Integer.valueOf(end), XSDDatatype.XSDnonNegativeInteger));
		int meaningId = 0;
		for (Marking marking : document.getMarkings())
			if ((marking instanceof Span)) {
				this.annotationWriter.addSpan(nifModel, documentResource, text, document.getDocumentURI(),
						(Span) marking);
			} else if ((marking instanceof Meaning)) {
				this.annotationWriter.addAnnotation(nifModel, documentResource, document.getDocumentURI(),
						(Annotation) marking, meaningId);
				meaningId++;
			}
	}
}
