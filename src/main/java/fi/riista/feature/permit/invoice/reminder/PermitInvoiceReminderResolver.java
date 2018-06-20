package fi.riista.feature.permit.invoice.reminder;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitContactPerson;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.PermitDecisionInvoice;
import fi.riista.feature.permit.invoice.QInvoice;
import fi.riista.feature.permit.invoice.QPermitDecisionInvoice;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptyList;

@Service
public class PermitInvoiceReminderResolver {
    private static final Logger LOG = LoggerFactory.getLogger(PermitInvoiceReminderResolver.class);

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    /**
     * Send reminder email before due date.
     * - send to original contact person and all other permit contact persons
     * - only for online payments, because otherwise paymentDate is never available
     * - only when payment is visible to customer (DELIVERED)
     */
    @Transactional(readOnly = true)
    public List<PermitInvoiceReminderDTO> resolve() {
        final Days daysBeforeDueDate = InvoiceType.PERMIT_PROCESSING.getDaysOfEmailReminderBeforeDueDate();
        final LocalDate dueDate = DateUtil.today().plus(daysBeforeDueDate);
        final List<PermitDecisionInvoice> decisionInvoiceList = getPermitDecisionsWithDuePayment(dueDate);

        final List<PermitDecision> decisionList = F.mapNonNullsToList(decisionInvoiceList, PermitDecisionInvoice::getDecision);
        final Function<PermitDecisionInvoice, Set<String>> emailMapping = createDecisionInvoiceToContactPersonEmailMapping(decisionList);
        final Function<PermitDecisionInvoice, Long> permitIdMapping = createDecisionToPermitIdMapping(decisionList);

        return F.mapNonNullsToList(decisionInvoiceList, decisionInvoice -> {
            final Set<String> recipientEmails = emailMapping.apply(decisionInvoice);
            final Long harvestPermitId = permitIdMapping.apply(decisionInvoice);
            final Locale locale = decisionInvoice.getDecision().getLocale();

            return harvestPermitId != null && recipientEmails != null && recipientEmails.size() > 0
                    ? new PermitInvoiceReminderDTO(locale, recipientEmails, harvestPermitId)
                    : null;
        });
    }

    // LIST DUE INVOICES

    private List<PermitDecisionInvoice> getPermitDecisionsWithDuePayment(final LocalDate dueDate) {
        final QPermitDecisionInvoice DECISION_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QPerson CONTACT_PERSON = QPerson.person;
        final QInvoice INVOICE = QInvoice.invoice;

        return jpqlQueryFactory
                .selectFrom(DECISION_INVOICE)
                .join(DECISION_INVOICE.invoice, INVOICE).fetchJoin()
                .join(DECISION_INVOICE.decision, DECISION).fetchJoin()
                .join(DECISION.contactPerson, CONTACT_PERSON).fetchJoin()
                .where(INVOICE.electronicInvoicingEnabled.isTrue(),
                        INVOICE.state.eq(InvoiceState.DELIVERED),
                        INVOICE.dueDate.eq(dueDate))
                .orderBy(DECISION_INVOICE.id.asc())
                .fetch();
    }

    // RESOLVE PERMIT ID

    private Function<PermitDecisionInvoice, Long> createDecisionToPermitIdMapping(final List<PermitDecision> decisionList) {
        final Map<Long, List<Long>> uniquePermitIdsForDecision = getDecisionToPermitIdMapping(decisionList);

        return decisionInvoice -> {
            final Long decisionId = F.getId(decisionInvoice.getDecision());
            final List<Long> permitIdList = uniquePermitIdsForDecision.getOrDefault(decisionId, emptyList());

            if (permitIdList.size() != 1) {
                LOG.error("Could not resolve permitId for decisionId {}. Got {} results.",
                        decisionId, permitIdList.size());
                return null;
            }

            return permitIdList.get(0);
        };
    }

    private Map<Long, List<Long>> getDecisionToPermitIdMapping(final List<PermitDecision> decisionList) {
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final NumberPath<Long> keyExpression = PERMIT.permitDecision.id;
        final NumberPath<Long> valueExpression = PERMIT.id;

        return jpqlQueryFactory.select(keyExpression, valueExpression)
                .from(PERMIT)
                .where(PERMIT.permitDecision.in(decisionList))
                .transform(GroupBy.groupBy(keyExpression).as(GroupBy.list(valueExpression)));
    }

    // RESOLVE OTHER CONTACT PERSONS

    private Function<PermitDecisionInvoice, Set<String>> createDecisionInvoiceToContactPersonEmailMapping(final List<PermitDecision> decisionList) {
        final Map<Long, List<String>> decisionToContactPersonMapping = getAdditionalPermitContactPersonsForDecision(decisionList);

        return decisionInvoice -> {
            final PermitDecision decision = decisionInvoice.getDecision();
            final String originalContactPersonEmail = decision.getContactPerson().getEmail();
            final List<String> otherContactPersonEmails = decisionToContactPersonMapping.getOrDefault(F.getId(decision), emptyList());

            final Set<String> recipientEmails = new HashSet<>();
            addValidEmailOnly(recipientEmails, originalContactPersonEmail);
            addValidEmailsOnly(recipientEmails, otherContactPersonEmails);

            return recipientEmails;
        };
    }

    private static void addValidEmailsOnly(final Collection<String> validEmails, final List<String> emailList) {
        emailList.forEach(email -> addValidEmailOnly(validEmails, email));
    }

    private static void addValidEmailOnly(final Collection<String> validEmails, final String email) {
        if (StringUtils.isNotBlank(email)) {
            validEmails.add(email.trim().toLowerCase());
        }
    }

    private Map<Long, List<String>> getAdditionalPermitContactPersonsForDecision(final List<PermitDecision> decisionList) {
        final QHarvestPermitContactPerson PERMIT_CONTACT_PERSON = QHarvestPermitContactPerson.harvestPermitContactPerson;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QPerson CONTACT_PERSON = QPerson.person;

        final NumberPath<Long> keyExpression = PERMIT.permitDecision.id;
        final StringPath valueExpression = CONTACT_PERSON.email;

        return jpqlQueryFactory.select(keyExpression, valueExpression)
                .from(PERMIT_CONTACT_PERSON)
                .join(PERMIT_CONTACT_PERSON.harvestPermit, PERMIT)
                .join(PERMIT_CONTACT_PERSON.contactPerson, CONTACT_PERSON)
                .where(PERMIT.permitDecision.in(decisionList), valueExpression.isNotNull())
                .transform(GroupBy.groupBy(keyExpression).as(GroupBy.list(valueExpression)));
    }
}
