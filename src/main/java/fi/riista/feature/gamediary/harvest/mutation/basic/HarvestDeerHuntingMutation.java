package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOBase;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestPermitNotApplicableForDeerHuntingException;

public class HarvestDeerHuntingMutation implements HarvestMutation {

    private final DeerHuntingType deerHuntingType;
    private final String deerHuntingOtherTypeDescription;
    private final HarvestSpecVersion version;

    public HarvestDeerHuntingMutation(final HarvestDTOBase dto) {
        this.deerHuntingType = dto.getDeerHuntingType();
        this.deerHuntingOtherTypeDescription = deerHuntingType == DeerHuntingType.OTHER ?
                dto.getDeerHuntingOtherTypeDescription() : null;
        this.version = dto.getHarvestSpecVersion();
        assertInfoNotPresentWithPermit(dto.getPermitNumber() != null);
        assertValuesSetOnlyForWhiteTailedDeer(dto);
    }

    @Override
    public void accept(final Harvest harvest) {
        if (version.supportsDeerHuntingType()) {
            harvest.setDeerHuntingType(deerHuntingType);
            harvest.setDeerHuntingOtherTypeDescription(deerHuntingOtherTypeDescription);
        }
    }

    private void assertInfoNotPresentWithPermit(final boolean harvestPermitPresent) {
        if (deerHuntingType != null && harvestPermitPresent) {
            throw new HarvestPermitNotApplicableForDeerHuntingException();
        }
    }

    private void assertValuesSetOnlyForWhiteTailedDeer(final HarvestDTOBase dto) {
        if (GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER != dto.getGameSpeciesCode() &&
                deerHuntingType != null) {
            throw new IllegalArgumentException("Deer hunting type can only be set for white tailed deer");
        }
    }
}
