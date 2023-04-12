package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.fixture.OrganisationFixtureMixin;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static fi.riista.feature.huntingclub.poi.PointOfInterestType.MINERAL_LICK;
import static fi.riista.test.Asserts.assertThat;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

public class PoiLocationGroupCrudFeatureTest extends EmbeddedDatabaseTest implements OrganisationFixtureMixin {

    private PoiIdAllocation poiIdAllocation;
    private PoiIdAllocation anotherPoiIdAllocation;
    private HuntingClub club;
    private HuntingClub anotherClub;
    private HuntingClubGroup huntingClubGroup;
    private Person clubMember;
    private Person contactPerson;
    private Person huntingLeader;

    @Resource
    private PoiLocationGroupCrudFeature feature;

    @Resource
    private PoiLocationGroupRepository poiRepository;

    @Resource
    PoiLocationRepository locationRepository;

    @Before
    public void setup() {
        club = model().newHuntingClub();
        huntingClubGroup = model().newHuntingClubGroup(club);

        poiIdAllocation = model().newPoiIdAllocation(club);

        clubMember = model().newPerson();
        model().newOccupation(club, clubMember, OccupationType.SEURAN_JASEN);

        contactPerson = model().newPerson();
        model().newOccupation(club, contactPerson, OccupationType.SEURAN_YHDYSHENKILO);

        huntingLeader = model().newPerson();
        model().newOccupation(club, huntingLeader, OccupationType.SEURAN_JASEN);
        model().newOccupation(huntingClubGroup, huntingLeader, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        anotherClub = model().newHuntingClub();
        anotherPoiIdAllocation = model().newPoiIdAllocation(anotherClub);

        persistInNewTransaction();
    }

    // Authorization, negative cases

    @Test(expected = AccessDeniedException.class)
    public void getPoi_unauthorized() {
        feature.list(club.getId());
    }


    @Test(expected = AccessDeniedException.class)
    public void createPoi_unauthorized() {
        final PoiLocationGroupDTO poiDTO = createPoiDto();
        feature.create(poiDTO);
    }

    @Test(expected = AccessDeniedException.class)
    public void createPoi_clubMember() {
        final PoiLocationGroupDTO poiDTO = createPoiDto();

        onSavedAndAuthenticated(createUser(clubMember), () -> feature.create(poiDTO));
    }

    @Test(expected = AccessDeniedException.class)
    public void deletePoi_unauthorized() {
        final PoiLocationGroup poi = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        persistInNewTransaction();

        feature.delete(poi.getId());
    }

    @Test(expected = AccessDeniedException.class)
    public void deletePoi_clubMember() {
        final PoiLocationGroup poi = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        onSavedAndAuthenticated(createUser(clubMember), () -> feature.delete(poi.getId()));
    }

    // Authorization, positive cases

    @Test
    public void getPoi_clubMember() {
        model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);

        onSavedAndAuthenticated(createUser(clubMember), () -> {
            final List<PoiLocationGroupDTO> pois = feature.list(club.getId());
            assertThat(pois, hasSize(1));
        });
    }

    // Features

    @Test
    public void listPois_clubContactPerson() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final List<PoiLocationGroupDTO> pois = feature.list(club.getId());
            assertThat(pois, hasSize(1));

            final PoiLocationGroupDTO dto = pois.get(0);
            assertEquality(poiLocationGroup, Collections.emptyList(), dto);
        });
    }

    @Test
    public void getPoi_clubContactPerson() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final PoiLocationGroupDTO dto = feature.read(poiLocationGroup.getId());

            assertEquality(poiLocationGroup, Collections.emptyList(), dto);
        });
    }

    @Test
    public void updatePoi_description() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        final AtomicReference<PoiLocationGroupDTO> pointOfInterestDTO = new AtomicReference<>();

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final PoiLocationGroupDTO poiDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            poiDTO.setLocations(emptyList());
            poiDTO.setDescription("New description");

            pointOfInterestDTO.set(feature.update(poiDTO));

        });

        runInTransaction(() -> {
            final List<PoiLocationGroup> pois = poiRepository.findAll();
            assertThat(pois, hasSize(1));
            final PoiLocationGroup poi = pois.get(0);

            assertEquality(poi, Collections.emptyList(), pointOfInterestDTO.get());
        });
    }

    @Test
    public void updatePoi_addLocation() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        final AtomicReference<PoiLocationGroupDTO> pointOfInterestDTO = new AtomicReference<>();

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final PoiLocationGroupDTO poiDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            poiDTO.setLocations(singletonList(location(poiLocationGroup.getId())));
            pointOfInterestDTO.set(feature.update(poiDTO));

        });

        runInTransaction(() -> {
            final List<PoiLocationGroup> pois = poiRepository.findAll();
            assertThat(pois, hasSize(1));
            final PoiLocationGroup poi = pois.get(0);

            final List<PoiLocation> locations = locationRepository.findAllByPoi(poi);
            assertEquality(poi, locations, pointOfInterestDTO.get());
        });
    }

    @Test
    public void updatePoi_updateLocation() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        final PoiLocation poiLocation = model().newPoiLocation(poiLocationGroup);

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final PoiLocationGroupDTO poiLocationGroupDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            final PoiLocationDTO locationDTO = location(poiLocationGroup.getId());
            locationDTO.setId(poiLocation.getId());
            locationDTO.setVisibleId(poiLocation.getVisibleId());
            locationDTO.setDescription("New location description");
            locationDTO.setGeoLocation(geoLocation());

            poiLocationGroupDTO.setLocations(singletonList(locationDTO));
            feature.update(poiLocationGroupDTO);

            runInTransaction(() -> {
                final List<PoiLocationGroup> pois = poiRepository.findAll();
                assertThat(pois, hasSize(1));
                final PoiLocationGroup poi = pois.get(0);

                final List<PoiLocation> locations = locationRepository.findAllByPoi(poi);
                assertEquality(poi, locations, poiLocationGroupDTO);
            });
        });
    }

    @Test
    public void updatePoi_deleteLocation() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        final PoiLocation poiLocation = model().newPoiLocation(poiLocationGroup);

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final PoiLocationGroupDTO poiLocationGroupDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            poiLocationGroupDTO.setLocations(emptyList());

            feature.update(poiLocationGroupDTO);

            runInTransaction(() -> {
                final List<PoiLocationGroup> pois = poiRepository.findAll();
                assertThat(pois, hasSize(1));
                final PoiLocationGroup poi = pois.get(0);

                final List<PoiLocation> locations = locationRepository.findAllByPoi(poi);

                assertThat(locations, is(empty()));
                assertEquality(poi, locations, poiLocationGroupDTO);
            });
        });
    }

    @Test
    public void createPoi_clubContactPerson() {
        final PoiLocationGroupDTO poiDTO = createPoiDto();
        onSavedAndAuthenticated(createUser(contactPerson), () -> feature.create(poiDTO));

        runInTransaction(() -> {
            final List<PoiLocationGroup> pois = poiRepository.findAll();
            assertThat(pois, hasSize(1));
            final PoiLocationGroup poi = pois.get(0);
            assertThat(poi.getPoiIdAllocation(), equalTo(poiIdAllocation));
            assertThat(poi.getDescription(), equalTo(poiDTO.getDescription()));
            assertThat(poi.getVisibleId(), is(notNullValue()));

            final List<PoiLocation> locations = locationRepository.findAll();
            assertThat(locations, is(empty()));
        });
    }

    @Test
    public void deletePoi_clubContactPerson() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);

        onSavedAndAuthenticated(createUser(contactPerson), () -> feature.delete(poiLocationGroup.getId()));

        runInTransaction(() -> {
            assertThat(poiRepository.findAll(), is(empty()));
            assertThat(locationRepository.findAll(), is(empty()));
        });
    }

    private PoiLocationGroupDTO createPoiDto() {
        final PoiLocationGroupDTO poiDTO = new PoiLocationGroupDTO();
        poiDTO.setClubId(club.getId());
        poiDTO.setDescription("description");
        poiDTO.setType(MINERAL_LICK);
        poiDTO.setLocations(Collections.emptyList());
        return poiDTO;
    }

    private PoiLocationDTO location(final long poiId) {
        final PoiLocationDTO locationDTO = new PoiLocationDTO();
        locationDTO.setPoiId(poiId);
        locationDTO.setVisibleId(nextPositiveInt());
        locationDTO.setDescription("description-" + nextPositiveInt());
        locationDTO.setGeoLocation(geoLocation());
        return locationDTO;
    }


    @Test
    public void listPois_huntingLeader() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);

        onSavedAndAuthenticated(createUser(huntingLeader), () -> {
            final List<PoiLocationGroupDTO> pois = feature.list(club.getId());
            assertThat(pois, hasSize(1));

            final PoiLocationGroupDTO dto = pois.get(0);
            assertEquality(poiLocationGroup, Collections.emptyList(), dto);
        });
    }

    @Test
    public void getPoi_huntingLeader() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);

        onSavedAndAuthenticated(createUser(huntingLeader), () -> {
            final PoiLocationGroupDTO dto = feature.read(poiLocationGroup.getId());

            assertEquality(poiLocationGroup, Collections.emptyList(), dto);
        });
    }

    @Test
    public void updatePoi_description_huntingLeader() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        final AtomicReference<PoiLocationGroupDTO> pointOfInterestDTO = new AtomicReference<>();

        onSavedAndAuthenticated(createUser(huntingLeader), () -> {
            final PoiLocationGroupDTO poiDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            poiDTO.setLocations(emptyList());
            poiDTO.setDescription("New description");

            pointOfInterestDTO.set(feature.update(poiDTO));

        });

        runInTransaction(() -> {
            final List<PoiLocationGroup> pois = poiRepository.findAll();
            assertThat(pois, hasSize(1));
            final PoiLocationGroup poi = pois.get(0);

            assertEquality(poi, Collections.emptyList(), pointOfInterestDTO.get());
        });
    }

    @Test
    public void updatePoi_addLocation_huntingLeader() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        final AtomicReference<PoiLocationGroupDTO> pointOfInterestDTO = new AtomicReference<>();

        onSavedAndAuthenticated(createUser(huntingLeader), () -> {
            final PoiLocationGroupDTO poiDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            poiDTO.setLocations(singletonList(location(poiLocationGroup.getId())));
            pointOfInterestDTO.set(feature.update(poiDTO));

        });

        runInTransaction(() -> {
            final List<PoiLocationGroup> pois = poiRepository.findAll();
            assertThat(pois, hasSize(1));
            final PoiLocationGroup poi = pois.get(0);

            final List<PoiLocation> locations = locationRepository.findAllByPoi(poi);
            assertEquality(poi, locations, pointOfInterestDTO.get());
        });
    }

    @Test
    public void updatePoi_updateLocation_huntingLeader() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        final PoiLocation poiLocation = model().newPoiLocation(poiLocationGroup);

        onSavedAndAuthenticated(createUser(huntingLeader), () -> {
            final PoiLocationGroupDTO poiLocationGroupDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            final PoiLocationDTO locationDTO = location(poiLocationGroup.getId());
            locationDTO.setId(poiLocation.getId());
            locationDTO.setVisibleId(poiLocation.getVisibleId());
            locationDTO.setDescription("New location description");
            locationDTO.setGeoLocation(geoLocation());

            poiLocationGroupDTO.setLocations(singletonList(locationDTO));
            feature.update(poiLocationGroupDTO);

            runInTransaction(() -> {
                final List<PoiLocationGroup> pois = poiRepository.findAll();
                assertThat(pois, hasSize(1));
                final PoiLocationGroup poi = pois.get(0);

                final List<PoiLocation> locations = locationRepository.findAllByPoi(poi);
                assertEquality(poi, locations, poiLocationGroupDTO);
            });
        });
    }

    @Test
    public void updatePoi_deleteLocation_huntingLeader() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);
        final PoiLocation poiLocation = model().newPoiLocation(poiLocationGroup);

        onSavedAndAuthenticated(createUser(huntingLeader), () -> {
            final PoiLocationGroupDTO poiLocationGroupDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            poiLocationGroupDTO.setLocations(emptyList());

            feature.update(poiLocationGroupDTO);

            runInTransaction(() -> {
                final List<PoiLocationGroup> pois = poiRepository.findAll();
                assertThat(pois, hasSize(1));
                final PoiLocationGroup poi = pois.get(0);

                final List<PoiLocation> locations = locationRepository.findAllByPoi(poi);

                assertThat(locations, is(empty()));
                assertEquality(poi, locations, poiLocationGroupDTO);
            });
        });
    }

    @Test
    public void createPoi_huntingLeader() {
        final PoiLocationGroupDTO poiDTO = createPoiDto();
        onSavedAndAuthenticated(createUser(huntingLeader), () -> feature.create(poiDTO));

        runInTransaction(() -> {
            final List<PoiLocationGroup> pois = poiRepository.findAll();
            assertThat(pois, hasSize(1));
            final PoiLocationGroup poi = pois.get(0);
            assertThat(poi.getPoiIdAllocation(), equalTo(poiIdAllocation));
            assertThat(poi.getDescription(), equalTo(poiDTO.getDescription()));
            assertThat(poi.getVisibleId(), is(notNullValue()));

            final List<PoiLocation> locations = locationRepository.findAll();
            assertThat(locations, is(empty()));
        });
    }

    @Test
    public void deletePoi_huntingLeader() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(poiIdAllocation, MINERAL_LICK);

        onSavedAndAuthenticated(createUser(huntingLeader), () -> feature.delete(poiLocationGroup.getId()));

        runInTransaction(() -> {
            assertThat(poiRepository.findAll(), is(empty()));
            assertThat(locationRepository.findAll(), is(empty()));
        });
    }

    @Test
    public void updatePoi_updateLocation_anotherClub_huntingLeader() {
        final PoiLocationGroup poiLocationGroup = model().newPoiLocationGroup(anotherPoiIdAllocation, MINERAL_LICK);
        final PoiLocation poiLocation = model().newPoiLocation(poiLocationGroup);

        onSavedAndAuthenticated(createUser(huntingLeader), () -> {
            final PoiLocationGroupDTO poiLocationGroupDTO = PoiLocationGroupDTO.create(poiLocationGroup);
            final PoiLocationDTO locationDTO = location(poiLocationGroup.getId());
            locationDTO.setId(poiLocation.getId());
            locationDTO.setVisibleId(poiLocation.getVisibleId());
            locationDTO.setDescription("New location description");
            locationDTO.setGeoLocation(geoLocation());

            poiLocationGroupDTO.setLocations(singletonList(locationDTO));
            // AccessDeniedException
            assertThrows(AccessDeniedException.class, () -> feature.update(poiLocationGroupDTO));
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
