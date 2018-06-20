package fi.riista.integration.mml.support;

import org.apache.commons.io.output.StringBuilderWriter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class WFSDomUtil {
    public static XPathExpression createXPathExpression(final String expression, final NamespaceContext namespaces) {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(namespaces);

        try {
            return xpath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public static String domToString(final Node doc,
                                     final boolean pretty,
                                     final boolean omitXml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXml ? "yes" : "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, pretty ? "yes" : "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            if (pretty) {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            }

            try (StringBuilderWriter writer = new StringBuilderWriter()) {
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                return writer.toString();
            }
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static NodeList getNodeSet(final XPathExpression expression,
                                      final Node node) {
        final NodeList nodeList;
        try {
            nodeList = (NodeList) expression.evaluate(node, XPathConstants.NODESET);

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return nodeList;
    }

    public static Node getSingleNode(final XPathExpression expression,
                                     final Node node) {
        try {
            return (Node) expression.evaluate(node, XPathConstants.NODE);

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getString(final XPathExpression expression,
                                   final Node node) {
        try {
            return (String) expression.evaluate(node, XPathConstants.STRING);

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
