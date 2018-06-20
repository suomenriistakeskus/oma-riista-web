package fi.riista.feature.permit.invoice;

import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.entity.BaseEntityEvent_;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.invoice.batch.PermitDecisionInvoiceBatchRepository;
import fi.riista.feature.permit.invoice.batch.QPermitDecisionInvoiceBatch;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class InvoiceDTOTransformer extends ListTransformer<Invoice, InvoiceDTO> {

    @Resource
    private AddressRepository addressRepo;

    @Resource
    private PermitDecisionInvoiceBatchRepository batchRepo;

    @Resource
    private InvoiceStateChangeEventRepository invoiceEventRepo;

    @Resource
    private UserRepository userRepo;

    @Resource
    private JPAQueryFactory queryFactory;

    @Override
    protected List<InvoiceDTO> transform(final List<Invoice> invoices) {

        final Function<Invoice, Address> getAddress = createInvoiceToRecipientAddressMapping(invoices);

        final Map<Long, DecisionIdPermitNumber> decisionIdPermitNumberMapping =
                getDecisionIdPermitNumberMapping(invoices);

        final Map<Long, InvoiceFivaldiState> invoiceToFivaldiStateMapping =
                createInvoiceToFivaldiStateMapping(invoices);

        final Map<Invoice, List<InvoiceStateChangeEvent>> eventMap = getEventsGroupedByInvoices(invoices);
        final Set<Long> moderatorIds = eventMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(InvoiceStateChangeEvent::getUserId)
                .collect(toSet());
        final Map<Long, SystemUser> userById = F.indexById(userRepo.findAll(moderatorIds));

        return invoices.stream().map(invoice -> {

            final InvoiceDTO dto = new InvoiceDTO();
            DtoUtil.copyBaseFields(invoice, dto);

            dto.setInvoiceNumber(invoice.getInvoiceNumber());
            dto.setType(invoice.getType());
            dto.setState(invoice.getDisplayState());
            dto.setElectronicInvoicingEnabled(invoice.isElectronicInvoicingEnabled());
            dto.setInvoiceDate(invoice.getInvoiceDate());
            dto.setDueDate(invoice.getDueDate());
            dto.setOverdue(invoice.isOverdue());
            dto.setPaymentAmount(invoice.getAmount());
            dto.setCreditorReference(invoice.getCreditorReference().toString());

            dto.setInvoiceRecipient(InvoiceContactDetailsDTO.create(invoice, getAddress.apply(invoice)));

            final DecisionIdPermitNumber decisionIdPermitNumber = decisionIdPermitNumberMapping.get(invoice.getId());
            dto.setPermitDecisionId(decisionIdPermitNumber.decisionId);
            dto.setPermitNumber(decisionIdPermitNumber.permitNumber);

            dto.setFivaldiState(invoiceToFivaldiStateMapping.get(invoice.getId()));

            final List<InvoiceStateChangeEvent> events = eventMap.getOrDefault(invoice, emptyList());
            dto.setEvents(events.stream()
                    .map(event -> InvoiceDTO.InvoiceEventDTO.create(event, userById.get(event.getUserId())))
                    .collect(toList()));

            return dto;

        }).collect(toList());
    }

    private Function<Invoice, Address> createInvoiceToRecipientAddressMapping(final Iterable<Invoice> invoices) {
        return CriteriaUtils.singleQueryFunction(invoices, Invoice::getRecipientAddress, addressRepo, true);
    }

    private Map<Long, InvoiceFivaldiState> createInvoiceToFivaldiStateMapping(final List<Invoice> invoices) {
        final QPermitDecisionInvoice PERMIT_DECISION_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QPermitDecisionInvoiceBatch BATCH = QPermitDecisionInvoiceBatch.permitDecisionInvoiceBatch;

        final NumberPath<Long> invoiceIdPath = PERMIT_DECISION_INVOICE.invoice.id;
        final BooleanPath downloadedPath = BATCH.downloaded;

        return queryFactory
                .select(invoiceIdPath, downloadedPath)
                .from(PERMIT_DECISION_INVOICE)
                .leftJoin(PERMIT_DECISION_INVOICE.batch, BATCH)
                .where(PERMIT_DECISION_INVOICE.invoice.in(invoices))
                .fetch()
                .stream()
                .collect(toMap(t -> t.get(invoiceIdPath), t -> {
                    final Boolean downloaded = t.get(downloadedPath);

                    if (downloaded == null) {
                        return InvoiceFivaldiState.NOT_BATCHED;
                    }

                    return downloaded.booleanValue() ? InvoiceFivaldiState.DOWNLOADED : InvoiceFivaldiState.BATCHED;
                }));
    }

    private Map<Invoice, List<InvoiceStateChangeEvent>> getEventsGroupedByInvoices(final Collection<Invoice> invoices) {
        return JpaGroupingUtils.groupRelations(
                invoices,
                InvoiceStateChangeEvent_.invoice,
                invoiceEventRepo,
                new JpaSort(BaseEntityEvent_.eventTime));
    }

    private Map<Long, DecisionIdPermitNumber> getDecisionIdPermitNumberMapping(final List<Invoice> invoices) {
        final QPermitDecisionInvoice PERMIT_DECISION_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QPermitDecision PERMIT_DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;

        final NumberPath<Long> invoiceIdPath = PERMIT_DECISION_INVOICE.invoice.id;
        final NumberPath<Long> decisionIdPath = PERMIT_DECISION.id;
        final StringPath permitNumberPath = APPLICATION.permitNumber;

        return queryFactory
                .select(invoiceIdPath, decisionIdPath, permitNumberPath)
                .from(PERMIT_DECISION_INVOICE)
                .join(PERMIT_DECISION_INVOICE.decision, PERMIT_DECISION)
                .join(PERMIT_DECISION.application, APPLICATION)
                .where(PERMIT_DECISION_INVOICE.invoice.in(invoices))
                .fetch()
                .stream()
                .collect(toMap(t -> t.get(invoiceIdPath), t -> {
                    return new DecisionIdPermitNumber(t.get(decisionIdPath), t.get(permitNumberPath));
                }));
    }

    private static class DecisionIdPermitNumber {

        public final long decisionId;
        public final String permitNumber;

        DecisionIdPermitNumber(final long decisionId, final String permitNumber) {
            this.decisionId = decisionId;
            this.permitNumber = permitNumber;
        }
    }
}
