package fi.riista.feature.permit.invoice.search;

import com.google.common.collect.Sets;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.entity.BaseEntityEvent_;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceContactDetailsDTO;
import fi.riista.feature.permit.invoice.InvoiceFivaldiState;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEvent;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEventRepository;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEvent_;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.batch.QPermitDecisionInvoiceBatch;
import fi.riista.feature.permit.invoice.decision.QPermitDecisionInvoice;
import fi.riista.feature.permit.invoice.harvest.QPermitHarvestInvoice;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLine;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLineDTO;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLineRepository;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLine_;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.permit.invoice.InvoiceType.PERMIT_HARVEST;
import static fi.riista.feature.permit.invoice.InvoiceType.PERMIT_PROCESSING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class InvoiceModeratorDTOTransformer extends ListTransformer<Invoice, InvoiceModeratorDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceModeratorDTOTransformer.class);

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private InvoicePaymentLineRepository paymentLineRepository;

    @Resource
    private InvoiceStateChangeEventRepository invoiceStateChangeEventRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private JPAQueryFactory queryFactory;

    @Nonnull
    @Override
    protected List<InvoiceModeratorDTO> transform(@Nonnull final List<Invoice> invoices) {

        final Map<Boolean, List<Invoice>> invoicePartition =
                F.partition(invoices, invoice -> invoice.getType() == PERMIT_PROCESSING);

        final List<Invoice> decisionInvoices = invoicePartition.get(true);
        final List<Invoice> harvestInvoices = invoicePartition.get(false);

        final Function<Invoice, Address> getAddress = createInvoiceToRecipientAddressMapping(invoices);

        final Map<Long, DecisionIdPermitNumber> decisionIdPermitNumberMapping =
                getDecisionIdPermitNumberMapping(decisionInvoices, harvestInvoices);

        final Map<Long, InvoiceFivaldiState> invoiceToFivaldiStateMapping =
                createInvoiceToFivaldiStateMapping(decisionInvoices);

        final Map<Invoice, List<InvoicePaymentLine>> paymentMap = getPaymentsGroupedByInvoices(harvestInvoices);
        final Map<Invoice, List<InvoiceStateChangeEvent>> actionMap = getActionsGroupedByInvoices(invoices);

        final Stream<Long> paymentModeratorIds = paymentMap.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(payment -> payment.getAccountTransfer() == null)
                .map(InvoicePaymentLine::getModifiedByUserId)
                .filter(Objects::nonNull);

        final Stream<Long> actorIds = actionMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(InvoiceStateChangeEvent::getUserId);

        final Set<Long> moderatorIds = Stream.concat(paymentModeratorIds, actorIds).collect(toSet());
        final Map<Long, SystemUser> userById = F.indexById(userRepository.findAllById(moderatorIds));

        return invoices.stream().map(invoice -> {

            final InvoiceModeratorDTO dto = new InvoiceModeratorDTO();
            DtoUtil.copyBaseFields(invoice, dto);

            final long invoiceId = invoice.getId();
            final InvoiceType invoiceType = invoice.getType();

            dto.setType(invoiceType);
            dto.setInvoiceNumber(invoice.getInvoiceNumber());
            dto.setState(invoice.getDisplayState());
            dto.setElectronicInvoicingEnabled(invoice.isElectronicInvoicingEnabled());
            dto.setInvoiceDate(invoice.getInvoiceDate());
            dto.setDueDate(invoice.getDueDate());
            dto.setOverdue(invoice.isOverdue());
            dto.setCreditorReference(invoice.getCreditorReference().toString());

            if (invoice.getCorrectedAmount() != null) {
                dto.setPaymentAmount(invoice.getCorrectedAmount());
                dto.setPaymentAmountCorrected(true);
            } else {
                dto.setPaymentAmount(invoice.getAmount());
                dto.setPaymentAmountCorrected(false);
            }

            // Null received amount for permit harvest invoice indicates that account transfer is
            // not present.
            dto.setReceivedAmount(Optional
                    .ofNullable(invoice.getReceivedAmount())
                    .orElseGet(() -> invoiceType == PERMIT_HARVEST ? BigDecimal.ZERO : null));

            dto.setInvoiceRecipient(InvoiceContactDetailsDTO.create(invoice, getAddress.apply(invoice)));

            final DecisionIdPermitNumber decisionIdPermitNumber = decisionIdPermitNumberMapping.get(invoiceId);

            if (decisionIdPermitNumber != null) {
                dto.setPermitDecisionId(decisionIdPermitNumber.decisionId);
                dto.setPermitNumber(decisionIdPermitNumber.permitNumber);
                dto.setPermitTypeCode(decisionIdPermitNumber.permitTypeCode);
            }

            if (invoiceType == PERMIT_PROCESSING) {
                dto.setFivaldiState(invoiceToFivaldiStateMapping.get(invoiceId));

            } else if (invoiceType == PERMIT_HARVEST) {
                dto.setPayments(paymentMap.getOrDefault(invoice, Collections.emptyList())
                        .stream()
                        .map(payment -> {
                            final SystemUser moderator = userById.get(payment.getModifiedByUserId());
                            return InvoicePaymentLineDTO.create(payment, moderator);
                        })
                        .collect(toList()));
            }

            dto.setActions(actionMap.getOrDefault(invoice, Collections.emptyList())
                    .stream()
                    .map(action -> {
                        final SystemUser moderator = userById.get(action.getUserId());
                        return InvoiceModeratorDTO.InvoiceActionDTO.create(action, moderator);
                    })
                    .collect(toList()));

            return dto;

        }).collect(toList());
    }

    private Function<Invoice, Address> createInvoiceToRecipientAddressMapping(final Iterable<Invoice> invoices) {
        return CriteriaUtils.singleQueryFunction(invoices, Invoice::getRecipientAddress, addressRepository, true);
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

                    return downloaded ? InvoiceFivaldiState.DOWNLOADED : InvoiceFivaldiState.BATCHED;
                }));
    }

    private Map<Invoice, List<InvoicePaymentLine>> getPaymentsGroupedByInvoices(final Collection<Invoice> invoices) {
        return JpaGroupingUtils.groupRelations(
                invoices,
                InvoicePaymentLine_.invoice,
                paymentLineRepository,
                JpaSort.of(Direction.DESC, InvoicePaymentLine_.paymentDate, InvoicePaymentLine_.id));
    }

    private Map<Invoice, List<InvoiceStateChangeEvent>> getActionsGroupedByInvoices(final Collection<Invoice> invoices) {
        return JpaGroupingUtils.groupRelations(
                invoices,
                InvoiceStateChangeEvent_.invoice,
                invoiceStateChangeEventRepository,
                JpaSort.of(BaseEntityEvent_.eventTime));
    }

    private Map<Long, DecisionIdPermitNumber> getDecisionIdPermitNumberMapping(final List<Invoice> decisionInvoices,
                                                                               final List<Invoice> harvestInvoices) {

        final Map<Long, DecisionIdPermitNumber> decisionMapping =
                getDecisionIdPermitNumberMappingOfDecisionInvoice(decisionInvoices);
        final Map<Long, DecisionIdPermitNumber> harvestMapping =
                getDecisionIdPermitNumberMappingOfHarvestInvoices(harvestInvoices);

        logDuplicateInvoiceReferences(decisionMapping.keySet(), harvestMapping.keySet());

        decisionMapping.putAll(harvestMapping);
        return decisionMapping;
    }

    private Map<Long, DecisionIdPermitNumber> getDecisionIdPermitNumberMappingOfDecisionInvoice(final List<Invoice> invoices) {
        final QPermitDecisionInvoice PERMIT_DECISION_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QPermitDecision PERMIT_DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;

        final NumberPath<Long> invoiceIdPath = PERMIT_DECISION_INVOICE.invoice.id;
        final NumberPath<Long> decisionIdPath = PERMIT_DECISION.id;
        final NumberPath<Integer> decisionYearPath = PERMIT_DECISION.decisionYear;
        final NumberPath<Integer> decisionNumberPath = PERMIT_DECISION.decisionNumber;
        final NumberPath<Integer> validityYearPath = PERMIT_DECISION.validityYears;
        final StringPath permitTypeCodePath = PERMIT_DECISION.permitTypeCode;

        return queryFactory
                .select(invoiceIdPath, decisionIdPath, decisionYearPath, decisionNumberPath,
                        validityYearPath, permitTypeCodePath)
                .from(PERMIT_DECISION_INVOICE)
                .join(PERMIT_DECISION_INVOICE.decision, PERMIT_DECISION)
                .join(PERMIT_DECISION.application, APPLICATION)
                .where(PERMIT_DECISION_INVOICE.invoice.in(invoices))
                .fetch()
                .stream()
                .collect(toMap(
                        t -> t.get(invoiceIdPath),
                        t -> new DecisionIdPermitNumber(t.get(decisionIdPath), t.get(decisionYearPath),
                                t.get(decisionNumberPath), t.get(validityYearPath), t.get(permitTypeCodePath))));
    }

    private Map<Long, DecisionIdPermitNumber> getDecisionIdPermitNumberMappingOfHarvestInvoices(final List<Invoice> invoices) {
        final QPermitHarvestInvoice HARVEST_INVOICE = QPermitHarvestInvoice.permitHarvestInvoice;
        final QPermitDecision PERMIT_DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

        final NumberPath<Long> invoiceIdPath = HARVEST_INVOICE.invoice.id;
        final NumberPath<Long> decisionIdPath = PERMIT_DECISION.id;
        final NumberPath<Integer> decisionYearPath = PERMIT_DECISION.decisionYear;
        final NumberPath<Integer> decisionNumberPath = PERMIT_DECISION.decisionNumber;
        final NumberPath<Integer> validityYearPath = PERMIT_DECISION.validityYears;
        final StringPath permitTypeCodePath = PERMIT_DECISION.permitTypeCode;

        return queryFactory
                .select(HARVEST_INVOICE.invoice.id, decisionIdPath,  decisionYearPath, decisionNumberPath,
                        validityYearPath, permitTypeCodePath)
                .from(HARVEST_INVOICE)
                .join(HARVEST_INVOICE.speciesAmount, SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.harvestPermit, PERMIT)
                .join(PERMIT.permitDecision, PERMIT_DECISION)
                .join(PERMIT_DECISION.application, APPLICATION)
                .where(HARVEST_INVOICE.invoice.in(invoices))
                .fetch()
                .stream()
                .collect(toMap(
                        t -> t.get(invoiceIdPath),
                        t -> new DecisionIdPermitNumber(t.get(decisionIdPath), t.get(decisionYearPath),
                                t.get(decisionNumberPath), t.get(validityYearPath), t.get(permitTypeCodePath))));
    }

    private static class DecisionIdPermitNumber {

        public final long decisionId;
        public final String permitNumber;
        public final String permitTypeCode;

        DecisionIdPermitNumber(final long decisionId, final int year, final int orderNumber, final int validityYears, final String permitTypeCode) {
            this.decisionId = decisionId;
            this.permitNumber = DocumentNumberUtil.createDocumentNumber(year, validityYears, orderNumber);
            this.permitTypeCode = permitTypeCode;
        }
    }

    private static void logDuplicateInvoiceReferences(final Set<Long> decisionInvoiceIds,
                                                      final Set<Long> harvestInvoiceIds) {

        final Set<Long> invoiceIds = Sets.intersection(decisionInvoiceIds, harvestInvoiceIds);

        if (!invoiceIds.isEmpty()) {
            LOG.warn("DecisionInvoices and HarvestInvoices are referencing to same Invoices invoiceIds: {}", invoiceIds);
        }
    }
}
