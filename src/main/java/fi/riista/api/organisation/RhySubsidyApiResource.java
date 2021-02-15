package fi.riista.api.organisation;

import fi.riista.feature.organization.rhy.subsidy.RhySubsidyAllocationFeature;
import fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationInputDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Locale;

import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys/subsidy")
public class RhySubsidyApiResource {

    @Resource
    private RhySubsidyAllocationFeature subsidyAllocationFeature;

    @PostMapping(value = "/excel", consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportSubsidyAllocationsToExcel(final @RequestParam BigDecimal totalSubsidyAmount,
                                                        final @RequestParam int subsidyYear,
                                                        final Locale locale) {

        final SubsidyAllocationInputDTO allocationInput =
                new SubsidyAllocationInputDTO(subsidyYear, totalSubsidyAmount);

        final AbstractXlsxView excelView = subsidyYear == 2019
                ? subsidyAllocationFeature.exportSubsidyAllocationsForYear2019(allocationInput, locale)
                : subsidyAllocationFeature.exportSubsidyAllocations(allocationInput, locale);

        return new ModelAndView(excelView);
    }
}
