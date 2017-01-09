package fi.riista.feature.huntingclub.members.rhy;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

@Component
public class RiistanhoitoyhdistysClubFeature {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public List<RhyClubOccupationDTO> listContacts(long rhyId) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        final QOccupation occupation = QOccupation.occupation;
        final QPerson person = QPerson.person;
        final QHuntingClub club = QHuntingClub.huntingClub;
        final QRiistanhoitoyhdistys rhy = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        final List<Occupation> res = queryFactory
                .select(occupation)
                .from(occupation)
                .join(occupation.person, person).fetchJoin()
                .join(occupation.organisation, club._super).fetchJoin()
                .join(club.parentOrganisation, rhy._super)
                .where(occupation.validAndNotDeleted(),
                        occupation.occupationType.eq(OccupationType.SEURAN_YHDYSHENKILO),
                        rhy.id.eq(rhyId)
                ).orderBy(occupation.organisation.nameFinnish.asc(), occupation.callOrder.asc().nullsLast())
                .fetch();

        return res.stream()
                .map(RhyClubOccupationDTO::createForClub)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<RhyClubOccupationDTO> listLeaders(long rhyId, int huntingYear) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        final QOccupation occupation = QOccupation.occupation;
        final QPerson person = QPerson.person;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QHuntingClub club = QHuntingClub.huntingClub;
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;

        final List<Occupation> res = queryFactory
                .select(occupation)
                .from(occupation)
                .join(occupation.person, person).fetchJoin()
                .join(occupation.organisation, group._super).fetchJoin()
                .join(group.parentOrganisation, club._super).fetchJoin()
                .join(group.harvestPermit, permit)
                .where(occupation.validAndNotDeleted(),
                        occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                        group.huntingYear.eq(huntingYear),
                        permit.rhy.id.eq(rhyId)
                ).orderBy(club.nameFinnish.asc(), group.nameFinnish.asc(), occupation.callOrder.asc().nullsLast())
                .fetch();

        return res.stream()
                .map(RhyClubOccupationDTO::createForGroup)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public View exportToExcel(final long rhyId, final int year, final Locale locale) {
        final List<RhyClubOccupationDTO> contacts = listContacts(rhyId);
        final List<RhyClubOccupationDTO> leaders = listLeaders(rhyId, year);
        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.getOne(rhyId);
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        return new RhyClubLeadersExcelView(localiser, locale, rhy.getNameLocalisation(), contacts, leaders);
    }
}
