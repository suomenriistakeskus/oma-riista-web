package fi.riista.feature.pub.occupation;


import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.feature.gis.organization.GISOrganisationRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationGroupType;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.pub.PublicDTOFactory;
import fi.riista.feature.pub.rhy.RhyWithRkaResultDTO;
import fi.riista.util.F;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.JoinType;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.util.Collect.idSet;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

@Component
public class PublicOccupationSearchFeature {

    public static final int MAX_RESULTS = 500;

    private static final QOccupation OCCUPATION = QOccupation.occupation;
    private static final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;
    private static final QOrganisation ORG = QOrganisation.organisation;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private PublicDTOFactory dtoFactory;

    @Resource
    private GISOrganisationRepository gisOrganisationRepository;

    @Transactional(readOnly = true)
    public PublicOrganisationDTO getRiistakeskus() {
        // Sort order is not critical since only one result is expected from the query.
        final List<Organisation> rk =
                organisationRepository.findByOrganisationType(OrganisationType.RK, JpaSort.of(Organisation_.id));

        if (rk.size() != 1) {
            throw new IllegalStateException("Invariant violation: There must be only one Riistakeskus organisation");
        }

        return toDTO(rk.iterator().next(), gisOrganisationRepository.getRKAAndRHYWGS84Locations());
    }

    @Transactional(readOnly = true)
    public RhyWithRkaResultDTO getRkaAndRhyByWGS84Location(final GISWGS84Point point) {


        return gisOrganisationRepository.getOfficialCodeForRhyByWGS84Location(point)
                .map(officialCode -> {
                    final Organisation rhy =
                            organisationRepository.findByTypeAndOfficialCode(OrganisationType.RHY, officialCode);
                    final Organisation rka = rhy.getParentOrganisation();
                    return RhyWithRkaResultDTO.create(rhy.getOfficialCode(), rka.getOfficialCode());
                })
                .orElse(RhyWithRkaResultDTO.EMPTY_RESULT);

    }

    @Transactional(readOnly = true)
    public PublicOrganisationDTO getByTypeAndOfficialCode(final OrganisationType organisationType,
                                                          final String officialCode) {
        return toDTO(organisationRepository.findByTypeAndOfficialCode(organisationType, officialCode),
                gisOrganisationRepository.getRKAAndRHYWGS84Locations());
    }

    private PublicOrganisationDTO toDTO(final Organisation org, final Map<Long, GISWGS84Point> locationMap) {
        if (org == null) {
            throw new NotFoundException();
        }

        final List<Organisation> organisations = Streams.concat(
                Stream.of(org),
                org.getSubOrganisations().stream())
                .collect(toList());

        return dtoFactory.createOrganisationWithSubOrganisations(org, locationMap, occupationRepository.listCoordinators(organisations));
    }

    @Transactional(readOnly = true)
    public PublicOccupationsAndOrganisationsDTO findOccupationsAndOrganisations(
            final PublicOccupationSearchParameters parameters) {

        return findOccupationsAndOrganisations(parameters, MAX_RESULTS);
    }

    // For testing
    @Transactional(readOnly = true)
    public PublicOccupationsAndOrganisationsDTO findOccupationsAndOrganisations(
            final PublicOccupationSearchParameters parameters, final int maxResults) {

        Preconditions.checkArgument(parameters.getPageSize() == null || parameters.getPageSize() <= maxResults,
                "Requested page size must not exceed " + maxResults);

        // If paging is not requested, set page size larger than MAX_RESULT in order to catch too many results case.
        final Integer searchPageSize = Optional.ofNullable(parameters.getPageSize()).orElse(MAX_RESULTS + 1);
        final Integer pageNumber = Optional.ofNullable(parameters.getPageNumber()).orElse(0);


        final List<Occupation> result = jpqlQueryFactory
                .select(OCCUPATION)
                .from(OCCUPATION)
                .join(OCCUPATION.organisation, ORG)
                .leftJoin(ORG.parentOrganisation, RKA._super)
                .where(parameters.toQueryDslPredicate())
                .limit(searchPageSize + 1)
                .offset(pageNumber * searchPageSize)
                .orderBy(
                        createOrdinalSortExpressionForOccupationType().asc(),
                        OCCUPATION.organisation.id.asc(),
                        OCCUPATION.callOrder.asc().nullsLast(),
                        OCCUPATION.id.asc())
                .fetch();

        boolean lastPage = true;
        if (result.size() > searchPageSize) {
            lastPage = false;
            result.remove(result.size() - 1);
        }

        if (result.isEmpty()) {
            return PublicOccupationsAndOrganisationsDTO.EMPTY_RESULT;
        } else if (result.size() > maxResults) {
            return PublicOccupationsAndOrganisationsDTO.TOO_MANY_RESULTS;
        }


        final Set<Long> organisationIds = result.stream().map(Occupation::getOrganisation).collect(idSet());
        final List<Organisation> resultOrganisations = organisationRepository.findAll(Specification
                .where(inCollection(Organisation_.id, organisationIds))
                .and(fetch(Organisation_.address, JoinType.LEFT)));

        final List<PublicOccupationDTO> occupations = toOccupationDTOs(result);
        return new PublicOccupationsAndOrganisationsDTO(
                lastPage,
                occupations,
                toOrganisationDTOs(resultOrganisations));
    }

    private List<PublicOccupationDTO> toOccupationDTOs(final List<Occupation> occupations) {
        return occupations.stream()
                .map(occupation -> {
                    final PublicOccupationTypeDTO dto = dtoFactory.create(
                            occupation.getOccupationType(), occupation.getOrganisation().getOrganisationType());
                    final PublicOccupationBoardRepresentationDTO boardRepresentation =
                            dtoFactory.create(occupation.getBoardRepresentation());
                    return PublicDTOFactory.createOrganisationWithSubOrganisations(occupation, dto, boardRepresentation);
                })
                .collect(toList());
    }

    private List<PublicOrganisationDTO> toOrganisationDTOs(final List<Organisation> resultOrganisations) {
        final Map<Organisation, Occupation> rhyToCoordinator = occupationRepository.listCoordinators(resultOrganisations);

        return F.mapNonNullsToList(resultOrganisations, org -> dtoFactory.createWithoutSuborganisations(org, emptyMap(), rhyToCoordinator));
    }

    public List<PublicOccupationGroupTypeDTO> getAllOccupationGroupTypes() {
        // Use EnumSet for correct ordering
        final List<PublicOccupationGroupTypeDTO> allGroupOccupations =
                EnumSet.of(OrganisationType.RHY, OrganisationType.RK, OrganisationType.VRN, OrganisationType.ARN, OrganisationType.RKA).stream()
                        .flatMap(orgType -> OccupationGroupType.getApplicableTypes(orgType).stream()
                                .map(groupType -> dtoFactory.createGroupType(groupType, orgType)))
                        .collect(toList());

        return allGroupOccupations;
    }

    private static NumberExpression<Integer> createOrdinalSortExpressionForOccupationType() {

        CaseBuilder.Cases<Integer, NumberExpression<Integer>> expression = null;

        for (OccupationType t : OccupationType.values()) {
            if (expression == null) {
                expression = new CaseBuilder().when(OCCUPATION.occupationType.eq(t)).then(t.ordinal());
            } else {
                expression = expression.when(OCCUPATION.occupationType.eq(t)).then(t.ordinal());
            }
        }
        return expression.otherwise(Integer.MAX_VALUE);
    }
}
