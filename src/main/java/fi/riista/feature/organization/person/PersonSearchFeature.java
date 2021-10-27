package fi.riista.feature.organization.person;

import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.search.PersonSearchResultMapper;
import fi.riista.util.F;
import fi.riista.validation.Validators;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;

@Service
public class PersonSearchFeature {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private PersonLookupService personLookupService;

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public PersonContactInfoDTO findPersonContactInfoByHunterNumber(final String hunterNumber,
                                                                    final boolean isForeignPersonEligible) {
        checkArgument(hasText(hunterNumber), "empty hunterNumber");

        return personLookupService
                .findByHunterNumber(hunterNumber, isForeignPersonEligible)
                .map(PersonContactInfoDTO::create)
                .orElseThrow(() -> PersonNotFoundException.byHunterNumber(hunterNumber));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public List<PersonContactInfoDTO> findPersonContactInfoByHunterNumbers(final List<String> hunterNumbers,
                                                                           final boolean isForeignPersonEligible) {
        checkArgument(!hunterNumbers.isEmpty(), "empty hunterNumbers");

        final Map<String, Person> personsByHunterNumber =
                F.index(personLookupService.findByHunterNumberIn(hunterNumbers, isForeignPersonEligible), Person::getHunterNumber);

        return hunterNumbers.stream()
                .map(number ->
                        ofNullable(personsByHunterNumber.get(number))
                                .map(PersonContactInfoDTO::create)
                                .orElseGet(() -> {
                                    final PersonContactInfoDTO dto = new PersonContactInfoDTO();
                                    dto.setHunterNumber(number);
                                    return dto;
                                }))
                .collect(Collectors.toList());
    }

    @Nonnull
    @Transactional(readOnly = true)
    public PersonWithHunterNumberDTO findNameByHunterNumber(final String hunterNumber) {
        checkArgument(hasText(hunterNumber), "empty hunterNumber");

        return personLookupService
                .findByHunterNumber(hunterNumber, true)
                .map(PersonWithHunterNumberDTO::create)
                .orElseThrow(() -> PersonNotFoundException.byHunterNumber(hunterNumber));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public PersonContactInfoDTO findPersonContactInfoBySsn(final String ssn) {
        checkArgument(hasText(ssn), "empty ssn");

        return personLookupService.findBySsnFallbackVtj(ssn)
                .map(PersonContactInfoDTO::create)
                .orElseThrow(() -> PersonNotFoundException.bySsn(ssn));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public PersonWithHunterNumberDTO findNameAndHunterNumberBySsn(final String ssn) {
        checkArgument(hasText(ssn), "empty ssn");

        return personLookupService.findBySsnFallbackVtj(ssn)
                .map(PersonWithHunterNumberDTO::create)
                .orElseThrow(() -> PersonNotFoundException.bySsn(ssn));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public PersonWithHunterNumberDTO findNameAndHunterNumberByPermitNumber(final String permitNumber) {
        checkArgument(hasText(permitNumber), "empty permitNumber");

        return personLookupService.findPermitOriginalContactPerson(permitNumber)
                .map(PersonWithHunterNumberDTO::create)
                .orElseThrow(() -> new HarvestPermitNotFoundException(permitNumber));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PersonWithHunterNumberDTO> findNameAndHunterNumberOfAllByNameMatch(final String name) {
        final List<Person> persons = personRepository.findAllPersonsByFuzzyFullNameMatch(name, PageRequest.of(0,
                20));

        final PersonSearchResultMapper personMapper = PersonSearchResultMapper.create(LocaleContextHolder.getLocale());

        return persons.stream()
                .map(person -> {
                    final PersonWithHunterNumberDTO dto = PersonWithHunterNumberDTO.create(person);
                    dto.setExtendedName(personMapper.getDescription(person));
                    return dto;
                })
                .collect(toList());
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PersonWithHunterNumberDTO> findPersonsByHunterNumberOrNameFuzzyMatch(final String searchTerm) {
        if (Validators.isValidHunterNumber(searchTerm)) {
            return F.listFromOptional(
                    personRepository.findByHunterNumber(searchTerm)
                            .map(PersonWithHunterNumberDTO::create));
        }
        return findNameAndHunterNumberOfAllByNameMatch(searchTerm);
    }
}
