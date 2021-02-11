package fi.riista.feature.permit.decision;

import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DecisionDocumentTransformerUtils {

    public static Function<String, String> createMarkdownTransformer() {
        final List<Extension> extensions = Collections.singletonList(TablesExtension.create());

        final Parser parser = Parser.builder()
                .extensions(extensions)
                .build();

        final HtmlRenderer renderer = HtmlRenderer.builder()
                .softBreak("<br />\n")
                .extensions(extensions)
                .build();

        return text -> StringUtils.hasText(text) ? renderer.render(parser.parse(text)) : null;
    }

    public static String transformLineFeeds(final String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        return "<p>" +
                text.replaceAll("\n", "<br />\n") +
                "</p>\n";
    }

    private DecisionDocumentTransformerUtils() {
        throw new AssertionError();
    }
}
