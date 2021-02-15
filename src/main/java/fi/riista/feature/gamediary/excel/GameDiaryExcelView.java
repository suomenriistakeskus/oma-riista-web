package fi.riista.feature.gamediary.excel;

import com.google.common.collect.Streams;
import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.F;
import fi.riista.util.LocalisedEnum;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class GameDiaryExcelView extends AbstractXlsxView {

    private final EnumLocaliser i18n;
    private final List<HarvestDTO> harvests;
    private final List<ObservationDTO> observations;
    private final Map<Integer, LocalisedString> species;

    GameDiaryExcelView(final EnumLocaliser localiser,
                       final Map<Integer, LocalisedString> species,
                       final List<HarvestDTO> harvests,
                       final List<ObservationDTO> observations) {
        this.i18n = localiser;
        this.species = species;
        this.harvests = harvests;
        this.observations = observations;
    }

    private String createFilename() {
        return String.format("%s-%s.xlsx", i18n.getTranslation("gameDiary"),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createHarvestsSheet(workbook);
        createObservationsSheet(workbook);
    }

    private void createHarvestsSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, i18n.getTranslation("harvests"));

        helper.appendHeaderRow(Streams.concat(
                Arrays.stream(getCommonHeaders()),
                Arrays.stream(getHarvestHeaders()),
                Arrays.stream(getHarvestSpecimenHeaders()))
                .map(i18n::getTranslation)
                .collect(Collectors.toList()));

        for (final HarvestDTO harvest : harvests) {
            final List<HarvestSpecimenDTO> specimens = harvest.getSpecimens();
            final int specimensSize = specimens.size();
            final int amount = harvest.getAmount();

            if (specimensSize < amount) {
                // Export remaining specimens without details
                helper.appendRow();
                addCommonColumns(helper, harvest, harvest.getAuthorInfo(), harvest.getActorInfo());
                addHarvestFields(helper, harvest, amount - specimensSize);
            }

            for (final HarvestSpecimenDTO specimen : specimens) {
                helper.appendRow();

                addCommonColumns(helper, harvest, harvest.getAuthorInfo(), harvest.getActorInfo());
                addHarvestFields(helper, harvest, 1);

                helper.appendTextCell(localise(specimen.getGender()))
                        .appendTextCell(localise(specimen.getAge()))
                        .appendNumberCell(specimen.getWeight())
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

    private void addHarvestFields(final ExcelHelper helper, final HarvestDTO dto, final int amount) {
        helper
                .appendNumberCell(amount)
                .appendTextCell(dto.getDescription())
                .appendTextCell(i18n.getTranslation(dto.getHarvestReportState()))
                .appendTextCell(dto.getPermitNumber())
                .appendTextCell(i18n.getTranslation(dto.getFeedingPlace()))
                .appendTextCell(i18n.getTranslation(dto.getTaigaBeanGoose()))
                .appendTextCell(i18n.getTranslation(dto.getReportedWithPhoneCall()))
                .appendTextCell(i18n.getTranslation(dto.getHuntingMethod()))
                .appendTextCell(i18n.getTranslation(dto.getHuntingAreaType()))
                .appendTextCell(dto.getHuntingParty())
                .appendNumberCell(dto.getHuntingAreaSize());
    }

    private static String[] getHarvestHeaders() {
        return new String[]{
                "amount", "description", "state", "permitNumber",
                "feedingPlace", "taigaBeanGoose", "reportedWithPhoneCall",
                "huntingMethod", "huntingAreaType", "nameOfHuntingClubOrParty", "huntingAreaSize"};
    }

    private static String[] getHarvestSpecimenHeaders() {
        return new String[]{"gender", "age", "weight", "notEdible",
                "weightEstimated", "weightMeasured", "fitnessClass", "antlersType", "antlersWidth",
                "antlerPointsLeft", "antlerPointsRight", "additionalInfo"};
    }

    private void createObservationsSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, i18n.getTranslation("observations"));

        helper.appendHeaderRow(Streams.concat(
                Arrays.stream(getCommonHeaders()),
                Arrays.stream(getObservationHeaders()),
                Arrays.stream(getObservationSpecimenHeaders()))
                .map(i18n::getTranslation)
                .collect(Collectors.toList()));

        for (ObservationDTO observation : observations) {
            final List<ObservationSpecimenDTO> specimens = observation.getSpecimens();
            final int specimensSize = ofNullable(specimens).map(List::size).orElse(0);
            final int amount = F.coalesceAsInt(observation.getAmount(), 0);

            if (specimensSize != amount && amount != 1 || specimensSize == 0) {
                helper.appendRow();
                addCommonColumns(helper, observation, observation.getAuthorInfo(), observation.getActorInfo());

                helper.appendTextCell(localise(observation.getObservationType()))
                        .appendNumberCell(amount - specimensSize)
                        .appendTextCell(observation.getDescription());
                addMooseLikeCells(helper, observation);
            }
            for (int i = 0; i < specimensSize; i++) {
                helper.appendRow();
                addCommonColumns(helper, observation, observation.getAuthorInfo(), observation.getActorInfo());

                helper.appendTextCell(localise(observation.getObservationType()))
                        .appendNumberCell(1)
                        .appendTextCell(observation.getDescription());
                addMooseLikeCells(helper, observation);

                final ObservationSpecimenDTO specimen = specimens.get(i);
                helper.appendTextCell(localise(specimen.getGender()))
                        .appendTextCell(localise(specimen.getAge()))
                        .appendTextCell(localise(specimen.getMarking()))
                        .appendTextCell(localise(specimen.getState()));
            }
        }
        helper.autoSizeColumns();
    }

    private static void addMooseLikeCells(final ExcelHelper helper, final ObservationDTO observation) {
        helper.appendNumberCell(observation.getMooselikeMaleAmount())
                .appendNumberCell(observation.getMooselikeFemaleAmount())
                .appendNumberCell(observation.getMooselikeFemale1CalfAmount())
                .appendNumberCell(observation.getMooselikeFemale2CalfsAmount())
                .appendNumberCell(observation.getMooselikeFemale3CalfsAmount())
                .appendNumberCell(observation.getMooselikeFemale4CalfsAmount())
                .appendNumberCell(observation.getMooselikeCalfAmount())
                .appendNumberCell(observation.getMooselikeUnknownSpecimenAmount());
    }

    private static String[] getObservationHeaders() {
        return new String[]{"observationType", "amount", "description"};
    }

    private static String[] getObservationSpecimenHeaders() {
        return new String[]{"mooselikeMaleAmount", "mooselikeFemaleAmount", "mooselikeFemale1CalfAmount",
                "mooselikeFemale2CalfsAmount", "mooselikeFemale3CalfsAmount", "mooselikeFemale4CalfsAmount",
                "mooselikeCalfAmount", "mooselikeUnknownSpecimenAmount", "gender", "age", "gameMarking",
                "observedGameState"};
    }

    private void addCommonColumns(final ExcelHelper helper,
                                  final HuntingDiaryEntryDTO entry,
                                  final PersonWithHunterNumberDTO author,
                                  final PersonWithHunterNumberDTO actor) {
        helper.appendNumberCell(entry.getId())
                .appendDateCell(entry.getPointOfTime().toDate())
                .appendTimeCell(entry.getPointOfTime().toDate())
                .appendTextCell(i18n.getTranslation(entry.getGeoLocation().getSource()))
                .appendNumberCell(entry.getGeoLocation().getAccuracy())
                .appendNumberCell(entry.getGeoLocation().getLatitude())
                .appendNumberCell(entry.getGeoLocation().getLongitude())
                .appendTextCell(author.getLastName())
                .appendTextCell(author.getByName())
                .appendTextCell(actor.getLastName())
                .appendTextCell(actor.getByName())
                .appendTextCell(i18n.getTranslation(species.get(entry.getGameSpeciesCode())));
    }

    private <E extends Enum<E> & LocalisedEnum> String localise(final E e) {
        return i18n.getTranslation(e);
    }

    private static String[] getCommonHeaders() {
        return new String[]{"commonId", "date", "clockTime", "geolocationSource", "geolocationAccuracy", "latitude", "longitude",
                "lastNameOfAuthor", "firstNameOfAuthor", "lastNameOfHunter", "firstNameOfHunter", "species"};
    }
}
