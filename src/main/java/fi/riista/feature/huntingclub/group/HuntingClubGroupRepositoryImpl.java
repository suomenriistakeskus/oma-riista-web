package fi.riista.feature.huntingclub.group;

import com.google.common.collect.ImmutableSet;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.sql.SQHuntingClubArea;
import fi.riista.sql.SQOrganisation;
import fi.riista.sql.SQZone;
import fi.riista.util.GISUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static fi.riista.util.Collect.idSet;


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
    public List<HuntingClubGroup> findGroupsByAuthorOrActorWithGroupAreaIntersecting(final GameDiaryEntry diaryEntry,
                                                                                     final int huntingYear) {

        final Set<Long> groupIds = findAuthorAndActorActiveGroupIds(diaryEntry);

        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }

        final SQOrganisation group = new SQOrganisation("clubgroup");
        final SQHuntingClubArea area = SQHuntingClubArea.huntingClubArea;
        final SQZone zone = SQZone.zone;

        final QHuntingClubGroup groupEntity = new QHuntingClubGroup("clubgroup");
        return new JPASQLQuery<>(entityManager, queryDslSqlTemplates)
                .select(groupEntity)
                .from(group)
                .join(area).on(area.huntingClubAreaId.eq(group.huntingAreaId))
                .join(zone).on(zone.zoneId.eq(area.zoneId))
                .where(group.organisationId.in(groupIds))
                .where(area.isActive.isTrue())
                .where(area.huntingYear.eq(huntingYear))
                .where(zone.geom.intersects(GISUtils.createPointWithDefaultSRID(diaryEntry.getGeoLocation())))
                .fetch();
    }

    private Set<Long> findAuthorAndActorActiveGroupIds(final GameDiaryEntry diaryEntry) {
        final ImmutableSet<Person> persons = ImmutableSet.of(diaryEntry.getAuthor(), diaryEntry.getActor());
        return occupationRepository.findActiveByPersonsAndOrganisationType(persons,
                OrganisationType.CLUBGROUP).stream()
                .map(Occupation::getOrganisation)
                .collect(idSet());
    }

}
