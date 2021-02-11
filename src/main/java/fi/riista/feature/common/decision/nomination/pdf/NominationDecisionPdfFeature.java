package fi.riista.feature.common.decision.nomination.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.decision.DecisionUtil;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.NominationDecisionDocumentTransformer;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static java.util.Optional.ofNullable;

@Component
public class NominationDecisionPdfFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional(readOnly = true)
    public NominationDecisionPdfFileDTO getDecisionFileName(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.READ);
        final String decisionDocumentNumber = decision.createDocumentNumber();
        final String filename = DecisionUtil.getNominationDecisionFileName(decision.getLocale(), decisionDocumentNumber);

        return new NominationDecisionPdfFileDTO(filename, decisionDocumentNumber);
    }

    @Transactional(readOnly = true)
    public NominationDecisionPdfDTO getModel(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.READ);
        final NominationDecisionDocument htmlDocument = ofNullable(decision.getDocument())
                .map(NominationDecisionDocumentTransformer.MARKDOWN_TO_HTML::copy)
                .orElseGet(NominationDecisionDocument::new);

        final String permitNumber = decision.createDocumentNumber();
        final Person contactPerson = decision.getContactPerson();

        return new NominationDecisionPdfDTO(permitNumber, decision, htmlDocument, contactPerson);
    }

}
