package fi.riista.feature.pub.statistics;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestLukeStatus;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class PublicWolfReportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PublicWolfReportFeature wolfReportFeature;

    @Test
    public void testSmoke() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        Person author = model().newPerson();

        HarvestPermit permit = model().newHarvestPermit(rhy);
        permit.setPermitTypeCode("209");

        GameSpecies wolf = model().newGameSpecies(46549, GameCategory.GAME_MAMMAL, "Susi", "Räv", "Wolf");

        Harvest harvest = model().newHarvest(wolf, author, author);
        harvest.setAmount(1);
        harvest.setPointOfTime(new LocalDate(2015, 3, 31).toDate());
        harvest.setGeoLocation(new GeoLocation(1312312, 2354123));
        harvest.setLukeStatus(HarvestLukeStatus.CONFIRMED_NOT_ALPHA);

        model().newHarvestSpecimen(harvest, GameAge.ADULT, GameGender.MALE);

        HarvestReport harvestReport = model().newHarvestReport(harvest, HarvestReport.State.APPROVED);
        harvestReport.setHarvestPermit(permit);
        harvest.setRhy(rhy);

        persistInNewTransaction();

        FeatureCollection report = wolfReportFeature.report(2014);

        assertNotNull(report);
        assertNotNull(report.getFeatures());
        assertThat(report.getFeatures(), hasSize(1));

        Map<String, Object> properties = report.getFeatures().get(0).getProperties();
        assertThat(properties, notNullValue());
        assertThat(properties, hasKey("day"));
        assertThat(properties, hasKey("gender"));
        assertThat(properties, hasKey("age"));
        assertThat(properties, hasKey("luke_status"));
        assertThat(properties, hasKey("rhy_code"));
        assertThat(properties, hasKey("rhy_fi"));
        assertThat(properties, hasKey("rhy_sv"));

        assertThat(properties.get("gender"), hasToString("MALE"));
        assertThat(properties.get("age"), hasToString("ADULT"));
        assertThat(properties.get("day"), hasToString("31.03.2015"));
        assertThat(properties.get("rhy_code"), notNullValue());
        assertThat(properties.get("rhy_fi"), notNullValue());
        assertThat(properties.get("rhy_sv"), notNullValue());
        assertThat(properties.get("luke_status"), hasToString("CONFIRMED_NOT_ALPHA"));

        Feature firstFeature = report.getFeatures().get(0);
        assertThat(firstFeature.getGeometry(), instanceOf(Point.class));

        Point geometry = Point.class.cast(firstFeature.getGeometry());
        assertThat(geometry.getCoordinates().getLatitude(), equalTo(1312312.0));
        assertThat(geometry.getCoordinates().getLongitude(), equalTo(2354123.0));
    }
}
