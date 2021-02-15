package fi.riista.feature.common.decision.nomination;

import com.google.common.base.Functions;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static fi.riista.feature.permit.decision.DecisionDocumentTransformerUtils.createMarkdownTransformer;
import static fi.riista.feature.permit.decision.DecisionDocumentTransformerUtils.transformLineFeeds;
import static java.util.Objects.requireNonNull;

public class NominationDecisionDocumentTransformer {
    public final static NominationDecisionDocumentTransformer PASS_THROUGH = new NominationDecisionDocumentTransformer();
    public final static NominationDecisionDocumentTransformer MARKDOWN_TO_HTML =
            new NominationDecisionDocumentTransformer(createMarkdownTransformer());


    private final Function<String, String> transformation;

    private NominationDecisionDocumentTransformer() {
        this.transformation = Functions.identity();
    }

    private NominationDecisionDocumentTransformer(final Function<String, String> transformation) {
        this.transformation = requireNonNull(transformation);
    }

    @Nonnull
    public NominationDecisionDocument copy(final @Nonnull NominationDecisionDocument from) {
        final NominationDecisionDocument to = new NominationDecisionDocument();
        copy(from, to);
        return to;
    }

    public void copy(final @Nonnull NominationDecisionDocument from,
                     final @Nonnull NominationDecisionDocument to) {
        requireNonNull(from, "from is null");
        requireNonNull(to, "to is null");
        copyBody(from, to);
    }

    public String transform(final String text) {
        return text != null ? transformation.apply(text) : null;
    }

    private void copyBody(final NominationDecisionDocument from, final NominationDecisionDocument to) {

        // Fields with generated content
        to.setProcessing(transform(from.getProcessing()));
        to.setDecision(transform(from.getDecision()));
        to.setAdditionalInfo(transform(from.getAdditionalInfo()));
        to.setDelivery(transform(from.getDelivery()));
        to.setPayment(transform(from.getPayment()));
        to.setAttachments(transform(from.getAttachments()));

        // Only line feeds transformed for fields containing free text
        to.setProposal(transformLineFeeds(from.getProposal()));
        to.setDecisionReasoning(transformLineFeeds(from.getDecisionReasoning()));
        to.setLegalAdvice(transformLineFeeds(from.getLegalAdvice()));
        to.setAppeal(transformLineFeeds(from.getAppeal()));
    }

}
