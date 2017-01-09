package fi.riista.feature.harvestpermit.report.state;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.state.HarvestReportStateTransitions;
import fi.riista.feature.organization.person.Person;
import org.junit.Test;

import static fi.riista.feature.harvestpermit.report.HarvestReport.State.APPROVED;
import static fi.riista.feature.harvestpermit.report.HarvestReport.State.PROPOSED;
import static fi.riista.feature.harvestpermit.report.HarvestReport.State.REJECTED;
import static fi.riista.feature.harvestpermit.report.HarvestReport.State.SENT_FOR_APPROVAL;
import static fi.riista.feature.harvestpermit.report.state.HarvestReportStateTransitions.ReportRole;
import static fi.riista.feature.harvestpermit.report.state.HarvestReportStateTransitions.assertChangeState;
import static fi.riista.feature.harvestpermit.report.state.HarvestReportStateTransitions.getInitialState;
import static fi.riista.feature.harvestpermit.report.state.HarvestReportStateTransitions.getRole;
import static fi.riista.feature.harvestpermit.report.state.HarvestReportStateTransitions.getTransitions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HarvestReportStateTransitionsTest {

    @Test
    public void testGetRole() {
        assertEquals(ReportRole.AUTHOR, getRole(user(1), report(1, null)));
        assertEquals(ReportRole.AUTHOR_FOR_PERMIT, getRole(user(1), report(1, "123")));

        assertEquals(ReportRole.MODERATOR, getRole(admin(), report(1, "123")));
        assertEquals(ReportRole.MODERATOR, getRole(admin(), report(1, null)));

        assertEquals(ReportRole.MODERATOR, getRole(moderator(), report(1, "123")));
        assertEquals(ReportRole.MODERATOR, getRole(moderator(), report(1, null)));

        assertEquals(ReportRole.AUTHOR_CONTACT_FOR_PERMIT, getRole(user(1), report(1, "123", true, true)));
        assertEquals(ReportRole.CONTACT_FOR_PERMIT, getRole(user(1), report(1, "123", true, false)));
        assertEquals(ReportRole.AUTHOR_FOR_PERMIT, getRole(user(1), report(1, "123", false, true)));
        assertNull(getRole(user(1), report(1, "123", false, false)));
    }

    private static HarvestReport report(long personId, String permitNumber) {
        return report(personId, permitNumber, false, true);
    }

    private static HarvestReport report(long personId, String permitNumber, boolean isContactPerson, boolean isAuthor) {
        HarvestReport r = mock(HarvestReport.class);
        Person person = mock(Person.class);
        if (isAuthor) {
            when(person.getId()).thenReturn(personId);
        } else {
            when(person.getId()).thenReturn(-1L);
        }
        when(r.getAuthor()).thenReturn(person);
//        when(r.getHunter()).thenReturn(person);
        if (permitNumber != null) {
            HarvestPermit harvestPermit = mock(HarvestPermit.class);
            when(harvestPermit.getPermitNumber()).thenReturn(permitNumber);
            when(r.getHarvestPermit()).thenReturn(harvestPermit);
            when(harvestPermit.hasContactPerson(any(Person.class))).thenReturn(isContactPerson);
        }
        return r;
    }

    private static SystemUser user(long personId) {
        Person person = mock(Person.class);
        when(person.getId()).thenReturn(personId);
        SystemUser systemUser = userWithRole(SystemUser.Role.ROLE_USER);
        when(systemUser.getPerson()).thenReturn(person);
        return systemUser;
    }

    private static SystemUser admin() {
        return userWithRole(SystemUser.Role.ROLE_ADMIN);
    }

    private static SystemUser moderator() {
        return userWithRole(SystemUser.Role.ROLE_MODERATOR);
    }

    private static SystemUser userWithRole(SystemUser.Role role) {
        SystemUser systemUser = mock(SystemUser.class);
        when(systemUser.getRole()).thenReturn(role);
        return systemUser;
    }

    @Test
    public void testCanEditOrDelete() {
        assertTrue(editOrDelete(ReportRole.AUTHOR, HarvestReport.State.SENT_FOR_APPROVAL));
        assertTrue(editOrDelete(ReportRole.AUTHOR, HarvestReport.State.PROPOSED));
        assertFalse(editOrDelete(ReportRole.AUTHOR, HarvestReport.State.APPROVED));
        assertTrue(editOrDelete(ReportRole.AUTHOR, HarvestReport.State.REJECTED));

        assertFalse(editOrDelete(ReportRole.AUTHOR_FOR_PERMIT, HarvestReport.State.SENT_FOR_APPROVAL));
        assertTrue(editOrDelete(ReportRole.AUTHOR_FOR_PERMIT, HarvestReport.State.PROPOSED));
        assertFalse(editOrDelete(ReportRole.AUTHOR_FOR_PERMIT, HarvestReport.State.APPROVED));
        assertTrue(editOrDelete(ReportRole.AUTHOR_FOR_PERMIT, HarvestReport.State.REJECTED));
    }

    private static boolean editOrDelete(ReportRole author, HarvestReport.State sentForApproval) {
        boolean edit = HarvestReportStateTransitions.canEdit(author, sentForApproval);
        boolean delete = HarvestReportStateTransitions.canDelete(author, sentForApproval);
        assertEquals(edit, delete);
        return edit;
    }

    @Test
    public void testContactForPermitCanEditOrDelete() {
        assertTrue(HarvestReportStateTransitions.canEdit(ReportRole.CONTACT_FOR_PERMIT, HarvestReport.State.SENT_FOR_APPROVAL));
        assertTrue(HarvestReportStateTransitions.canDelete(ReportRole.CONTACT_FOR_PERMIT, HarvestReport.State.SENT_FOR_APPROVAL));

        assertTrue(HarvestReportStateTransitions.canEdit(ReportRole.CONTACT_FOR_PERMIT, HarvestReport.State.PROPOSED));
        assertTrue(HarvestReportStateTransitions.canDelete(ReportRole.CONTACT_FOR_PERMIT, HarvestReport.State.PROPOSED));

        assertFalse(editOrDelete(ReportRole.CONTACT_FOR_PERMIT, HarvestReport.State.APPROVED));
        assertTrue(editOrDelete(ReportRole.CONTACT_FOR_PERMIT, HarvestReport.State.REJECTED));
    }

    @Test
    public void testModeratorCanEditOrDelete() {
        assertFalse(editOrDelete(ReportRole.MODERATOR, HarvestReport.State.SENT_FOR_APPROVAL));
        assertFalse(editOrDelete(ReportRole.MODERATOR, HarvestReport.State.PROPOSED));
        assertFalse(editOrDelete(ReportRole.MODERATOR, HarvestReport.State.APPROVED));
        assertFalse(editOrDelete(ReportRole.MODERATOR, HarvestReport.State.REJECTED));
    }

    @Test
    public void testCoordinator() {
        // This tests that transitions can be read for null ReportRole
        // Coordinators permission to read harvest report is determined elsewhere.
        for (HarvestReport.State s : HarvestReport.State.values()) {
            assertEquals(0, getTransitions(null, s).size());
        }
    }

    @Test
    public void testReportAuthor() {
        AssertHelper h = new AssertHelper(ReportRole.AUTHOR);
        h.testNoTransitions(ReportRole.AUTHOR, SENT_FOR_APPROVAL);
        h.testValid(REJECTED, SENT_FOR_APPROVAL);

        h.testInvalid(REJECTED, PROPOSED);
        h.testInvalid(REJECTED, APPROVED);
    }

    @Test
    public void testReportAuthorForPermit() {
        AssertHelper h = new AssertHelper(ReportRole.AUTHOR_FOR_PERMIT);
        h.testNoTransitions(ReportRole.AUTHOR_FOR_PERMIT, SENT_FOR_APPROVAL);
        h.testValid(REJECTED, PROPOSED);

        h.testInvalid(REJECTED, SENT_FOR_APPROVAL);
        h.testInvalid(REJECTED, APPROVED);
    }

    @Test
    public void testReportContactForPermit() {
        AssertHelper h = new AssertHelper(ReportRole.CONTACT_FOR_PERMIT);
        h.testNoTransitions(ReportRole.CONTACT_FOR_PERMIT, SENT_FOR_APPROVAL);
        h.testValid(PROPOSED, SENT_FOR_APPROVAL);
        h.testValid(PROPOSED, REJECTED);

        h.testInvalid(REJECTED, SENT_FOR_APPROVAL);
        h.testInvalid(REJECTED, APPROVED);
    }

    @Test
    public void testReportAuthorContactForPermit() {
        AssertHelper h = new AssertHelper(ReportRole.AUTHOR_CONTACT_FOR_PERMIT);
        h.testNoTransitions(ReportRole.AUTHOR_CONTACT_FOR_PERMIT, SENT_FOR_APPROVAL);
        h.testValid(PROPOSED, SENT_FOR_APPROVAL);
        h.testValid(PROPOSED, REJECTED);
        h.testValid(REJECTED, SENT_FOR_APPROVAL);

        h.testInvalid(REJECTED, APPROVED);
    }


    @Test
    public void testReportModerator() {
        AssertHelper h = new AssertHelper(ReportRole.MODERATOR);
        h.testValid(PROPOSED, SENT_FOR_APPROVAL);

        h.testValid(SENT_FOR_APPROVAL, APPROVED);
        h.testValid(SENT_FOR_APPROVAL, REJECTED);
        h.testValid(APPROVED, REJECTED);
        h.testValid(REJECTED, APPROVED);
    }

    @Test
    public void testInitialStates() {
        assertEquals(SENT_FOR_APPROVAL, getInitialState(ReportRole.AUTHOR));
        assertEquals(PROPOSED, getInitialState(ReportRole.AUTHOR_FOR_PERMIT));
        assertEquals(SENT_FOR_APPROVAL, getInitialState(ReportRole.CONTACT_FOR_PERMIT));
        assertEquals(SENT_FOR_APPROVAL, getInitialState(ReportRole.MODERATOR));
    }

    private static class AssertHelper {
        private final ReportRole role;

        public AssertHelper(ReportRole role) {
            this.role = role;
        }

        public void testInvalid(HarvestReport.State from, HarvestReport.State to) {
            try {
                assertChangeState(role, from, to);
                fail("State change from " + from + " to " + to + " should fail");
            } catch (IllegalStateException e) {
                //we want this
            }
        }

        public void testValid(HarvestReport.State from, HarvestReport.State to) {
            assertChangeState(role, from, to);
        }

        public void testNoTransitions(ReportRole role, HarvestReport.State state) {
            assertEquals(0, getTransitions(role, state).size());
        }
    }
}
