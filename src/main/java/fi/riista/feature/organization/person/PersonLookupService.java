package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.integration.vtj.VtjSearchService;
import fi.riista.validation.FinnishHunterNumberValidator;
import fi.riista.validation.FinnishSocialSecurityNumberValidator;
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
                .filter(FinnishSocialSecurityNumberValidator::isValid)
                .map(validSsn -> {
                    final Optional<Person> localPerson = personRepository.findBySsn(validSsn);

                    if (localPerson.isPresent()) {
                        return localPerson.get();
                    }

                    if (!fallbackToVtj) {
                        return null;
                    }

                    // Fallback to VTJ search if person lookup by SSN from local repository yields nothing.
                    return vtjSearchService.searchAndAdd(validSsn)
                            .map(personRepository::getOne)
                            .orElse(null);
                })
                .filter(this::handleResult);
    }

    @Transactional(readOnly = true)
    public Optional<Person> findByHunterNumber(final String hunterNumber) {
        return Optional.ofNullable(hunterNumber)
                .filter(StringUtils::hasText)
                .filter(FinnishHunterNumberValidator::isValid)
                .flatMap(personRepository::findByHunterNumber)
                .filter(this::handleResult);
    }

    @Transactional(readOnly = true)
    public Optional<Person> findPermitOriginalContactPerson(final String permitNumber) {
        return Optional.ofNullable(harvestPermitRepository.findByPermitNumber(permitNumber))
                .map(HarvestPermit::getOriginalContactPerson)
                .filter(this::handleResult);
    }
}
