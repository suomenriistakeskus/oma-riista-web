package fi.riista.feature.harvestpermit.statistics;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;

import java.util.Collection;

import static fi.riista.util.NumberUtils.percentRatio;
import static fi.riista.util.NumberUtils.sum;

public class MoosePermitStatisticsAmountDTO {
    private static float getRestrictionAmount(final HarvestPermitSpeciesAmount spa,
                                              final HarvestPermitSpeciesAmount.RestrictionType restrictionType) {
        return spa.getRestrictionAmount() != null && spa.getRestrictionType() == restrictionType ? spa.getRestrictionAmount() : 0;
    }

    public static MoosePermitStatisticsAmountDTO create(final HarvestPermitSpeciesAmount originalSpeciesAmount,
                                                        final float amendmentPermitAmount,
                                                        final float applicationPermitAmount) {
        final double restrictionAdultAmount = getRestrictionAmount(originalSpeciesAmount, HarvestPermitSpeciesAmount.RestrictionType.AE);
        final double restrictionAdultMaleAmount = getRestrictionAmount(originalSpeciesAmount, HarvestPermitSpeciesAmount.RestrictionType.AU);

        return new MoosePermitStatisticsAmountDTO(originalSpeciesAmount.getAmount(),
                amendmentPermitAmount,
                applicationPermitAmount,
                restrictionAdultAmount,
                restrictionAdultMaleAmount,
                originalSpeciesAmount.isMooselikeHuntingFinished());
    }

    public static MoosePermitStatisticsAmountDTO createTotal(final Collection<MoosePermitStatisticsAmountDTO> amounts) {
        final double originalAmount = sum(amounts, MoosePermitStatisticsAmountDTO::getOriginal);
        final double amendmentAmount = sum(amounts, MoosePermitStatisticsAmountDTO::getAmendment);
        final double applicationAmount = sum(amounts, MoosePermitStatisticsAmountDTO::getApplication);
        final double restrictionAdultAmount = sum(amounts, MoosePermitStatisticsAmountDTO::getRestrictionAdult);
        final double restrictionAdultMaleAmount = sum(amounts, MoosePermitStatisticsAmountDTO::getRestrictionAdultMale);
        final boolean mooselikeHuntingFinished = amounts.stream().allMatch(MoosePermitStatisticsAmountDTO::isMooselikeHuntingFinished);

        return new MoosePermitStatisticsAmountDTO(originalAmount, amendmentAmount, applicationAmount,
                restrictionAdultAmount, restrictionAdultMaleAmount, mooselikeHuntingFinished);
    }

    private final double original;
    private final double amendment;
    private final double application;
    private final double restrictionAdult;
    private final double restrictionAdultMale;
    private final boolean mooselikeHuntingFinished;

    private MoosePermitStatisticsAmountDTO(final double original,
                                           final double amendment,
                                           final double application,
                                           final double restrictionAdult,
                                           final double restrictionAdultMale,
                                           final boolean mooselikeHuntingFinished) {
        this.original = original;
        this.amendment = amendment;
        this.application = application;
        this.restrictionAdult = restrictionAdult;
        this.restrictionAdultMale = restrictionAdultMale;
        this.mooselikeHuntingFinished = mooselikeHuntingFinished;
    }

    @JsonGetter
    public double getTotal() {
        return original + amendment;
    }

    @JsonGetter
    public double getRestrictedYoungPercentage() {
        // Rajoitusehdosta (M) johtuva vasa %
        // K = Myönnetty pyyntilupia yhteensä
        // M = Rajoitusehto aikuisia enintään
        // R = ((K - M) * 2) / (((K - M) * 2) + M)
        // Eli kokonaislupamäärä (K) vähennettynä rajoitetulla aikuissaaliilla (M) tuottaa luvat, jotka voidaan käyttää vasoihin.
        // Koska yhdellä luvalla saa kaataa kaksi vasaa, niin kerrotaan kahdella eli saadaan kaadettavien vasojen lukumäärä eli (K-M)*2.
        // Tuo jaetaan koko saaliilla eli yhteenlasketulla vasojen ja aikuisten lukumäärällä (((K - M) * 2) + M).
        final double K = getTotal();
        final double M = getRestrictionAdult();
        final double V = (K - M) * 2;

        return M > 0 ? percentRatio(V, V + M) : 0;
    }

    public double getOriginal() {
        return original;
    }

    public double getAmendment() {
        return amendment;
    }

    public double getApplication() {
        return application;
    }

    public double getRestrictionAdult() {
        return restrictionAdult;
    }

    public double getRestrictionAdultMale() {
        return restrictionAdultMale;
    }

    public boolean isMooselikeHuntingFinished() {
        return mooselikeHuntingFinished;
    }
}
