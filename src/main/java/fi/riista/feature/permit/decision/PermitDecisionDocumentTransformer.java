package fi.riista.feature.permit.decision;

import com.google.common.base.Functions;
import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PermitDecisionDocumentTransformer {
    public final static PermitDecisionDocumentTransformer SIMPLE = new PermitDecisionDocumentTransformer();
    public final static PermitDecisionDocumentTransformer MARKDOWN_TO_HTML =
            new PermitDecisionDocumentTransformer(createMarkdownTransformer());

    private static Function<String, String> createMarkdownTransformer() {
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

    private final Function<String, String> transformation;

    private PermitDecisionDocumentTransformer() {
        this.transformation = Functions.identity();
    }

    private PermitDecisionDocumentTransformer(final Function<String, String> transformation) {
        this.transformation = transformation;
    }

    @Nonnull
    public PermitDecisionDocument copy(final @Nonnull PermitDecisionDocument from) {
        final PermitDecisionDocument to = new PermitDecisionDocument();
        copy(from, to);
        return to;
    }

    public void copy(final @Nonnull PermitDecisionDocument from,
                     final @Nonnull PermitDecisionDocument to) {
        Objects.requireNonNull(from, "from is null");
        Objects.requireNonNull(to, "to is null");
        copyBody(from, to);
    }

    public String transform(final String text) {
        return text != null ? transformation.apply(text) : null;
    }

    private void copyBody(final PermitDecisionDocument from, final PermitDecisionDocument to) {
        to.setApplication(transform(from.getApplication()));
        to.setApplicationReasoning(transform(from.getApplicationReasoning()));
        to.setProcessing(transform(from.getProcessing()));
        to.setDecisionReasoning(transform(from.getDecisionReasoning()));
        to.setDecision(transform(from.getDecision()));
        to.setRestriction(transform(from.getRestriction()));
        to.setRestrictionExtra(transform(from.getRestrictionExtra()));
        to.setExecution(transform(from.getExecution()));
        to.setLegalAdvice(transform(from.getLegalAdvice()));
        to.setNotificationObligation(transform(from.getNotificationObligation()));
        to.setAppeal(transform(from.getAppeal()));
        to.setAdditionalInfo(transform(from.getAdditionalInfo()));
        to.setDelivery(transform(from.getDelivery()));
        to.setPayment(transform(from.getPayment()));
        to.setAttachments(transform(from.getAttachments()));
        to.setAdministrativeCourt(transform(from.getAdministrativeCourt()));
    }

}
