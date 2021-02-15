package fi.riista.api.moderator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.harvestpermit.contactsearch.HarvestPermitContactSearchFeature;
import fi.riista.feature.harvestpermit.contactsearch.PermitContactSearchConditionDTO;
import fi.riista.feature.harvestpermit.contactsearch.PermitContactSearchExcelView;
import fi.riista.feature.harvestpermit.contactsearch.PermitContactSearchResultDTO;
import fi.riista.feature.organization.occupation.search.ContactSearchAreaListFeature;
import fi.riista.feature.organization.occupation.search.ContactSearchFeature;
import fi.riista.feature.organization.occupation.search.OccupationContactSearchDTO;
import fi.riista.feature.organization.occupation.search.OccupationContactSearchExcelView;
import fi.riista.feature.organization.occupation.search.OccupationContactSearchResultDTO;
import fi.riista.feature.organization.occupation.search.RhyContactSearchDTO;
import fi.riista.feature.organization.occupation.search.RhyContactSearchExcelView;
import fi.riista.feature.organization.occupation.search.RhyContactSearchResultDTO;
import fi.riista.feature.pub.occupation.PublicOrganisationDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import javax.validation.constraints.NotBlank;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/contactsearch", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContactSearchApiResource {

    @Resource
    private ContactSearchFeature contactSearchFeature;

    @Resource
    private ContactSearchAreaListFeature contactSearchAreaListFeature;

    @Resource
    private HarvestPermitContactSearchFeature harvestPermitContactSearchFeature;

    @Resource
    private MessageSource messageSource;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/areas", method = RequestMethod.GET)
    public List<PublicOrganisationDTO> listAreas() {
        return contactSearchAreaListFeature.listAreas();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value="/occupation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<OccupationContactSearchResultDTO> searchOccupations(@RequestBody @Valid List<OccupationContactSearchDTO> search) {
        return contactSearchFeature.searchOccupations(search, LocaleContextHolder.getLocale());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/occupation/excel", method = RequestMethod.POST)
    public ModelAndView searchOccupationsExcel(@RequestParam(value = "json") @NotBlank String jsonData) throws IOException {
        List<OccupationContactSearchDTO> search = new ObjectMapper().readValue(jsonData,
                new TypeReference<List<OccupationContactSearchDTO>>() {});

        List<OccupationContactSearchResultDTO> results = contactSearchFeature.searchOccupations(search, LocaleContextHolder.getLocale());

        ModelAndView modelAndView = new ModelAndView(new OccupationContactSearchExcelView(results));

        return modelAndView;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value="/rhy", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<RhyContactSearchResultDTO> searchRhys(@RequestBody @Valid List<RhyContactSearchDTO> search) {
        return contactSearchFeature.searchRhy(search, LocaleContextHolder.getLocale());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/rhy/excel", method = RequestMethod.POST)
    public ModelAndView searchRhysExcel(@RequestParam(value = "json") @NotBlank String jsonData) throws IOException {
        List<RhyContactSearchDTO> search = new ObjectMapper().readValue(jsonData,
                new TypeReference<List<RhyContactSearchDTO>>() {});

        List<RhyContactSearchResultDTO> results = contactSearchFeature.searchRhy(search, LocaleContextHolder.getLocale());

        ModelAndView modelAndView = new ModelAndView(new RhyContactSearchExcelView(results));
        modelAndView.addObject("results", results);

        return modelAndView;
    }

    @PostMapping(value = "/permit", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PermitContactSearchResultDTO> searchPermits(@RequestBody @Valid final List<PermitContactSearchConditionDTO> searchParams,
                                                            final Locale locale) {
        return harvestPermitContactSearchFeature.searchPermitContacts(searchParams, locale);
    }

    @PostMapping(value = "/permit/excel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView searchPermitsExcel(@RequestBody @Valid final List<PermitContactSearchConditionDTO> searchParams, final Locale locale) {
        final List<PermitContactSearchResultDTO> results = harvestPermitContactSearchFeature.searchPermitContacts(searchParams, locale);
        return new ModelAndView(new PermitContactSearchExcelView(results, new EnumLocaliser(messageSource, locale)));
    }
}
