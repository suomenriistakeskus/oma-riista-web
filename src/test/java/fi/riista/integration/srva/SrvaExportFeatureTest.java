package fi.riista.integration.srva;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.integration.srva.dto.SrvaPublicExportDTO;
import fi.riista.integration.srva.rvr.RVR_SrvaEvent;
import fi.riista.integration.srva.rvr.RVR_SrvaEvents;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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
                    SrvaEventNameEnum.ACCIDENT, SrvaEventStateEnum.APPROVED, createNewUser(), true, true);
        }

        onSavedAndAuthenticated(apiUser, () -> {
            assertEquals(numberOfEvents, srvaExportFeature.createRVREventsForXmlMarshal().getSrvaEvent().size());
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
                SrvaEventNameEnum.ACCIDENT, SrvaEventStateEnum.APPROVED, createNewUser(), allFields, otherSpecies);

        onSavedAndAuthenticated(apiUser, () -> {
            final RVR_SrvaEvents rvrEvents = srvaExportFeature.createRVREventsForXmlMarshal();
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
        assertEquals(new DateTime(event.getPointOfTime().getTime()), rvrEvent.getPointOfTime());
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
        event.setState(state);

        if (state != SrvaEventStateEnum.UNFINISHED) {
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
    public void TestExportPublic() {
        final SystemUser user = createNewUser();
        persistInNewTransaction();

        final SrvaEvent accidentEvent = createSrvaEventWithNameAndState(SrvaEventNameEnum.ACCIDENT, SrvaEventStateEnum.APPROVED, user);
        createSrvaEventWithNameAndState(SrvaEventNameEnum.ACCIDENT, SrvaEventStateEnum.REJECTED, user);
        createSrvaEventWithNameAndState(SrvaEventNameEnum.ACCIDENT, SrvaEventStateEnum.UNFINISHED, user);
        createSrvaEventWithNameAndState(SrvaEventNameEnum.DEPORTATION, SrvaEventStateEnum.APPROVED, user);
        createSrvaEventWithNameAndState(SrvaEventNameEnum.INJURED_ANIMAL, SrvaEventStateEnum.APPROVED, user);

        persistInNewTransaction();

        final List<SrvaPublicExportDTO> dtos = srvaExportFeature.exportPublic();

        assertEquals(1, dtos.size());

        final SrvaPublicExportDTO dto = dtos.get(0);
        assertEquals(accidentEvent.getId().longValue(), dto.getId());
        assertEquals(accidentEvent.getConsistencyVersion().intValue(), dto.getRev());
        assertEquals(accidentEvent.getTotalSpecimenAmount(), dto.getTotalSpecimenAmount());
        assertEquals(accidentEvent.getEventName(), dto.getEventName());
        assertEquals(accidentEvent.getEventType(), dto.getEventType());
        assertEquals(accidentEvent.getPointOfTime(), dto.getPointOfTime().toDate());
        assertEquals(accidentEvent.getGeoLocation().getLatitude(), dto.getGeoLocation().getLatitude());
        assertEquals(accidentEvent.getGeoLocation().getLongitude(), dto.getGeoLocation().getLongitude());
    }

    @Test
    public void TestExportPublicNoEvents() {
        assertEquals(0, srvaExportFeature.exportPublic().size());

    }

    private SrvaEvent newSrvaEvent() {
        return model().newSrvaEvent(this.rhy);
    }

    private SrvaEvent createSrvaEventWithNameAndState(SrvaEventNameEnum name, SrvaEventStateEnum state, SystemUser user) {
        final SrvaEvent event = newSrvaEvent();
        event.setEventName(name);
        event.setState(state);

        if(state != SrvaEventStateEnum.UNFINISHED) {
            event.setApproverAsUser(user);
        }

        return event;
    }
}
