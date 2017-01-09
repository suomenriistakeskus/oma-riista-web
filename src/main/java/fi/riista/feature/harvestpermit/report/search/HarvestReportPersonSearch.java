package fi.riista.feature.harvestpermit.report.search;

import com.google.common.base.Preconditions;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.search.SiteSearchFeature;
import fi.riista.feature.search.PersonSearchResultMapper;
import fi.riista.util.F;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * Search result contain limited information about the Person.
 * Normal users can only search using the hunterNumber parameter.
 *
 * @throws NotFoundException when no result is found
 */
@Component
public class HarvestReportPersonSearch {

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private PersonRepository personRepository;

    @Nonnull
    @Transactional(readOnly = true)
    public PersonWithHunterNumberDTO findHunterByNumber(String hunterNumber) {
        Preconditions.checkArgument(StringUtils.hasText(hunterNumber), "empty hunterNumber");

        return PersonWithHunterNumberDTO.create(personLookupService.findByHunterNumber(hunterNumber)
                .orElseThrow(NotFoundException::new));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public PersonWithHunterNumberDTO findBySsn(String ssn) {
        Preconditions.checkArgument(StringUtils.hasText(ssn), "empty ssn");

        return PersonWithHunterNumberDTO.create(personLookupService.findBySsnFallbackVtj(ssn)
                .orElseThrow(NotFoundException::new));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public PersonWithHunterNumberDTO findByPermitNumber(String permitNumber) {
        Preconditions.checkArgument(StringUtils.hasText(permitNumber), "empty permitNumber");

        return PersonWithHunterNumberDTO.create(personLookupService.findPermitOriginalContactPerson(permitNumber)
                .orElseThrow(NotFoundException::new));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PersonWithHunterNumberDTO> searchPersons(String name) {
        List<Person> persons = personRepository.findByFuzzyFullNameMatch(
                name, SiteSearchFeature.MAX_FUZZY_DISTANCE_PERSON_NAME,
                new PageRequest(0, SiteSearchFeature.MAX_RESULT_PERSON));

        final PersonSearchResultMapper personMapper = PersonSearchResultMapper.create(LocaleContextHolder.getLocale());
        return F.mapNonNullsToList(persons, person -> {
            PersonWithHunterNumberDTO dto = PersonWithHunterNumberDTO.create(Objects.requireNonNull(person));
            dto.setExtendedName(personMapper.getDescription(person));
            return dto;
        });
    }
}
