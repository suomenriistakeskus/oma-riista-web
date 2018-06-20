package fi.riista.feature.huntingclub.group;

import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.sql.SQHuntingClubArea;
import fi.riista.sql.SQOrganisation;
import fi.riista.sql.SQZone;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;


@Repository
@Transactional
public class HuntingClubGroupRepositoryImpl implements HuntingClubGroupRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SQLTemplates queryDslSqlTemplates;

    @Resource
    private OccupationRepository occupationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HuntingClubGroup> findGroupsByAuthorAndActorWuthAreaIntersecting(final GameDiaryEntry diaryEntry,
                                                                                 final int huntingYear) {

        final Set<Long> authorAndActorClub = findAuthorAndActorClubMemberships(diaryEntry).stream()
                .map(Occupation::getOrganisation)
                .map(Organisation::getId)
                .collect(toSet());

        if (authorAndActorClub.isEmpty()) {
            return Collections.emptyList();
        }

        final SQOrganisation club = new SQOrganisation("club");
        final SQOrganisation group = new SQOrganisation("clubgroup");
        final SQHuntingClubArea area = SQHuntingClubArea.huntingClubArea;
        final SQZone zone = SQZone.zone;

        final QHuntingClubGroup groupEntity = new QHuntingClubGroup("clubgroup");
        return new JPASQLQuery<>(entityManager, queryDslSqlTemplates)
                .select(groupEntity)
                .from(group)
                .join(club).on(club.organisationId.eq(group.parentOrganisationId))
                .join(area).on(area.huntingClubAreaId.eq(group.huntingAreaId))
                .join(zone).on(zone.zoneId.eq(area.zoneId))
                .where(club.organisationId.in(authorAndActorClub))
                .where(area.isActive.isTrue())
                .where(area.huntingYear.eq(huntingYear))
                .where(zone.geom.intersects(GISUtils.createPointWithDefaultSRID(diaryEntry.getGeoLocation())))
                .fetch();
    }

    private List<Occupation> findAuthorAndActorClubMemberships(final GameDiaryEntry diaryEntry) {
        final Person actor = diaryEntry.getActor();
        final Person author = diaryEntry.getAuthor();
        if (actor.equals(author)) {
            return findClubMemberships(actor);
        }
        return F.concat(findClubMemberships(actor), findClubMemberships(author));
    }

    private List<Occupation> findClubMemberships(final Person person) {
        return occupationRepository.findActiveByPersonAndOrganisationTypes(person, EnumSet.of(OrganisationType.CLUB));
    }
}
