package fi.riista.integration.mml.service;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import fi.riista.integration.mml.dto.MMLPalstanTietoja;
import fi.riista.feature.gis.GISPoint;
import fi.riista.integration.mml.parser.MMLPalstanTietojaParser;
import fi.riista.integration.mml.support.MMLWebFeatureServiceRequestTemplate;
import fi.riista.integration.mml.support.WFSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.util.List;

@Service
public class MMLPalstanTietojaService {
    private static final Logger LOG = LoggerFactory.getLogger(MMLPalstanTietojaService.class);

    public static final String[] KTJ_PROPERTY_NAMES = new String[]{
            "ktjkiiwfs:sijainti",
            "ktjkiiwfs:rekisteriyksikonKiinteistotunnus",
            "ktjkiiwfs:tekstiKartalla",
            "ktjkiiwfs:paivityspvm"
    };

    private final MMLWebFeatureServiceRequestTemplate requestTemplate;

    @Autowired
    public MMLPalstanTietojaService(MMLWebFeatureServiceRequestTemplate requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    public List<MMLPalstanTietoja> findByPropertyIdentifier(final String propertyIdentifier) {
        return getQuery(WFSUtil.propertyIsEqual("ktjkiiwfs:rekisteriyksikonKiinteistotunnus", propertyIdentifier));
    }

    public List<MMLPalstanTietoja> findByPosition(final GISPoint position) {
        return getQuery(WFSUtil.intersects(position, "ktjkiiwfs:PalstanTietoja/ktjkiiwfs:sijainti"));
    }

    public List<MMLPalstanTietoja> findByBBox(GISPoint position, int size) {
        final GISPoint lowerPoint = new GISPoint(position.getLatitude() - size / 2, position.getLongitude() - size / 2);
        final GISPoint upperPoint = new GISPoint(position.getLatitude() + size / 2, position.getLongitude() + size / 2);

        return getQuery(WFSUtil.bbox("ktjkiiwfs:PalstanTietoja/ktjkiiwfs:sijainti", lowerPoint, upperPoint));
    }

    private List<MMLPalstanTietoja> getQuery(final String filter) {
        final ImmutableMap<String, String> parameters = ImmutableMap.<String, String>builder()
                .put("SERVICE", "WFS")
                .put("REQUEST", "GetFeature")
                .put("VERSION", "1.1.0")
                .put("NAMESPACE", "xmlns(ktjkiiwfs=http://xml.nls.fi/ktjkiiwfs/2010/02)")
                .put("TYPENAME", "ktjkiiwfs:PalstanTietoja")
                .put("PROPERTYNAME", Joiner.on(',').join(KTJ_PROPERTY_NAMES))
                .put("SRSNAME", WFSUtil.SRS_NAME)
                .put("FILTER", filter)
                .build();

        Document document = requestTemplate.makeXMLGetRequest(parameters);

        if (LOG.isDebugEnabled()) {
            LOG.debug("XML response: {}", WFSUtil.domToString(document, true, false));
        }

        return MMLPalstanTietojaParser.parse(document);
    }
}
