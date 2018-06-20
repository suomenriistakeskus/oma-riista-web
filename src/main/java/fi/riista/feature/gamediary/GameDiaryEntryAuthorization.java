package fi.riista.feature.gamediary;

import com.google.common.collect.Ordering;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class GameDiaryEntryAuthorization<T extends GameDiaryEntry>
        extends AbstractEntityAuthorization<T> {

    private static final List<OccupationType> CLUB_OCCUPATION_TYPE_PRIORITY = Arrays.asList(
            OccupationType.SEURAN_YHDYSHENKILO,
            OccupationType.RYHMAN_METSASTYKSENJOHTAJA,
            OccupationType.RYHMAN_JASEN,
            OccupationType.SEURAN_JASEN);

    @Resource
    protected UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    protected GameDiaryEntryAuthorization() {
        super();
    }

    @Override
    protected final void authorizeTarget(
            @Nonnull final AuthorizationTokenCollector collector,
            @Nonnull final T diaryEntry,
            @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collectNonClubRoles(activePerson, collector, diaryEntry);
            if (collector.hasPermission()) {
                return;
            }

            if (diaryEntry.getHuntingDayOfGroup() != null) {
                final HuntingClubGroup group = diaryEntry.getHuntingDayOfGroup().getGroup();

                getClubAndGroupAndOccupationTypes(activePerson, Collections.singleton(group))
                        .forEach(collector::addAuthorizationRole);

            } else {// diaryEntry.getHuntingDayOfGroup() == null
                getClubAndGroupAndOccupationTypes(activePerson, findCandidateGroups(diaryEntry)).stream()
                        .filter(CLUB_OCCUPATION_TYPE_PRIORITY::contains)
                        .min(Ordering.explicit(CLUB_OCCUPATION_TYPE_PRIORITY))
                        .ifPresent(collector::addAuthorizationRole);
            }
        });
    }

    protected abstract void collectNonClubRoles(
            Person person, AuthorizationTokenCollector collector, T entity);

    private Set<OccupationType> getClubAndGroupAndOccupationTypes(final Person person,
                                                                  final Collection<HuntingClubGroup> groups) {
        final Map<Organisation, Set<OccupationType>> personOccupationTypes =
                getOccupationTypesGroupedByOrganisation(person);

        return Stream.concat(groups.stream(), groups.stream().map(Organisation::getParentOrganisation))
                .flatMap(org -> personOccupationTypes.getOrDefault(org, emptySet()).stream())
                .collect(toSet());
    }

    private Map<Organisation, Set<OccupationType>> getOccupationTypesGroupedByOrganisation(final Person person) {
        return occupationRepository.findActiveByPerson(person).stream()
                .collect(Collectors.groupingBy(
                        Occupation::getOrganisation,
                        Collectors.mapping(Occupation::getOccupationType, toSet())));
    }

    private List<HuntingClubGroup> findCandidateGroups(@Nonnull final GameDiaryEntry diaryEntry) {
        final LocalDate diaryEntryDate = DateUtil.toLocalDateNullSafe(diaryEntry.getPointOfTime());
        final int huntingYearOfDiaryEntry = DateUtil.huntingYearContaining(diaryEntryDate);

        return huntingClubGroupRepository
                .findGroupsByAuthorAndActorWuthAreaIntersecting(diaryEntry, huntingYearOfDiaryEntry).stream()
                .filter(group -> hasCorrectSpeciesOrObservedWithinHunting(diaryEntry, group))
                .filter(group -> authorOrActorHasValidClubAndGroupRole(diaryEntry, group))
                .filter(group -> groupPermitIsValidDuringPointOfTime(diaryEntry, group))
                .collect(toList());
    }

    private static Boolean hasCorrectSpeciesOrObservedWithinHunting(final GameDiaryEntry diaryEntry,
                                                                    final HuntingClubGroup group) {
        return diaryEntry.getType().apply(diaryEntry,
                harvest -> harvest.getSpecies().equals(group.getSpecies()),
                Observation::observedWithinMooseHunting);
    }

    private boolean groupPermitIsValidDuringPointOfTime(final GameDiaryEntry diaryEntry,
                                                        final HuntingClubGroup huntingClubGroup) {
        return harvestPermitSpeciesAmountRepository.findByHuntingClubGroupPermit(huntingClubGroup)
                .map(speciesAmount -> speciesAmount.containsDate(DateUtil.toLocalDateNullSafe(diaryEntry.getPointOfTime())))
                .orElse(false);
    }

    private boolean authorOrActorHasValidClubAndGroupRole(final GameDiaryEntry diaryEntry,
                                                          final HuntingClubGroup group) {
        final LocalDate validOn = DateUtil.toLocalDateNullSafe(diaryEntry.getPointOfTime());

        return diaryEntry.getAuthor() != null && personHasValidClubAndGroupRole(diaryEntry.getAuthor(), group, validOn) ||
                diaryEntry.getActor() != null && personHasValidClubAndGroupRole(diaryEntry.getActor(), group, validOn);
    }

    private boolean personHasValidClubAndGroupRole(final Person person,
                                                   final HuntingClubGroup group,
                                                   final LocalDate localDate) {
        final EnumSet<OccupationType> groupRoles = EnumSet.of(OccupationType.RYHMAN_JASEN, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final EnumSet<OccupationType> clubRoles = EnumSet.of(OccupationType.SEURAN_JASEN, OccupationType.SEURAN_YHDYSHENKILO);

        return 0 < occupationRepository.countActiveByTypeAndPersonAndOrganizationValidOn(group, person, groupRoles, localDate) &&
                0 < occupationRepository.countActiveByTypeAndPersonAndOrganizationValidOn(group.getParentOrganisation(), person, clubRoles, localDate);
    }
}
