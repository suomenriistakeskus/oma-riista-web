package fi.riista.feature.organization.occupation.search;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class RkaListFeature {

    @Resource
    private OrganisationRepository organisationRepository;

    @Transactional(readOnly = true)
    public List<RkaListOrganisationDTO> listAreasWithAllRhys(final Locale locale) {
        // Sort areas by officialCode which is natural and consistent ordering
        // criteria for this type of organisations.
        final List<Organisation> areas = organisationRepository.findByOrganisationType(
                OrganisationType.RKA,
                JpaSort.of(Organisation_.officialCode, Organisation_.id));

        final List<Organisation> rhys = organisationRepository.findByOrganisationType(
                OrganisationType.RHY,
                JpaSort.of(Organisation_.officialCode, Organisation_.id));

        return mapToDtos(areas, rhys, locale);
    }

    @Transactional(readOnly = true)
    public List<RkaListOrganisationDTO> listAreasWithActiveRhys(final Locale locale) {
        // Sort areas by officialCode which is natural and consistent ordering
        // criteria for this type of organisations.
        final List<Organisation> areas = organisationRepository.findActiveByOrganisationType(
                OrganisationType.RKA,
                JpaSort.of(Organisation_.officialCode, Organisation_.id));

        List<Organisation> rhys = organisationRepository.findActiveByOrganisationType(
                OrganisationType.RHY,
                JpaSort.of(Organisation_.officialCode, Organisation_.id));

        return mapToDtos(areas, rhys, locale);
    }

    private static List<RkaListOrganisationDTO> mapToDtos(final List<Organisation> areas,
                                                          final List<Organisation> rhys,
                                                          final Locale locale) {
        final Map<Organisation, Set<Organisation>> rkaToRhy = rhys.stream()
                .collect(
                        groupingBy(Organisation::getParentOrganisation,
                                mapping(rhy -> rhy, toSet())));
        return areas.stream().map(rka -> toDTO(rka, locale)
                .withSubOrganisations(mapRhysToDtos(rkaToRhy.getOrDefault(rka, emptySet()), locale))
                .build()).collect(toList());
    }

    private static List<RkaListOrganisationDTO> mapRhysToDtos(final Set<Organisation> rhys, final Locale locale) {
        return rhys.stream()
                .map(rhy -> toDTO(rhy, locale).build())
                .sorted(comparing(RkaListOrganisationDTO::getName))
                .collect(Collectors.toList());
    }

    private static RkaListOrganisationDTO.Builder toDTO(final Organisation organisation, final Locale locale) {
        return RkaListOrganisationDTO.Builder.builder()
                .withName(organisation.getNameLocalisation().getAnyTranslation(locale))
                .withOfficialCode(organisation.getOfficialCode())
                .withOrganisationType(organisation.getOrganisationType());
    }
}
