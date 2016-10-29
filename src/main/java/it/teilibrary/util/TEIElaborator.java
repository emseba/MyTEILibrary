package it.teilibrary.util;

import it.teilibrary.models.ManuscriptTranscription;
import java.io.StringReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.jdom2.Content;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.util.IteratorIterable;


public class TEIElaborator {

    private static Namespace ns = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
    private static String _publisher = "Publisher";
    private static String _infrastructure = "My Infrastructure";

    /**
     * Set publisher paramenter for TEI header
     * @param publisher publisher name, "Publisher" by default
     */
    public static void setPublisher(String publisher) {
        TEIElaborator._publisher = publisher;
    }

    /**
     * Set infrastructure for TEI header
     * @param _infrastructure infrastructure name, "My Infrastructure" by default
     */
    public static void setInfrastructure(String _infrastructure) {
        TEIElaborator._infrastructure = _infrastructure;
    }

    /**
     * Utility function that generates TEI xml structure from a list of HTML transcriptions
     * 
     * @param transcriptions List of ManuscriptTranscription, saved under transcription pubblication mask
     * @return String with built xml TEI corresponding to the aggregation of Manuscript Transcriptions in order of scan, for a choose of semplification
     * @throws Exception 
     */
    public static String export(List<ManuscriptTranscription> transcriptions) throws Exception {
        Document doc = new Document();

        Element teiRoot = new Element("TEI", ns);
        teiRoot.setAttribute("version", "5.0");
        doc.setRootElement(teiRoot);

        Element teiHeader = newEle("teiHeader", teiRoot);
        Element fileDesc = newEle("fileDesc", teiHeader);

        Element titleStmt = newEle("titleStmt", fileDesc);
        Element title = newEle("title", titleStmt);
        title.setAttribute("type", "main");
        title.setText("Automatically generated TEI document");
        Element publicationStmt = newEle("publicationStmt", fileDesc);
        Element publisher = newEle("publisher", publicationStmt);
        publisher.setText(TEIElaborator._publisher);
        Element pubPlace = newEle("pubPlace", publicationStmt);
        pubPlace.setText(TEIElaborator._infrastructure);
        Element date = newEle("date", publicationStmt);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        date.setAttribute("when", dateFormat.format(new Date()));
        Element sourceDesc = newEle("sourceDesc", fileDesc);
        Element sourceDescP = newEle("p", sourceDesc);
        sourceDescP.setText("No source: this is an original work.");

        Element text = newEle("text", teiRoot);
        Element body = newEle("body", text);

        SAXBuilder builder = new SAXBuilder();

        for (ManuscriptTranscription t : transcriptions) {
            //correzione da html a tei
            Document textDoc = builder.build(new StringReader("<my_tei>" + getTEIfromHTML(t.getManuscriptXmlTei()) + "</my_tei>"));

            //set current namespace for built Document
            textDoc.getRootElement().setNamespace(ns);
            IteratorIterable<Content> descendants = textDoc.getRootElement().getDescendants();
            for (Content descendant : descendants) {
                if (descendant.getCType().equals(Content.CType.Element)) {
                    Element e = (Element) descendant;
                    e.setNamespace(ns);
                }
            }
            //append childern element of root element my_tei to body Element
            for (Element e : textDoc.getRootElement().getChildren()) {
                body.addContent(e.clone().detach());
            }
        }

        XMLOutputter out = new XMLOutputter(Format.getCompactFormat().setOmitDeclaration(true));
        //out.setFormat(Format.getPrettyFormat());
        StringWriter sw = new StringWriter();

        out.output(doc, sw);

        return sw.toString();
    }

    /**
     * Utility function to generate new Element connected to a parent Element, under TEI namespace
     * 
     * @param name Element name
     * @param parent parent Element
     * @return generate new Element attached to his parent node
     */
    private static Element newEle(String name, Element parent) {
        Element e = new Element(name, ns);
        parent.addContent(e);
        return e;
    }
    
    /**
     * Utility function to translate from html to tei
     * 
     * @param html html format string
     * @return tei corrections applied on html string
     */
    private static String getTEIfromHTML(String html) {
        html = html.replaceAll("&nbsp;", " ");
        html = html.replaceAll("em>", "emph>");
        html = html.replaceAll("strong>", "hi>");
        return html;
    }
}
