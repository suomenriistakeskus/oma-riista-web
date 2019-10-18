package fi.riista.feature.permit.invoice.harvest.excel;

import fi.riista.feature.harvestpermit.HarvestPermitIdFetchService;
import fi.riista.feature.harvestpermit.payment.MooselikePrice;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountService;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.permit.invoice.harvest.InvoicePaymentAmountsDTO;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.feature.organization.RiistakeskuksenAlue.shortenRkaPrefixFi;
import static fi.riista.feature.organization.RiistakeskuksenAlue.shortenRkaPrefixSv;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class MooselikeHarvestPaymentSummaryService {

    @Resource
    private HarvestPermitIdFetchService harvestPermitIdFetchService;

    @Resource
    private PermitHarvestInvoiceRepository harvestInvoiceRepository;

    @Resource
    private HarvestCountService harvestCountService;

    @Transactional(readOnly = true)
    public List<MooselikeHarvestPaymentSummaryDTO> getMooselikeHarvestPaymentSummary(final int huntingYear,
                                                                                     final int speciesCode) {
        final Map<RiistakeskuksenAlue, Set<Long>> permitIdsByRka =
                harvestPermitIdFetchService.getMooselikePermitIdsGroupedByRka(huntingYear, speciesCode);

        return permitIdsByRka.entrySet()
                .stream()
                .map(entry -> {

                    final Set<Long> permitIds = entry.getValue();

                    final HarvestCountDTO rkaHarvestCount = harvestCountService
                            .countHarvestsGroupingByPermitAndClubId(permitIds, speciesCode)
                            .sumAllCounts();

                    final Map<Long, InvoicePaymentAmountsDTO> paymentAmountsByPermitId =
                            harvestInvoiceRepository.getMooselikeHarvestInvoicePaymentAmounts(permitIds, speciesCode);

                    final InvoicePaymentAmountsDTO sumOfPaymentAmounts =
                            InvoicePaymentAmountsDTO.reduce(paymentAmountsByPermitId.values().stream());

                    return new MooselikeHarvestPaymentSummaryDTO(
                            transformToDTO(entry.getKey()),
                            rkaHarvestCount,
                            sumOfPaymentAmounts,
                            countPaymentAmountFromHarvestCounts(speciesCode, rkaHarvestCount));

                })
                .sorted(comparing(dto -> dto.getOrganisation().getOfficialCode()))
                .collect(toList());
    }

    private static OrganisationNameDTO transformToDTO(final RiistakeskuksenAlue rka) {
        final OrganisationNameDTO dto = new OrganisationNameDTO();
        dto.setOfficialCode(rka.getOfficialCode());
        dto.setNameFI(shortenRkaPrefixFi(rka.getNameFinnish()));
        dto.setNameSV(shortenRkaPrefixSv(rka.getNameSwedish()));
        return dto;
    }

    private static BigDecimal countPaymentAmountFromHarvestCounts(final int speciesCode,
                                                                  final HarvestCountDTO harvestCounts) {

        final MooselikePrice prices = MooselikePrice.get(speciesCode);

        final BigDecimal numberOfEdibleAdults =
                new BigDecimal(harvestCounts.getNumberOfAdults() - harvestCounts.getNumberOfNonEdibleAdults());
        final BigDecimal numberOfEdibleYoungs =
                new BigDecimal(harvestCounts.getNumberOfYoung() - harvestCounts.getNumberOfNonEdibleYoungs());

        final BigDecimal paymentAmountForAdults = prices.getAdultPrice().multiply(numberOfEdibleAdults);
        final BigDecimal paymentAmountForYoung = prices.getYoungPrice().multiply(numberOfEdibleYoungs);

        return paymentAmountForAdults.add(paymentAmountForYoung);
    }
}
