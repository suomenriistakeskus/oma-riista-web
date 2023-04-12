package fi.riista.feature.organization.rka;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.F;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RKA;
import static fi.riista.feature.organization.occupation.OccupationType.ALUEKOKOUKSEN_EDUSTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.ALUEKOKOUKSEN_VARAEDUSTAJA;

@Service
public class AreaMeetingRepresentativeExcelFeature {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private MessageSource messageSource;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public AreaMeetingRepresentativeExcelView export(final long areaId, final Locale locale) {
        final Organisation rka = organisationRepository.getOne(areaId);
        Preconditions.checkArgument(rka.getOrganisationType() == RKA);

        final List<Organisation> rhys = organisationRepository.findActiveByParentOrganisationAndOrganisationType(
                rka, RHY, JpaSort.of(Organisation_.officialCode, Organisation_.id));

        final List<Occupation> occupations =
                occupationRepository.findActiveByOrganisationsAndTypes(
                        F.getUniqueIds(rhys),
                        ImmutableSet.of(ALUEKOKOUKSEN_EDUSTAJA, ALUEKOKOUKSEN_VARAEDUSTAJA));

        final Map<Long, Person> personMap = F.indexById(personRepository.findAllById(occupations.stream()
                .map(Occupation::getPerson)
                .map(Person::getId)
                .collect(Collectors.toList())));

        final Map<Long, List<RepresentativePersonDTO>> representativesByRhyId = filterAndMap(occupations, personMap, ALUEKOKOUKSEN_EDUSTAJA);
        final Map<Long, List<RepresentativePersonDTO>> sustitutesByRhyId = filterAndMap(occupations, personMap, ALUEKOKOUKSEN_VARAEDUSTAJA);

        final List<AreaMeetingRhyRepresentativesDTO> representativeDTOS = rhys.stream()
                .map(rhy -> new AreaMeetingRhyRepresentativesDTO(
                        OrganisationNameDTO.createWithOfficialCode(rhy),
                        representativesByRhyId.get(rhy.getId()),
                        sustitutesByRhyId.get(rhy.getId())))
                .collect(Collectors.toList());

        return new AreaMeetingRepresentativeExcelView(
                new EnumLocaliser(messageSource, locale),
                OrganisationNameDTO.create(rka),
                representativeDTOS);
    }

    private Map<Long, List<RepresentativePersonDTO>> filterAndMap(final List<Occupation> occupations,
                                                        final Map<Long, Person> personMap,
                                                        final OccupationType occupationType) {
        return occupations.stream()
                .filter(o -> o.getOccupationType().equals(occupationType))
                .map(occ -> RepresentativePersonDTO.create(occ.getOrganisation().getId(), personMap.get(occ.getPerson().getId())))
                .collect(Collectors.groupingBy(RepresentativePersonDTO::getOrganisationId));

    }
}
