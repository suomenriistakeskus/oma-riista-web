package fi.riista.feature.huntingclub.hunting.excel;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.HasHuntingDayId;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.hunting.GroupHuntingDiaryService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTOTransformer;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class ClubHuntingDataExcelFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private GroupHuntingDiaryService huntingService;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private GroupHuntingDayDTOTransformer dtoTransformer;

    @Resource
    private MessageSource messageSource;

    @Resource
    private HarvestPermitRepository permitRepository;

    @Transactional(readOnly = true)
    public ClubHuntingDataExcelView export(final long clubId,
                                           final int huntingYear,
                                           final int gameSpeciesCode) {
        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);
        final LocalisedString clubName = huntingClub.getNameLocalisation();

        final LocalisedString rhyName = huntingClub.getParentOrganisation().getNameLocalisation();

        final Locale locale = LocaleContextHolder.getLocale();
        final Map<Integer, LocalisedString> speciesIndex = gameSpeciesService.getNameIndex();

        final Function<HuntingClubGroup, String> nameGetter = Locales.isSwedish(locale)
                ? HuntingClubGroup::getNameSwedish
                : HuntingClubGroup::getNameFinnish;

        final List<HuntingClubGroup> clubGroups = huntingClubGroupRepository.findByParentOrganisation(huntingClub).stream()
                .filter(group -> gameSpecies.equals(group.getSpecies()))
                .filter(group -> huntingYear == group.getHuntingYear())
                .collect(toList());

        final Function<HuntingClubGroup, HarvestPermit> groupToPermitMapping =
                singleQueryFunction(clubGroups, HuntingClubGroup::getHarvestPermit, permitRepository, false);

        final List<ClubHuntingDataExcelDTO> excelData = clubGroups.stream()
                .sorted(comparing(HuntingClubGroup::getHuntingYear).thenComparing(nameGetter))
                .map(group -> {
                    final LocalisedString groupName = group.getNameLocalisation();
                    final List<GroupHuntingDayDTO> days =
                            sortDays(dtoTransformer.apply(groupHuntingDayService.findByClubGroup(group)));

                    final Map<GameDiaryEntryType, List<Long>> rejections = groupHuntingDayService.listRejected(group);
                    final List<HarvestDTO> harvests = postProcess(rejections.get(GameDiaryEntryType.HARVEST),
                            huntingService.getHarvestsOfGroupMembers(group));
                    final List<ObservationDTO> observations = postProcess(rejections.get(GameDiaryEntryType.OBSERVATION),
                            huntingService.getObservationsOfGroupMembers(group));

                    final String permitNumber = F.mapNullable(groupToPermitMapping.apply(group), HarvestPermit::getPermitNumber);

                    return new ClubHuntingDataExcelDTO(groupName, days, harvests, observations, rhyName, permitNumber);
                }).collect(toList());

        return new ClubHuntingDataExcelView(new EnumLocaliser(messageSource, locale), speciesIndex, clubName, excelData);
    }

    private static List<GroupHuntingDayDTO> sortDays(final List<GroupHuntingDayDTO> days) {
        return days.stream()
                .sorted(comparing(GroupHuntingDayDTO::getStartDate).thenComparing(GroupHuntingDayDTO::getStartTime))
                .collect(toList());
    }

    private static <T extends HuntingDiaryEntryDTO & HasHuntingDayId> List<T> postProcess(
            final List<Long> rejections, final List<T> entries) {

        return entries.stream()
                .filter(isNotRejected(rejections))
                .filter(isLinkedToHuntingDay())
                .sorted(comparing(HuntingDiaryEntryDTO::getPointOfTime))
                .collect(toList());
    }

    private static <T extends HuntingDiaryEntryDTO & HasHuntingDayId> Predicate<T> isNotRejected(List<Long> rejections) {
        return entry -> !rejections.contains(entry.getId());
    }

    private static <T extends HuntingDiaryEntryDTO & HasHuntingDayId> Predicate<T> isLinkedToHuntingDay() {
        return entry -> entry.getHuntingDayId() != null;
    }
}
