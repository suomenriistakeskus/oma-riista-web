package fi.riista.feature.huntingclub.statistics.luke;

import com.google.common.collect.ImmutableMap;
import fi.riista.util.F;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LukeReportParams {
    public enum Presentation {
        FIGURE("figure", ".png", MediaType.IMAGE_PNG, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"),
        MAP("map", ".png", MediaType.IMAGE_PNG,
                "h1", "h2", "h3", "h4", "h5",
                "s1", "s2", "s3", "s4",
                "r1", "r2", "r3", "r4", "r5", "r6"),
        TABLE_FULL("table", ".html", MediaType.TEXT_HTML, "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"),
        TABLE_COMPARISON("table", ".html", MediaType.TEXT_HTML, "1", "2", "3", "4", "5", "6", "7", "8"),
        FORECAST("figure", ".png", MediaType.IMAGE_PNG, "m1");

        private final String lukeValue;
        private final String fileExtension;
        private final MediaType contentType;
        private final List<String> fileNames;

        Presentation(final String lukeValue,
                     final String fileExtension,
                     final MediaType contentType,
                     final String... fileNames) {
            this.lukeValue = lukeValue;
            this.contentType = contentType;
            this.fileExtension = fileExtension;
            this.fileNames = Arrays.asList(fileNames);
        }

        public MediaType getContentType() {
            return contentType;
        }

        public String getLukeValue() {
            return lukeValue;
        }

        public String composeFileName(final String file) {
            return file + fileExtension;
        }

        public Map<String, Object> toMapValue(final List<String> extraFileNames) {
            return ImmutableMap.of("name", name(), "files", F.concat(fileNames, extraFileNames));
        }

        public List<String> getFileNames() {
            return fileNames;
        }
    }

    public enum Organisation {
        CLUB("rse", ImmutableMap.of(),
                Presentation.FIGURE, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON),
        RHY("rhy", ImmutableMap.of(Presentation.TABLE_FULL, Arrays.asList("s1", "s2")),
                Presentation.FIGURE, Presentation.MAP, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON),
        HTA("uta", ImmutableMap.of(Presentation.TABLE_FULL, Arrays.asList("s1", "s3")),
                Presentation.FIGURE, Presentation.MAP, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON, Presentation.FORECAST),
        AREA("rhp", ImmutableMap.of(Presentation.TABLE_FULL, Arrays.asList("s1"), Presentation.MAP, Arrays.asList("u1", "u2", "u3", "u4", "u5")),
                Presentation.FIGURE, Presentation.MAP, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON),
        COUNTRY("maa", ImmutableMap.of(Presentation.TABLE_FULL, Arrays.asList("s1"), Presentation.MAP, Arrays.asList("u1", "u2", "u3", "u4", "u5")),
                Presentation.FIGURE, Presentation.MAP, Presentation.TABLE_FULL, Presentation.TABLE_COMPARISON);

        private final String lukeValue;
        private final Map<Presentation, List<String>> extraFileNames;
        private final List<Presentation> presentations;

        Organisation(final String lukeValue,
                     final Map<Presentation, List<String>> extraFileNames,
                     final Presentation... presentations) {
            this.lukeValue = lukeValue;
            this.extraFileNames = extraFileNames;
            this.presentations = Arrays.asList(presentations);
        }

        public Map<String, Object> toMapValue() {
            return ImmutableMap.of("name", name(),
                    "presentations", presentations.stream()
                            .map(p -> p.toMapValue(extraFileNames.getOrDefault(p, Collections.emptyList())))
                            .collect(toList()));
        }

        public static List<Map<String, Object>> allValues() {
            return Stream.of(CLUB, RHY, HTA, AREA, COUNTRY)
                    .map(Organisation::toMapValue)
                    .collect(toList());
        }

        public static List<Map<String, Object>> valuesWithoutClub() {
            return Stream.of(RHY, HTA, AREA, COUNTRY)
                    .map(Organisation::toMapValue)
                    .collect(toList());
        }

        public String getLukeValue() {
            return this.lukeValue;
        }
    }

    private LukeReportParams() {
        throw new AssertionError();
    }
}
