package fi.riista.integration.mml.parser;

import com.google.common.collect.Lists;
import fi.riista.integration.mml.dto.MMLPalstanTietoja;
import fi.riista.integration.mml.support.KTJConstants;
import fi.riista.integration.mml.support.WFSDomUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.xml.SimpleNamespaceContext;
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
        final SimpleNamespaceContext ns = KTJConstants.createNamespaceContextForWfs();
        XPATH_PALSTAN_TIETOJA = WFSDomUtil.createXPathExpression("/wfs:FeatureCollection/gml:featureMember/ktjkiiwfs:PalstanTietoja", ns);
        XPATH_GEOMETRY = WFSDomUtil.createXPathExpression("ktjkiiwfs:sijainti/*[1]", ns);
        XPATH_IDENTIFIER = WFSDomUtil.createXPathExpression("ktjkiiwfs:rekisteriyksikonKiinteistotunnus[1]/text()", ns);
        XPATH_UPDATED_AT = WFSDomUtil.createXPathExpression("ktjkiiwfs:paivityspvm[1]/text()", ns);
    }

    public static List<MMLPalstanTietoja> parse(Document document) {
        document.getDocumentElement().normalize();

        final NodeList nodeList = WFSDomUtil.getNodeSet(XPATH_PALSTAN_TIETOJA, document);
        final List<MMLPalstanTietoja> resultList = Lists.newArrayListWithExpectedSize(nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            final Node gmlNode = WFSDomUtil.getSingleNode(XPATH_GEOMETRY, node);
            final String gml = WFSDomUtil.domToString(gmlNode, false, true);

            final String propertyIdentifier = WFSDomUtil.getString(XPATH_IDENTIFIER, node);
            final String updatedAt = WFSDomUtil.getString(XPATH_UPDATED_AT, node);
            final LocalDate updateAtDate = StringUtils.isNotBlank(updatedAt) ? DATE_FORMAT.parseLocalDate(updatedAt) : null;

            resultList.add(new MMLPalstanTietoja(propertyIdentifier, updateAtDate, gml));
        }

        return resultList;
    }

    private MMLPalstanTietojaParser() {
        throw new AssertionError();
    }
}
