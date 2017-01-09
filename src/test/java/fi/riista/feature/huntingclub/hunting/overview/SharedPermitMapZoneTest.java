package fi.riista.feature.huntingclub.hunting.overview;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SharedPermitMapZoneTest extends EmbeddedDatabaseTest {

    @Resource
    private SharedPermitMapFeature sharedPermitMapFeature;

    private static class SimpleFixture {
        final int huntingYear;
        final GameSpecies gameSpecies;
        final HuntingClub huntingClub;
        final HuntingClubGroup huntingClubGroup;
        final HuntingClubArea huntingClubArea;
        final HarvestPermit harvestPermit;
        final GISZone zone;

        public SimpleFixture(final EntitySupplier model) {
            final Riistanhoitoyhdistys rhy = model.newRiistanhoitoyhdistys();

            this.huntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();
            this.gameSpecies = model.newGameSpecies();

            // Permit with club as partner
            this.harvestPermit = model.newHarvestPermit(rhy);
            this.harvestPermit.setPermitTypeCode(HarvestPermit.MOOSELIKE_PERMIT_TYPE);

            this.huntingClub = model.newHuntingClub(rhy);
            this.harvestPermit.getPermitPartners().add(huntingClub);

            this.huntingClubGroup = model.newHuntingClubGroup(huntingClub, gameSpecies, huntingYear);
            this.huntingClubGroup.updateHarvestPermit(this.harvestPermit);

            this.huntingClubArea = model.newHuntingClubArea(this.huntingClub, "fi", "sv", this.huntingClubGroup.getHuntingYear());
            this.zone = model.newGISZone();
            this.huntingClubArea.setZone(this.zone);
            this.huntingClubGroup.setHuntingArea(huntingClubArea);
        }

        public Map<Long, Map<String, Object>> loadAreas(final SharedPermitMapFeature feature) {
            return feature.getPermitZones(this.harvestPermit,
                    this.huntingClubGroup.getHuntingYear(),
                    this.huntingClubGroup.getSpecies());
        }
    }

    @Test
    public void testSmoke() {
        final SimpleFixture f = new SimpleFixture(model());

        persistInNewTransaction();

        final Map<Long, Map<String, Object>> permitZones = f.loadAreas(sharedPermitMapFeature);

        assertThat(permitZones, hasKey(f.zone.getId()));

        final Map<String, Object> props = permitZones.get(f.zone.getId());

        @SuppressWarnings("unchecked")
        final Map<String, String> clubName = (Map<String, String>) props.get(GeoJSONConstants.PROPERTY_CLUB_NAME);
        assertNotNull(clubName);
        assertEquals(f.huntingClub.getNameFinnish(), clubName.get("fi"));
        assertEquals(f.huntingClub.getNameSwedish(), clubName.get("sv"));
    }

    @Test
    public void testFilterZones() {
        final SimpleFixture f1 = new SimpleFixture(model());
        final SimpleFixture f2 = new SimpleFixture(model());

        persistInNewTransaction();

        final Map<Long, Map<String, Object>> permitZones = f1.loadAreas(sharedPermitMapFeature);

        assertThat(permitZones, not(hasKey(f2.zone.getId())));
        assertThat(permitZones, hasKey(f1.zone.getId()));
    }

    @Test
    public void testFilterAreaNotSetForGroup() {
        final SimpleFixture f = new SimpleFixture(model());
        f.huntingClubGroup.setHuntingArea(null);

        persistInNewTransaction();

        final Map<Long, Map<String, Object>> permitZones = f.loadAreas(sharedPermitMapFeature);

        assertThat(permitZones, notNullValue());
        assertThat(permitZones.values(), empty());
    }

    @Test
    public void testFilterNonPartnerClub() {
        final SimpleFixture f = new SimpleFixture(model());
        f.harvestPermit.getPermitPartners().clear();

        persistInNewTransaction();

        final Map<Long, Map<String, Object>> permitZones = f.loadAreas(sharedPermitMapFeature);

        assertThat(permitZones, notNullValue());
        assertThat(permitZones.values(), empty());
    }

    @Test
    public void testFilterNonPartnerGroup() {
        final SimpleFixture f = new SimpleFixture(model());
        f.huntingClubGroup.updateHarvestPermit(null);

        persistInNewTransaction();

        final Map<Long, Map<String, Object>> permitZones = f.loadAreas(sharedPermitMapFeature);

        assertThat(permitZones, notNullValue());
        assertThat(permitZones.values(), empty());
    }

    @Test
    public void testFilterByHuntingYear() {
        final SimpleFixture f = new SimpleFixture(model());
        f.huntingClubGroup.updateHarvestPermit(null);

        persistInNewTransaction();

        final Map<Long, Map<String, Object>> permitZones = sharedPermitMapFeature.getPermitZones(f.harvestPermit,
                f.huntingClubGroup.getHuntingYear() + 1,
                f.huntingClubGroup.getSpecies());

        assertThat(permitZones, notNullValue());
        assertThat(permitZones.values(), empty());
    }

    @Test
    public void testFilterByGameSpecies() {
        final SimpleFixture f = new SimpleFixture(model());
        f.huntingClubGroup.updateHarvestPermit(null);

        final GameSpecies otherSpecies = model().newGameSpecies();

        persistInNewTransaction();

        final Map<Long, Map<String, Object>> permitZones = sharedPermitMapFeature.getPermitZones(f.harvestPermit,
                f.huntingClubGroup.getHuntingYear(),
                otherSpecies);

        assertThat(permitZones, notNullValue());
        assertThat(permitZones.values(), empty());
    }
}
