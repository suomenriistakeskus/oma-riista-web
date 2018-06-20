package fi.riista.feature.search;

import fi.riista.feature.harvestpermit.HarvestPermit;

import java.util.function.Function;

public class PermitSearchResultMapper implements Function<HarvestPermit, SearchResultsDTO.Result> {

    public static PermitSearchResultMapper create() {
        return new PermitSearchResultMapper();
    }

    @Override
    public SearchResultsDTO.Result apply(HarvestPermit permit) {
        final String description = permit.getPermitNumber() + " " + permit.getPermitType();
        if (permit.isMooselikePermitType()) {
            return SearchResultsDTO.createResult(
                    permit.isAmendmentPermit() ? permit.getOriginalPermit().getId() : permit.getId(),
                    description);
        }
        return SearchResultsDTO.createResult(permit.getId(), description);
    }
}
