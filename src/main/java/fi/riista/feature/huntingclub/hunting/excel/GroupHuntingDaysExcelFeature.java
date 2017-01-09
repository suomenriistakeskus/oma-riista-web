package fi.riista.feature.huntingclub.hunting.excel;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameDiaryEntryDTO;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.HasHuntingDayId;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.GroupHuntingDiaryService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class GroupHuntingDaysExcelFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameDiaryService gameDiaryService;

    @Resource
    private GroupHuntingDiaryService huntingService;

    @Resource
    private GroupHuntingDayService groupHuntingDayService;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public GroupHuntingDaysExcelView export(final long groupId) {
        final HuntingClubGroup group = requireEntityService.requireHuntingGroup(groupId, EntityPermission.READ);

        final LocalisedString clubName = group.getParentOrganisation().getNameLocalisation();
        final LocalisedString groupName = group.getNameLocalisation();

        final Locale locale = LocaleContextHolder.getLocale();
        final Map<Integer, GameSpeciesDTO> species =
                F.index(gameDiaryService.getGameSpecies(), GameSpeciesDTO::getCode);

        final List<GroupHuntingDayDTO> days = sortDays(groupHuntingDayService.findByClubGroup(group));

        final Map<GameDiaryEntryType, List<Long>> rejections = groupHuntingDayService.listRejected(group);
        final List<HarvestDTO> harvests = postProcess(rejections.get(GameDiaryEntryType.HARVEST), huntingService.getHarvestsOfGroupMembers(group));
        final List<ObservationDTO> observations = postProcess(rejections.get(GameDiaryEntryType.OBSERVATION), huntingService.getObservationsOfGroupMembers(group));

        return new GroupHuntingDaysExcelView(locale, new EnumLocaliser(messageSource, locale), species, clubName,
                groupName, days, harvests, observations);
    }

    private static List<GroupHuntingDayDTO> sortDays(final List<GroupHuntingDayDTO> days) {
        return days.stream()
                .sorted(comparing(GroupHuntingDayDTO::getStartDate).thenComparing(GroupHuntingDayDTO::getStartTime))
                .collect(toList());
    }

    private static <T extends GameDiaryEntryDTO & HasHuntingDayId> List<T> postProcess(
            final List<Long> rejections, final List<T> entries) {

        return entries.stream()
                .filter(isNotRejected(rejections))
                .filter(isLinkedToHuntingDay())
                .sorted(comparing(GameDiaryEntryDTO::getPointOfTime))
                .collect(toList());
    }

    private static <T extends GameDiaryEntryDTO & HasHuntingDayId> Predicate<T> isNotRejected(List<Long> rejections) {
        return entry -> !rejections.contains(entry.getId());
    }

    private static <T extends GameDiaryEntryDTO & HasHuntingDayId> Predicate<T> isLinkedToHuntingDay() {
        return entry -> entry.getHuntingDayId() != null;
    }
}
