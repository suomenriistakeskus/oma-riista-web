package fi.riista.feature.harvestpermit;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.Collect;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.today;
import static java.util.Collections.emptyList;

@Component
public class HarvestPermitWithHuntingClubGroupsFeature {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitWithHuntingClubGroupsFeature.class);

    @Resource
    protected ActiveUserService activeUserService;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Transactional(readOnly = true)
    public List<HarvestPermitWithHuntingClubGroupsDTO> listPermitsWithHuntingClubGroups() {

        final SystemUser user = activeUserService.requireActiveUser();
        Person person = user.getPerson();

        if (person == null) {
            LOG.warn("Person not found, returning empty list");
            return emptyList();
        }

        Map<HuntingClubGroup, Occupation> occupationsByGroup = findHuntingLeaderOccupations(person);

        // Fetch clubs in one query
        organisationRepository.findAllById(
                F.mapNonNullsToSet(occupationsByGroup.keySet(), g -> g.getParentOrganisation().getId()));

        // Fetch permits in one query
        Set<Long> permitIds = occupationsByGroup.keySet().stream()
                .map(HuntingClubGroup::getHarvestPermit)
                .filter(Objects::nonNull)
                .map(HarvestPermit::getId)
                .collect(Collectors.toSet());
        harvestPermitRepository.findAllById(permitIds);

        Map<HarvestPermit, List<HuntingClubGroup>> groupsByPermit = occupationsByGroup.keySet().stream()
                .collect(Collect.nullSafeGroupingBy(HuntingClubGroup::getHarvestPermit));

        return groupsByPermit.entrySet().stream()
                        .map(e-> {
                            HarvestPermit permit = e.getKey();
                            List<HuntingClubGroup> groups = e.getValue();
                            List<Organisation> clubs = groups.stream()
                                    .map(HuntingClubGroup::getParentOrganisation)
                                    .distinct()
                                    .collect(Collectors.toList());
                            Occupation occupation = groups.stream()
                                    .map(occupationsByGroup::get)
                                    .findFirst()
                                    .orElse(null);
                            return HarvestPermitWithHuntingClubGroupsDTO.create(permit, groups, clubs, occupation);
                        })
                                .collect(Collectors.toList());

    }

    private Map<HuntingClubGroup, Occupation> findHuntingLeaderOccupations(final Person person) {
        QOccupation OCCUPATION = QOccupation.occupation;
        QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;

        return jpqlQueryFactory
                .from(OCCUPATION)
                .innerJoin(OCCUPATION.organisation, GROUP._super)
                .where(OCCUPATION.person.eq(person))
                .where(OCCUPATION.occupationType.eq(RYHMAN_METSASTYKSENJOHTAJA))
                .where(OCCUPATION.validAndNotDeleted())
                .where(GROUP.lifecycleFields.deletionTime.isNull())
                .where(GROUP.harvestPermit.id.isNotNull())
                .where(GROUP.huntingYear.goe(huntingYear()))
                .transform(groupBy(GROUP).as(OCCUPATION));
    }
}
