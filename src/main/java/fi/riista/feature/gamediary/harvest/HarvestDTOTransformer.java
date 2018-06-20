package fi.riista.feature.gamediary.harvest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestQuotaRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class HarvestDTOTransformer extends HarvestDTOTransformerBase<HarvestDTO> {

    @Resource
    private JPAQueryFactory queryFactory;

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Override
    protected List<HarvestDTO> transform(@Nonnull final List<Harvest> harvests) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        final Function<Harvest, GameSpecies> harvestToSpecies = getGameDiaryEntryToSpeciesMapping(harvests);
        final Function<Harvest, Person> harvestToAuthor = getGameDiaryEntryToAuthorMapping(harvests);
        final Function<Harvest, Person> harvestToHunter = getHarvestToHunterMapping(harvests);

        final Function<Harvest, HarvestPermit> harvestToPermit = getHarvestToPermitMapping(harvests);
        final Function<Harvest, HarvestQuota> harvestToQuota = getHarvestToQuotaMapping(harvests);

        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);
        final Map<Harvest, List<GameDiaryImage>> groupedImages = getImagesGroupedByHarvests(harvests);

        final Map<Harvest, Organisation> harvestToGroupOfHuntingDay = getGroupOfHuntingDay(harvests);
        final Function<Harvest, Person> harvestToHuntingDayApprover = getApproverToHuntingDay(harvests);

        final Predicate<Harvest> groupHuntingStatusTester = h -> h.getHuntingDayOfGroup() != null;
        final Predicate<Harvest> contactPersonTester = getContactPersonOfPermittedHarvestTester(activeUser.getPerson());

        return harvests.stream().filter(Objects::nonNull).map(harvest -> {
            final Person author = harvestToAuthor.apply(harvest);
            final Person hunter = harvestToHunter.apply(harvest);
            final Organisation groupOfHuntingDay = harvestToGroupOfHuntingDay.get(harvest);
            final Person approverToHuntingDay = harvestToHuntingDayApprover.apply(harvest);

            final HarvestQuota harvestQuota = harvestToQuota.apply(harvest);
            final Person authenticatedPerson = activeUser.getPerson();

            final boolean authorOrActor = author.equals(authenticatedPerson) || hunter.equals(authenticatedPerson);
            final boolean canEdit = HarvestLockedCondition.canEdit(
                    authenticatedPerson, harvest, null,
                    groupHuntingStatusTester, contactPersonTester);

            final HarvestDTO dto = HarvestDTO.builder()
                    .populateWith(harvest)
                    .populateWith(harvestToSpecies.apply(harvest))
                    .populateWith(harvestToPermit.apply(harvest))
                    .populateSpecimensWith(groupedSpecimens.get(harvest))
                    .populateWith(groupedImages.get(harvest))
                    .withAuthorInfo(author)
                    .withActorInfo(hunter)
                    .withGroupOfHuntingDay(groupOfHuntingDay)
                    .withApproverToHuntingDay(approverToHuntingDay)
                    // Should not cause too many queries, because there are only 5 unique harvest areas currently
                    .withHarvestArea(harvestQuota != null ? harvestQuota.getHarvestArea() : null)
                    .withCanEdit(canEdit)
                    .build();

            if (activeUser.isModeratorOrAdmin()) {
                dto.setHarvestReportMemo(harvest.getHarvestReportMemo());
            }

            if (!authorOrActor) {
                dto.setDescription(null);
                dto.getImageIds().clear();
            }

            return dto;

        }).collect(toList());
    }

    private Function<Harvest, Person> getHarvestToHunterMapping(final Iterable<Harvest> harvests) {
        return createGameDiaryEntryToPersonMapping(harvests, Harvest::getActualShooter);
    }

    private Function<Harvest, HarvestQuota> getHarvestToQuotaMapping(final Iterable<Harvest> harvests) {
        return CriteriaUtils.singleQueryFunction(harvests, Harvest::getHarvestQuota, harvestQuotaRepository, false);
    }

    private Function<Harvest, Person> getApproverToHuntingDay(final Iterable<Harvest> harvests) {
        return createGameDiaryEntryToPersonMapping(harvests, Harvest::getApproverToHuntingDay, false);
    }

    private Map<Harvest, Organisation> getGroupOfHuntingDay(final List<Harvest> harvests) {
        final QHarvest harvest = QHarvest.harvest;
        final QGroupHuntingDay day = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        return queryFactory.select(harvest, group)
                .from(harvest)
                .join(harvest.huntingDayOfGroup, day)
                .join(day.group, group)
                .where(harvest.in(harvests))
                .fetch()
                .stream().collect(toMap(t -> t.get(0, Harvest.class), t -> t.get(1, HuntingClubGroup.class)));
    }
}
