package fi.riista.feature.permit.invoice.harvest.excel;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

@Service
public class MooselikeHarvestPaymentSummaryFeature {

    @Resource
    private MooselikeHarvestPaymentSummaryService paymentSummaryService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private MessageSource messageSource;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public MooselikeHarvestPaymentSummaryExcelView exportMooselikeHarvestPaymentSummaryToExcel(final int huntingYear,
                                                                                               final int speciesCode,
                                                                                               final Locale locale) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(speciesCode);

        return new MooselikeHarvestPaymentSummaryExcelView(
                huntingYear,
                species.getNameLocalisation(),
                paymentSummaryService.getMooselikeHarvestPaymentSummary(huntingYear, speciesCode),
                getLocaliser(locale));
    }

    private EnumLocaliser getLocaliser(final Locale locale) {
        return new EnumLocaliser(messageSource, locale);
    }
}
