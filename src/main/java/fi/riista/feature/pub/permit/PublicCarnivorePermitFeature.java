package fi.riista.feature.pub.permit;

import fi.riista.api.pub.PublicCarnivorePermitDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;


@Service
public class PublicCarnivorePermitFeature {

    private static final int MAX_PAGE_SIZE = 1000;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Transactional(readOnly = true)
    public Slice<PublicCarnivorePermitDTO> getPageNoAuthorization(
            final String permitNumber, final Integer speciesCode, final Integer calendarYear,
            final String rkaCode, final Pageable pageRequest) {
        assertValidPermitNumberOrNull(permitNumber);
        assertCarnivoreOrNull(speciesCode);
        assertPageRequest(pageRequest);

        return harvestPermitRepository.findCarnivorePermits(
                permitNumber, speciesCode, calendarYear, rkaCode, pageRequest);
    }

    private static void assertPageRequest(final Pageable pageRequest) {
        requireNonNull(pageRequest);
        final int pageSize = pageRequest.getPageSize();

        checkArgument(pageRequest.getPageNumber() >= 0, "Negative page number is illegal.");
        checkArgument(pageSize > 0, "Page size must be larger than zero.");
        checkArgument(pageSize <= MAX_PAGE_SIZE,
                "Page size exceeds maximum value of " + MAX_PAGE_SIZE);
    }

    private static void assertValidPermitNumberOrNull(final String permitNumber) {
        if (permitNumber != null) {
            checkArgument(FinnishHuntingPermitNumberValidator.validate(permitNumber, true));
        }
    }

    private static void assertCarnivoreOrNull(final Integer speciesCode) {
        checkArgument(speciesCode == null || GameSpecies.isLargeCarnivore(speciesCode));

    }
}
