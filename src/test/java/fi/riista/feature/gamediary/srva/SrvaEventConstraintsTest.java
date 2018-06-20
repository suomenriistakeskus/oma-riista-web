package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;

public class SrvaEventConstraintsTest extends EmbeddedDatabaseTest {

    @Resource
    private PasswordEncoder passwordEncoder;

    private Riistanhoitoyhdistys rhy;

    @Before
    public void initRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
    }

    @Test(expected = ConstraintViolationException.class)
    public void testNotExclusiveSpeciesOrOtherSpeciesDescription() {
        createSrvaEventWithSpecies(model().newGameSpecies(), "other");
        persistInNewTransaction();
    }

    @Test(expected = ConstraintViolationException.class)
    public void testSpeciesAndOtherSpeciesDescriptionNull() {
        createSrvaEventWithSpecies(null, null);
        persistInNewTransaction();
    }

    @Test
    public void testIsExclusiveSpeciesOrOtherSpeciesDescription() {
        createSrvaEventWithSpecies(null, "other");
        createSrvaEventWithSpecies(model().newGameSpecies(), null);
        persistInNewTransaction();
    }

    private void createSrvaEventWithSpecies(final GameSpecies species, final String otherSpeciesDescription) {
        final SrvaEvent srvaEvent = newSrvaEvent();
        srvaEvent.setSpecies(species);
        srvaEvent.setOtherSpeciesDescription(otherSpeciesDescription);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testGeolocationNotSet() {
        newSrvaEvent().setGeoLocation(null);
        persistInNewTransaction();
    }

    @Test(expected = ConstraintViolationException.class)
    public void testGeolocationSourceNotSet() {
        newSrvaEvent().getGeoLocation().setSource(null);
        persistInNewTransaction();
    }

    @Test
    public void testIsApproverDefinedForOtherThanUnfinished() {
        final SystemUser user = model().newUser("user", passwordEncoder);

        createSrvaEventWithApprover(SrvaEventStateEnum.UNFINISHED, null);
        createSrvaEventWithApprover(SrvaEventStateEnum.APPROVED, user);
        createSrvaEventWithApprover(SrvaEventStateEnum.REJECTED, user);
        persistInNewTransaction();
    }

    @Test(expected = ConstraintViolationException.class)
    public void testIsApproverDefinedForOtherThanUnfinished_ApprovedNoApprover() {
        createSrvaEventWithApprover(SrvaEventStateEnum.APPROVED, null);
        persistInNewTransaction();
    }

    @Test(expected = ConstraintViolationException.class)
    public void testIsApproverDefinedForOtherThanUnfinished_RejectedNoApprover() {
        createSrvaEventWithApprover(SrvaEventStateEnum.REJECTED, null);
        persistInNewTransaction();
    }

    @Test(expected = ConstraintViolationException.class)
    public void testIsApproverDefinedForOtherThanUnfinished_UnfinishedWithApprover() {
        final SystemUser user = model().newUser("user", passwordEncoder);

        createSrvaEventWithApprover(SrvaEventStateEnum.UNFINISHED, user);
        persistInNewTransaction();
    }

    private void createSrvaEventWithApprover(final SrvaEventStateEnum state, final SystemUser approver) {
        final SrvaEvent srvaEvent = newSrvaEvent();
        srvaEvent.setState(state);
        srvaEvent.setApproverAsUser(approver);
    }

    private SrvaEvent newSrvaEvent() {
        return model().newSrvaEvent(this.rhy);
    }

}
