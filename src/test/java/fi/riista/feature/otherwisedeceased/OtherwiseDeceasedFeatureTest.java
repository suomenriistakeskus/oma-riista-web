package fi.riista.feature.otherwisedeceased;

import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.currentYear;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(Theories.class)
public class OtherwiseDeceasedFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private OtherwiseDeceasedFeature feature;

    @Resource
    private OtherwiseDeceasedRepository otherwiseDeceasedRepository;

    @Resource
    private OtherwiseDeceasedChangeRepository changeRepository;

    private SystemUser privilegedModerator;

    private SystemUser notAuthorised;

    private EntitySupplier es;

    private GameSpecies gameSpecies1;
    private GameSpecies gameSpecies2;

    private GeoLocation location1;
    private GeoLocation location2;

    private Municipality municipality1;
    private Municipality municipality2;

    private Riistanhoitoyhdistys rhy1;
    private Riistanhoitoyhdistys rhy2;

    @Before
    public void setUp() {
        es = getEntitySupplier();
        privilegedModerator = createNewModerator(SystemUserPrivilege.MUUTOIN_KUOLLEET);
        notAuthorised = createNewModerator();
        gameSpecies1 = es.newGameSpecies();
        gameSpecies2 = es.newGameSpecies();
        location1 = geoLocation();
        location2 = geoLocation();
        municipality1 = es.newMunicipality();
        municipality2 = es.newMunicipality();
        rhy1 = es.newRiistanhoitoyhdistys();
        rhy2 = es.newRiistanhoitoyhdistys();
        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void searchPage_moderatorMustHavePrivilege() {
        onSavedAndAuthenticated(notAuthorised, () -> feature.searchPage(new OtherwiseDeceasedFilterDTO(), PageRequest.of(0, 10)));
    }

    @Test
    public void searchPage_returnsEmptyListIfNoneFound() {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedFilterDTO dto = new OtherwiseDeceasedFilterDTO();
            dto.setBeginDate(DateUtil.beginOfCalendarYear(currentYear()).toLocalDate());
            dto.setEndDate(DateUtil.beginOfCalendarYear(currentYear() + 1).toLocalDate());
            final Slice<OtherwiseDeceasedBriefDTO> dtos = feature.searchPage(dto, PageRequest.of(0, 10));
            assertThat(dtos.getContent(), hasSize(0));
        });
    }

    @Test
    public void searchPage_andOnlyByThatYear() {
        final OtherwiseDeceased item =
                es.newOtherwiseDeceased(new DateTime(2021, 1, 1, 0, 0, Constants.DEFAULT_TIMEZONE)); // 1.1.2021 0:00
        es.newOtherwiseDeceased(new DateTime(2020, 12, 31, 23, 59, 59, 999, Constants.DEFAULT_TIMEZONE));
        es.newOtherwiseDeceased(new DateTime(2022, 1, 1, 0, 1, Constants.DEFAULT_TIMEZONE)); // 1.1.2022 0:01

        onSavedAndAuthenticated(privilegedModerator, () -> {
            runInTransaction(() -> {
                final OtherwiseDeceasedFilterDTO dto = new OtherwiseDeceasedFilterDTO();
                dto.setBeginDate(DateUtil.beginOfCalendarYear(2021).toLocalDate());
                dto.setEndDate(DateUtil.beginOfCalendarYear(2022).minusDays(1).toLocalDate());
                final Slice<OtherwiseDeceasedBriefDTO> dtos = feature.searchPage(dto, PageRequest.of(0, 10));
                final List<OtherwiseDeceasedBriefDTO> results = dtos.getContent();

                assertThat(results, hasSize(1));

                final OtherwiseDeceasedBriefDTO result = results.get(0);
                assertEquals(item, result);
            });
        });
    }

    @Test
    public void searchPage_speciesFiltering() {
        withRhy(rhy -> {
            final GameSpecies lynxSpecies = es.newGameSpecies(OFFICIAL_CODE_LYNX);
            final GameSpecies wolfSpecies = es.newGameSpecies(OFFICIAL_CODE_WOLF);
            final OtherwiseDeceased lynx = createEntity(rhy, lynxSpecies);
            createEntity(rhy, wolfSpecies);

            onSavedAndAuthenticated(privilegedModerator, () -> {
                runInTransaction(() -> {
                    final OtherwiseDeceasedFilterDTO dto = new OtherwiseDeceasedFilterDTO();
                    dto.setBeginDate(DateUtil.beginOfCalendarYear(2021).toLocalDate());
                    dto.setEndDate(DateUtil.beginOfCalendarYear(2022).minusDays(1).toLocalDate());
                    dto.setGameSpeciesCode(OFFICIAL_CODE_LYNX);
                    final Slice<OtherwiseDeceasedBriefDTO> dtos = feature.searchPage(dto, PageRequest.of(0, 10));
                    final List<OtherwiseDeceasedBriefDTO> results = dtos.getContent();

                    assertThat(results, hasSize(1));

                    final OtherwiseDeceasedBriefDTO result = results.get(0);
                    assertEquals(lynx, result);
                });
            });

        });
    }

    @Test
    public void searchPage_rkaFiltering() {
        withRhy(rhyInOtherRka -> {
            final GameSpecies lynxSpecies = es.newGameSpecies(OFFICIAL_CODE_LYNX);
            createEntity(rhyInOtherRka, lynxSpecies);

            withRhy(rhy -> {
                final Riistanhoitoyhdistys otherRhyInSameRka =
                        es.newRiistanhoitoyhdistys((RiistakeskuksenAlue) rhy.getRiistakeskuksenAlue());

                final OtherwiseDeceased lynx = createEntity(rhy, lynxSpecies);
                final OtherwiseDeceased otherLynx = createEntity(otherRhyInSameRka, lynxSpecies);

                onSavedAndAuthenticated(privilegedModerator, () -> {
                    runInTransaction(() -> {
                        final OtherwiseDeceasedFilterDTO dto = new OtherwiseDeceasedFilterDTO();
                        dto.setBeginDate(DateUtil.beginOfCalendarYear(2021).toLocalDate());
                        dto.setEndDate(DateUtil.beginOfCalendarYear(2022).minusDays(1).toLocalDate());
                        dto.setRkaOfficialCode(rhy.getRiistakeskuksenAlue().getOfficialCode());

                        final Slice<OtherwiseDeceasedBriefDTO> dtos = feature.searchPage(dto, PageRequest.of(0, 10));

                        assertThat(dtos.getContent(), hasSize(2));

                        final Map<String, OtherwiseDeceasedBriefDTO> indexed =
                                F.index(dtos.getContent(), briefDto -> briefDto.getRhy().getOfficialCode());

                        assertEquals(lynx, indexed.get(rhy.getOfficialCode()));
                        assertEquals(otherLynx, indexed.get(otherRhyInSameRka.getOfficialCode()));
                    });
                });

            });
        });
    }

    @Test
    public void searchPage_rejectedFiltering() {
        withRhy(rhy -> {
            final GameSpecies lynxSpecies = es.newGameSpecies(OFFICIAL_CODE_LYNX);
            final OtherwiseDeceased lynx = createEntity(rhy, lynxSpecies);
            final OtherwiseDeceased rejectedLynx = createEntity(rhy, lynxSpecies);
            rejectedLynx.setRejected(true);

            onSavedAndAuthenticated(privilegedModerator, () -> {
                runInTransaction(() -> {
                    final OtherwiseDeceasedFilterDTO dto = new OtherwiseDeceasedFilterDTO();
                    dto.setBeginDate(DateUtil.beginOfCalendarYear(2021).toLocalDate());
                    dto.setEndDate(DateUtil.beginOfCalendarYear(2022).minusDays(1).toLocalDate());

                    final Slice<OtherwiseDeceasedBriefDTO> valid = feature.searchPage(dto, PageRequest.of(0, 10));

                    dto.setShowRejected(true);
                    final Slice<OtherwiseDeceasedBriefDTO> rejected = feature.searchPage(dto, PageRequest.of(0, 10));

                    assertThat(valid.getContent(), hasSize(1));
                    assertEquals(lynx, valid.getContent().get(0));

                    assertThat(valid.getContent(), hasSize(1));
                    assertEquals(rejectedLynx, rejected.getContent().get(0));
                });
            });

        });
    }

    @Test
    public void searchPage_rhyFiltering() {
        withRhy(rhyInOtherRka -> {
            final GameSpecies lynxSpecies = es.newGameSpecies(OFFICIAL_CODE_LYNX);
            createEntity(rhyInOtherRka, lynxSpecies);

            withRhy(rhy -> {
                final Riistanhoitoyhdistys otherRhyInSameRka =
                        es.newRiistanhoitoyhdistys((RiistakeskuksenAlue) rhy.getRiistakeskuksenAlue());

                final OtherwiseDeceased lynx = createEntity(rhy, lynxSpecies);
                final OtherwiseDeceased otherLynx = createEntity(otherRhyInSameRka, lynxSpecies);

                onSavedAndAuthenticated(privilegedModerator, () -> {
                    runInTransaction(() -> {
                        final OtherwiseDeceasedFilterDTO dto = new OtherwiseDeceasedFilterDTO();
                        dto.setBeginDate(DateUtil.beginOfCalendarYear(2021).toLocalDate());
                        dto.setEndDate(DateUtil.beginOfCalendarYear(2022).minusDays(1).toLocalDate());
                        dto.setRkaOfficialCode(rhy.getRiistakeskuksenAlue().getOfficialCode());
                        dto.setRhyOfficialCode(rhy.getOfficialCode());

                        final Slice<OtherwiseDeceasedBriefDTO> dtos = feature.searchPage(dto, PageRequest.of(0, 10));

                        assertThat(dtos.getContent(), hasSize(1));

                        assertEquals(lynx, dtos.getContent().get(0));
                    });
                });

            });
        });

    }

    @Test
    public void searchPage_paging() {
        withRhy(rhy -> {
            final GameSpecies lynxSpecies = es.newGameSpecies(OFFICIAL_CODE_LYNX);
            final GameSpecies wolfSpecies = es.newGameSpecies(OFFICIAL_CODE_WOLF);

            final OtherwiseDeceased lynx = createEntity(rhy, lynxSpecies);
            final OtherwiseDeceased wolf = createEntity(rhy, wolfSpecies);
            wolf.setPointOfTime(lynx.getPointOfTime().plusHours(1)); // Newer should be first

            onSavedAndAuthenticated(privilegedModerator, () -> {
                runInTransaction(() -> {
                    final OtherwiseDeceasedFilterDTO dto = new OtherwiseDeceasedFilterDTO();
                    dto.setBeginDate(DateUtil.beginOfCalendarYear(2021).toLocalDate());
                    dto.setEndDate(DateUtil.beginOfCalendarYear(2022).minusDays(1).toLocalDate());

                    final Slice<OtherwiseDeceasedBriefDTO> sliceWithWolf = feature.searchPage(dto, PageRequest.of(0, 1));

                    assertThat(sliceWithWolf.getContent(), hasSize(1));
                    assertThat(sliceWithWolf.hasNext(), is(true));
                    assertEquals(wolf, sliceWithWolf.getContent().get(0));

                    final Slice<OtherwiseDeceasedBriefDTO> sliceWithLynx = feature.searchPage(dto, PageRequest.of(1, 1));

                    assertThat(sliceWithLynx.getContent(), hasSize(1));
                    assertThat(sliceWithLynx.hasNext(), is(false));
                    assertEquals(lynx, sliceWithLynx.getContent().get(0));
                });
            });

        });
    }

    @Test(expected = AccessDeniedException.class)
    public void getDetails_moderatorMustHavePrivilege() {
        onSavedAndAuthenticated(notAuthorised, () -> feature.getDetails(1));
    }

    @Test(expected = NotFoundException.class)
    public void getDetails_throwsExceptionIfNotFound() {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            feature.getDetails(1);
        });
    }

    @Theory
    public void getDetails_causeAndSourceValues(final OtherwiseDeceasedCause cause,
                                                final OtherwiseDeceasedSource source) {
        final OtherwiseDeceased item = es.newOtherwiseDeceased(cause, source);
        onSavedAndAuthenticated(privilegedModerator, () -> {
            runInTransaction(() -> {
                OtherwiseDeceasedDTO result = feature.getDetails(item.getId());
                assertThatEquals(result, item);
                checkOutgoingDtoFields(result);
            });
        });
    }

    @Theory
    public void getDetails_ageAndGenderValues(final GameAge age, final GameGender gender) {
        final OtherwiseDeceased item = es.newOtherwiseDeceased(age, gender);
        onSavedAndAuthenticated(privilegedModerator, () -> {
            runInTransaction(() -> {
                OtherwiseDeceasedDTO result = feature.getDetails(item.getId());
                assertThatEquals(result, item);
                checkOutgoingDtoFields(result);
            });
        });
    }

    @Test
    public void getDetails_optionalFieldsCanBeNull() {
        final OtherwiseDeceased item = es.newOtherwiseDeceased();
        item.setWeight(null);
        item.setDescription(null);
        item.setAdditionalInfo(null);
        item.setCauseDescription(null);
        item.setSourceDescription(null);
        onSavedAndAuthenticated(privilegedModerator, () -> {
            runInTransaction(() -> {
                OtherwiseDeceasedDTO result = feature.getDetails(item.getId());
                assertThat(result.getWeight(), is(nullValue()));
                assertThat(result.getDescription(), is(nullValue()));
                assertThat(result.getAdditionalInfo(), is(nullValue()));
                assertThat(result.getCauseDescription(), is(nullValue()));
                assertThat(result.getSourceDescription(), is(nullValue()));
                assertThatEquals(result, item);
                checkOutgoingDtoFields(result);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void save_moderatorMustHavePrivilege() {
        onSavedAndAuthenticated(notAuthorised, () -> {
            feature.save(newOtherwiseDeceasedDTO());
        });
    }

    @Theory
    public void save_andCreate_withAgeAndGender(final GameAge age, final GameGender gender) {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO input = newOtherwiseDeceasedDTO();
            input.setAge(age);
            input.setGender(gender);
            saveAndCheckDTO(input);
        });
    }

    @Theory
    public void save_andCreate_withCauseAndSource(final OtherwiseDeceasedCause cause, final OtherwiseDeceasedSource source) {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO input = newOtherwiseDeceasedDTO();
            input.setCause(cause);
            input.setSource(source);
            saveAndCheckDTO(input);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void save_andCreate_descriptionNeededWhenCauseOther() {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO input = newOtherwiseDeceasedDTO();
            input.setCause(OtherwiseDeceasedCause.OTHER);
            input.setCauseDescription("");
            saveAndCheckDTO(input);
        });
    }

    @Theory
    public void save_andCreate_descriptionAllowedForAllCauses(final OtherwiseDeceasedCause cause) {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO input = newOtherwiseDeceasedDTO();
            input.setCause(cause);
            input.setCauseDescription("causeDescription");
            saveAndCheckDTO(input);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void save_andCreate_descriptionNeededWhenSourceOther() {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO input = newOtherwiseDeceasedDTO();
            input.setSource(OtherwiseDeceasedSource.OTHER);
            input.setSourceDescription("");
            saveAndCheckDTO(input);
        });
    }

    @Theory
    public void save_andCreate_descriptionAllowedForAllCauses(final OtherwiseDeceasedSource source) {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO input = newOtherwiseDeceasedDTO();
            input.setSource(source);
            input.setSourceDescription("sourceDescription");
            saveAndCheckDTO(input);
        });
    }

    @Theory
    public void save_andCreate_withRejectedAndNoExactLocation(final boolean isRejected, final boolean noExactLocation) {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO input = newOtherwiseDeceasedDTO();
            input.setRejected(isRejected);
            input.setNoExactLocation(noExactLocation);
            saveAndCheckDTO(input);
        });
    }

    @Theory
    public void save_andUpdate_withAgeAndGender(final GameAge age, final GameGender gender) {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO created = feature.save(newOtherwiseDeceasedDTO());
            created.setAge(age);
            created.setGender(gender);
            created.setReasonForChange("Reason for change");
            updateAndCheckDTO(created);
        });
    }

    @Theory
    public void save_andUpdate_withCauseAndSource(final OtherwiseDeceasedCause cause, final OtherwiseDeceasedSource source) {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO created = feature.save(newOtherwiseDeceasedDTO());
            created.setCause(cause);
            created.setSource(source);
            created.setReasonForChange("Reason for change");
            updateAndCheckDTO(created);
        });
    }

    @Theory
    public void save_andUpdate_withRejectedAndNoExactLocation(final boolean isRejected, final boolean noExactLocation) {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO created = feature.save(newOtherwiseDeceasedDTO());
            created.setRejected(isRejected);
            created.setNoExactLocation(noExactLocation);
            created.setReasonForChange("Reason for change");
            updateAndCheckDTO(created);
        });
    }

    @Test
    public void save_andUpdate_location() {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO created = feature.save(newOtherwiseDeceasedDTO());
            created.setGeoLocation(location2);
            final OtherwiseDeceasedDTO updated = feature.save(created);
            assertThat(updated.getGeoLocation(), equalTo(location2));
            assertThat(updated.getMunicipality(), is(notNullValue()));
            assertThat(updated.getRhy(), is(notNullValue()));
            assertThat(updated.getRka(), is(notNullValue()));
            assertThat(updated.getMunicipality(), not(equalTo(created.getMunicipality())));
            assertThat(updated.getRhy(), not(equalTo(created.getRhy())));
            assertThat(updated.getRka(), not(equalTo(created.getRka())));
        });
    }

    @Test
    public void save_andUpdate_species() {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO created = feature.save(newOtherwiseDeceasedDTO());
            created.setGameSpeciesCode(gameSpecies2.getOfficialCode());
            final OtherwiseDeceasedDTO updated = feature.save(created);
            assertThat(updated.getGameSpeciesCode(), equalTo(created.getGameSpeciesCode()));
        });
    }

    @Test(expected = NotFoundException.class)
    public void save_andUpdate_itemNotFound() {
        onSavedAndAuthenticated(privilegedModerator, () -> {
            final OtherwiseDeceasedDTO created = feature.save(newOtherwiseDeceasedDTO());
            created.setId(created.getId() + 1);
            feature.save(created);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void save_withAttachments_moderatorMustHavePrivilege() throws IOException {
        authenticate(notAuthorised);
        feature.save(newOtherwiseDeceasedDTO(), emptyList());
    }

    @Test
    public void save_withAttachments_butThereIsNone() throws IOException {
        authenticate(privilegedModerator);
        final OtherwiseDeceasedDTO dto = newOtherwiseDeceasedDTO();
        final OtherwiseDeceasedDTO saved = feature.save(dto, emptyList());
        assertThat(saved.getAttachments(), hasSize(0));
    }

    @Test
    public void save_withAttachments_andTheyExists() throws IOException {
        authenticate(privilegedModerator);
        final OtherwiseDeceasedDTO dto = newOtherwiseDeceasedDTO();
        final MultipartFile file = newAttachment();
        final OtherwiseDeceasedDTO saved = feature.save(dto, Arrays.asList(file));
        runInTransaction(() -> {
            assertThat(saved.getAttachments(), hasSize(1));
            assertThat(saved.getAttachments().get(0).getFilename(), equalTo(file.getOriginalFilename()));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void reject_moderatorMustHavePrivilege() {
        onSavedAndAuthenticated(notAuthorised, () -> feature.reject(1));
    }

    @Test(expected = NotFoundException.class)
    public void reject_itemNotFound() {
        onSavedAndAuthenticated(privilegedModerator, () -> feature.reject(1));
    }

    @Test
    public void reject_restoredItem() {
        final OtherwiseDeceased item = es.newOtherwiseDeceased();
        onSavedAndAuthenticated(privilegedModerator, () -> {
            feature.reject(item.getId());
            runInTransaction(() -> {
                final OtherwiseDeceased saved = otherwiseDeceasedRepository.findById(item.getId()).orElseThrow(NotFoundException::new);
                assertThat(saved.isRejected(), is(true));
                final List<OtherwiseDeceasedChange> changes = changeRepository.findAllByOtherwiseDeceasedOrderByPointOfTime(saved);
                assertThat(changes, hasSize(1));
                final OtherwiseDeceasedChange change = changes.get(0);
                assertThat(change.getChangeType(), equalTo(OtherwiseDeceasedChange.ChangeType.DELETE));
                assertThat(change.getReasonForChange(), is(nullValue()));
                assertThat(change.getUserId(), equalTo(privilegedModerator.getId()));
            });
        });
    }

    @Test
    public void reject_rejectedItem() {
        final OtherwiseDeceased item = es.newOtherwiseDeceased();
        item.setRejected(true);
        onSavedAndAuthenticated(privilegedModerator, () -> {
            feature.reject(item.getId());
            runInTransaction(() -> {
                final OtherwiseDeceased saved = otherwiseDeceasedRepository.findById(item.getId()).orElseThrow(NotFoundException::new);
                assertThat(saved.isRejected(), is(true));
                final List<OtherwiseDeceasedChange> changes = changeRepository.findAllByOtherwiseDeceasedOrderByPointOfTime(saved);
                assertThat(changes, hasSize(0));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void restore_moderatorMustHavePrivilege() {
        onSavedAndAuthenticated(notAuthorised, () -> feature.restore(1));
    }

    @Test(expected = NotFoundException.class)
    public void restore_itemNotFound() {
        onSavedAndAuthenticated(privilegedModerator, () -> feature.restore(1));
    }

    @Test
    public void restore_rejectedItem() {
        final OtherwiseDeceased item = es.newOtherwiseDeceased();
        item.setRejected(true);
        onSavedAndAuthenticated(privilegedModerator, () -> {
            feature.restore(item.getId());
            runInTransaction(() -> {
                final OtherwiseDeceased saved = otherwiseDeceasedRepository.findById(item.getId()).orElseThrow(NotFoundException::new);
                assertThat(saved.isRejected(), is(false));
                final List<OtherwiseDeceasedChange> changes = changeRepository.findAllByOtherwiseDeceasedOrderByPointOfTime(saved);
                assertThat(changes, hasSize(1));
                final OtherwiseDeceasedChange change = changes.get(0);
                assertThat(change.getChangeType(), equalTo(OtherwiseDeceasedChange.ChangeType.RESTORE));
                assertThat(change.getReasonForChange(), is(nullValue()));
                assertThat(change.getUserId(), equalTo(privilegedModerator.getId()));
            });
        });
    }

    @Test
    public void restore_restoredItem() {
        final OtherwiseDeceased item = es.newOtherwiseDeceased();
        onSavedAndAuthenticated(privilegedModerator, () -> {
            feature.restore(item.getId());
            runInTransaction(() -> {
                final OtherwiseDeceased saved = otherwiseDeceasedRepository.findById(item.getId()).orElseThrow(NotFoundException::new);
                assertThat(saved.isRejected(), is(false));
                final List<OtherwiseDeceasedChange> changes = changeRepository.findAllByOtherwiseDeceasedOrderByPointOfTime(saved);
                assertThat(changes, hasSize(0));
            });
        });
    }

    private MultipartFile newAttachment() {
        final byte[] attachmentData = new byte[4096];
        new Random().nextBytes(attachmentData);
        final String filename = "test" + nextLong() + ".png";
        return new MockMultipartFile(filename, "//test/" + filename, "image/png", attachmentData);
    }

    private void saveAndCheckDTO(final OtherwiseDeceasedDTO input) {
        final OtherwiseDeceasedDTO output = feature.save(input);
        assertThatEquals(output, input);
        checkOutgoingDtoFields(output);
        checkSavedDtoFields(output);
        assertThat(output.isRejected(), is(false), "rejected should be false after creation");
        assertThat(output.getChangeHistory(), hasSize(1), "no change info");
        final OtherwiseDeceasedChangeDTO changeDTO = output.getChangeHistory().get(0);
        assertThat(changeDTO.getReasonForChange(), is(nullValue()), "change reason not null");
        assertThat(changeDTO.getChangeType(), equalTo(OtherwiseDeceasedChange.ChangeType.CREATE), "change type not CREATE");
        assertThat(changeDTO.getAuthor().getId(), equalTo(privilegedModerator.getId()), "author id mismatch");
    }

    private void updateAndCheckDTO(final OtherwiseDeceasedDTO created) {
        final OtherwiseDeceasedDTO updated = feature.save(created);
        assertThatEquals(created, updated);
        checkOutgoingDtoFields(updated);
        checkSavedDtoFields(updated);
        assertThat(updated.isRejected(), is(false), "rejected should be false after creation");

        assertThat(updated.getChangeHistory(), hasSize(2), "no change info");
        final OtherwiseDeceasedChangeDTO changeDTO = updated.getChangeHistory().get(1);
        assertThat(changeDTO.getReasonForChange(), equalTo(created.getReasonForChange()), "change reason mismatch");
        assertThat(changeDTO.getChangeType(), equalTo(OtherwiseDeceasedChange.ChangeType.MODIFY), "change type not MODIFY");
        assertThat(changeDTO.getAuthor().getId(), equalTo(privilegedModerator.getId()), "author id mismatch");
    }

    private void assertThatEquals(final OtherwiseDeceasedDTO actual, final OtherwiseDeceasedDTO expected) {
        assertThat(actual.getGameSpeciesCode(), equalTo(expected.getGameSpeciesCode()), "species mismatch");
        assertThat(actual.getAge(), equalTo(expected.getAge()), "age mismatch");
        assertThat(actual.getGender(), equalTo(expected.getGender()), "gender mismatch");
        assertThat(actual.getWeight(), equalTo(expected.getWeight()), "weight mismatch");
        assertThat(actual.getPointOfTime(), equalTo(expected.getPointOfTime()), "pointOfTime mismatch");
        assertThat(actual.isNoExactLocation(), equalTo(expected.isNoExactLocation()), "isNoExactLocation mismatch");
        assertThat(actual.getGeoLocation(), equalTo(expected.getGeoLocation()), "geoLocation mismatch");
        assertThat(actual.getCause(), equalTo(expected.getCause()), "cause mismatch");
        assertThat(actual.getSource(), equalTo(expected.getSource()), "source mismatch");
        assertThat(actual.getDescription(), equalTo(expected.getDescription()), "description mismatch");
        assertThat(actual.getAdditionalInfo(), equalTo(expected.getAdditionalInfo()), "additionalInfo mismatch");
    }

    private void checkSavedDtoFields(final OtherwiseDeceasedDTO dto) {
        if (dto.getCause() == OtherwiseDeceasedCause.OTHER) {
            assertThat(dto.getCauseDescription(), is(not(isEmptyString())), "value OTHER but cause description is empty");
        }

        if (dto.getSource() == OtherwiseDeceasedSource.OTHER) {
            assertThat(dto.getSourceDescription(), is(not(isEmptyString())), "value OTHER but source description is empty");
        }
    }

    private void assertThatEquals(final OtherwiseDeceasedDTO dto, final OtherwiseDeceased entity) {
        assertThat(dto.getId(), equalTo(entity.getId()), "id mismatch");
        assertThat(dto.getPointOfTime(), equalTo(entity.getPointOfTime().toLocalDateTime()));
        assertThat(dto.getGameSpeciesCode(), equalTo(entity.getSpecies().getOfficialCode()), "species mismatch");
        assertThat(dto.getAge(), equalTo(entity.getAge()), "age mismatch");
        assertThat(dto.getGender(), equalTo(entity.getGender()), "gender mismatch");
        assertThat(dto.getWeight(), equalTo(entity.getWeight()), "weight mismatch");
        assertThat(dto.getGeoLocation(), equalTo(entity.getGeoLocation()), "geoLocation mismatch");
        assertThat(dto.getMunicipality().getNameLocalisation(), equalTo(entity.getMunicipality().getNameLocalisation()), "municipality name");
        assertThat(dto.getRhy().getNameLocalisation(), equalTo(entity.getRhy().getNameLocalisation()), "rhy name mismatch");
        assertThat(dto.getRka().getNameLocalisation(), equalTo(entity.getRka().getNameLocalisation()), "rka name mismatch");
        assertThat(dto.getCause(), equalTo(entity.getCause()), "cause mismatch");
        assertThat(dto.getCauseDescription(), equalTo(entity.getCauseDescription()), "cause mismatch");
        assertThat(dto.getSource(), equalTo(entity.getSource()), "source mismatch");
        assertThat(dto.getSourceDescription(), equalTo(entity.getSourceDescription()), "sourceOther mismatch");
        assertThat(dto.getDescription(), equalTo(entity.getDescription()), "description mismatch");
        assertThat(dto.getAdditionalInfo(), equalTo(entity.getAdditionalInfo()), "additional info mismatch");
        assertThat(dto.isRejected(), equalTo(entity.isRejected()), "rejected mismatch");
        assertThat(dto.getAttachments(), hasSize(entity.getAttachments().size()), "attachment mismatch");
        assertThat(dto.getChangeHistory(), hasSize(entity.getChangeHistory().size()), "changes mismatch");
        assertThatAttachmentsEquals(dto.getAttachments(), entity.getAttachments());
        assertThatChangesEquals(dto.getChangeHistory(), entity.getChangeHistory());
    }

    private void assertThatAttachmentsEquals(final List<OtherwiseDeceasedAttachmentDTO> dtos,
                                             final List<OtherwiseDeceasedAttachment> entities) {
        assertThat(dtos, hasSize(entities.size()), "number of attachment mismatch");
        final Map<Long, OtherwiseDeceasedAttachment> entityMap = F.indexById(entities);
        dtos.forEach(dto -> assertThatEquals(dto, entityMap.get(dto.getId())));
    }

    private void assertThatEquals(final OtherwiseDeceasedAttachmentDTO dto,
                                  final OtherwiseDeceasedAttachment entity) {
        assertThat(dto.getId(), equalTo(entity.getId()), "attachment id mismatch");
        assertThat(dto.getFilename(),
                equalTo(entity.getAttachmentMetadata().getOriginalFilename()),
                "attachment filename mismatch");
    }

    private void assertThatChangesEquals(final List<OtherwiseDeceasedChangeDTO> dtos,
                                         final List<OtherwiseDeceasedChange> entities) {
        assertThat(dtos, hasSize(entities.size()));
        final Map<Long, OtherwiseDeceasedChange> entityMap = F.indexById(entities);
        dtos.forEach(dto -> assertThatEquals(dto, entityMap.get(dto.getId())));
    }

    private void assertThatEquals(final OtherwiseDeceasedChangeDTO dto,
                                  final OtherwiseDeceasedChange entity) {
        assertThat(dto.getId(), equalTo(entity.getId()));
        assertThat(dto.getAuthor().getId(), equalTo(entity.getUserId()), "change author mismatch");
        assertThat(dto.getModificationTime(), equalTo(entity.getPointOfTime()), "change pointOfTime mismatch");
        assertThat(dto.getChangeType(), equalTo(entity.getChangeType()), "change type mismatch");
        assertThat(dto.getReasonForChange(), equalTo(entity.getReasonForChange()), "change reason mismatch");
    }

    private void checkOutgoingDtoFields(final OtherwiseDeceasedDTO dto) {
        assertThat(dto.getId(), is(notNullValue()), "outgoing dto id is null");
        assertThat(dto.getRhy().getOfficialCode(), is(notNullValue()), "rhy officialCode is null");
        assertThat(dto.getRka().getOfficialCode(), is(notNullValue()), "rka officialCode is null");
        assertThat(dto.getReasonForChange(), is(nullValue()), "reasonForChange is not null");
    }

    private OtherwiseDeceasedDTO newOtherwiseDeceasedDTO() {
        final OtherwiseDeceasedDTO dto = new OtherwiseDeceasedDTO();
        dto.setGameSpeciesCode(gameSpecies1.getOfficialCode());
        dto.setAge(some(GameAge.class));
        dto.setGender(some(GameGender.class));
        dto.setWeight(weight());
        dto.setPointOfTime(LocalDateTime.now());
        dto.setNoExactLocation(someBoolean());
        dto.setGeoLocation(location1);

        dto.setCause(some(OtherwiseDeceasedCause.class));
        dto.setCauseDescription("Cause other " + nextLong());
        dto.setSource(some(OtherwiseDeceasedSource.class));
        dto.setSourceDescription("Source other " + nextLong());
        dto.setDescription("Description " + nextLong());
        dto.setAdditionalInfo("Additional ingo " + nextLong());
        return dto;
    }


    private static void assertEquals(final OtherwiseDeceased lynx, final OtherwiseDeceasedBriefDTO result) {
        assertThat(result.getPointOfTime(), equalTo(lynx.getPointOfTime().toLocalDateTime()));
        assertThat(result.getCause(), equalTo(lynx.getCause()));
        assertThat(result.getGameSpeciesCode(), equalTo(lynx.getSpecies().getOfficialCode()));
        assertThat(result.getMunicipality().getNameFI(),
                equalTo(lynx.getMunicipality().getNameLocalisation().getFinnish()));
        assertThat(result.getRhy().getNameFI(), equalTo(lynx.getRhy().getNameFinnish()));
        assertThat(result.getRka().getNameFI(), equalTo(lynx.getRka().getNameFinnish()));
    }

    private OtherwiseDeceased createEntity(final Riistanhoitoyhdistys rhy, final GameSpecies species) {
        return es.newOtherwiseDeceased(
                new DateTime(2021, 1, 1, 0, 0, Constants.DEFAULT_TIMEZONE),
                OtherwiseDeceasedCause.HIGHWAY_ACCIDENT,
                species,
                rhy,
                (RiistakeskuksenAlue) rhy.getRiistakeskuksenAlue(),
                false);
    }
}
