package fi.riista.feature.organization.person;

import com.google.common.base.Preconditions;
import fi.riista.feature.error.NotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class PersonSearchFeature {

    @Resource
    private PersonLookupService personLookupService;

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public PersonDTO findHunterByNumber(final String hunterNumber) {
        Preconditions.checkArgument(StringUtils.hasText(hunterNumber), "empty hunterNumber");

        return PersonDTO.create(personLookupService.findByHunterNumber(hunterNumber)
                .orElseThrow(NotFoundException::new));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public PersonDTO findBySsn(final String ssn) {
        Preconditions.checkArgument(StringUtils.hasText(ssn), "empty ssn");

        return PersonDTO.create(personLookupService.findBySsnFallbackVtj(ssn)
                .orElseThrow(NotFoundException::new));
    }
}
