package fi.riista.api.personal;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.area.union.PersonalAreaUnionAddPartnerDTO;
import fi.riista.feature.account.area.union.PersonalAreaUnionBasicDetailsDTO;
import fi.riista.feature.account.area.union.PersonalAreaUnionCreateRequestDTO;
import fi.riista.feature.account.area.union.PersonalAreaUnionDTO;
import fi.riista.feature.account.area.union.PersonalAreaUnionFeature;
import fi.riista.feature.account.area.union.PersonalAreaUnionModifyRequestDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaHuntingYearException;
import fi.riista.feature.permit.area.partner.MetsahallitusYearMismatchException;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = AccountPersonalAreaUnionApiResource.ACCOUNT_RESOURCE_URL)
public class AccountPersonalAreaUnionApiResource {
    public static final String ACCOUNT_RESOURCE_URL = "/api/v1/account/area-union";

    @Resource
    private PersonalAreaUnionFeature personalAreaUnionFeature;

    // READ

    @GetMapping(value = "/page/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public Slice<PersonalAreaUnionDTO> listMinePaged(@RequestParam int huntingYear,
                                                     @RequestParam int page,
                                                     @RequestParam int size) {
        return personalAreaUnionFeature.listMinePaged(huntingYear,
                PageRequest.of(page, size, Sort.Direction.DESC, "id"));
    }

    @GetMapping(value = "/page/{personId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public Slice<PersonalAreaUnionDTO> listForPersonPaged(@RequestParam int huntingYear,
                                                          @PathVariable long personId,
                                                          @RequestParam int page,
                                                          @RequestParam int size) {
        return personalAreaUnionFeature.listForPersonPaged(
                personId, huntingYear,
                PageRequest.of(page, size, Sort.Direction.DESC, "id"));
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<PersonalAreaUnionBasicDetailsDTO> listMineReady(@RequestParam int huntingYear) {
        return personalAreaUnionFeature.listMineReady(huntingYear);
    }

    @GetMapping(value = "/{personId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<PersonalAreaUnionBasicDetailsDTO> listForPersonReady(@RequestParam int huntingYear,
                                                                     @PathVariable long personId) {
        return personalAreaUnionFeature.listForPersonReady(personId, huntingYear);
    }

    // CREATE

    @PostMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public PersonalAreaUnionDTO createAreaUnion(@Valid @RequestBody final PersonalAreaUnionCreateRequestDTO dto) {
        return personalAreaUnionFeature.createAreaUnionForMe(dto);
    }

    @PostMapping(value = "/{personId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PersonalAreaUnionDTO createAreaUnion(@PathVariable long personId,
                                                @Valid @RequestBody final PersonalAreaUnionCreateRequestDTO dto) {
        return personalAreaUnionFeature.createAreaUnionForPerson(dto, personId);
    }

    // RENAME

    @PutMapping(value = "/{areaId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PersonalAreaUnionDTO updateAreaUnion(@PathVariable long areaId,
                                                @Valid @RequestBody final PersonalAreaUnionModifyRequestDTO dto) {
        return personalAreaUnionFeature.updateAreaUnion(areaId, dto);
    }

    // AREA PARTNERS

    @PostMapping(value = "/{areaId:\\d+}/partner")
    public ResponseEntity<?> addPartnerArea(@Valid @RequestBody PersonalAreaUnionAddPartnerDTO dto) {
        try {
            personalAreaUnionFeature.addPartner(dto);
            return ResponseEntity.noContent().build();

        } catch (final HarvestPermitAreaHuntingYearException | MetsahallitusYearMismatchException e) {
            return ResponseEntity.badRequest().body(ImmutableMap.of("exception", e.getClass().getSimpleName()));
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{areaId:\\d+}/partner/{partnerId:\\d+}")
    public void removePartnerArea(@PathVariable long areaId, @PathVariable long partnerId) {
        personalAreaUnionFeature.removePartner(areaId, partnerId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{areaId:\\d+}/partner/{partnerId:\\d+}")
    public void refreshPartnerGeometry(@PathVariable long areaId,
                                       @PathVariable long partnerId) {
        personalAreaUnionFeature.refreshPartner(areaId, partnerId);
    }

    @GetMapping(value = "/available-clubs/me")
    public List<OrganisationNameDTO> listAvailablePartners() {
        return personalAreaUnionFeature.listAvailableClubs();
    }

    @GetMapping(value = "/available-clubs/{personId:\\d+}")
    public List<OrganisationNameDTO> listAvailablePartners(@PathVariable final long personId) {
        return personalAreaUnionFeature.listAvailableClubs(personId);
    }

    // CALCULATE GEOMETRY

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{areaId:\\d+}/area/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, HarvestPermitArea.StatusCode> getStatus(@PathVariable long areaId) {
        return ImmutableMap.of("status", personalAreaUnionFeature.getStatus(areaId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{areaId:\\d+}/area/ready")
    public void setReadyForProcessing(@PathVariable long areaId) {
        personalAreaUnionFeature.setReadyForProcessing(areaId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{areaId:\\d+}/area/incomplete")
    public void setIncomplete(@PathVariable long areaId) {
        personalAreaUnionFeature.setIncomplete(areaId);
    }


    // PDF

    @PostMapping(value = "/{id:\\d+}/print-map", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> printMap(@PathVariable final long id, final Locale locale,
                                      @ModelAttribute @Valid final MapPdfParameters dto) {
        final PersonalAreaUnionFeature.PdfData pdfData = personalAreaUnionFeature.exportMapPdf(id, locale, dto);

        return ResponseEntity.ok()
                .contentType(MediaTypeExtras.APPLICATION_PDF)
                .contentLength(pdfData.getData().length)
                .headers(ContentDispositionUtil.header(pdfData.getFilename()))
                .body(pdfData.getData());
    }

    @PostMapping(value = "/{id:\\d+}/print-partners", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> printPartners(@PathVariable final long id, final Locale locale) {
        final PersonalAreaUnionFeature.PdfData pdfData = personalAreaUnionFeature.exportPartnersPdf(id, locale);

        return ResponseEntity.ok()
                .contentType(MediaTypeExtras.APPLICATION_PDF)
                .contentLength(pdfData.getData().length)
                .headers(ContentDispositionUtil.header(pdfData.getFilename()))
                .body(pdfData.getData());
    }
}
