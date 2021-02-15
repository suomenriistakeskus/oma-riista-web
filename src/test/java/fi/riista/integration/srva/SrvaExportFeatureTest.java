package fi.riista.integration.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.srva.dto.SrvaPublicExportDTO;
import fi.riista.integration.srva.rvr.RVR_SrvaEvent;
import fi.riista.integration.srva.rvr.RVR_SrvaEvents;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.ACCIDENT;
import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.DEPORTATION;
import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.INJURED_ANIMAL;
import static fi.riista.feature.gamediary.srva.SrvaEventStateEnum.APPROVED;
import static fi.riista.feature.gamediary.srva.SrvaEventStateEnum.REJECTED;
import static fi.riista.feature.gamediary.srva.SrvaEventStateEnum.UNFINISHED;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertEquals;

public class SrvaExportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private SrvaExportFeature srvaExportFeature;

    private Riistanhoitoyhdistys rhy;

    @Before
    public void initRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
    }

    @After
    public void tearDown() {
        srvaExportFeature.invalidatePublicDtoCache();
    }

    @Test(expected = AccessDeniedException.class)
    public void testExportRvrWithWrongPrivilege() {
        final SystemUser apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_HUNTINGCLUB_AREA);

        onSavedAndAuthenticated(apiUser, srvaExportFeature::exportRVR);
    }

    @Test(expected = AccessDeniedException.class)
    public void testExportRvrWithNormalUser() {
        onSavedAndAuthenticated(createNewUser(), srvaExportFeature::exportRVR);
    }

    @Test
    public void testExportRvrCount() {
        final SystemUser apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_SRVA_RVR);
        final int numberOfEvents = 23;

        for (int i = 0; i < 23; i++) {
            createSrvaEventWithNameAndState(
                    ACCIDENT, APPROVED, createNewUser(), true, true);
        }

        onSavedAndAuthenticated(apiUser, () -> {
            assertEquals(numberOfEvents, srvaExportFeature.getRVREventsForXmlMarshal().getSrvaEvent().size());
            srvaExportFeature.exportRVR();
        });
    }

    @Test
    @Transactional
    public void testExportRvrAllFields() {
        testExportRvr(true, true);
    }

    @Test
    @Transactional
    public void testExportRvrMandatoryFields() {
        testExportRvr(false, false);
    }

    private void testExportRvr(final boolean allFields, final boolean otherSpecies) {
        final SystemUser apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_SRVA_RVR);

        final SrvaEvent event = createSrvaEventWithNameAndState(
                ACCIDENT, APPROVED, createNewUser(), allFields, otherSpecies);

        onSavedAndAuthenticated(apiUser, () -> {
            final RVR_SrvaEvents rvrEvents = srvaExportFeature.getRVREventsForXmlMarshal();
            assertEquals(1, rvrEvents.getSrvaEvent().size());

            assertRvrEvent(rvrEvents.getSrvaEvent().get(0), event);

            srvaExportFeature.exportRVR();
        });
    }

    private static void assertRvrEvent(final RVR_SrvaEvent rvrEvent, final SrvaEvent event) {
        assertEquals(event.getEventName().name(), rvrEvent.getEventName().name());
        assertEquals(event.getEventType().name(), rvrEvent.getEventType().name());
        assertEquals(event.getOtherTypeDescription(), rvrEvent.getOtherTypeDescription());
        assertEquals(event.getId().longValue(), rvrEvent.getId());
        assertEquals(event.getConsistencyVersion().intValue(), rvrEvent.getRev());
        assertEquals(event.getGeoLocation().getLatitude(), rvrEvent.getGeoLocation().getLatitude());
        assertEquals(event.getGeoLocation().getLongitude(), rvrEvent.getGeoLocation().getLongitude());
        assertEquals(event.getPointOfTime(), rvrEvent.getPointOfTime());
        assertEquals(event.getOtherSpeciesDescription(), rvrEvent.getOtherSpeciesDescription());
        assertEquals(event.getTotalSpecimenAmount(), rvrEvent.getTotalSpecimenAmount());
        assertEquals(event.getPersonCount(), rvrEvent.getPersonCount());
        assertEquals(event.getTimeSpent(), rvrEvent.getTimeSpent());
        assertEquals(event.getOtherMethodDescription(), rvrEvent.getOtherMethodDescription());
        assertEquals(event.getDescription(), rvrEvent.getDescription());
        assertEquals(event.getRhy().getOfficialCode(), rvrEvent.getRhyOfficialCode());
        assertEquals(event.getRhy().getNameFinnish(), rvrEvent.getRhyHumanReadableName());

        assertEquals(ofNullable(event.getSpecies()).map(GameSpecies::getOfficialCode).orElse(null),
                rvrEvent.getGameSpeciesOfficialCode());

        assertEquals(ofNullable(event.getSpecies()).map(GameSpecies::getNameFinnish).orElse(null),
                rvrEvent.getGameSpeciesHumanReadableName());

        assertEquals(ofNullable(event.getEventResult()).map(Enum::name).orElse(null),
                ofNullable(rvrEvent.getEventResult()).map(Enum::name).orElse(null));
    }

    private SrvaEvent createSrvaEventWithNameAndState(final SrvaEventNameEnum name,
                                                      final SrvaEventStateEnum state,
                                                      final SystemUser user,
                                                      final boolean allFields,
                                                      final boolean setGameSpeciesCode) {
        final SrvaEvent event = allFields ?
                createSrvaEventWithAllFields(setGameSpeciesCode) :
                createSrvaEventWithMandatoryFields(setGameSpeciesCode);

        event.setEventName(name);
        event.setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(name)));
        event.setState(state);

        if (state != UNFINISHED) {
            event.setApproverAsUser(user);
        }

        return event;
    }

    private SrvaEvent createSrvaEventWithMandatoryFields(final boolean setGameSpeciesCode) {
        final SrvaEvent srvaEvent = newSrvaEvent();
        srvaEvent.setDescription(null);
        srvaEvent.setPersonCount(null);
        srvaEvent.setTimeSpent(null);
        srvaEvent.setEventResult(null);
        srvaEvent.setOtherMethodDescription(null);
        srvaEvent.setOtherTypeDescription(null);

        if (!setGameSpeciesCode) {
            srvaEvent.setSpecies(null);
            srvaEvent.setOtherSpeciesDescription("Other species");
        }

        return srvaEvent;
    }

    private SrvaEvent createSrvaEventWithAllFields(final boolean setGameSpeciesCode) {
        final SrvaEvent srvaEvent = newSrvaEvent();
        srvaEvent.getGeoLocation().setAccuracy(0.0);

        model().newSrvaSpecimen(srvaEvent);
        model().newSrvaMethod(srvaEvent);

        if (!setGameSpeciesCode) {
            srvaEvent.setSpecies(null);
            srvaEvent.setOtherSpeciesDescription("Other species");
        }

        return srvaEvent;
    }

    @Test
    public void testExportPublic() {
        final SystemUser user = createNewUser();

        final GameSpecies moose = model().newGameSpeciesMoose();
        final GameSpecies lynx = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_LYNX);

        final SrvaEvent accidentEvent = createSrvaEventWithNameAndState(ACCIDENT, APPROVED, user, moose, 2017);
        accidentEvent.setEventType(SrvaEventTypeEnum.RAILWAY_ACCIDENT);

        createSrvaEventWithNameAndState(ACCIDENT, REJECTED, user, moose, 2017);
        createSrvaEventWithNameAndState(ACCIDENT, UNFINISHED, user, moose, 2017);
        createSrvaEventWithNameAndState(DEPORTATION, APPROVED, user, moose, 2017);
        createSrvaEventWithNameAndState(INJURED_ANIMAL, APPROVED, user, moose, 2017);
        createSrvaEventWithNameAndState(ACCIDENT, APPROVED, user, lynx, 2017);

        persistInNewTransaction();

        final List<SrvaPublicExportDTO> dtos = srvaExportFeature.exportPublic(2017);

        assertEquals(1, dtos.size());

        final SrvaPublicExportDTO dto = dtos.get(0);
        assertEquals(accidentEvent.getId(), dto.getId());
        assertEquals(accidentEvent.getConsistencyVersion().intValue(), dto.getRev());
        assertEquals(accidentEvent.getTotalSpecimenAmount(), dto.getTotalSpecimenAmount());
        assertEquals(accidentEvent.getEventName(), dto.getEventName());
        assertEquals(accidentEvent.getEventType(), dto.getEventType());
        assertEquals(accidentEvent.getPointOfTime(), dto.getPointOfTime());
        assertEquals(accidentEvent.getGeoLocation().getLatitude(), dto.getGeoLocation().getLatitude());
        assertEquals(accidentEvent.getGeoLocation().getLongitude(), dto.getGeoLocation().getLongitude());
    }

    @Test
    public void testExportPublicOnlyTrafficAndRailwayAreReturned() {
        final SystemUser user = createNewUser();

        final GameSpecies moose = model().newGameSpeciesMoose();

        final SrvaEvent accidentEventTraffic = createSrvaEventWithNameAndState(ACCIDENT, APPROVED, user, moose, 2017);
        accidentEventTraffic.setEventType(SrvaEventTypeEnum.TRAFFIC_ACCIDENT);

        final SrvaEvent accidentEventRailway = createSrvaEventWithNameAndState(ACCIDENT, APPROVED, user, moose, 2017);
        accidentEventRailway.setEventType(SrvaEventTypeEnum.RAILWAY_ACCIDENT);

        final SrvaEvent accidentEventOther = createSrvaEventWithNameAndState(ACCIDENT, APPROVED, user, moose, 2017);
        accidentEventOther.setEventType(SrvaEventTypeEnum.OTHER);

        persistInNewTransaction();

        final List<SrvaPublicExportDTO> dtos = srvaExportFeature.exportPublic(pointOfTimeToYear(accidentEventTraffic));

        assertEquals(2, dtos.size());
        assertEquals(F.getUniqueIds(accidentEventTraffic, accidentEventRailway), F.getUniqueIds(dtos));
    }

    @Test
    public void testExportPublicYearSelection() {
        try {
            DateTimeUtils.setCurrentMillisFixed(DateUtil.now().withYear(2018).getMillis());

            final SystemUser user = createNewUser();

            final GameSpecies moose = model().newGameSpeciesMoose();
            final SrvaEvent accidentEvent2016 = createSrvaEventWithNameAndState(ACCIDENT, APPROVED, user, moose, 2016);
            final SrvaEvent accidentEvent2017 = createSrvaEventWithNameAndState(ACCIDENT, APPROVED, user, moose, 2017);
            final SrvaEvent accidentEvent2018 = createSrvaEventWithNameAndState(ACCIDENT, APPROVED, user, moose, 2018);

            accidentEvent2016.setEventType(some(EnumSet.of(SrvaEventTypeEnum.TRAFFIC_ACCIDENT,
                    SrvaEventTypeEnum.RAILWAY_ACCIDENT)));
            accidentEvent2017.setEventType(some(EnumSet.of(SrvaEventTypeEnum.TRAFFIC_ACCIDENT,
                    SrvaEventTypeEnum.RAILWAY_ACCIDENT)));
            accidentEvent2018.setEventType(some(EnumSet.of(SrvaEventTypeEnum.TRAFFIC_ACCIDENT,
                    SrvaEventTypeEnum.RAILWAY_ACCIDENT)));

            persistInNewTransaction();

            assertPublicExport(2016);
            assertPublicExport(2017, accidentEvent2017);
            assertPublicExport(2018, accidentEvent2018);
            assertPublicExport(2019);
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    private void assertPublicExport(final int year, final SrvaEvent... expectedEvents) {
        final Set<Long> expectedIds = F.getUniqueIds(expectedEvents);
        final Set<Long> actualIds = F.getUniqueIds(srvaExportFeature.exportPublic(year));
        assertEquals(expectedIds, actualIds);
    }

    private static int pointOfTimeToYear(final SrvaEvent accidentEvent) {
        return accidentEvent.getPointOfTime().getYear();
    }

    @Test
    public void testExportPublicNoEvents() {
        assertEquals(0, srvaExportFeature.exportPublic(2017).size());

    }

    private SrvaEvent newSrvaEvent() {
        return model().newSrvaEvent(this.rhy);
    }

    private SrvaEvent createSrvaEventWithNameAndState(
            final SrvaEventNameEnum name,
            final SrvaEventStateEnum state,
            final SystemUser user,
            final GameSpecies species,
            final int year) {

        final SrvaEvent event = newSrvaEvent();
        event.setEventName(name);
        event.setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(name)));
        event.setState(state);
        event.setSpecies(species);
        event.setPointOfTime(new LocalDate(year, 1, 1)
                .toDateTime(new LocalTime(12, 0)));

        if (state != UNFINISHED) {
            event.setApproverAsUser(user);
        }

        return event;
    }
}
