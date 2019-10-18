package fi.riista.feature.account.area.union;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaEventRepository;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.feature.permit.area.QHarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerService;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.security.EntityPermission;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Service
public class PersonalAreaUnionFeature {

    public static class PdfData {
        private final byte[] data;
        private final String filename;

        public PdfData(final byte[] data, final String filename) {
            this.data = data;
            this.filename = filename;
        }

        public byte[] getData() {
            return data;
        }

        public String getFilename() {
            return filename;
        }
    }

    private static final QPersonalAreaUnion UNION = QPersonalAreaUnion.personalAreaUnion;
    private static final QHarvestPermitArea HPA = QHarvestPermitArea.harvestPermitArea;

    @Resource
    private PersonalAreaUnionRepository personalAreaUnionRepository;

    @Resource
    HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    GISZoneRepository gisZoneRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PersonalAreaUnionToDTOTransformer personalAreaUnionToDTOTransformer;

    @Resource
    private PersonalAreaUnionToBasicDetailsDTOTransformer personalAreaUnionToBasicDetailsDTOTransformer;

    @Resource
    private HarvestPermitAreaPartnerService harvestPermitAreaPartnerService;

    @Resource
    private HarvestPermitAreaEventRepository harvestPermitAreaEventRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private PersonalAreaUnionPrintService personalAreaUnionPrintService;

    @Transactional(readOnly = true)
    public Slice<PersonalAreaUnionDTO> listMinePaged(final int huntingYear, final PageRequest pageRequest) {
        final Person person = activeUserService.requireActivePerson();
        final Slice<PersonalAreaUnion> all = fetchSlice(pageRequest, huntingYear, person);

        return personalAreaUnionToDTOTransformer.apply(all, pageRequest);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public Slice<PersonalAreaUnionDTO> listForPersonPaged(final long personId, final int huntingYear,
                                                          final PageRequest pageRequest) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.READ);
        final Slice<PersonalAreaUnion> all = fetchSlice(pageRequest, huntingYear, person);

        return personalAreaUnionToDTOTransformer.apply(all, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<PersonalAreaUnionBasicDetailsDTO> listMineReady(final int huntingYear) {
        final Person person = activeUserService.requireActivePerson();
        return fetchListReady(person, huntingYear);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<PersonalAreaUnionBasicDetailsDTO> listForPersonReady(final long personId, final int huntingYear) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.READ);
        return fetchListReady(person, huntingYear);
    }

    @Transactional
    public PersonalAreaUnionDTO createAreaUnionForMe(final PersonalAreaUnionCreateRequestDTO dto) {
        final Person person = activeUserService.requireActivePerson();

        return doCreateAreaUnion(dto, person);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional
    public PersonalAreaUnionDTO createAreaUnionForPerson(final PersonalAreaUnionCreateRequestDTO dto,
                                                         final long personId) {
        final Person person = requireEntityService.requirePerson(requireNonNull(personId),
                EntityPermission.READ);
        return doCreateAreaUnion(dto, person);
    }

    @Transactional
    public PersonalAreaUnionDTO updateAreaUnion(final long id, final PersonalAreaUnionModifyRequestDTO dto) {
        final PersonalAreaUnion personalAreaUnion = requireEntityService.requireAccountAreaUnion(id,
                EntityPermission.UPDATE);

        personalAreaUnion.setName(dto.getName());

        return personalAreaUnionToDTOTransformer.transform(personalAreaUnion);

    }

    @Transactional
    public void addPartner(final PersonalAreaUnionAddPartnerDTO dto) {
        final PersonalAreaUnion personalAreaUnion = requireEntityService.requireAccountAreaUnion(dto.getAreaUnionId(),
                EntityPermission.UPDATE);
        harvestPermitAreaPartnerService.addPartner(personalAreaUnion.getHarvestPermitArea(), dto.getExternalId());
        personalAreaUnion.forceRevisionUpdate();
    }

    @Transactional
    public void removePartner(final long areaId, final long partnerId) {
        final PersonalAreaUnion personalAreaUnion = requireEntityService.requireAccountAreaUnion(areaId,
                EntityPermission.UPDATE);
        harvestPermitAreaPartnerService.removePartner(personalAreaUnion.getHarvestPermitArea(), partnerId);
        personalAreaUnion.forceRevisionUpdate();
    }

    @Transactional
    public void refreshPartner(final long areaId, final long partnerId) {
        final PersonalAreaUnion personalAreaUnion = requireEntityService.requireAccountAreaUnion(areaId,
                EntityPermission.UPDATE);
        harvestPermitAreaPartnerService.refreshPartner(personalAreaUnion.getHarvestPermitArea(), partnerId);
        personalAreaUnion.forceRevisionUpdate();
    }

    @Transactional(readOnly = true)
    public HarvestPermitArea.StatusCode getStatus(final long areaId) {
        final PersonalAreaUnion personalAreaUnion = requireEntityService.requireAccountAreaUnion(areaId,
                EntityPermission.READ);
        return personalAreaUnion.getHarvestPermitArea().getStatus();
    }

    @Transactional
    public void setReadyForProcessing(final long areaId) {
        final PersonalAreaUnion personalAreaUnion = requireEntityService.requireAccountAreaUnion(areaId,
                EntityPermission.UPDATE);
        personalAreaUnion.getHarvestPermitArea().setStatusPending().ifPresent(harvestPermitAreaEventRepository::save);
    }

    @Transactional
    public void setIncomplete(final long areaId) {
        final PersonalAreaUnion personalAreaUnion = requireEntityService.requireAccountAreaUnion(areaId,
                EntityPermission.UPDATE);
        final HarvestPermitArea harvestPermitArea = personalAreaUnion.getHarvestPermitArea();

        harvestPermitArea.setStatusIncomplete().ifPresent(harvestPermitAreaEventRepository::save);

        final GISZone zone = harvestPermitArea.getZone();
        zone.setGeom(null);
        zone.setExcludedGeom(null);
        zone.setMetsahallitusHirvi(emptySet());
        zone.setComputedAreaSize(0);
        zone.setWaterAreaSize(0);
        zone.setStateLandAreaSize(null);
    }

    @Transactional(readOnly = true)
    public List<OrganisationNameDTO> listAvailableClubs() {
        final Person person = activeUserService.requireActivePerson();
        return fetchAvailableClubs(person);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<OrganisationNameDTO> listAvailableClubs(long personId) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.READ);
        return fetchAvailableClubs(person);
    }

    @Transactional(readOnly = true)
    public PdfData exportPdf(final long id, final Locale locale, final MapPdfParameters dto) {
        final PersonalAreaUnion personalAreaUnion = requireEntityService.requireAccountAreaUnion(id,
                EntityPermission.READ);

        final MapPdfModel model = personalAreaUnionPrintService.getModel(personalAreaUnion, dto.getOverlay(), locale);
        final byte[] pdf = personalAreaUnionPrintService.createPdf(personalAreaUnion, locale, model, dto);
        return new PdfData(pdf, model.getExportFileName());
    }

    private PersonalAreaUnionDTO doCreateAreaUnion(final PersonalAreaUnionCreateRequestDTO dto, final Person person) {
        final PersonalAreaUnion personalAreaUnion = new PersonalAreaUnion();
        personalAreaUnion.setPerson(person);
        personalAreaUnion.setName(dto.getName());
        final HarvestPermitArea harvestPermitArea = new HarvestPermitArea();
        harvestPermitArea.setHuntingYear(dto.getHuntingYear());
        harvestPermitArea.generateAndStoreExternalId(secureRandom);
        harvestPermitArea.setZone(gisZoneRepository.save(new GISZone()));
        personalAreaUnion.setHarvestPermitArea(harvestPermitAreaRepository.save(harvestPermitArea));

        return personalAreaUnionToDTOTransformer.transform(personalAreaUnionRepository.save(personalAreaUnion));
    }

    private List<OrganisationNameDTO> fetchAvailableClubs(final Person person) {
        return occupationRepository.findActiveByPersonAndOrganisationTypes(
                person, EnumSet.of(OrganisationType.CLUB)).stream()
                .map(Occupation::getOrganisation)
                .map(OrganisationNameDTO::create)
                .collect(toList());
    }

    private Slice<PersonalAreaUnion> fetchSlice(final Pageable pageRequest, final int huntingYear,
                                                final Person person) {
        final BooleanExpression predicate = buildPredicate(person, huntingYear);
        final List<PersonalAreaUnion> fetch = jpqlQueryFactory.selectFrom(UNION)
                .innerJoin(UNION.harvestPermitArea, HPA)
                .where(predicate)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize() + 1)
                .orderBy(UNION.id.desc())
                .fetch();
        return BaseRepositoryImpl.toSlice(fetch, pageRequest);
    }

    private List<PersonalAreaUnionBasicDetailsDTO> fetchListReady(final Person person, final int huntingYear) {
        final BooleanExpression predicate =
                buildPredicate(person, huntingYear).and(UNION.harvestPermitArea.status.eq(HarvestPermitArea.StatusCode.READY));

        final List<PersonalAreaUnion> fetch = jpqlQueryFactory.selectFrom(UNION)
                .innerJoin(UNION.harvestPermitArea, HPA)
                .where(predicate)
                .fetch();
        return personalAreaUnionToBasicDetailsDTOTransformer.apply(fetch);
    }

    private BooleanExpression buildPredicate(final Person person, final int huntingYear) {
        return UNION.person.eq(person).and(UNION.harvestPermitArea.huntingYear.eq(huntingYear));
    }


}
