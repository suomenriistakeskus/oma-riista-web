package fi.riista.feature.huntingclub.support;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUser.Role;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class ClubGroupUserFunctionsBuilder {

    private final EntitySupplier es;
    private final PasswordEncoder passwordEncoder;

    private boolean includeClubContactPerson = true;
    private boolean includeClubMember = true;
    private boolean includeGroupLeader = true;
    private boolean includeGroupMember = true;

    private boolean includeOriginalPermitConcatPerson = false;
    private boolean includePermitConcatPerson = false;

    private boolean includeRhyCoordinator = false;
    private boolean includeAdmin = false;
    private boolean includeModerator = false;

    public ClubGroupUserFunctionsBuilder(final EntitySupplier entitySupplier, final PasswordEncoder passwordEncoder) {
        this.es = entitySupplier;
        this.passwordEncoder = passwordEncoder;
    }

    public ClubGroupUserFunctionsBuilder withAll() {
        return withClubContactPerson(true)
                .withClubMember(true)
                .withGroupLeader(true)
                .withGroupMember(true)

                .withOriginalPermitConcatPerson(true)
                .withPermitConcatPerson(true)
                .withRhyCoordinator(true)

                .withAdminAndModerator(true);
    }

    public ClubGroupUserFunctionsBuilder withAllButAdminAndModerator() {
        return withAll().withAdminAndModerator(false);
    }

    public ClubGroupUserFunctionsBuilder withClubContactPerson(final boolean includeClubContactPerson) {
        this.includeClubContactPerson = includeClubContactPerson;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withClubMember(final boolean includeClubMember) {
        this.includeClubMember = includeClubMember;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withGroupLeader(final boolean includeGroupLeader) {
        this.includeGroupLeader = includeGroupLeader;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withGroupMember(final boolean includeGroupMember) {
        this.includeGroupMember = includeGroupMember;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withOriginalPermitConcatPerson(
            final boolean includeOriginalPermitConcatPerson) {

        this.includeOriginalPermitConcatPerson = includeOriginalPermitConcatPerson;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withPermitConcatPerson(final boolean includePermitConcatPerson) {
        this.includePermitConcatPerson = includePermitConcatPerson;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withRhyCoordinator(final boolean includeRhyCoordinator) {
        this.includeRhyCoordinator = includeRhyCoordinator;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withAdmin(final boolean includeAdmin) {
        this.includeAdmin = includeAdmin;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withModerator(final boolean includeModerator) {
        this.includeModerator = includeModerator;
        return this;
    }

    public ClubGroupUserFunctionsBuilder withAdminAndModerator(final boolean includeAdminAndModerator) {
        return withAdmin(includeAdminAndModerator).withModerator(includeAdminAndModerator);
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createClubContactPerson() {
        return (club, group) -> {
            final Person person = es.newPerson();
            es.newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
            return createUser("clubContactPerson", person);
        };
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createClubMember() {
        return (club, group) -> {
            final Person person = es.newPerson();
            es.newOccupation(club, person, OccupationType.SEURAN_JASEN);
            return createUser("clubMember", person);
        };
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createGroupLeader() {
        return (club, group) -> {
            final Person person = es.newPerson();
            es.newOccupation(club, person, OccupationType.SEURAN_JASEN);
            es.newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
            return createUser("groupLeader", person);
        };
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createGroupMember() {
        return (club, group) -> {
            final Person person = es.newPerson();
            es.newOccupation(club, person, OccupationType.SEURAN_JASEN);
            es.newOccupation(group, person, OccupationType.RYHMAN_JASEN);
            return createUser("groupMember", person);
        };
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createOriginalPermitConcatPerson() {
        return (club, group) -> {
            final Person person = es.newPerson();
            group.getHarvestPermit().setOriginalContactPerson(person);
            return createUser("permitOriginalContactPerson", person);
        };
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createPermitConcatPerson() {
        return (club, group) -> {
            final Person person = es.newPerson();
            es.newHarvestPermitContactPerson(group.getHarvestPermit(), person);
            return createUser("permitContactPerson", person);
        };
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createRhyCoordinator() {
        return (club, group) -> {
            final Person person = es.newPerson();
            es.newOccupation(club.getParentOrganisation(), person, OccupationType.TOIMINNANOHJAAJA);
            return createUser("rhyCoordinator", person);
        };
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createAdmin() {
        return (club, group) -> createUser(Role.ROLE_ADMIN);
    }

    public BiFunction<HuntingClub, HuntingClubGroup, SystemUser> createModerator() {
        return (club, group) -> createUser(Role.ROLE_MODERATOR);
    }

    public List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> createAdminAndModerator() {
        return Arrays.asList(createAdmin(), createModerator());
    }

    public List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> build() {
        final List<BiFunction<HuntingClub, HuntingClubGroup, SystemUser>> ret = new ArrayList<>();

        if (includeClubContactPerson) {
            ret.add(createClubContactPerson());
        }

        if (includeClubMember) {
            ret.add(createClubMember());
        }

        if (includeGroupLeader) {
            ret.add(createGroupLeader());
        }

        if (includeOriginalPermitConcatPerson) {
            ret.add(createOriginalPermitConcatPerson());
        }

        if (includePermitConcatPerson) {
            ret.add(createPermitConcatPerson());
        }

        if (includeGroupMember) {
            ret.add(createGroupMember());
        }

        if (includeRhyCoordinator) {
            ret.add(createRhyCoordinator());
        }

        if (includeAdmin) {
            ret.add(createAdmin());
        }

        if (includeModerator) {
            ret.add(createModerator());
        }

        return ret;
    }

    private SystemUser createUser(final Role role) {
        return es.newUser(role, passwordEncoder);
    }

    private SystemUser createUser(final String username, final Person person) {
        final SystemUser user = createUser(Role.ROLE_USER);
        user.setUsername(username);
        user.setPerson(person);
        return user;
    }

}
