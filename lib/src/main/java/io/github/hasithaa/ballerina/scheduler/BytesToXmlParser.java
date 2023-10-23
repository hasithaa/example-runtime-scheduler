package io.github.hasithaa.ballerina.scheduler;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class BytesToXmlParser implements Parser {

    private static final XMLInputFactory xmlInputFactory;
    StringBuilder sb = new StringBuilder();

    static {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    private XMLStreamReader xmlStreamReader;

    BytesToXmlParser(InputStream inputStream) throws XMLStreamException {
        xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
    }

    public void parse() throws Exception {
        while (xmlStreamReader.hasNext()) {
            int next = xmlStreamReader.next();
            switch (next) {
                case START_ELEMENT:
                    handleStartElement();
                    break;
                case END_ELEMENT:
                    handleEndElement();
                    break;
                case PROCESSING_INSTRUCTION:
                    handleXmlPI();
                    break;
                case COMMENT:
                    handleXmlComment();
                    break;
                case CDATA:
                case CHARACTERS:
                    handleXmlText();
                    break;
                case END_DOCUMENT:
                case DTD:
                    break;
                default:
                    assert false;
            }
        }
    }

    private boolean isQualified(QName name) {
        return name.getPrefix() != null && !name.getPrefix().isEmpty();
    }

    private void handleStartElement() {
        QName name = xmlStreamReader.getName();
        sb.append("<").append(isQualified(name) ? name.getPrefix() + ":" : "").append(name.getLocalPart());

        for (int i = 0; i < xmlStreamReader.getNamespaceCount(); i++) {
            sb.append(" xmlns");
            String prefix = xmlStreamReader.getNamespacePrefix(i);
            if (null != prefix) {
                sb.append(":").append(prefix);
            }
            sb.append("=\"").append(xmlStreamReader.getNamespaceURI(i)).append("\"");
        }

        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            sb.append(" ");
            String prefix = xmlStreamReader.getAttributePrefix(i);
            if (null != prefix) {
                sb.append(prefix).append(":");
            }
            sb.append(xmlStreamReader.getAttributeLocalName(i)).append("=\"")
                    .append(xmlStreamReader.getAttributeValue(i)).append("\"");
        }

        sb.append(">");
    }

    private void handleEndElement() {
        QName name = xmlStreamReader.getName();
        sb.append("</").append(isQualified(name) ? name.getPrefix() + ":" : "").append(name.getLocalPart()).append(">");
    }

    private void handleXmlPI() {
        sb.append("<?").append(xmlStreamReader.getPITarget()).append(" ").append(xmlStreamReader.getPIData())
                .append("?>");
    }

    private void handleXmlComment() {
        sb.append("<!--").append(xmlStreamReader.getText()).append("-->");
    }

    private void handleXmlText() {
        sb.append(xmlStreamReader.getText());
    }

    public String getResultString() {
        return sb.toString();
    }

    public Integer getResultInt() {
        return -1;
    }
}
