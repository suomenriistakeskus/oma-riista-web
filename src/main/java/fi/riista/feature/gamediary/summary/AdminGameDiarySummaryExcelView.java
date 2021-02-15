package fi.riista.feature.gamediary.summary;

import com.google.common.base.Stopwatch;
import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.F;
import fi.riista.util.LocalisedEnum;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.document.AbstractXlsxStreamingView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class AdminGameDiarySummaryExcelView extends AbstractXlsxStreamingView {

    private static final Logger LOG = LoggerFactory.getLogger(AdminGameDiarySummaryExcelView.class);

    private final EnumLocaliser localiser;

    private final List<HarvestDTO> harvests;
    private final List<ObservationDTO> observations;
    private final List<SrvaEventDTO> srvaEvents;
    private final Map<Integer, LocalisedString> species;
    private final Map<Long, RiistanhoitoyhdistysNameDTO> rhyNameMapping;

    public AdminGameDiarySummaryExcelView(final EnumLocaliser localiser,
                                          final Map<Integer, LocalisedString> species,
                                          final Map<Long, RiistanhoitoyhdistysNameDTO> rhyNameMapping,
                                          final List<HarvestDTO> harvests,
                                          final List<ObservationDTO> observations,
                                          final List<SrvaEventDTO> srvaEvents) {
        this.localiser = requireNonNull(localiser);
        this.species = requireNonNull(species);
        this.rhyNameMapping = requireNonNull(rhyNameMapping);
        this.harvests = requireNonNull(harvests);
        this.observations = requireNonNull(observations);
        this.srvaEvents = requireNonNull(srvaEvents);
    }

    private static String createFilename() {
        return String.format("%s-%s.xlsx", "poiminta", Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final Stopwatch sw = Stopwatch.createStarted();
        createHarvestsSheet(workbook);
        createObservationsSheet(workbook);
        createSrvaSheet(workbook);
        LOG.info("Excel generation took {}", sw);
    }

    private void createHarvestsSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("harvests"));
        helper.appendHeaderRow(concatAndTranslate(getCommonHeaders(), "amount", "solitaryCalf", "gender", "age",
                "weight", "notEdible", "weightEstimated", "weightMeasured", "fitnessClass", "antlersType",
                "antlersWidth", "antlerPointsLeft", "antlerPointsRight"));

        for (final HarvestDTO harvest : harvests) {
            final List<HarvestSpecimenDTO> specimens = harvest.getSpecimens();
            final int specimensSize = specimens.size();
            final int amount = harvest.getAmount();

            if (specimensSize != amount && amount != 1 || specimensSize == 0) {
                helper.appendRow();
                addCommonColumns(helper, harvest);
                helper.appendNumberCell(amount - specimensSize);
            }
            for (int i = 0; i < specimensSize; i++) {
                final HarvestSpecimenDTO specimen = specimens.get(i);
                helper.appendRow();
                addCommonColumns(helper, harvest);
                helper.appendNumberCell(1)
                        .appendTextCell(localiseBoolean(specimen.getAlone()))
                        .appendTextCell(localise(specimen.getGender()))
                        .appendTextCell(localise(specimen.getAge()))
                        .appendNumberCell(specimen.getWeight())
                        .appendTextCell(localiseBoolean(specimen.getNotEdible()))
                        .appendNumberCell(specimen.getWeightEstimated())
                        .appendNumberCell(specimen.getWeightMeasured())
                        .appendTextCell(localise(specimen.getFitnessClass()))
                        .appendTextCell(localise(specimen.getAntlersType()))
                        .appendNumberCell(specimen.getAntlersWidth())
                        .appendNumberCell(specimen.getAntlerPointsLeft())
                        .appendNumberCell(specimen.getAntlerPointsRight());
            }
        }
    }

    private void createObservationsSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("observations"));
        helper.appendHeaderRow(concatAndTranslate(getCommonHeaders(), "observationType", "amount",
                "mooselikeMaleAmount", "mooselikeFemaleAmount", "mooselikeFemale1CalfAmount",
                "mooselikeFemale2CalfsAmount", "mooselikeFemale3CalfsAmount", "mooselikeFemale4CalfsAmount",
                "mooselikeUnknownSpecimenAmount", "gender", "age", "gameMarking", "observedGameState"));

        for (ObservationDTO observation : observations) {
            final List<ObservationSpecimenDTO> specimens = observation.getSpecimens();
            final int specimensSize = ofNullable(specimens).map(List::size).orElse(0);
            final int amount = F.coalesceAsInt(observation.getAmount(), 0);

            if (specimensSize != amount && amount != 1 || specimensSize == 0) {
                helper.appendRow();
                addCommonColumns(helper, observation);

                helper.appendTextCell(localise(observation.getObservationType()))
                        .appendNumberCell(amount - specimensSize);
                addMooseLikeCells(helper, observation);
            }
            for (int i = 0; i < specimensSize; i++) {
                helper.appendRow();
                addCommonColumns(helper, observation);

                helper.appendTextCell(localise(observation.getObservationType()))
                        .appendNumberCell(1);
                addMooseLikeCells(helper, observation);

                final ObservationSpecimenDTO specimen = specimens.get(i);
                helper.appendTextCell(localise(specimen.getGender()))
                        .appendTextCell(localise(specimen.getAge()))
                        .appendTextCell(localise(specimen.getMarking()))
                        .appendTextCell(localise(specimen.getState()));
            }
        }
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

    private void addCommonColumns(final ExcelHelper helper, final HarvestDTO entry) {
        final RiistanhoitoyhdistysNameDTO rhyRka = rhyNameMapping.get(entry.getRhyId());

        helper.appendNumberCell(entry.getId())
                .appendTextCell(localiser.getTranslation(rhyRka.getRhyName()))
                .appendTextCell(localiser.getTranslation(rhyRka.getRkaName()))
                .appendDateCell(entry.getPointOfTime().toDate())
                .appendTimeCell(entry.getPointOfTime().toDate())
                .appendTextCell(localiser.getTranslation(entry.getGeoLocation().getSource()))
                .appendNumberCell(entry.getGeoLocation().getAccuracy())
                .appendNumberCell(entry.getGeoLocation().getLatitude())
                .appendNumberCell(entry.getGeoLocation().getLongitude())
                .appendTextCell(localiser.getTranslation(species.get(entry.getGameSpeciesCode())));
    }

    private void addCommonColumns(final ExcelHelper helper, final ObservationDTO entry) {
        final RiistanhoitoyhdistysNameDTO rhyRka = rhyNameMapping.get(entry.getRhyId());

        helper.appendNumberCell(entry.getId())
                .appendTextCell(localiser.getTranslation(rhyRka.getRhyName()))
                .appendTextCell(localiser.getTranslation(rhyRka.getRkaName()))
                .appendDateCell(entry.getPointOfTime().toDate())
                .appendTimeCell(entry.getPointOfTime().toDate())
                .appendTextCell(localiser.getTranslation(entry.getGeoLocation().getSource()))
                .appendNumberCell(entry.getGeoLocation().getAccuracy())
                .appendNumberCell(entry.getGeoLocation().getLatitude())
                .appendNumberCell(entry.getGeoLocation().getLongitude())
                .appendTextCell(localiser.getTranslation(species.get(entry.getGameSpeciesCode())));
    }

    private void createSrvaSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, "SRVA");

        helper.appendHeaderRow(concatAndTranslate(getCommonHeaders(),
                "state",
                "SrvaEventExportExcel.event",
                "SrvaEventExportExcel.type",
                "SrvaEventExportExcel.otherType",
                "SrvaEventExportExcel.otherSpecies",
                "SrvaEventExportExcel.result",
                "SrvaEventExportExcel.personCount",
                "SrvaEventExportExcel.timeSpent",
                "SrvaEventExportExcel.otherMethod",
                "SrvaEventExportExcel.numberOfAnimals",
                "SrvaEventExportExcel.methods",
                "gender",
                "age"
        ));

        for (final SrvaEventDTO entry : srvaEvents) {
            final RiistanhoitoyhdistysNameDTO rhyRka = rhyNameMapping.get(entry.getRhyId());

            helper
                    // Common fields
                    .appendRow()
                    .appendNumberCell(entry.getId())
                    .appendTextCell(localiser.getTranslation(rhyRka.getRhyName()))
                    .appendTextCell(localiser.getTranslation(rhyRka.getRkaName()))
                    .appendDateCell(entry.getPointOfTime().toDate())
                    .appendTimeCell(entry.getPointOfTime().toDate())
                    .appendTextCell(localiser.getTranslation(entry.getGeoLocation().getSource()))
                    .appendNumberCell(entry.getGeoLocation().getAccuracy())
                    .appendNumberCell(entry.getGeoLocation().getLatitude())
                    .appendNumberCell(entry.getGeoLocation().getLongitude())
                    .appendTextCell(localiser.getTranslation(species.get(entry.getGameSpeciesCode())))

                    // SRVA event fields
                    .appendTextCell(localiser.getTranslation(entry.getState()))
                    .appendTextCell(localiser.getTranslation(entry.getEventName()))
                    .appendTextCell(localiser.getTranslation(entry.getEventType()))
                    .appendTextCell(entry.getOtherTypeDescription())
                    .appendTextCell(entry.getOtherSpeciesDescription())
                    .appendTextCell(localiser.getTranslation(entry.getEventResult()))
                    .appendNumberCell(entry.getPersonCount())
                    .appendNumberCell(entry.getTimeSpent())
                    .appendTextCell(entry.getOtherMethodDescription())
                    .appendNumberCell(entry.getTotalSpecimenAmount())

                    // SRVA specimen fields
                    .appendTextCell(entry.getMethods().stream()
                            .filter(SrvaMethodDTO::isChecked)
                            .map(SrvaMethodDTO::getName)
                            .map(localiser::getTranslation)
                            .collect(joining(", ")))
                    .appendTextCell(entry.getSpecimens().stream()
                            .map(SrvaSpecimenDTO::getGender)
                            .map(localiser::getTranslation)
                            .collect(joining(", ")))
                    .appendTextCell(entry.getSpecimens().stream()
                            .map(SrvaSpecimenDTO::getAge)
                            .map(localiser::getTranslation)
                            .collect(joining(", ")));
        }
    }

    private <E extends Enum<E> & LocalisedEnum> String localise(final E e) {
        return localiser.getTranslation(e);
    }

    private <E extends Enum<E> & LocalisedEnum> String localiseBoolean(final Boolean value) {
        if (value == null) {
            return null;
        }

        return value
                ? localiser.getTranslation("Boolean.true")
                : localiser.getTranslation("Boolean.false");
    }

    private static String[] getCommonHeaders() {
        return new String[]{
                "commonId", "rhy", "rka", "date", "clockTime", "geolocationSource", "geolocationAccuracy",
                "latitude", "longitude", "species"
        };
    }

    private String[] concatAndTranslate(final String[] arr1, final String... arr2) {
        return localiser.translate(F.concat(arr1, arr2));
    }
}
