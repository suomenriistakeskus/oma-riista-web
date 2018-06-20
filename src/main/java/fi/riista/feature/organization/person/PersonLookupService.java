package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.integration.vtj.VtjSearchService;
import fi.riista.validation.Validators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Optional;

@Service
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

    private boolean handleResult(final Person person) {
        return !person.isDeceased() && (activeUserService.isModeratorOrAdmin() || !person.isArtificialPerson());
    }

    @Transactional(readOnly = true)
    public Optional<Person> findById(final Long personId) {
        return Optional.of(personRepository.getOne(personId))
                .filter(this::handleResult);
    }

    @Transactional(readOnly = true)
    public Optional<Person> findBySsnNoFallback(final String ssn) {
        return findBySsn(ssn, false);
    }

    @Transactional(readOnly = true)
    public Optional<Person> findBySsnFallbackVtj(final String ssn) {
        return findBySsn(ssn, true);
    }

    private Optional<Person> findBySsn(final String ssn, final boolean fallbackToVtj) {
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
                .filter(this::handleResult);
    }

    @Transactional(readOnly = true)
    public Optional<Person> findByHunterNumber(final String hunterNumber) {
        return Optional.ofNullable(hunterNumber)
                .filter(StringUtils::hasText)
                .filter(Validators::isValidHunterNumber)
                .flatMap(personRepository::findByHunterNumber)
                .filter(this::handleResult);
    }

    @Transactional(readOnly = true)
    public Optional<Person> findPermitOriginalContactPerson(final String permitNumber) {
        return Optional.ofNullable(harvestPermitRepository.findByPermitNumber(permitNumber))
                .map(HarvestPermit::getOriginalContactPerson)
                .filter(this::handleResult);
    }

    @Transactional(readOnly = true)
    public Optional<Person> findPerson(final PersonWithHunterNumberDTO dto) {
        if (dto == null) {
            return Optional.empty();
        }

        final String hunterNumber = dto.getHunterNumber();
        final Long personId = dto.getId();

        if (StringUtils.hasText(hunterNumber)) {
            return findByHunterNumber(hunterNumber)
                    .map(Optional::of)
                    .orElseThrow(() -> new PersonNotFoundException(hunterNumber));

        } else if (personId != null) {
            // Usually the actor has a hunter number, unless you are a non-hunter adding a diary entry to yourself
            return findById(personId)
                    .map(Optional::of)
                    .orElseThrow(() -> new PersonNotFoundException(personId));
        }

        return Optional.empty();
    }
}
