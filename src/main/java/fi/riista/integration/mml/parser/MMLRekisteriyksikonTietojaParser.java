package fi.riista.integration.mml.parser;

import com.google.common.collect.Lists;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.integration.mml.support.KTJConstants;
import fi.riista.integration.mml.support.WFSDomUtil;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpression;
import java.util.List;

public final class MMLRekisteriyksikonTietojaParser {
    private static final XPathExpression XPATH_REKISTERIYKSIKON_TIETOJA;
    private static final XPathExpression XPATH_IDENTIFIER;
    private static final XPathExpression XPATH_MUNICIPALITY;
    private static final XPathExpression XPATH_MUNICIPALITY_CODE;

    static {
        final SimpleNamespaceContext ns = KTJConstants.createNamespaceContextForWfs();
        XPATH_REKISTERIYKSIKON_TIETOJA = WFSDomUtil.createXPathExpression("/wfs:FeatureCollection/gml:featureMember/ktjkiiwfs:RekisteriyksikonTietoja", ns);
        XPATH_IDENTIFIER = WFSDomUtil.createXPathExpression("ktjkiiwfs:kiinteistotunnus[1]/text()", ns);
        XPATH_MUNICIPALITY = WFSDomUtil.createXPathExpression("ktjkiiwfs:kuntaTieto/ktjkiiwfs:KuntaTieto", ns);
        XPATH_MUNICIPALITY_CODE = WFSDomUtil.createXPathExpression("ktjkiiwfs:kuntatunnus[1]/text()", ns);
    }

    public static List<MMLRekisteriyksikonTietoja> parse(Document document) {
        document.getDocumentElement().normalize();

        final NodeList nodeList = WFSDomUtil.getNodeSet(XPATH_REKISTERIYKSIKON_TIETOJA, document);
        final List<MMLRekisteriyksikonTietoja> resultList = Lists.newArrayListWithExpectedSize(nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            final String propertyIdentifier = WFSDomUtil.getString(XPATH_IDENTIFIER, node);
            final String municipalityCode;

            final Node municipalityNode = WFSDomUtil.getSingleNode(XPATH_MUNICIPALITY, node);

            if (municipalityNode != null) {
                municipalityCode = WFSDomUtil.getString(XPATH_MUNICIPALITY_CODE, municipalityNode);
            } else {
                municipalityCode = null;
            }

            resultList.add(new MMLRekisteriyksikonTietoja(propertyIdentifier, municipalityCode));
        }

        return resultList;
    }

    private MMLRekisteriyksikonTietojaParser() {
        throw new AssertionError();
    }
}
