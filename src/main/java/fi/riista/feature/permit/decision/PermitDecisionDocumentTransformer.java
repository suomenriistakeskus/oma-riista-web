package fi.riista.feature.permit.decision;

import com.google.common.base.Functions;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

import static fi.riista.feature.permit.decision.DecisionDocumentTransformerUtils.createMarkdownTransformer;
import static fi.riista.feature.permit.decision.DecisionDocumentTransformerUtils.transformLineFeeds;

public class PermitDecisionDocumentTransformer {
    public final static PermitDecisionDocumentTransformer SIMPLE = new PermitDecisionDocumentTransformer();
    public final static PermitDecisionDocumentTransformer MARKDOWN_TO_HTML =
            new PermitDecisionDocumentTransformer(createMarkdownTransformer());

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

        // Fields with generated content
        to.setApplication(transform(from.getApplication()));
        to.setApplicationReasoning(transform(from.getApplicationReasoning()));
        to.setProcessing(transform(from.getProcessing()));
        to.setDecision(transform(from.getDecision()));
        to.setRestriction(transform(from.getRestriction()));
        to.setAdditionalInfo(transform(from.getAdditionalInfo()));
        to.setDelivery(transform(from.getDelivery()));
        to.setPayment(transform(from.getPayment()));
        to.setAttachments(transform(from.getAttachments()));
        to.setAdministrativeCourt(transform(from.getAdministrativeCourt()));

        // Only line feeds transformed for fields containing free text
        to.setDecisionReasoning(transformLineFeeds(from.getDecisionReasoning()));
        to.setDecisionExtra(transformLineFeeds(from.getDecisionExtra()));
        to.setRestrictionExtra(transformLineFeeds(from.getRestrictionExtra()));
        to.setExecution(transformLineFeeds(from.getExecution()));
        to.setLegalAdvice(transformLineFeeds(from.getLegalAdvice()));
        to.setNotificationObligation(transformLineFeeds(from.getNotificationObligation()));
        to.setAppeal(transformLineFeeds(from.getAppeal()));
    }

}
