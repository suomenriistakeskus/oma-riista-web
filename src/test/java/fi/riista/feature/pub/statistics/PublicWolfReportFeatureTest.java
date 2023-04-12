package fi.riista.feature.pub.statistics;

import fi.riista.config.Constants;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestLukeStatus;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
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
import java.util.Map;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;

public class PublicWolfReportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PublicWolfReportFeature wolfReportFeature;

    @Test
    public void testSmoke() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person author = model().newPerson();

        final HarvestPermit permit = model().newHarvestPermit(rhy);
        permit.setPermitTypeCode("209");

        final GameSpecies wolf = model().newGameSpecies(46549, GameCategory.GAME_MAMMAL, "Susi", "Räv", "Wolf");

        final Harvest harvest = model().newHarvest(wolf, author, author);
        harvest.setAmount(1);
        harvest.setPointOfTime(new LocalDate(2022, 3, 31).toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE));
        harvest.setGeoLocation(new GeoLocation(1312312, 2354123));
        harvest.setLukeStatus(HarvestLukeStatus.CONFIRMED_NOT_ALPHA);

        model().newHarvestSpecimen(harvest, ADULT_MALE);

        harvest.setHarvestReportState(HarvestReportState.APPROVED);
        harvest.setHarvestReportAuthor(author);
        harvest.setHarvestReportDate(DateUtil.now());
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setRhy(rhy);

        persistInNewTransaction();

        final FeatureCollection report = wolfReportFeature.report(2021);

        assertThat(report, notNullValue());
        assertThat(report.getFeatures(), notNullValue());
        assertThat(report.getFeatures(), hasSize(1));

        final Map<String, Object> properties = report.getFeatures().get(0).getProperties();
        assertThat(properties, notNullValue());
        assertThat(properties, hasKey("day"));
        assertThat(properties, hasKey("gender"));
        assertThat(properties, hasKey("age"));
        assertFalse(properties.containsKey("luke_status"));
        assertThat(properties, not(hasKey("luke_status")));
        assertThat(properties, hasKey("rhy_code"));
        assertThat(properties, hasKey("rhy_fi"));
        assertThat(properties, hasKey("rhy_sv"));

        assertThat(properties.get("gender"), hasToString("MALE"));
        assertThat(properties.get("age"), hasToString("ADULT"));
        assertThat(properties.get("day"), hasToString("31.03.2022"));
        assertThat(properties.get("rhy_code"), notNullValue());
        assertThat(properties.get("rhy_fi"), notNullValue());
        assertThat(properties.get("rhy_sv"), notNullValue());

        final Feature firstFeature = report.getFeatures().get(0);
        assertThat(firstFeature.getGeometry(), instanceOf(Point.class));

        final Point geometry = (Point) firstFeature.getGeometry();
        assertThat(geometry.getCoordinates().getLatitude(), equalTo(1312312.0));
        assertThat(geometry.getCoordinates().getLongitude(), equalTo(2354123.0));
    }
}
