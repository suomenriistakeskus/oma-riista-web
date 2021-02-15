package fi.riista.feature.permit.decision.document;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class PermitDecisionTextUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    public static String escape(final String string) {
        return StringUtils.replace(string, "-", "\\-");
    }

    public static void joinNotBlankBy(final StringBuilder sb, final String divider, final String first, final String... theRest) {
        sb.append(first);
        for (String s : theRest) {
            if (isNotBlank(s)) {
                sb.append(divider).append(s);
            }
        }
    }

    public static StringBuilder joinNotBlankBy(final String divider, final String first, final String... theRest) {
        final StringBuilder sb = new StringBuilder();
        joinNotBlankBy(sb, divider, first, theRest);
        return sb;
    }

    public static StringBuilder join(final Object... objects) {
        return Stream.of(objects).reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append);
    }

    public static StringBuilder beginAndEndDate(final LocalDate beginDate, final LocalDate endDate) {
        final StringBuilder sb = new StringBuilder();
        sb.append(DATE_FORMAT.print(beginDate));
        if (!(endDate == null || Objects.equals(beginDate, endDate))) {
            sb.append(" - ").append(DATE_FORMAT.print(endDate));
        }
        return sb;
    }

    public static void optionalContent(final StringBuilder sb,
                                       final Object prefix,
                                       final String content,
                                       final Object... postfix) {

        if (isNotBlank(content)) {
            sb.append(prefix).append(content).append(join(postfix));
        }
    }

    /**
     *  Markdown helpers
     */

    public static void titleLine(final StringBuilder sb, final Object title) {
        sb.append("**").append(title).append("**\n\n");
    }

    public static StringBuilder minorTitle(final Object title) {
        return new StringBuilder("*").append(title).append("*:");
    }

    public static void minorTitleLine(final StringBuilder sb, final Object title) {
        sb.append(minorTitle(title)).append("\n");
    }

    public static void table2(final StringBuilder sb, final Object... objects) {
        if (objects.length % 2 != 0) {
            throw new IllegalArgumentException("Number of objects is not even");
        }

        sb.append("---|--:\n");

        for (int i = 0; i <= objects.length - 2; i += 2) {
            sb.append(objects[i]).append("|").append(objects[i+1]).append("\n");
        }
        sb.append("\n\n");
    }

    public static void unorderedList(final StringBuilder sb, final Object... objects) {
        for (Object o : objects) {
            sb.append("\\- ").append(o).append("\n");
        }
    }
}
