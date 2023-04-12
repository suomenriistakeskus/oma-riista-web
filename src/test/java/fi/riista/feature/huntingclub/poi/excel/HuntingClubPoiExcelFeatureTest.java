package fi.riista.feature.huntingclub.poi.excel;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.poi.PoiIdAllocation;
import fi.riista.feature.huntingclub.poi.PoiLocation;
import fi.riista.feature.huntingclub.poi.PoiLocationGroup;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupDTO;
import fi.riista.feature.huntingclub.poi.PointOfInterestType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static fi.riista.test.Asserts.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class HuntingClubPoiExcelFeatureTest extends EmbeddedDatabaseTest {


    @Resource
    private HuntingClubPoiExcelFeature excelFeature;

    private PoiIdAllocation poiIdAllocation;
    private HuntingClub club;
    private Person clubMember;
    private Person contactPerson;

    @Before
    public void setup() {
        club = model().newHuntingClub();

        poiIdAllocation = model().newPoiIdAllocation(club);

        clubMember = model().newPerson();
        model().newOccupation(club, clubMember, OccupationType.SEURAN_JASEN);

        contactPerson = model().newPerson();
        model().newOccupation(club, contactPerson, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void export_unauthorized() {
        excelFeature.exportExcel(club.getId());
    }

    @Test
    public void export_contactPerson() {
        onSavedAndAuthenticated(createUser(contactPerson), () ->{
            final ClubPoiExcelDTO result = excelFeature.exportExcel(club.getId());
            assertThat(result.getClub().getNameLocalisation(), equalTo(club.getNameLocalisation()));
            assertThat(result.getPois(), is(empty()));
        });
    }

    @Test
    public void export_clubMember() {
        onSavedAndAuthenticated(createUser(clubMember), () -> {
            final ClubPoiExcelDTO result = excelFeature.exportExcel(club.getId());
            assertThat(result.getClub().getNameLocalisation(), equalTo(club.getNameLocalisation()));
            assertThat(result.getPois(), is(empty()));
        });
    }

    @Test
    public void export() {
        final PoiLocationGroup sightingPlace = model().newPoiLocationGroup(poiIdAllocation, PointOfInterestType.SIGHTING_PLACE);
        final PoiLocation sightingLocation1 = model().newPoiLocation(sightingPlace);
        sightingLocation1.setDescription("1st location");
        final PoiLocation sightingLocation2 = model().newPoiLocation(sightingPlace);
        sightingLocation2.setDescription("2nd location");
        final PoiLocation sightingLocation3 = model().newPoiLocation(sightingPlace);
        sightingLocation3.setDescription("3rd location");

        final PoiLocationGroup otherPoiWithNoLocations = model().newPoiLocationGroup(poiIdAllocation, PointOfInterestType.OTHER);

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final ClubPoiExcelDTO result = excelFeature.exportExcel(club.getId());
            assertThat(result.getClub().getNameLocalisation(), equalTo(club.getNameLocalisation()));
            assertThat(result.getPois(), hasSize(2));
            final Map<Long, PoiLocationGroupDTO> poiMap = F.indexById(result.getPois());

            final PoiLocationGroupDTO sigthingDto = requireNonNull(poiMap.get(sightingPlace.getId()));
            assertEquality(sightingPlace, asList(sightingLocation1, sightingLocation2, sightingLocation3), sigthingDto);

            final PoiLocationGroupDTO otherDto = requireNonNull(poiMap.get(otherPoiWithNoLocations.getId()));
            assertEquality(otherPoiWithNoLocations, emptyList(), otherDto);

        });
    }

    private void assertEquality(final PoiLocationGroup poiLocationGroup, final List<PoiLocation> locations,
                                final PoiLocationGroupDTO dto) {
        assertThat(dto.getId(), equalTo(poiLocationGroup.getId()));
        assertThat(dto.getDescription(), equalTo(poiLocationGroup.getDescription()));
        assertThat(dto.getType(), equalTo(poiLocationGroup.getType()));
        assertThat(dto.getVisibleId(), equalTo(poiLocationGroup.getVisibleId()));
        assertThat(dto.getLocations(), hasSize(locations.size()));

        if (!dto.getLocations().isEmpty()) {
            final Map<Long, PoiLocation> locationMap = F.indexById(locations);
            dto.getLocations().forEach(locationDto -> {
                final PoiLocation poiLocation = requireNonNull(locationMap.get(locationDto.getId()));
                assertThat(locationDto.getId(), equalTo(poiLocation.getId()));
                assertThat(locationDto.getVisibleId(), equalTo(poiLocation.getVisibleId()));
                assertThat(locationDto.getDescription(), equalTo(poiLocation.getDescription()));
                assertThat(locationDto.getGeoLocation(), equalTo(poiLocation.getGeoLocation()));
            });
        }
    }
}
