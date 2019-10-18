package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.permit.invoice.InvoiceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class InvoiceSearchFeature {

    private static final int MAX_SEARCH_RESULTS = 200;

    @Resource
    private InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public List<InvoiceSearchResultDTO> searchInvoices(@Nonnull final InvoiceSearchFilterDTO dto) {
        return invoiceRepository.search(dto, MAX_SEARCH_RESULTS)
                .stream()
                .map(invoice -> InvoiceSearchResultDTO.create(invoice, invoice.getRecipientAddress()))
                .collect(toList());
    }
}
