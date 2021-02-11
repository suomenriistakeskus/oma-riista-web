package fi.riista.feature.huntingclub.hunting.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.F;
import fi.riista.util.LocalisedEnum;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class ClubHuntingDataExcelView extends AbstractXlsxView {

    private final EnumLocaliser localiser;
    private final Map<Integer, LocalisedString> species;
    private final List<ClubHuntingDataExcelDTO> groupData;
    private final String filename;
    private final boolean includeDeerPilotFields;

    public ClubHuntingDataExcelView(final EnumLocaliser localiser,
                                    final Map<Integer, LocalisedString> species,
                                    final LocalisedString clubName,
                                    final List<ClubHuntingDataExcelDTO> groupData,
                                    final boolean includeDeerPilotFields) {
        this.localiser = localiser;
        this.species = species;
        this.groupData = groupData;
        this.filename = String.format("%s - %s.xlsx",
                localiser.getTranslation(clubName),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
        this.includeDeerPilotFields = includeDeerPilotFields;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, filename);

        createDaysSheet(workbook);
        createHarvestsSheet(workbook);
        createObservationsSheet(workbook);
    }

    private void createDaysSheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("huntingDays"));
        helper.appendHeaderRow(concatAndTranslate(new String[]{
                "groupName", "startDate", "startTime", "endDate", "endTime", "breakDuration", "huntingDuration", "showDepth",
                "groupHuntingMethod", "numberOfHunters", "numberOfHounds"
        }));

        for (final ClubHuntingDataExcelDTO group : groupData) {
            final String groupName = localiser.getTranslation(group.getGroupName());

            for (GroupHuntingDayDTO day : group.getDays()) {
                helper.appendRow()
                        .appendTextCell(groupName)
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
        }
        helper.autoSizeColumns();
    }

    private void createHarvestsSheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("harvests"));
        helper.appendHeaderRow(concatAndTranslate(F.concat(getCommonHeaders(), getWithinDeerHuntingHeaders()),
                "gender", "age", "notEdible", "weightEstimated", "weightMeasured", "fitnessClass", "antlersType",
                "antlersWidth", "antlerPointsLeft", "antlerPointsRight", "additionalInfo"));

        for (final ClubHuntingDataExcelDTO group : groupData) {
            final String groupName = localiser.getTranslation(group.getGroupName());

            for (HarvestDTO harvest : group.getHarvests()) {
                helper.appendRow();
                addCommonColumns(helper, groupName, harvest, harvest.getAuthorInfo(), harvest.getActorInfo());
                addWithinDeerHuntingCells(helper, harvest);

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
                        .appendTextCell(specimen.getAdditionalInfo());
            }
        }
        helper.autoSizeColumns();
    }

    private void createObservationsSheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("observations"));
        helper.appendHeaderRow(concatAndTranslate(F.concat(getCommonHeaders(), getWithinDeerHuntingHeaders()),
                "observationType", "amount", "mooselikeMaleAmount", "mooselikeFemaleAmount", "mooselikeFemale1CalfAmount",
                "mooselikeFemale2CalfsAmount", "mooselikeFemale3CalfsAmount", "mooselikeFemale4CalfsAmount",
                "mooselikeCalfAmount", "mooselikeUnknownSpecimenAmount", "gender", "age", "gameMarking",
                "observedGameState"));

        for (final ClubHuntingDataExcelDTO group : groupData) {
            final String groupName = localiser.getTranslation(group.getGroupName());

            group.getObservations().forEach(observation -> {
                final List<ObservationSpecimenDTO> specimens = observation.getSpecimens();

                if (CollectionUtils.isEmpty(specimens)) {
                    helper.appendRow();
                    addCommonColumns(helper, groupName, observation, observation.getAuthorInfo(), observation.getActorInfo());

                    addWithinDeerHuntingCells(helper, observation);
                    helper.appendTextCell(localise(observation.getObservationType()))
                            .appendNumberCell(observation.getAmount());
                    addMooseLikeCells(helper, observation);
                } else {
                    specimens.forEach(specimen -> {
                        helper.appendRow();
                        addCommonColumns(helper, groupName, observation, observation.getAuthorInfo(), observation.getActorInfo());

                        addWithinDeerHuntingCells(helper, observation);
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
        }
        helper.autoSizeColumns();
    }

    private static void addMooseLikeCells(ExcelHelper helper, ObservationDTO observation) {
        helper.appendNumberCell(observation.getMooselikeMaleAmount())
                .appendNumberCell(observation.getMooselikeFemaleAmount())
                .appendNumberCell(observation.getMooselikeFemale1CalfAmount())
                .appendNumberCell(observation.getMooselikeFemale2CalfsAmount())
                .appendNumberCell(observation.getMooselikeFemale3CalfsAmount())
                .appendNumberCell(observation.getMooselikeFemale4CalfsAmount())
                .appendNumberCell(observation.getMooselikeCalfAmount())
                .appendNumberCell(observation.getMooselikeUnknownSpecimenAmount());
    }

    private void addWithinDeerHuntingCells(ExcelHelper helper, ObservationDTO observation) {
        if (includeDeerPilotFields) {
            helper.appendTextCell(localiser.getTranslation(observation.getDeerHuntingType()))
                    .appendTextCell(observation.getDeerHuntingTypeDescription());
        }
    }

    private void addWithinDeerHuntingCells(ExcelHelper helper, HarvestDTO harvest) {
        if (includeDeerPilotFields) {
            helper.appendTextCell(localiser.getTranslation(harvest.getDeerHuntingType()))
                    .appendTextCell(harvest.getDeerHuntingOtherTypeDescription());
        }
    }

    private  String[] getWithinDeerHuntingHeaders() {

        if (!includeDeerPilotFields) {
            return new String[]{};
        }

        return new String[]{ "deerHuntingType", "deerHuntingTypeDescription" };
    }

    private void addCommonColumns(ExcelHelper helper,
                                  String groupName,
                                  HuntingDiaryEntryDTO entry,
                                  PersonWithHunterNumberDTO author,
                                  PersonWithHunterNumberDTO actor) {

        helper.appendTextCell(groupName)
                .appendDateCell(entry.getPointOfTime().toDate())
                .appendTimeCell(entry.getPointOfTime().toDate())
                .appendTextCell(localiser.getTranslation(entry.getGeoLocation().getSource()))
                .appendNumberCell(entry.getGeoLocation().getAccuracy())
                .appendNumberCell(entry.getGeoLocation().getLatitude())
                .appendNumberCell(entry.getGeoLocation().getLongitude())
                .appendTextCell(author.getLastName())
                .appendTextCell(author.getByName())
                .appendTextCell(actor.getLastName())
                .appendTextCell(actor.getByName())
                .appendTextCell(localiser.getTranslation(species.get(entry.getGameSpeciesCode())));
    }

    private <E extends Enum<E> & LocalisedEnum> String localise(final E e) {
        return localiser.getTranslation(e);
    }

    private static String[] getCommonHeaders() {
        return new String[]{
                "groupName", "date", "clockTime", "geolocationSource", "geolocationAccuracy", "latitude", "longitude",
                "lastNameOfAuthor", "firstNameOfAuthor", "lastNameOfHunter", "firstNameOfHunter", "species"
        };
    }

    private String[] concatAndTranslate(String[] arr1, String... arr2) {
        return localiser.translate(F.concat(arr1, arr2));
    }
}
