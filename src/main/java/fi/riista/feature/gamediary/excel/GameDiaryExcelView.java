package fi.riista.feature.gamediary.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpeciesDTO;
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
import fi.riista.util.MediaTypeExtras;
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

import static java.util.Optional.ofNullable;

public class GameDiaryExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private final Locale locale;
    private final EnumLocaliser localiser;

    private final List<HarvestDTO> harvests;
    private final List<ObservationDTO> observations;
    private final Map<Integer, GameSpeciesDTO> species;

    public GameDiaryExcelView(final Locale locale,
                              final EnumLocaliser localiser,
                              final Map<Integer, GameSpeciesDTO> species,
                              final List<HarvestDTO> harvests,
                              final List<ObservationDTO> observations) {
        this.locale = locale;
        this.localiser = localiser;

        this.species = species;
        this.harvests = harvests;
        this.observations = observations;
    }

    private String createFilename() {
        return String.format("%s-%s.xls", localiser.getTranslation("gameDiary"), DATETIME_PATTERN.print(DateUtil.now()));
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

        createHarvestsSheet(workbook);
        createObservationsSheet(workbook);
    }

    private void createHarvestsSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("harvests"));
        helper.appendHeaderRow(concatAndTranslate(getCommonHeaders(), "amount", "description", "gender", "age", "weight", "notEdible",
                "weightEstimated", "weightMeasured", "fitnessClass", "antlersType", "antlersWidth",
                "antlerPointsLeft", "antlerPointsRight", "additionalInfo"));

        for (HarvestDTO harvest : harvests) {
            final List<HarvestSpecimenDTO> specimens = harvest.getSpecimens();
            final int specimensSize = ofNullable(specimens).map(List::size).orElse(0);
            final int amount = harvest.getAmount();

            if (specimensSize != amount && amount != 1 || specimensSize == 0) {
                helper.appendRow();
                addCommonColumns(helper, harvest, harvest.getAuthorInfo(), harvest.getActorInfo());
                helper.appendNumberCell(amount - specimensSize)
                        .appendTextCell(harvest.getDescription());
            }
            for (int i = 0; i < specimensSize; i++) {
                final HarvestSpecimenDTO specimen = specimens.get(i);
                helper.appendRow();
                addCommonColumns(helper, harvest, harvest.getAuthorInfo(), harvest.getActorInfo());
                helper.appendNumberCell(1)
                        .appendTextCell(harvest.getDescription())
                        .appendTextCell(localise(specimen.getGender()))
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

    private void createObservationsSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("observations"));
        helper.appendHeaderRow(concatAndTranslate(getCommonHeaders(), "observationType", "amount", "description",
                "mooselikeMaleAmount", "mooselikeFemaleAmount", "mooselikeFemale1CalfAmount",
                "mooselikeFemale2CalfsAmount", "mooselikeFemale3CalfsAmount", "mooselikeFemale4CalfsAmount",
                "mooselikeUnknownSpecimenAmount", "gender", "age", "gameMarking", "observedGameState"));

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
                .appendNumberCell(observation.getMooselikeUnknownSpecimenAmount());
    }

    private void addCommonColumns(final ExcelHelper helper,
                                  final HuntingDiaryEntryDTO entry,
                                  final PersonWithHunterNumberDTO author,
                                  final PersonWithHunterNumberDTO actor) {
        helper.appendNumberCell(entry.getId())
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
                .appendTextCell(species.get(entry.getGameSpeciesCode()).getName().get(locale.getLanguage()));
    }

    private String localise(final Enum<?> e) {
        return localiser.getTranslation(e);
    }

    private static String[] getCommonHeaders() {
        return new String[]{"commonId", "date", "clockTime", "geolocationSource", "geolocationAccuracy", "latitude", "longitude",
                "lastNameOfAuthor", "firstNameOfAuthor", "lastNameOfHunter", "firstNameOfHunter", "species"};
    }

    private String[] concatAndTranslate(final String[] arr1, final String... arr2) {
        return Stream.concat(Arrays.stream(arr1), Arrays.stream(arr2))
                .map(localiser::getTranslation)
                .toArray(String[]::new);
    }
}
