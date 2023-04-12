package fi.riista.feature.huntingclub.members.excel;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.QMunicipality;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.organization.address.QAddress;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Component
public class HuntingClubMembersExportFeature {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public HuntingClubMembersExportView export(final long clubId) {
        final HuntingClub exportedClub = huntingClubRepository.getOne(clubId);

        userAuthorizationHelper.assertClubContactOrModerator(exportedClub);

        final List<HuntingClubMemberRowDTO> data = exportData(exportedClub);

        return new HuntingClubMembersExportView(new EnumLocaliser(messageSource), exportedClub, data);
    }

    @Transactional(readOnly = true)
    public List<HuntingClubMemberRowDTO> exportDataForTest(final long clubId) {
        final HuntingClub exportedClub = huntingClubRepository.getOne(clubId);

        userAuthorizationHelper.assertClubContactOrModerator(exportedClub);

        return exportData(exportedClub);
    }

    private List<HuntingClubMemberRowDTO> exportData(final HuntingClub exportedClub) {
        final QHuntingClub club = QHuntingClub.huntingClub;
        final QOccupation clubOccupation = QOccupation.occupation;
        final QPerson person = QPerson.person;
        final QAddress mrAddress = QAddress.address;
        final QMunicipality municipality = QMunicipality.municipality;

        return queryFactory.select(club, person, clubOccupation.contactInfoShare)
                .from(club)
                .join(club.occupations, clubOccupation)
                .join(clubOccupation.person, person)
                .leftJoin(person.mrAddress, mrAddress).fetchJoin()
                .leftJoin(person.homeMunicipality, municipality).fetchJoin()
                .where(club.eq(exportedClub),
                        clubOccupation.organisation.eq(exportedClub),
                        clubOccupation.validAndNotDeleted()
                )
                .orderBy(club.nameFinnish.asc(), person.lastName.asc(), person.firstName.asc())
                .fetch()
                .stream()
                .map(t -> new HuntingClubMemberRowDTO(
                        new LocalisedString(
                                Objects.requireNonNull(t.get(club)).getNameFinnish(),
                                Objects.requireNonNull(t.get(club)).getNameSwedish()
                        ),
                        Objects.requireNonNull(t.get(person)),
                        t.get(clubOccupation.contactInfoShare) != null))
                .collect(toList());
    }
}
