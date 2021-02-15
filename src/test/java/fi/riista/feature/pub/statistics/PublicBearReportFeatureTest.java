package fi.riista.feature.pub.statistics;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gis.geojson.FeatureCollectionWithProperties;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PublicBearReportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PublicBearReportFeature bearReportFeature;

    @Test
    public void testSmoke() {
        final GameSpecies bear = model().newGameSpecies(47348, GameCategory.GAME_MAMMAL, "karhu", "björn", "bear");

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestArea area = model().newHarvestArea();
        final HarvestArea area2 = model().newHarvestArea();

        final HarvestSeason season = model().newHarvestSeason(bear,
                new LocalDate(2015, 8, 20),
                new LocalDate(2015, 10, 30),
                new LocalDate(2015, 11, 4));
        final HarvestQuota quota = model().newHarvestQuota(season, area, 10);
        model().newHarvestQuota(season, area2, 20);

        final Municipality municipality = model().newMunicipality();

        final Person author = model().newPerson();

        // only approved harvest report should be counted
        createHarvestReport(bear, season, quota, municipality, author, HarvestReportState.APPROVED);

        // should not be counted
        createHarvest(bear, municipality, author);

        // should not be counted, only approved should be counted
        createHarvestReport(bear, season, quota, municipality, author, HarvestReportState.SENT_FOR_APPROVAL);
        createHarvestReport(bear, season, quota, municipality, author, HarvestReportState.REJECTED);

        persistInNewTransaction();

        runInTransaction(() -> {
            assertReport(area, area2, bearReportFeature.report(2015, season.getEndDate()), true);
            assertReport(area, area2, bearReportFeature.report(2015, season.getBeginDate().minusDays(1)), false);
            assertReport(area, area2, bearReportFeature.report(2015, season.getEndDate().plusDays(1)), false);
        });
    }

    private static void assertReport(
            HarvestArea area, HarvestArea area2, FeatureCollectionWithProperties report, boolean seasonOngoing) {

        assertNotNull(report);
        assertNotNull(report.getFeatures());
        assertThat(report.getFeatures(), hasSize(1));

        final Map<String, Object> properties = report.getFeatures().get(0).getProperties();
        assertThat(properties, notNullValue());
        assertThat(properties, hasKey("day"));
        assertThat(properties, hasKey("gender"));
        assertThat(properties, hasKey("area_fi"));
        assertThat(properties, hasKey("area_sv"));
        assertThat(properties, hasKey("municipality_fi"));
        assertThat(properties, hasKey("municipality_sv"));

        assertThat(properties.get("day"), hasToString("31.08.2015"));
        assertThat(properties.get("gender"), hasToString("MALE"));
        assertThat(properties.get("area_fi"), notNullValue());
        assertThat(properties.get("area_sv"), notNullValue());
        assertThat(properties.get("municipality_fi"), notNullValue());
        assertThat(properties.get("municipality_sv"), notNullValue());

        assertLocation(report);
        assertRemainingQuotas(area, area2, report, seasonOngoing);
    }

    private void createHarvestReport(GameSpecies bear, HarvestSeason season, HarvestQuota quota, Municipality municipality, Person author, HarvestReportState state) {
        final Harvest harvest = createHarvest(bear, municipality, author);
        harvest.setHarvestSeason(season);
        harvest.setHarvestQuota(quota);
        harvest.setHarvestReportState(Objects.requireNonNull(state));
        harvest.setHarvestReportAuthor(Objects.requireNonNull(author));
        harvest.setHarvestReportDate(DateUtil.now());
        harvest.setHarvestReportMemo("asdf");
    }

    private Harvest createHarvest(GameSpecies bear, Municipality municipality, Person author) {
        final Harvest harvest = model().newHarvest(bear, author, author);
        harvest.setAmount(1);
        harvest.setPointOfTime(DateUtil.toDateTimeNullSafe(new LocalDate(2015, 8, 31)));
        harvest.setGeoLocation(new GeoLocation(1312312, 2354123));
        harvest.setMunicipalityCode(municipality.getOfficialCode());

        model().newHarvestSpecimen(harvest, ADULT_MALE);
        return harvest;
    }

    private static void assertLocation(FeatureCollection report) {
        final Feature firstFeature = report.getFeatures().get(0);
        assertThat(firstFeature.getGeometry(), instanceOf(Point.class));

        final Point geometry = Point.class.cast(firstFeature.getGeometry());
        assertThat(geometry.getCoordinates().getLatitude(), equalTo(1312312.0));
        assertThat(geometry.getCoordinates().getLongitude(), equalTo(2354123.0));
    }

    private static void assertRemainingQuotas(HarvestArea area, HarvestArea area2, FeatureCollectionWithProperties report, boolean seasonOngoing) {
        final Map<String, Object> commonProperties = report.getProperties();
        assertEquals(1, commonProperties.size());
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> usedQuotas = (List<Map<String, Object>>) commonProperties.get("usedQuotas");
        assertNotNull(usedQuotas);
        assertEquals(2, usedQuotas.size());

        if (seasonOngoing) {
            assertQuota(usedQuotas, area, 9);
            assertQuota(usedQuotas, area2, 20);
        } else {
            assertQuota(usedQuotas, area, 0);
            assertQuota(usedQuotas, area2, 0);
        }
    }

    private static void assertQuota(List<Map<String, Object>> usedQuotas, HarvestArea area, int expected) {
        final Map<String, Object> map1 = usedQuotas.stream()
                .filter(map -> map.get("area_fi").equals(area.getNameFinnish()))
                .findAny()
                .get();
        assertNotNull(map1);
        assertEquals(expected, map1.get("remaining"));
    }

}
