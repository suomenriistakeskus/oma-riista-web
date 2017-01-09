package fi.riista.integration.mml.parser;

import com.google.common.collect.Lists;
import fi.riista.integration.mml.dto.MMLPalstanTietoja;
import fi.riista.integration.mml.support.WFSUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpression;
import java.util.List;

public final class MMLPalstanTietojaParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");

    private static final XPathExpression XPATH_PALSTAN_TIETOJA;
    private static final XPathExpression XPATH_GEOMETRY;
    private static final XPathExpression XPATH_IDENTIFIER;
    private static final XPathExpression XPATH_UPDATED_AT;

    static {
        XPATH_PALSTAN_TIETOJA = WFSUtil.createXPathExpression("/wfs:FeatureCollection/gml:featureMember/ktjkiiwfs:PalstanTietoja");
        XPATH_GEOMETRY = WFSUtil.createXPathExpression("ktjkiiwfs:sijainti/*[1]");
        XPATH_IDENTIFIER = WFSUtil.createXPathExpression("ktjkiiwfs:rekisteriyksikonKiinteistotunnus[1]/text()");
        XPATH_UPDATED_AT = WFSUtil.createXPathExpression("ktjkiiwfs:paivityspvm[1]/text()");
    }

    public static List<MMLPalstanTietoja> parse(Document document) {
        document.getDocumentElement().normalize();

        final NodeList nodeList = WFSUtil.getNodeSet(XPATH_PALSTAN_TIETOJA, document);
        final List<MMLPalstanTietoja> resultList = Lists.newArrayListWithExpectedSize(nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            final Node gmlNode = WFSUtil.getSingleNode(XPATH_GEOMETRY, node);
            final String gml = WFSUtil.domToString(gmlNode, false, true);

            final String propertyIdentifier = WFSUtil.getString(XPATH_IDENTIFIER, node);
            final String updatedAt = WFSUtil.getString(XPATH_UPDATED_AT, node);
            final LocalDate updateAtDate = StringUtils.isNotBlank(updatedAt) ?  DATE_FORMAT.parseLocalDate(updatedAt) : null;

            resultList.add(new MMLPalstanTietoja(propertyIdentifier, updateAtDate, gml));
        }

        return resultList;
    }

    private MMLPalstanTietojaParser() {
        throw new AssertionError();
    }
}
