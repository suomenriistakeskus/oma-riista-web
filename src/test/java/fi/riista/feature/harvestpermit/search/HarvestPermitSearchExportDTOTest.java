package fi.riista.feature.harvestpermit.search;


import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.harvestpermit.search.HarvestPermitValidity.ACTIVE;
import static fi.riista.feature.harvestpermit.search.HarvestPermitValidity.FUTURE;
import static fi.riista.feature.harvestpermit.search.HarvestPermitValidity.PASSED;
import static fi.riista.feature.harvestpermit.search.HarvestPermitValidity.UNKNOWN;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;


public class HarvestPermitSearchExportDTOTest extends EmbeddedDatabaseTest {

    private final LocalDate PAST_DATE = DateUtil.today().minusDays(1);
    private final LocalDate TODAY_DATE = DateUtil.today();
    private final LocalDate FUTURE_DATE = DateUtil.today().plusDays(1);
    private final LocalDate NO_DATE = null;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    HarvestPermitSearchFeature feature;

    @Test
    public void test_validity_no_speciesAmounts() {
        PermitHelper.create(model());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                assertThat(getExportDTO().getValidity(), equalTo(UNKNOWN));
            });
        });
    }

    @Test
    public void test_validity_one_speciesAmounts_now() {
        PermitHelper.create(model()).addSpeciesAmount(PAST_DATE, FUTURE_DATE, NO_DATE, NO_DATE);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                assertThat(getExportDTO().getValidity(), equalTo(ACTIVE));
            });
        });
    }

    @Test
    public void test_validity_one_speciesAmounts_today() {
        PermitHelper.create(model()).addSpeciesAmount(TODAY_DATE, TODAY_DATE, NO_DATE, NO_DATE);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                assertThat(getExportDTO().getValidity(), equalTo(ACTIVE));
            });
        });
    }

    @Test
    public void test_validity_one_speciesAmounts_inPast() {
        PermitHelper.create(model()).addSpeciesAmount(PAST_DATE, PAST_DATE, NO_DATE, NO_DATE);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                assertThat(getExportDTO().getValidity(), equalTo(PASSED));
            });
        });
    }

    @Test
    public void test_validity_one_speciesAmounts_inFuture() {
        PermitHelper.create(model()).addSpeciesAmount(FUTURE_DATE, FUTURE_DATE, NO_DATE, NO_DATE);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                assertThat(getExportDTO().getValidity(), equalTo(FUTURE));
            });
        });
    }

    @Test
    public void test_validity_one_speciesAmounts_now_with2ndDates() {
        PermitHelper.create(model()).addSpeciesAmount(PAST_DATE, PAST_DATE, FUTURE_DATE, FUTURE_DATE);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                assertThat(getExportDTO().getValidity(), equalTo(ACTIVE));
            });
        });
    }

    @Test
    public void test_validity_many_speciesAmounts_now() {
        PermitHelper.create(model())
                .addSpeciesAmount(PAST_DATE, PAST_DATE, NO_DATE, NO_DATE)
                .addSpeciesAmount(PAST_DATE, PAST_DATE, TODAY_DATE, FUTURE_DATE);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                assertThat(getExportDTO().getValidity(), equalTo(ACTIVE));
            });
        });
    }

    @Test
    public void test_contacts_onlyOriginal() {
        PermitHelper.create(model());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                final HarvestPermit permit = getPermit();
                final List<HarvestPermitSearchExportDTO> dtos = feature.doWithSingleQueries(Arrays.asList(permit));
                assertThat(dtos, hasSize(1));
                final HarvestPermitSearchExportDTO dto = dtos.get(0);
                assertThat(dto.getContacts(), hasSize(1));
                assertThatContactEquals(dto.getContacts().get(0), permit.getOriginalContactPerson());
            });
        });
    }

    @Test
    public void test_contacts_originalAndAdditional() {
        final Person additionalContact = model().newPerson();
        persistInNewTransaction();

        PermitHelper.create(model()).addContact(additionalContact);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                final HarvestPermit permit = getPermit();
                final List<HarvestPermitSearchExportDTO> dtos = feature.doWithSingleQueries(Arrays.asList(permit));
                assertThat(dtos, hasSize(1));
                final HarvestPermitSearchExportDTO dto = dtos.get(0);
                assertThat(dto.getContacts(), hasSize(2));
                assertThatContactEquals(dto.getContacts().get(0), permit.getContactPersons().get(0).getContactPerson());
                assertThatContactEquals(dto.getContacts().get(1), permit.getOriginalContactPerson());
            });
        });
    }

    @Test
    public void test_generalPermitData() {
        PermitHelper.create(model());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            runInTransaction(() -> {
                final HarvestPermit permit = getPermit();
                final List<HarvestPermitSearchExportDTO> dtos = feature.doWithSingleQueries(Arrays.asList(permit));
                assertThat(dtos, hasSize(1));
                final HarvestPermitSearchExportDTO dto = dtos.get(0);
                assertThatPermitInfoEquals(dto, permit);
            });
        });
    }

    /**
     * Helpers
     */

    private void assertThatPermitInfoEquals(final HarvestPermitSearchExportDTO dto,
                                            final HarvestPermit permit) {
        assertThat(dto.getPermitNumber(), equalTo(permit.getPermitNumber()));
        assertThat(dto.getPermitType(), equalTo(permit.getPermitType()));
        assertThat(dto.getPermitHolderName(), equalTo(permit.getPermitHolder().getName()));
        assertThat(dto.getPermitHolderType(), equalTo(permit.getPermitHolder().getType()));
        assertThat(dto.getHarvestReportState(), equalTo(permit.getHarvestReportState()));
        assertThat(dto.getRka(), equalTo(permit.getRhy().getRiistakeskuksenAlue().getNameLocalisation()));
    }

    private void assertThatContactEquals(final HarvestPermitSearchExportDTO.ContactPersonDTO actual,
                                         final Person expected) {

        assertThat(actual.getHunterNumber(), equalTo(expected.getHunterNumber()));
        assertThat(actual.getFirstName(), equalTo(expected.getFirstName()));
        assertThat(actual.getLastName(), equalTo(expected.getLastName()));
        assertThat(actual.getPhoneNumber(), equalTo(expected.getPhoneNumber()));
        assertThat(actual.getEmail(), equalTo(expected.getEmail()));
    }

    private HarvestPermit getPermit() {
        final List<HarvestPermit> allPermits = harvestPermitRepository.findAll();
        assertThat(allPermits, hasSize(1));
        return allPermits.get(0);
    }

    private HarvestPermitSearchExportDTO getExportDTO() {
        final List<HarvestPermitSearchExportDTO> dtos = feature.doWithSingleQueries(Arrays.asList(getPermit()));
        assertThat(dtos, hasSize(1));
        return dtos.get(0);
    }

    private static class PermitHelper {

        private final EntitySupplier supplier;

        private final HarvestPermit permit;

        private final GameSpecies otterSpecies;

        public static PermitHelper create(final EntitySupplier supplier) {
            return new PermitHelper(supplier);
        }

        private PermitHelper(final EntitySupplier supplier) {
            this.supplier = supplier;
            permit = supplier.newHarvestPermit();
            permit.setPermitTypeCode("123");
            this.otterSpecies = supplier.newGameSpecies(OFFICIAL_CODE_OTTER);
        }

        public PermitHelper addSpeciesAmount(final LocalDate beginDate, final LocalDate endDate,
                                             final LocalDate beginDate2, final LocalDate endDate2) {

            supplier.newHarvestPermitSpeciesAmount(permit, otterSpecies, beginDate, endDate, beginDate2, endDate2);
            return this;
        }

        public PermitHelper addContact(final Person person) {
            supplier.newHarvestPermitContactPerson(permit, person);
            return this;
        }
    }
}
