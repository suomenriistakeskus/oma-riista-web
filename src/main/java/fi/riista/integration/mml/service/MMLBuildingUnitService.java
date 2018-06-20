package fi.riista.integration.mml.service;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import fi.riista.feature.gis.GISPoint;
import fi.riista.integration.mml.support.InspireWFSConstants;
import fi.riista.integration.mml.support.MMLWebFeatureServiceRequestTemplate;
import fi.riista.integration.mml.support.WFS20Filters;
import fi.riista.integration.mml.support.WFSDomUtil;
import fi.riista.util.GISUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.annotation.Resource;
import javax.xml.xpath.XPathExpression;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class MMLBuildingUnitService {
    private static final Logger LOG = LoggerFactory.getLogger(MMLBuildingUnitService.class);

    private static final String PROPERTY_BUILDING_GEOMETRY2D = "bu-core2d:BuildingType/bu-base:BuildingGeometry2DPropertyType";

    @Resource(name = "buildingWFS")
    private MMLWebFeatureServiceRequestTemplate requestTemplate;

    private static final XPathExpression XPATH_POSLIST_TEXT;

    static {
        XPATH_POSLIST_TEXT = WFSDomUtil.createXPathExpression("/wfs:FeatureCollection" +
                "/wfs:member" +
                "/bu:core2d:Building" +
                "/bu:core2d:geometry2D" +
                "/bu:base:BuildingGeometry2D" +
                "/bu:base:geometry" +
                "//gml:LinearRing" +
                "/gml:posList" +
                "/text()", InspireWFSConstants.createNamespaceContextForWfs());
    }

    public MMLBuildingUnitService() {
    }

    public MMLBuildingUnitService(final MMLWebFeatureServiceRequestTemplate requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    public int findCountDWithin(final GISPoint position, final int distanceMeters) {
        final Document document = getQuery(WFS20Filters.dWithin(PROPERTY_BUILDING_GEOMETRY2D, position, distanceMeters));
        return Integer.parseInt(document.getDocumentElement().getAttribute("numberMatched"));
    }

    public OptionalDouble findMinimumDistanceToGeometryDWithin(final GISPoint position, final int distanceMeters) {
        final GeometryFactory geometryFactory = GISUtils.getGeometryFactory(GISUtils.SRID.ETRS_TM35FIN);
        final Document document = getQuery(WFS20Filters.dWithin(PROPERTY_BUILDING_GEOMETRY2D, position, distanceMeters));

        final NodeList nodeSet = WFSDomUtil.getNodeSet(XPATH_POSLIST_TEXT, document);
        final Splitter coordSplitter = Splitter.on(CharMatcher.whitespace());

        final List<com.vividsolutions.jts.geom.Point> result = new LinkedList<>();

        for (int i = 0; i < nodeSet.getLength(); i++) {
            final Iterable<String> posList = coordSplitter.split(nodeSet.item(i).getTextContent());
            final Iterator<String> postListIterator = posList.iterator();
            final String longitudeString = postListIterator.hasNext() ? postListIterator.next() : null;
            final String latitudeString = postListIterator.hasNext() ? postListIterator.next() : null;

            if (longitudeString != null && latitudeString != null) {
                result.add(geometryFactory.createPoint(new Coordinate(
                        Double.parseDouble(longitudeString),
                        Double.parseDouble(latitudeString))));
            }
        }

        final com.vividsolutions.jts.geom.Point refPoint =
                geometryFactory.createPoint(new Coordinate(position.getLongitude(), position.getLatitude()));

        return result.stream().mapToDouble(p -> p.distance(refPoint)).min();
    }

    private Document getQuery(final String filter) {
        final ImmutableMap<String, String> parameters = ImmutableMap.<String, String>builder()
                .put("service", "WFS")
                .put("request", "GetFeature")
                .put("version", "2.0.0")
                .put("typeNames", "bu-core2d:Building")
                .put("srsName", WFS20Filters.SRS_NAME)
                .put("filter", filter)
                .build();

        Document document = requestTemplate.makeXMLGetRequest(parameters);

        if (LOG.isDebugEnabled()) {
            LOG.debug("XML response: {}", WFSDomUtil.domToString(document, true, false));
        }

        return document;
    }
}
