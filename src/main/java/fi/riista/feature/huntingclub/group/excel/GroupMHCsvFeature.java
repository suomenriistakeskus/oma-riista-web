package fi.riista.feature.huntingclub.group.excel;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.address.QAddress;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class GroupMHCsvFeature {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private GameDiaryService gameDiaryService;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional
    public GroupMHCsvView export(final long clubId, final int year, @Nullable final Integer speciesCode) {
        final HuntingClub exportedClub = huntingClubRepository.getOne(clubId);

        userAuthorizationHelper.assertClubContactOrModerator(exportedClub);

        final GameSpecies selectedSpecies = Optional.ofNullable(speciesCode)
                .map(gameDiaryService::getGameSpeciesByOfficialCode).orElse(null);

        final QHuntingClub club = QHuntingClub.huntingClub;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QGameSpecies groupSpecies = QGameSpecies.gameSpecies;
        final QOccupation occupation = new QOccupation("groupOccupation");
        final QOccupation clubOccupation = new QOccupation("clubOccupation");
        final QPerson person = QPerson.person;
        final QAddress mrAddress = QAddress.address;

        final Predicate clubMemberExists = JPAExpressions.selectFrom(clubOccupation)
                .where(clubOccupation.organisation.eq(exportedClub),
                        clubOccupation.person.eq(occupation.person),
                        clubOccupation.validAndNotDeleted())
                .exists();

        final List<String[]> data = queryFactory.select(club, group, groupSpecies.nameFinnish, person, occupation.contactInfoShare)
                .from(club)
                .join(club.subOrganisations, group._super)
                .join(group.occupations, occupation)
                .join(occupation.person, person)
                .leftJoin(person.mrAddress, mrAddress).fetchJoin()
                .join(group.species, groupSpecies)
                .where(club.eq(exportedClub),
                        group.huntingYear.eq(year),
                        selectedSpecies != null ? groupSpecies.eq(selectedSpecies) : null,
                        occupation.validAndNotDeleted(),
                        clubMemberExists
                )
                .orderBy(club.nameFinnish.asc(), groupSpecies.nameFinnish.asc(), group.nameFinnish.asc(),
                        person.lastName.asc(), person.firstName.asc())
                .fetch()
                .stream()
                .map(t -> GroupMHCsvView.csvRow(
                        t.get(club).getNameFinnish(),
                        t.get(group).getNameFinnish(),
                        t.get(groupSpecies.nameFinnish),
                        year,
                        t.get(person),
                        t.get(occupation.contactInfoShare) != null))
                .collect(toList());

        return GroupMHCsvView.create(exportedClub, data);
    }
}
