package fi.riista.feature.huntingclub.statistics.luke;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.F;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LukeReportParams {

    /**
     * Mapping values that are used in Luke API
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

        MOOSE_FIGURE(LukeType.NOT_DEFINED, LukeReportType.FIGURE, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"),
        MOOSE_MAP(LukeType.NOT_DEFINED, LukeReportType.MAP,
                  "h1", "h2", "h3", "h4", "h5",
                  "s1", "s2", "s3", "s4",
                  "r1", "r2", "r3", "r4", "r5", "r6"),
        MOOSE_TABLE_FULL(LukeType.NOT_DEFINED, LukeReportType.TABLE, "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"),
        MOOSE_TABLE_COMPARISON(LukeType.NOT_DEFINED, LukeReportType.TABLE, "1", "2", "3", "4", "5", "6", "7", "8"),
        MOOSE_FORECAST(LukeType.NOT_DEFINED, LukeReportType.FIGURE, "m1"),

        /**
         * For white tailed deer before hunting year 2020
         */

        WTD_PRE2020_FIGURE(LukeType.NOT_DEFINED, LukeReportType.FIGURE, "wtd_S1", "wtd_S2", "wtd_S3", "wtd_S4", "wtd_S5", "wtd_S6"),
        WTD_PRE2020_TABLE_FULL(LukeType.NOT_DEFINED, LukeReportType.TABLE, "wtd_S1"),

        /**
         * For white tailed deer hunting year 2020 ->
         */

        WTD_HARVEST_FIGURE(LukeType.HARVEST, LukeReportType.FIGURE, "1", "2", "3", "4", "5", "6", "7"),
        WTD_HARVEST_MAP(LukeType.HARVEST, LukeReportType.MAP, "s1", "s2", "s3", "s4", "s5", "r1", "r2"),
        WTD_HARVEST_TABLE_FULL(LukeType.HARVEST, LukeReportType.TABLE, "1", "2", "3", "4", "5"),

        WTD_OBSERVATION_FIGURE(LukeType.OBSERVATION, LukeReportType.FIGURE, "1", "2", "3", "4", "5", "6", "7"),
        WTD_OBSERVATION_MAP(LukeType.OBSERVATION, LukeReportType.MAP, "h1", "h2", "h3", "h4", "h5", "r1", "r2"),
        WTD_OBSERVATION_TABLE_FULL(LukeType.OBSERVATION, LukeReportType.TABLE, "1", "2", "3", "4", "5");

        private final LukeType lukeType;
        private final LukeReportType reportType;
        private final List<String> fileNames;

        Presentation(final LukeType lukeType,
                     final LukeReportType reportType,
                     final String... fileNames) {
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
    }

    public enum PresentationGroup {

        MOOSE_FIGURE(LukeReportGroup.FIGURE, Presentation.MOOSE_FIGURE),
        MOOSE_MAP(LukeReportGroup.MAP, Presentation.MOOSE_MAP),
        MOOSE_TABLE_FULL(LukeReportGroup.TABLE_FULL, Presentation.MOOSE_TABLE_FULL),
        MOOSE_TABLE_COMPARISON(LukeReportGroup.TABLE_COMPARISON, Presentation.MOOSE_TABLE_COMPARISON),
        MOOSE_TABLE_FORECAST(LukeReportGroup.FORECAST, Presentation.MOOSE_FORECAST),

        WTD_PRE2020_FIGURE(LukeReportGroup.FIGURE, Presentation.WTD_PRE2020_FIGURE),
        WTD_PRE2020_TABLE_FULL(LukeReportGroup.TABLE_FULL, Presentation.WTD_PRE2020_TABLE_FULL),

        WTD_FIGURE(LukeReportGroup.FIGURE, Presentation.WTD_HARVEST_FIGURE),
        WTD_MAP(LukeReportGroup.MAP, Presentation.WTD_HARVEST_MAP),
        WTD_TABLE_FULL(LukeReportGroup.TABLE_FULL, Presentation.WTD_HARVEST_TABLE_FULL),

        /**
         * Show deer observation presentations only for pilot groups. Cleanup these after pilot.
         */

        WTD_PILOT_FIGURE(LukeReportGroup.FIGURE, ImmutableList.of(Presentation.WTD_PRE2020_FIGURE, Presentation.WTD_HARVEST_FIGURE, Presentation.WTD_OBSERVATION_FIGURE)),
        WTD_PILOT_MAP(LukeReportGroup.MAP, ImmutableList.of(Presentation.WTD_HARVEST_MAP, Presentation.WTD_OBSERVATION_MAP)),
        WTD_PILOT_TABLE_FULL(LukeReportGroup.TABLE_FULL, ImmutableList.of(Presentation.WTD_PRE2020_TABLE_FULL, Presentation.WTD_HARVEST_TABLE_FULL, Presentation.WTD_OBSERVATION_TABLE_FULL)),

        // For pilot clubs there are club level white tailed deer reports
        WTD_PILOT_CLUB_FIGURE(LukeReportGroup.FIGURE, ImmutableList.of(Presentation.WTD_HARVEST_FIGURE, Presentation.WTD_OBSERVATION_FIGURE)),
        WTD_PILOT_CLUB_TABLE_FULL(LukeReportGroup.TABLE_FULL, ImmutableList.of(Presentation.WTD_HARVEST_TABLE_FULL, Presentation.WTD_OBSERVATION_TABLE_FULL));

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

        public Map<String, Object> toMapValue(final List<String> extraFilenames) {
            return ImmutableMap.of("name", reportType.name(),
                                   "presentations", presentations.stream().map(p -> p.toMapValue(extraFilenames)).collect(toList()));
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
         * White tailed deer before hunting year 2020
         */

        WTD_PRE2020_RHY(LukeArea.RHY, ImmutableMap.of(),
                PresentationGroup.WTD_PRE2020_FIGURE,
                PresentationGroup.WTD_PRE2020_TABLE_FULL),

        WTD_PRE2020_HTA(LukeArea.HTA, ImmutableMap.of(),
                PresentationGroup.WTD_PRE2020_FIGURE,
                PresentationGroup.WTD_PRE2020_TABLE_FULL),

        WTD_PRE2020_AREA(LukeArea.AREA, ImmutableMap.of(),
                PresentationGroup.WTD_PRE2020_FIGURE,
                PresentationGroup.WTD_PRE2020_TABLE_FULL),

        WTD_PRE2020_COUNTRY(LukeArea.COUNTRY, ImmutableMap.of(),
                PresentationGroup.WTD_PRE2020_FIGURE,
                PresentationGroup.WTD_PRE2020_TABLE_FULL),

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

        /**
         * White tailed deer for pilot RHY's. Cleanup after deer pilot.
         */

        WTD_PILOT_CLUB(LukeArea.CLUB, ImmutableMap.of(),
                 PresentationGroup.WTD_PILOT_CLUB_FIGURE,
                 PresentationGroup.WTD_PILOT_CLUB_TABLE_FULL),

        WTD_PILOT_RHY(LukeArea.RHY, ImmutableMap.of(),
                PresentationGroup.WTD_PILOT_FIGURE,
                PresentationGroup.WTD_PILOT_MAP,
                PresentationGroup.WTD_PILOT_TABLE_FULL),

        WTD_PILOT_HTA(LukeArea.HTA, ImmutableMap.of(),
                PresentationGroup.WTD_PILOT_FIGURE,
                PresentationGroup.WTD_PILOT_MAP,
                PresentationGroup.WTD_PILOT_TABLE_FULL),

        WTD_PILOT_AREA(LukeArea.AREA, ImmutableMap.of(),
                PresentationGroup.WTD_PILOT_FIGURE,
                PresentationGroup.WTD_PILOT_MAP,
                PresentationGroup.WTD_PILOT_TABLE_FULL),

        WTD_PILOT_COUNTRY(LukeArea.COUNTRY, ImmutableMap.of(),
                PresentationGroup.WTD_PILOT_FIGURE,
                PresentationGroup.WTD_PILOT_MAP,
                PresentationGroup.WTD_PILOT_TABLE_FULL);

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
            return ImmutableMap.of(
                    "name", area.name(),
                    "reportTypes", presentationGroups.stream()
                            .map(p -> p.toMapValue(extraFileNames.getOrDefault(p, Collections.emptyList())))
                            .collect(toList()));
        }

        public static List<Map<String, Object>> allValues(final int species, final int huntingYear, final boolean isInPilot) {
            if (species == GameSpecies.OFFICIAL_CODE_MOOSE) {
                return Stream.of(MOOSE_CLUB, MOOSE_RHY, MOOSE_HTA, MOOSE_AREA, MOOSE_COUNTRY)
                        .map(Organisation::toMapValue)
                        .collect(toList());
            } else {
                return valuesForWhiteTailedDeer(huntingYear, isInPilot);
            }
        }

        public static List<Map<String, Object>> valuesWithoutClub(final int species, final int huntingYear, final boolean isInPilot) {
            if (species == GameSpecies.OFFICIAL_CODE_MOOSE) {
                return Stream.of(MOOSE_RHY, MOOSE_HTA, MOOSE_AREA, MOOSE_COUNTRY)
                        .map(Organisation::toMapValue)
                        .collect(toList());
            } else if (huntingYear >= 2020 && isInPilot) {
                return Stream.of(WTD_PILOT_RHY, WTD_PILOT_HTA, WTD_PILOT_AREA, WTD_PILOT_COUNTRY)
                        .map(Organisation::toMapValue)
                        .collect(toList());
            }
            return valuesForWhiteTailedDeerPre2020();
        }

        private static List<Map<String, Object>> valuesForWhiteTailedDeer(final int huntingYear, final boolean isInPilot) {
            if (huntingYear >= 2020 && isInPilot) {
                return Stream.of(WTD_PILOT_CLUB, WTD_PILOT_RHY, WTD_PILOT_HTA, WTD_PILOT_AREA, WTD_PILOT_COUNTRY)
                        .map(Organisation::toMapValue)
                        .collect(toList());
            } else {
                return valuesForWhiteTailedDeerPre2020();
            }
        }

        private static List<Map<String, Object>> valuesForWhiteTailedDeerPre2020() {
            return Stream.of(WTD_PRE2020_RHY, WTD_PRE2020_HTA, WTD_PRE2020_AREA, WTD_PRE2020_COUNTRY)
                    .map(Organisation::toMapValue)
                    .collect(toList());
        }
    }

    private LukeReportParams() {
        throw new AssertionError();
    }
}
