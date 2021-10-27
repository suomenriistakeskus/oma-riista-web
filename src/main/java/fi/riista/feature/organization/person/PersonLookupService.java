package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.integration.vtj.VtjSearchService;
import fi.riista.validation.Validators;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
@Transactional(propagation = Propagation.MANDATORY)
public class PersonLookupService {

    @Resource
    private VtjSearchService vtjSearchService;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private ActiveUserService activeUserService;

    private boolean validatePerson(final Person person, final boolean isForeignPersonEligible) {
        // Deceased persons should not be found.
        if (!person.isDeceased()) {

            // Artificial persons should not be found unless active user is admin/moderator.
            if (!person.isArtificialPerson() || activeUserService.isModeratorOrAdmin()) {

                if (!isForeignPersonEligible && person.isForeignPerson()) {
                    throw PersonNotFoundException.foreignPersonNotEligible(person);
                }

                return true;
            }
        }

        return false;
    }

    @Transactional(readOnly = true)
    public Optional<Person> findById(final Long personId, final boolean isForeignPersonEligible) {
        return Optional
                .of(personRepository.getOne(personId))
                .filter(person -> validatePerson(person, isForeignPersonEligible));
    }

    @Transactional(readOnly = true)
    public Optional<Person> findBySsnNoFallback(@Nullable final String ssn) {
        return findBySsn(ssn, false);
    }

    @Transactional(readOnly = true)
    public Optional<Person> findBySsnFallbackVtj(@Nullable final String ssn) {
        return findBySsn(ssn, true);
    }

    private Optional<Person> findBySsn(@Nullable final String ssn, final boolean fallbackToVtj) {
        return Optional.ofNullable(ssn)
                .filter(StringUtils::hasText)
                .map(String::toUpperCase)
                .filter(Validators::isValidSsn)
                .map(validSsn -> {
                    return personRepository.findBySsn(validSsn).orElseGet(() -> {
                        if (!fallbackToVtj) {
                            return null;
                        }

                        // Fallback to VTJ search if person lookup by SSN from local repository yields nothing.
                        return vtjSearchService.searchAndAdd(validSsn)
                                .map(personRepository::getOne)
                                .orElse(null);
                    });
                })
                // Foreign persons are not given SSN, hence second parameter is set to false as
                // an additional data integrity check that could reveal potential bugs elsewhere.
                .filter(person -> validatePerson(person, false));
    }

    @Transactional(readOnly = true)
    public Optional<Person> findByHunterNumber(@Nullable final String hunterNumber,
                                               final boolean isForeignPersonEligible) {

        return Optional.ofNullable(hunterNumber)
                .filter(StringUtils::hasText)
                .filter(Validators::isValidHunterNumber)
                .flatMap(personRepository::findByHunterNumber)
                .filter(person -> validatePerson(person, isForeignPersonEligible));
    }

    @Transactional(readOnly = true)
    public List<Person> findByHunterNumberIn(@Nonnull final List<String> hunterNumbers,
                                             final boolean isForeignPersonEligible) {
        requireNonNull(hunterNumbers);

        return personRepository.findAllByHunterNumberIn(hunterNumbers).stream()
                .filter(person -> validatePerson(person, isForeignPersonEligible))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Person> findPermitOriginalContactPerson(final String permitNumber) {
        return Optional
                .ofNullable(harvestPermitRepository.findByPermitNumber(permitNumber))
                .map(HarvestPermit::getOriginalContactPerson)
                .filter(person -> validatePerson(person, true));
    }

    @Transactional(readOnly = true)
    public Optional<Person> findPerson(@Nullable final PersonWithHunterNumberDTO dto,
                                       final boolean isForeignPersonElibile) {
        if (dto != null) {
            final String hunterNumber = dto.getHunterNumber();

            if (StringUtils.hasText(hunterNumber)) {
                return findByHunterNumber(hunterNumber, isForeignPersonElibile)
                        .map(Optional::of)
                        .orElseThrow(() -> PersonNotFoundException.byHunterNumber(hunterNumber));
            }

            final Long personId = dto.getId();

            if (personId != null) {
                // Usually the actor has a hunter number, unless you are a non-hunter adding a diary entry to yourself
                return findById(personId, isForeignPersonElibile)
                        .map(Optional::of)
                        .orElseThrow(() -> PersonNotFoundException.byPersonId(personId));
            }
        }

        return Optional.empty();
    }
}
