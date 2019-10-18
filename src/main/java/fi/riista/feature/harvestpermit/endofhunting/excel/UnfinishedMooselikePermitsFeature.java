package fi.riista.feature.harvestpermit.endofhunting.excel;

import fi.riista.feature.common.EnumLocaliser;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

@Service
public class UnfinishedMooselikePermitsFeature {

    @Resource
    private UnfinishedMooselikePermitsService service;

    @Resource
    private MessageSource messageSource;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public UnfinishedMooselikePermitsExcelView exportUnfinishedMooselikePermitsToExcel(final int huntingYear,
                                                                                       final Locale locale) {

        return new UnfinishedMooselikePermitsExcelView(
                huntingYear, getLocaliser(locale), service.findUnfinishedWithinMooselikeHunting(huntingYear));
    }

    private EnumLocaliser getLocaliser(final Locale locale) {
        return new EnumLocaliser(messageSource, locale);
    }
}
