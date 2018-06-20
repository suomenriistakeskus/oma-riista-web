package fi.riista.feature.organization.person;

import com.google.common.base.Preconditions;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.search.PersonSearchResultMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class PersonSearchFeature {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private PersonLookupService personLookupService;

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public PersonContactInfoDTO findPersonContactInfoByHunterNumber(final String hunterNumber) {
        Preconditions.checkArgument(StringUtils.hasText(hunterNumber), "empty hunterNumber");

        return personLookupService.findByHunterNumber(hunterNumber)
                .map(PersonContactInfoDTO::create)
                .orElseThrow(NotFoundException::new);
    }

    @Nonnull
    @Transactional(readOnly = true)
    public PersonWithHunterNumberDTO findNameByHunterNumber(final String hunterNumber) {
        Preconditions.checkArgument(StringUtils.hasText(hunterNumber), "empty hunterNumber");

        return personLookupService.findByHunterNumber(hunterNumber)
                .map(PersonWithHunterNumberDTO::create)
                .orElseThrow(NotFoundException::new);
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public PersonContactInfoDTO findPersonContactInfoBySsn(final String ssn) {
        Preconditions.checkArgument(StringUtils.hasText(ssn), "empty ssn");

        return personLookupService.findBySsnFallbackVtj(ssn)
                .map(PersonContactInfoDTO::create)
                .orElseThrow(NotFoundException::new);
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public PersonWithHunterNumberDTO findNameAndHunterNumberBySsn(final String ssn) {
        Preconditions.checkArgument(StringUtils.hasText(ssn), "empty ssn");

        return personLookupService.findBySsnFallbackVtj(ssn)
                .map(PersonWithHunterNumberDTO::create)
                .orElseThrow(NotFoundException::new);
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public PersonWithHunterNumberDTO findNameAndHunterNumberByPermitNumber(final String permitNumber) {
        Preconditions.checkArgument(StringUtils.hasText(permitNumber), "empty permitNumber");

        return personLookupService.findPermitOriginalContactPerson(permitNumber)
                .map(PersonWithHunterNumberDTO::create)
                .orElseThrow(NotFoundException::new);
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<PersonWithHunterNumberDTO> findNameAndHunterNumberOfAllByNameMatch(final String name) {
        final List<Person> persons = personRepository.findByFuzzyFullNameMatch(name, new PageRequest(0, 20));

        final PersonSearchResultMapper personMapper = PersonSearchResultMapper.create(LocaleContextHolder.getLocale());

        return persons.stream()
                .map(person -> {
                    final PersonWithHunterNumberDTO dto = PersonWithHunterNumberDTO.create(person);
                    dto.setExtendedName(personMapper.getDescription(person));
                    return dto;
                })
                .collect(toList());
    }
}
