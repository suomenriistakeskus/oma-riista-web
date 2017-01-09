package fi.riista.integration.mml.parser;

import com.google.common.collect.Lists;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.integration.mml.support.WFSUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpression;
import java.util.List;

public final class MMLRekisteriyksikonTietojaParser {
    private static final XPathExpression XPATH_REKISTERIYKSIKON_TIETOJA =
            WFSUtil.createXPathExpression("/wfs:FeatureCollection/gml:featureMember/ktjkiiwfs:RekisteriyksikonTietoja");

    private static final XPathExpression XPATH_IDENTIFIER = WFSUtil.createXPathExpression("ktjkiiwfs:kiinteistotunnus[1]/text()");
    private static final XPathExpression XPATH_MUNICIPALITY = WFSUtil.createXPathExpression("ktjkiiwfs:kuntaTieto/ktjkiiwfs:KuntaTieto");
    private static final XPathExpression XPATH_MUNICIPALITY_CODE = WFSUtil.createXPathExpression("ktjkiiwfs:kuntatunnus[1]/text()");

    public static List<MMLRekisteriyksikonTietoja> parse(Document document) {
        document.getDocumentElement().normalize();

        final NodeList nodeList = WFSUtil.getNodeSet(XPATH_REKISTERIYKSIKON_TIETOJA, document);
        final List<MMLRekisteriyksikonTietoja> resultList = Lists.newArrayListWithExpectedSize(nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            final String propertyIdentifier = WFSUtil.getString(XPATH_IDENTIFIER, node);
            final String municipalityCode;

            final Node municipalityNode = WFSUtil.getSingleNode(XPATH_MUNICIPALITY, node);

            if (municipalityNode != null) {
                municipalityCode = WFSUtil.getString(XPATH_MUNICIPALITY_CODE, municipalityNode);
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
