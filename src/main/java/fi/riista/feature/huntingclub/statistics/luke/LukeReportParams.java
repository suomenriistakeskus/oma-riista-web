package fi.riista.feature.huntingclub.statistics.luke;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import fi.riista.util.F;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static java.util.stream.Collectors.toList;

public class LukeReportParams {

    /**
     * Mapping values that are used in Luke API
     * 
     * LukeReportType:      figure/map/table
     * LukeReportGroup:     figure/map/tableFull/tableComparison/forecast
     * LukeType:            observation(new)/harvest(new)/null(old)
     * LukeArea:            club/rhy/hta/area/country
     *
     * Presentation:        LukeType + LukeReportType + files
     * PresentationGroup:   LukeReportGroup + Presentation(s)
     *
     * Organisation:        LukeArea + PresentationGroup with extra files + PresentationGroup(s)
     */

    public enum LukeReportType {
        FIGURE("figure", ".png", MediaType.IMAGE_PNG),
        MAP("map", ".png", MediaType.IMAGE_PNG),
        TABLE("table", ".html", MediaType.TEXT_HTML);

        private final String lukeValue;
        private final String fileExtension;
        private final MediaType contentType;

        LukeReportType(final String lukeValue, final String fileExtension, final MediaType contentType) {
            this.lukeValue = lukeValue;
            this.fileExtension = fileExtension;
            this.contentType = contentType;
        }

        public String getLukeValue() {
            return lukeValue;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public MediaType getContentType() {
            return contentType;
        }

    }

    public enum LukeReportGroup {
        FIGURE,
        MAP,
        TABLE_FULL,
        TABLE_COMPARISON,
        FORECAST
    }

    public enum LukeType {
        OBSERVATION("observations"),
        HARVEST("harvest"),
        NOT_DEFINED(null);

        private final String lukeValue;

        LukeType(final String lukeValue) {
            this.lukeValue = lukeValue;
        }

        public String getLukeValue() {
            return lukeValue;
        }
    }

    public enum LukeArea {
        CLUB("rse"),
        RHY("rhy"),
        HTA("uta"),
        AREA("rhp"),
        COUNTRY("maa");

        private final String lukeValue;

        LukeArea(final String lukeValue) {
            this.lukeValue = lukeValue;
        }

        public String getLukeValue() {
            return lukeValue;
        }
    }

    public enum Presentation {

        /**
         * For moose
         */

        MOOSE_FIGURE(Range.all(), LukeType.NOT_DEFINED, LukeReportType.FIGURE,
                     "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"),
        MOOSE_MAP(Range.all(), LukeType.NOT_DEFINED, LukeReportType.MAP,
                  "h1", "h2", "h3", "h4", "h5",
                  "s1", "s2", "s3", "s4",
                  "r1", "r2", "r3", "r4", "r5", "r6"),
        MOOSE_TABLE_FULL(Range.all(), LukeType.NOT_DEFINED, LukeReportType.TABLE, "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"),
        MOOSE_TABLE_COMPARISON(Range.all(), LukeType.NOT_DEFINED, LukeReportType.TABLE, "1", "2", "3", "4", "5", "6", "7", "8"),
        MOOSE_FORECAST(Range.all(), LukeType.NOT_DEFINED, LukeReportType.FIGURE, "m1"),

        /**
         * For white tailed deer
         */

        WTD_PRE2020_FIGURE(Range.atMost(2019), LukeType.NOT_DEFINED, LukeReportType.FIGURE, "wtd_S1", "wtd_S2", "wtd_S3", "wtd_S4", "wtd_S5", "wtd_S6"),
        WTD_PRE2020_TABLE_FULL(Range.atMost(2019), LukeType.NOT_DEFINED, LukeReportType.TABLE, "wtd_S1"),

        WTD_HARVEST_FIGURE(Range.atLeast(2020), LukeType.HARVEST, LukeReportType.FIGURE, "1", "2", "3", "4", "5", "6", "7"),
        WTD_HARVEST_ANTLER_FIGURE(Range.atLeast(2021), LukeType.HARVEST, LukeReportType.FIGURE, "hist_1", "hist_3", "hist_4", "hist_5"),
        WTD_HARVEST_WEIGHT_FIGURE(Range.atLeast(2021), LukeType.HARVEST, LukeReportType.FIGURE,  "hist_6", "hist_7", "hist_8"),
        WTD_HARVEST_MAP(Range.atLeast(2020), LukeType.HARVEST, LukeReportType.MAP,"s1", "s2", "s3", "s4", "s5", "r1", "r2"),
        WTD_HARVEST_TABLE_FULL(Range.atLeast(2020), LukeType.HARVEST, LukeReportType.TABLE, "1", "2", "3", "4", "5"),

        WTD_OBSERVATION_FIGURE(Range.atLeast(2020), LukeType.OBSERVATION, LukeReportType.FIGURE, "1", "2", "3", "4", "5", "6", "7"),
        WTD_OBSERVATION_MAP(Range.atLeast(2020), LukeType.OBSERVATION, LukeReportType.MAP, "h1", "h2", "h3", "h4", "h5", "r1", "r2"),

        WTD_OBSERVATION_TABLE_FULL_2020(Range.singleton(2020), LukeType.OBSERVATION, LukeReportType.TABLE, "1", "2", "3", "4", "5"),
        WTD_OBSERVATION_TABLE_FULL(Range.atLeast(2021), LukeType.OBSERVATION, LukeReportType.TABLE, "1", "2", "4", "5", "3");

        private final Range<Integer> validHuntingYears;
        private final LukeType lukeType;
        private final LukeReportType reportType;
        private final List<String> fileNames;

        Presentation(final Range<Integer> validHuntingYears,
                     final LukeType lukeType,
                     final LukeReportType reportType,
                     final String... fileNames) {
            this.validHuntingYears = validHuntingYears;
            this.lukeType = lukeType;
            this.reportType = reportType;
            this.fileNames = Arrays.asList(fileNames);
        }

        public MediaType getContentType() {
            return reportType.getContentType();
        }

        public String getLukeType() {
            return lukeType.getLukeValue();
        }

        public String getLukeValue() {
            return reportType.getLukeValue();
        }

        public String composeFileName(final String file) {
            return file + reportType.getFileExtension();
        }

        public Map<String, Object> toMapValue(final List<String> extraFileNames) {
            return ImmutableMap.of("name", name(), "files", F.concat(fileNames, extraFileNames));
        }

        public List<String> getFileNames() {
            return fileNames;
        }

        public boolean forHuntingYear(final Integer huntingYear) {
            return huntingYear == null || validHuntingYears.contains(huntingYear);
        }
    }

    public enum PresentationGroup {

        MOOSE_FIGURE(LukeReportGroup.FIGURE, Presentation.MOOSE_FIGURE),
        MOOSE_MAP(LukeReportGroup.MAP, Presentation.MOOSE_MAP),
        MOOSE_TABLE_FULL(LukeReportGroup.TABLE_FULL, Presentation.MOOSE_TABLE_FULL),
        MOOSE_TABLE_COMPARISON(LukeReportGroup.TABLE_COMPARISON, Presentation.MOOSE_TABLE_COMPARISON),
        MOOSE_TABLE_FORECAST(LukeReportGroup.FORECAST, Presentation.MOOSE_FORECAST),

        WTD_FIGURE(LukeReportGroup.FIGURE, ImmutableList.of(Presentation.WTD_PRE2020_FIGURE, Presentation.WTD_HARVEST_FIGURE, Presentation.WTD_HARVEST_ANTLER_FIGURE, Presentation.WTD_HARVEST_WEIGHT_FIGURE, Presentation.WTD_OBSERVATION_FIGURE)),
        WTD_MAP(LukeReportGroup.MAP, ImmutableList.of(Presentation.WTD_HARVEST_MAP, Presentation.WTD_OBSERVATION_MAP)),
        WTD_TABLE_FULL(LukeReportGroup.TABLE_COMPARISON, ImmutableList.of(Presentation.WTD_PRE2020_TABLE_FULL, Presentation.WTD_HARVEST_TABLE_FULL, Presentation.WTD_OBSERVATION_TABLE_FULL_2020, Presentation.WTD_OBSERVATION_TABLE_FULL)),

        WTD_CLUB_FIGURE(LukeReportGroup.FIGURE, ImmutableList.of(Presentation.WTD_HARVEST_FIGURE, Presentation.WTD_HARVEST_ANTLER_FIGURE, Presentation.WTD_HARVEST_WEIGHT_FIGURE, Presentation.WTD_OBSERVATION_FIGURE)),
        WTD_CLUB_TABLE_FULL(LukeReportGroup.TABLE_COMPARISON, ImmutableList.of(Presentation.WTD_HARVEST_TABLE_FULL, Presentation.WTD_OBSERVATION_TABLE_FULL_2020, Presentation.WTD_OBSERVATION_TABLE_FULL));

        private final LukeReportGroup reportType;
        private final List<Presentation> presentations;

        PresentationGroup(final LukeReportGroup reportType,
                          final Presentation presentations) {
            this.reportType = reportType;
            this.presentations = ImmutableList.of(presentations);
        }

        PresentationGroup(final LukeReportGroup reportType,
                          final List<Presentation> presentations) {
            this.reportType = reportType;
            this.presentations = presentations;
        }

        public Map<String, Object> toMapValue(final Integer huntingYear, final List<String> extraFilenames) {
            final List<Object> resultList = presentations.stream()
                    .filter(p -> p.forHuntingYear(huntingYear))
                    .map(p -> p.toMapValue(extraFilenames))
                    .collect(toList());

            return resultList.isEmpty() ? null : ImmutableMap.of("name", reportType.name(), "presentations", resultList);
        }
    }

    public enum Organisation {
        MOOSE_CLUB(LukeArea.CLUB, ImmutableMap.of(),
                PresentationGroup.MOOSE_FIGURE,
                PresentationGroup.MOOSE_TABLE_FULL,
                PresentationGroup.MOOSE_TABLE_COMPARISON),

        MOOSE_RHY(LukeArea.RHY, ImmutableMap.of(PresentationGroup.MOOSE_TABLE_FULL, Arrays.asList("s1", "s2")),
                PresentationGroup.MOOSE_FIGURE,
                PresentationGroup.MOOSE_MAP,
                PresentationGroup.MOOSE_TABLE_FULL,
                PresentationGroup.MOOSE_TABLE_COMPARISON),

        MOOSE_RHY_FOR_GROUP_OCCUPATION(LukeArea.RHY, ImmutableMap.of(PresentationGroup.MOOSE_TABLE_FULL, Arrays.asList("s1")),
                PresentationGroup.MOOSE_FIGURE,
                PresentationGroup.MOOSE_MAP,
                PresentationGroup.MOOSE_TABLE_FULL,
                PresentationGroup.MOOSE_TABLE_COMPARISON),

        MOOSE_HTA(LukeArea.HTA, ImmutableMap.of(PresentationGroup.MOOSE_TABLE_FULL, Arrays.asList("s1", "s3")),
                PresentationGroup.MOOSE_FIGURE,
                PresentationGroup.MOOSE_MAP,
                PresentationGroup.MOOSE_TABLE_FULL,
                PresentationGroup.MOOSE_TABLE_COMPARISON,
                PresentationGroup.MOOSE_TABLE_FORECAST),

        MOOSE_AREA(LukeArea.AREA, ImmutableMap.of(PresentationGroup.MOOSE_TABLE_FULL, Arrays.asList("s1"),
                                                  PresentationGroup.MOOSE_MAP, Arrays.asList("u1", "u2", "u3", "u4", "u5")),
                PresentationGroup.MOOSE_FIGURE,
                PresentationGroup.MOOSE_MAP,
                PresentationGroup.MOOSE_TABLE_FULL,
                PresentationGroup.MOOSE_TABLE_COMPARISON),

        MOOSE_COUNTRY(LukeArea.COUNTRY, ImmutableMap.of(PresentationGroup.MOOSE_TABLE_FULL, Arrays.asList("s1"),
                                                        PresentationGroup.MOOSE_MAP, Arrays.asList("u1", "u2", "u3", "u4", "u5")),
                PresentationGroup.MOOSE_FIGURE,
                PresentationGroup.MOOSE_MAP,
                PresentationGroup.MOOSE_TABLE_FULL,
                PresentationGroup.MOOSE_TABLE_COMPARISON),

        /**
         * White tailed deer hunting year 2020 ->
         */

        WTD_RHY(LukeArea.RHY, ImmutableMap.of(),
                PresentationGroup.WTD_FIGURE,
                PresentationGroup.WTD_MAP,
                PresentationGroup.WTD_TABLE_FULL),

        WTD_HTA(LukeArea.HTA, ImmutableMap.of(),
                PresentationGroup.WTD_FIGURE,
                PresentationGroup.WTD_MAP,
                PresentationGroup.WTD_TABLE_FULL),

        WTD_AREA(LukeArea.AREA, ImmutableMap.of(),
                PresentationGroup.WTD_FIGURE,
                PresentationGroup.WTD_MAP,
                PresentationGroup.WTD_TABLE_FULL),

        WTD_COUNTRY(LukeArea.COUNTRY, ImmutableMap.of(),
                PresentationGroup.WTD_FIGURE,
                PresentationGroup.WTD_MAP,
                PresentationGroup.WTD_TABLE_FULL),

        WTD_CLUB(LukeArea.CLUB, ImmutableMap.of(),
                 PresentationGroup.WTD_CLUB_FIGURE,
                 PresentationGroup.WTD_CLUB_TABLE_FULL);

        private final LukeArea area;
        private final Map<PresentationGroup, List<String>> extraFileNames;
        private final List<PresentationGroup> presentationGroups;

        Organisation(final LukeArea area,
                     final Map<PresentationGroup, List<String>> extraFileNames,
                     final PresentationGroup... presentationGroups) {
            this.area = area;
            this.extraFileNames = extraFileNames;
            this.presentationGroups = Arrays.asList(presentationGroups);
        }

        public Map<String, Object> toMapValue() {
            return toMapValue(null);
        }

        public Map<String, Object> toMapValue(final Integer huntingYear) {
            final List<Object> result = presentationGroups.stream()
                    .map(p -> p.toMapValue(huntingYear, extraFileNames.getOrDefault(p, Collections.emptyList())))
                    .filter(Objects::nonNull)
                    .collect(toList());

            return result.isEmpty() ? null : ImmutableMap.of( "name", area.name(), "reportTypes", result);
        }

        public static List<Map<String, Object>> allValues(final int species,
                                                          final int huntingYear,
                                                          final boolean isModeratorOrCoordinator) {
            if (species == OFFICIAL_CODE_MOOSE) {
                final Stream<LukeReportParams.Organisation> orgStream = isModeratorOrCoordinator
                        ? Stream.of(MOOSE_CLUB, MOOSE_RHY, MOOSE_HTA, MOOSE_AREA, MOOSE_COUNTRY)
                        : Stream.of(MOOSE_CLUB, MOOSE_RHY_FOR_GROUP_OCCUPATION, MOOSE_HTA, MOOSE_AREA, MOOSE_COUNTRY);
                return orgStream
                        .map(Organisation::toMapValue)
                        .collect(toList());
            } else {
                return valuesForWhiteTailedDeer(huntingYear);
            }
        }

        public static List<Map<String, Object>> valuesWithoutClub(final int species,
                                                                  final int huntingYear,
                                                                  final boolean isModeratorOrCoordinator) {
            if (species == OFFICIAL_CODE_MOOSE) {
                final Stream<LukeReportParams.Organisation> orgStream = isModeratorOrCoordinator
                        ? Stream.of(MOOSE_RHY, MOOSE_HTA, MOOSE_AREA, MOOSE_COUNTRY)
                        : Stream.of(MOOSE_RHY_FOR_GROUP_OCCUPATION, MOOSE_HTA, MOOSE_AREA, MOOSE_COUNTRY);
                return orgStream
                        .map(Organisation::toMapValue)
                        .collect(toList());
            } else  {
                return Stream.of(WTD_RHY, WTD_HTA, WTD_AREA, WTD_COUNTRY)
                        .map(o -> o.toMapValue(huntingYear))
                        .filter(Objects::nonNull)
                        .collect(toList());
            }
        }

        private static List<Map<String, Object>> valuesForWhiteTailedDeer(final int huntingYear) {
                return Stream.of(WTD_CLUB, WTD_RHY, WTD_HTA, WTD_AREA, WTD_COUNTRY)
                        .map(o -> o.toMapValue(huntingYear))
                        .filter(Objects::nonNull)
                        .collect(toList());
        }
    }

    private LukeReportParams() {
        throw new AssertionError();
    }
}
