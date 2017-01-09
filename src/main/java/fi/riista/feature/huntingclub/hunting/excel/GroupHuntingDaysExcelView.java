package fi.riista.feature.huntingclub.hunting.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class GroupHuntingDaysExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private final Locale locale;
    private final EnumLocaliser localiser;
    private final Map<Integer, GameSpeciesDTO> species;
    private final LocalisedString clubName;
    private final LocalisedString groupName;
    private final List<GroupHuntingDayDTO> days;
    private final List<HarvestDTO> harvests;
    private final List<ObservationDTO> observations;

    public GroupHuntingDaysExcelView(final Locale locale,
                                     final EnumLocaliser localiser,
                                     final Map<Integer, GameSpeciesDTO> species,
                                     final LocalisedString clubName,
                                     final LocalisedString groupName,
                                     final List<GroupHuntingDayDTO> days,
                                     final List<HarvestDTO> harvests,
                                     final List<ObservationDTO> observations) {
        this.locale = locale;
        this.localiser = localiser;
        this.species = species;
        this.clubName = clubName;
        this.groupName = groupName;
        this.days = days;
        this.harvests = harvests;
        this.observations = observations;
    }

    private String createFilename() {
        return String.format(
                "%s - %s-%s.xls",
                clubName.getAnyTranslation(locale),
                groupName.getAnyTranslation(locale),
                DATETIME_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        response.setHeader(ContentDispositionUtil.HEADER_NAME,
                ContentDispositionUtil.encodeAttachmentFilename(createFilename()));

        createDaysSheet(workbook);
        createHarvestsSheet(workbook);
        createObservationsSheet(workbook);
    }


    private void createDaysSheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("huntingDays"));
        helper.appendHeaderRow(concatAndTranslate(new String[]{"startDate", "startTime", "endDate", "endTime",
                "breakDuration", "hungingDuration", "showDepth", "groupHuntingMethod", "numberOfHunters",
                "numberOfHounds"}));

        for (GroupHuntingDayDTO day : days) {
            helper.appendRow()
                    .appendDateCell(DateUtil.toDateNullSafe(day.getStartDate()))
                    .appendTimeCell(DateUtil.toDateTodayNullSafe(day.getStartTime()))
                    .appendDateCell(DateUtil.toDateNullSafe(day.getEndDate()))
                    .appendTimeCell(DateUtil.toDateTodayNullSafe(day.getEndTime()))
                    .appendNumberCell(day.getBreakDurationInMinutes())
                    .appendNumberCell(day.getDurationInMinutes())
                    .appendNumberCell(day.getSnowDepth())
                    .appendTextCell(localise(day.getHuntingMethod()))
                    .appendNumberCell(day.getNumberOfHunters())
                    .appendNumberCell(day.getNumberOfHounds());
        }
        helper.autoSizeColumns();
    }

    private void createHarvestsSheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("harvests"));
        helper.appendHeaderRow(concatAndTranslate(getCommonHeaders(), "gender", "age", "notEdible",
                "weightEstimated", "weightMeasured", "fitnessClass", "antlersType", "antlersWidth",
                "antlerPointsLeft", "antlerPointsRight", "additionalInfo"));

        for (HarvestDTO harvest : harvests) {
            helper.appendRow();
            addCommonColumns(helper, harvest, harvest.getAuthorInfo(), harvest.getActorInfo());

            HarvestSpecimenDTO specimen = harvest.getSpecimens().get(0);
            helper.appendTextCell(localise(specimen.getGender()))
                    .appendTextCell(localise(specimen.getAge()))
                    .appendBoolCell(specimen.getNotEdible())
                    .appendNumberCell(specimen.getWeightEstimated())
                    .appendNumberCell(specimen.getWeightMeasured())
                    .appendTextCell(localise(specimen.getFitnessClass()))
                    .appendTextCell(localise(specimen.getAntlersType()))
                    .appendNumberCell(specimen.getAntlersWidth())
                    .appendNumberCell(specimen.getAntlerPointsLeft())
                    .appendNumberCell(specimen.getAntlerPointsRight())
                    .appendTextCell(specimen.getAdditionalInfo())
            ;
        }
        helper.autoSizeColumns();
    }

    private void createObservationsSheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("observations"));
        helper.appendHeaderRow(concatAndTranslate(getCommonHeaders(), "observationType", "amount",
                "mooselikeMaleAmount", "mooselikeFemaleAmount", "mooselikeFemale1CalfAmount",
                "mooselikeFemale2CalfsAmount", "mooselikeFemale3CalfsAmount", "mooselikeFemale4CalfsAmount",
                "mooselikeUnknownSpecimenAmount", "gender", "age", "gameMarking", "observedGameState"));

        observations.forEach(observation -> {
            final List<ObservationSpecimenDTO> specimens = observation.getSpecimens();

            if (CollectionUtils.isEmpty(specimens)) {
                helper.appendRow();
                addCommonColumns(helper, observation, observation.getAuthorInfo(), observation.getActorInfo());

                helper.appendTextCell(localise(observation.getObservationType()))
                        .appendNumberCell(observation.getAmount());
                addMooseLikeCells(helper, observation);
            } else {
                specimens.forEach(specimen -> {
                    helper.appendRow();
                    addCommonColumns(helper, observation, observation.getAuthorInfo(), observation.getActorInfo());

                    helper.appendTextCell(localise(observation.getObservationType()))
                            .appendNumberCell(1);
                    addMooseLikeCells(helper, observation);

                    helper.appendTextCell(localise(specimen.getGender()))
                            .appendTextCell(localise(specimen.getAge()))
                            .appendTextCell(localise(specimen.getMarking()))
                            .appendTextCell(localise(specimen.getState()));
                });
            }
        });
        helper.autoSizeColumns();
    }

    private static void addMooseLikeCells(ExcelHelper helper, ObservationDTO observation) {
        helper.appendNumberCell(observation.getMooselikeMaleAmount())
                .appendNumberCell(observation.getMooselikeFemaleAmount())
                .appendNumberCell(observation.getMooselikeFemale1CalfAmount())
                .appendNumberCell(observation.getMooselikeFemale2CalfsAmount())
                .appendNumberCell(observation.getMooselikeFemale3CalfsAmount())
                .appendNumberCell(observation.getMooselikeFemale4CalfsAmount())
                .appendNumberCell(observation.getMooselikeUnknownSpecimenAmount());
    }

    private void addCommonColumns(
            ExcelHelper helper,
            HuntingDiaryEntryDTO entry,
            PersonWithHunterNumberDTO author,
            PersonWithHunterNumberDTO actor) {

        helper.appendDateCell(entry.getPointOfTime().toDate())
                .appendTimeCell(entry.getPointOfTime().toDate())
                .appendTextCell(localiser.getTranslation(entry.getGeoLocation().getSource()))
                .appendNumberCell(entry.getGeoLocation().getAccuracy())
                .appendNumberCell(entry.getGeoLocation().getLatitude())
                .appendNumberCell(entry.getGeoLocation().getLongitude())
                .appendTextCell(author.getLastName())
                .appendTextCell(author.getByName())
                .appendTextCell(actor.getLastName())
                .appendTextCell(actor.getByName())
                .appendTextCell(species.get(entry.getGameSpeciesCode()).getName().get(locale.getLanguage()));
    }

    private String localise(Enum<?> e) {
        return localiser.getTranslation(e);
    }

    private static String[] getCommonHeaders() {
        return new String[]{"date", "clockTime", "geolocationSource", "geolocationAccuracy", "latitude", "longitude",
                "lastNameOfAuthor", "firstNameOfAuthor", "lastNameOfHunter", "firstNameOfHunter", "species"};
    }

    private String[] concatAndTranslate(String[] arr1, String... arr2) {
        return Stream.concat(Arrays.stream(arr1), Arrays.stream(arr2))
                .map(localiser::getTranslation)
                .toArray(String[]::new);
    }
}
