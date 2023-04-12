package fi.riista.feature.account.area.union;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartner;
import fi.riista.integration.mapexport.MapPdfBasemap;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import fi.riista.util.MockTimeProvider;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PersonalAreaUnionFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PersonalAreaUnionFeature feature;

    @Resource
    PersonalAreaUnionRepository personalAreaUnionRepository;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    private Person person;
    private HuntingClub club1;
    private HuntingClubArea partnerArea1;
    private GeoLocation geoLocation;

    @Before
    public void setup() {
        person = model().newPerson();
        club1 = model().newHuntingClub();
        geoLocation = model().geoLocation();
        final GISZone zone = model().newGISZoneContaining(geoLocation);
        partnerArea1 = model().newHuntingClubArea(club1, zone);

        persistInNewTransaction();
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    @Ignore("H2 does not support bounds fetching")
    public void testCreateSmoke() {
        final PersonalAreaUnionCreateRequestDTO createDTO = new PersonalAreaUnionCreateRequestDTO("test", 2019);

        onSavedAndAuthenticated(createUser(person), () -> {
            runInTransaction(() -> {
                feature.createAreaUnionForMe(createDTO);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization_pdfMapExportOtherUser() {
        final PersonalAreaUnion union = model().newPersonalAreaUnion("unioni", person);
        model().newHarvestPermitAreaPartner(union.getHarvestPermitArea(), partnerArea1);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final MapPdfParameters mapPdfParameters = new MapPdfParameters();
            mapPdfParameters.setOverlay(MapPdfParameters.Overlay.NONE);
            mapPdfParameters.setPaperOrientation(MapPdfParameters.PaperOrientation.PORTRAIT);
            mapPdfParameters.setPaperSize(MapPdfParameters.PaperSize.A4);
            mapPdfParameters.setLayer(MapPdfBasemap.MAASTOKARTTA);

            feature.exportMapPdf(union.getId(), Locales.FI, mapPdfParameters);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization_pdfPartnersExportOtherUser() {
        final PersonalAreaUnion union = model().newPersonalAreaUnion("unioni", person);
        model().newHarvestPermitAreaPartner(union.getHarvestPermitArea(), partnerArea1);

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.exportPartnersPdf(union.getId(), Locales.FI);
        });
    }

    @Test
    @Ignore("H2 does not support bounds fetching")
    public void testCreateSmokeModerator() {
        final PersonalAreaUnionCreateRequestDTO createDTO = new PersonalAreaUnionCreateRequestDTO("test", 2019);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                feature.createAreaUnionForPerson(createDTO, person.getId());
            });
        });
    }

    @Test
    @Ignore("H2 does not support bounds fetching")
    public void testChangeName() {
        model().newPersonalAreaUnion("test1", person);

        onSavedAndAuthenticated(createUser(person), () -> {
            runInTransaction(() -> {
                final List<PersonalAreaUnion> all = personalAreaUnionRepository.findAll();
                assertThat(all, hasSize(1));
                final PersonalAreaUnionDTO updated = feature.updateAreaUnion(all.get(0).getId(),
                        new PersonalAreaUnionModifyRequestDTO("updated"));

                assertEquals("updated", updated.getName());
            });
        });

    }

    @Test
    public void testAddPartner() {
        model().newPersonalAreaUnion("test1", person);

        onSavedAndAuthenticated(createUser(person), () -> {
            runInTransaction(() -> {
                final List<PersonalAreaUnion> all = personalAreaUnionRepository.findAll();
                assertThat(all, hasSize(1));
                final PersonalAreaUnionAddPartnerDTO dto = new PersonalAreaUnionAddPartnerDTO(all.get(0).getId(),
                        partnerArea1.getExternalId());

                feature.addPartner(dto);
            });
        });

        runInTransaction(() -> {
            final Optional<HuntingClubArea> partnerOptional =
                    huntingClubAreaRepository.findByExternalId(partnerArea1.getExternalId());
            assertTrue(partnerOptional.isPresent());
            final List<PersonalAreaUnion> all = personalAreaUnionRepository.findAll();
            assertThat(all, hasSize(1));
            final Set<HarvestPermitAreaPartner> partners = all.get(0).getHarvestPermitArea().getPartners();
            assertThat(partners, hasSize(1));

            // TODO: Assert geometry matches
        });
    }

    @Test
    public void testRefreshPartner() {
        MockTimeProvider.mockTime(DateUtil.now().getMillis());
        final PersonalAreaUnion personalAreaUnion = model().newPersonalAreaUnion("test1", person);
        final HarvestPermitAreaPartner harvestPermitAreaPartner =
                model().newHarvestPermitAreaPartner(personalAreaUnion.getHarvestPermitArea(), partnerArea1);
        persistInNewTransaction();
        MockTimeProvider.advance();


        final GISZone gisZone = model().newGISZoneNotContaining(geoLocation);
        partnerArea1.getZone().setGeom(gisZone.getGeom());
        persistInNewTransaction();
        MockTimeProvider.advance();

        final DateTime now = DateUtil.now();

        onSavedAndAuthenticated(createUser(person), () -> {
            runInTransaction(() -> {
                final List<PersonalAreaUnion> all = personalAreaUnionRepository.findAll();
                assertThat(all, hasSize(1));
                final Optional<HuntingClubArea> partnerOptional =
                        huntingClubAreaRepository.findByExternalId(partnerArea1.getExternalId());
                assertTrue(partnerOptional.isPresent());

                feature.refreshPartner(all.get(0).getId(), harvestPermitAreaPartner.getId());
            });
        });

        runInTransaction(() -> {
            final List<PersonalAreaUnion> all = personalAreaUnionRepository.findAll();
            assertThat(all, hasSize(1));
            final Set<HarvestPermitAreaPartner> partners = all.get(0).getHarvestPermitArea().getPartners();
            assertThat(partners, hasSize(1));
            assertEquals(now.getMillis(), all.get(0).getModificationTime().getMillis());
            // TODO: Assert geometry matches
        });
    }

    @Test
    public void testRemovePartner() {
        final PersonalAreaUnion personalAreaUnion = model().newPersonalAreaUnion("test1", person);
        final HarvestPermitAreaPartner harvestPermitAreaPartner =
                model().newHarvestPermitAreaPartner(personalAreaUnion.getHarvestPermitArea(), partnerArea1);
        persistInNewTransaction();

        onSavedAndAuthenticated(createUser(person), () -> {
            runInTransaction(() -> {
                final List<PersonalAreaUnion> all = personalAreaUnionRepository.findAll();
                assertThat(all, hasSize(1));

                feature.removePartner(all.get(0).getId(), harvestPermitAreaPartner.getId());
            });
        });

        runInTransaction(() -> {

            final List<PersonalAreaUnion> all = personalAreaUnionRepository.findAll();
            assertThat(all, hasSize(1));
            final Set<HarvestPermitAreaPartner> partners = all.get(0).getHarvestPermitArea().getPartners();
            assertThat(partners, hasSize(0));
        });
    }
}
