package fi.riista.feature.harvestpermit.report.state;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.organization.person.Person;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HarvestReportStateTransitions {

    public enum ReportRole {
        AUTHOR, // author to report which has no permit
        AUTHOR_FOR_PERMIT, // author to report which has permit
        CONTACT_FOR_PERMIT, // contact person to report which has permit
        AUTHOR_CONTACT_FOR_PERMIT, // author and contact person to report which has permit
        MODERATOR;
    }

    private static final Map<ReportRole, HarvestReport.State> initialStates =
            new ImmutableMap.Builder<ReportRole, HarvestReport.State>()
                    .put(ReportRole.AUTHOR, HarvestReport.State.SENT_FOR_APPROVAL)
                    .put(ReportRole.AUTHOR_FOR_PERMIT, HarvestReport.State.PROPOSED)
                    .put(ReportRole.CONTACT_FOR_PERMIT, HarvestReport.State.SENT_FOR_APPROVAL)
                    .put(ReportRole.AUTHOR_CONTACT_FOR_PERMIT, HarvestReport.State.SENT_FOR_APPROVAL)
                    .put(ReportRole.MODERATOR, HarvestReport.State.SENT_FOR_APPROVAL)
                    .build();

    private static final Map<ReportRole, EnumSet<HarvestReport.State>> disallowedStatesToEdit =
            new ImmutableMap.Builder<ReportRole, EnumSet<HarvestReport.State>>()
                    .put(ReportRole.AUTHOR, EnumSet.of(HarvestReport.State.APPROVED))
                    .put(ReportRole.AUTHOR_FOR_PERMIT, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL, HarvestReport.State.APPROVED))
                    .put(ReportRole.CONTACT_FOR_PERMIT, EnumSet.of(HarvestReport.State.APPROVED))
                    .put(ReportRole.AUTHOR_CONTACT_FOR_PERMIT, EnumSet.of(HarvestReport.State.APPROVED))
                    .build();

    private static final Map<ReportRole, EnumSet<HarvestReport.State>> disallowedStatesToDelete =
            new ImmutableMap.Builder<ReportRole, EnumSet<HarvestReport.State>>()
                    .put(ReportRole.AUTHOR, EnumSet.of(HarvestReport.State.APPROVED))
                    .put(ReportRole.AUTHOR_FOR_PERMIT, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL, HarvestReport.State.APPROVED))
                    .put(ReportRole.CONTACT_FOR_PERMIT, EnumSet.of(HarvestReport.State.APPROVED))
                    .put(ReportRole.AUTHOR_CONTACT_FOR_PERMIT, EnumSet.of(HarvestReport.State.APPROVED))
                    .build();


    private static final EnumSet<HarvestReport.State> moderatorCanEdit = EnumSet.of(HarvestReport.State.PROPOSED, HarvestReport.State.SENT_FOR_APPROVAL, HarvestReport.State.REJECTED);
    private static final EnumSet<HarvestReport.State> moderatorCanDelete = EnumSet.of(HarvestReport.State.REJECTED);

    private static final Map<ReportRole, Map<HarvestReport.State, EnumSet<HarvestReport.State>>> allowedStateTransitions =
            new ImmutableMap.Builder<ReportRole, Map<HarvestReport.State, EnumSet<HarvestReport.State>>>()
                    .put(ReportRole.AUTHOR,
                            new ImmutableMap.Builder<HarvestReport.State, EnumSet<HarvestReport.State>>()
                                    .put(HarvestReport.State.REJECTED, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL))
                                    .build())
                    .put(ReportRole.AUTHOR_FOR_PERMIT,
                            new ImmutableMap.Builder<HarvestReport.State, EnumSet<HarvestReport.State>>()
                                    .put(HarvestReport.State.REJECTED, EnumSet.of(HarvestReport.State.PROPOSED))
                                    .build())
                    .put(ReportRole.CONTACT_FOR_PERMIT,
                            new ImmutableMap.Builder<HarvestReport.State, EnumSet<HarvestReport.State>>()
                                    .put(HarvestReport.State.PROPOSED, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL, HarvestReport.State.REJECTED))
                                    .build())
                    .put(ReportRole.AUTHOR_CONTACT_FOR_PERMIT,
                            new ImmutableMap.Builder<HarvestReport.State, EnumSet<HarvestReport.State>>()
                                    .put(HarvestReport.State.REJECTED, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL))
                                    .put(HarvestReport.State.PROPOSED, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL, HarvestReport.State.REJECTED))
                                    .build())
                    .put(ReportRole.MODERATOR,
                            new ImmutableMap.Builder<HarvestReport.State, EnumSet<HarvestReport.State>>()
                                    .put(HarvestReport.State.PROPOSED, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL))
                                    .put(HarvestReport.State.SENT_FOR_APPROVAL, EnumSet.of(HarvestReport.State.APPROVED, HarvestReport.State.REJECTED))
                                    .put(HarvestReport.State.APPROVED, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL, HarvestReport.State.REJECTED))
                                    .put(HarvestReport.State.REJECTED, EnumSet.of(HarvestReport.State.SENT_FOR_APPROVAL, HarvestReport.State.APPROVED))
                                    .build())
                    .build();


    public static HarvestReport.State getInitialState(ReportRole reportRole) {
        return initialStates.get(reportRole);
    }

    public static boolean canDelete(ReportRole reportRole, HarvestReport.State state) {
        EnumSet<HarvestReport.State> disallowedStates = disallowedStatesToDelete.get(reportRole);
        return disallowedStates != null && !disallowedStates.contains(state);
    }

    public static void assertCanDelete(ReportRole reportRole, HarvestReport.State state) {
        Preconditions.checkState(canDelete(reportRole, state));
    }

    public static boolean canEdit(ReportRole reportRole, HarvestReport.State state) {
        EnumSet<HarvestReport.State> disallowedStates = disallowedStatesToEdit.get(reportRole);
        return disallowedStates != null && !disallowedStates.contains(state);
    }

    public static void assertCanEdit(ReportRole reportRole, HarvestReport.State state) {
        Preconditions.checkState(canEdit(reportRole, state));
    }

    public static boolean canModeratorEdit(HarvestReport.State state) {
        return moderatorCanEdit.contains(state);
    }

    public static boolean canModeratorDelete(HarvestReport.State state){
        return moderatorCanDelete.contains(state);
    }

    public static void assertModeratorCanEdit(HarvestReport.State state) {
        Preconditions.checkState(canModeratorEdit(state));
    }

    public static void assertChangeState(ReportRole reportRole, HarvestReport.State state, HarvestReport.State to) {
        Preconditions.checkState(allowedStateTransitions.containsKey(reportRole),
                "State change not allowed in role " + reportRole);
        Map<HarvestReport.State, EnumSet<HarvestReport.State>> transitionsForRole = allowedStateTransitions.get(reportRole);

        Preconditions.checkState(transitionsForRole.containsKey(state),
                "State change not allowed in state " + state);

        EnumSet<HarvestReport.State> transitions = transitionsForRole.get(state);
        Preconditions.checkState(transitions.contains(to),
                "State change not allowed from state " + state + " to state " + to);
    }

    public static List<HarvestReport.State> getTransitions(ReportRole reportRole, HarvestReport.State state) {
        Map<HarvestReport.State, EnumSet<HarvestReport.State>> roleStates = allowedStateTransitions.get(reportRole);
        if (roleStates == null) {
            return ImmutableList.of();
        }
        EnumSet<HarvestReport.State> transtitions = roleStates.get(state);
        if (transtitions == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(transtitions);
    }

    public static HarvestReportStateTransitions.ReportRole getRole(SystemUser user, HarvestReport report) {
        if (user.getRole() == SystemUser.Role.ROLE_MODERATOR || user.getRole() == SystemUser.Role.ROLE_ADMIN) {
            return ReportRole.MODERATOR;
        }
        Person person = user.getPerson();
        boolean isAuthorOrHunter = eq(person, report.getAuthor());
        if (report.getHarvestPermit() != null) {
            boolean isContactPerson = report.getHarvestPermit().hasContactPerson(person);
            if (isContactPerson && isAuthorOrHunter) {
                return ReportRole.AUTHOR_CONTACT_FOR_PERMIT;
            } else if (isAuthorOrHunter) {
                return ReportRole.AUTHOR_FOR_PERMIT;
            } else if (isContactPerson) {
                return ReportRole.CONTACT_FOR_PERMIT;
            }
        } else if (isAuthorOrHunter) {
            return ReportRole.AUTHOR;
        }
        return null;
    }

    private static boolean eq(Person a, Person b) {
        return Objects.equals(a.getId(), b.getId());
    }

    private HarvestReportStateTransitions() {
        throw new AssertionError();
    }
}
