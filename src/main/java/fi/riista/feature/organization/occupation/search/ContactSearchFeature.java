package fi.riista.feature.organization.occupation.search;

import com.google.common.collect.Lists;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.Occupation_;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaPreds;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;

@Component
public class ContactSearchFeature {

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<OccupationContactSearchResultDTO> searchOccupations(List<OccupationContactSearchDTO> search, Locale userLocale) {
        return toDTOs(occupationRepository.findAll(createSpecification(search)), userLocale);
    }

    private static Specification<Occupation> createSpecification(final List<OccupationContactSearchDTO> search) {
        return (root, query, cb) -> cb.or(toArray(Lists.transform(search, dto -> createPredicate(root, cb, dto))));
    }

    private static Predicate[] toArray(List<Predicate> predicates) {
        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private static Predicate createPredicate(Root<Occupation> root, CriteriaBuilder cb, OccupationContactSearchDTO dto) {
        final List<Predicate> predicates = Lists.newArrayList();

        final Join<Occupation, Organisation> organisationJoin = root.join(Occupation_.organisation, JoinType.LEFT);

        predicates.add(validNow(root, cb));
        predicates.add(byOrgType(cb, organisationJoin, dto.getOrganisationType()));
        if (dto.getAreaCode() != null) {
            predicates.add(byArea(cb, organisationJoin, dto.getAreaCode()));
        }
        if (dto.getRhyCode() != null) {
            predicates.add(cb.equal(organisationJoin.get(Organisation_.officialCode), dto.getRhyCode()));
        }
        if (dto.getOccupationType() != null) {
            predicates.add(cb.equal(root.get(Occupation_.occupationType), dto.getOccupationType()));
        }

        return cb.and(toArray(predicates));
    }

    private static Predicate validNow(Root<Occupation> root, CriteriaBuilder cb) {
        return JpaPreds.withinInterval(
                cb, root.get(Occupation_.beginDate), root.get(Occupation_.endDate), DateUtil.today());
    }

    private static Predicate byOrgType(CriteriaBuilder cb, Join<?, Organisation> organisationJoin, OrganisationType orgType) {
        return cb.equal(organisationJoin.get(Organisation_.organisationType), orgType);
    }

    private static Predicate byArea(CriteriaBuilder cb, Join<?, Organisation> organisationJoin, String areaCode) {
        final Join<Organisation, Organisation> parentOrganisationtoJoin = organisationJoin.join(Organisation_.parentOrganisation, JoinType.LEFT);
        final Predicate parentOfficialCode = cb.equal(parentOrganisationtoJoin.get(Organisation_.officialCode), areaCode);
        final Predicate parentType = cb.equal(parentOrganisationtoJoin.get(Organisation_.organisationType), OrganisationType.RKA);
        return cb.and(parentOfficialCode, parentType);
    }

    private List<OccupationContactSearchResultDTO> toDTOs(final List<Occupation> occupations, final Locale userLocale) {
        return occupations.stream()
                .map(occupation -> OccupationContactSearchResultDTO.create(occupation, userLocale, messageSource))
                .sorted(comparing(OccupationContactSearchResultDTO::getOccupationName, nullsLast(naturalOrder()))
                        .thenComparing(OccupationContactSearchResultDTO::getLastName, nullsLast(naturalOrder())))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<RhyContactSearchResultDTO> searchRhy(List<RhyContactSearchDTO> search, Locale userLocale) {
        return toRhyDTOs(organisationRepository.findAll(createRhySpecification(search)), userLocale);
    }

    private static Specification<Organisation> createRhySpecification(final List<RhyContactSearchDTO> search) {
        return (root, query, cb) -> cb.or(toArray(Lists.transform(search, dto -> createPredicate(root, cb, dto))));
    }

    private static Predicate createPredicate(Root<Organisation> root, CriteriaBuilder cb, RhyContactSearchDTO dto) {
        final List<Predicate> predicates = Lists.newArrayList();

        predicates.add(cb.equal(root.get(Organisation_.organisationType), OrganisationType.RHY));

        if (dto.getAreaCode() != null) {
            Join<Organisation, Organisation> parentOrganisationtoJoin = root.join(Organisation_.parentOrganisation, JoinType.LEFT);
            predicates.add(cb.equal(parentOrganisationtoJoin.get(Organisation_.officialCode), dto.getAreaCode()));
        }
        if (dto.getRhyCode() != null) {
            predicates.add(cb.equal(root.get(Organisation_.officialCode), dto.getRhyCode()));
        }

        return cb.and(toArray(predicates));
    }

    private List<RhyContactSearchResultDTO> toRhyDTOs(final List<Organisation> occupations, final Locale userLocale) {
        final Map<Organisation, Occupation> rhyToCoordinator = occupationRepository.listCoordinators(occupations);
        return occupations.stream()
                .map(rhy -> RhyContactSearchResultDTO.create(rhy, rhyToCoordinator.get(rhy), userLocale))
                .sorted(comparing(RhyContactSearchResultDTO::getRhyName))
                .collect(toList());
    }
}
