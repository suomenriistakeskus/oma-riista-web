package fi.riista.feature.harvestpermit.payment;

import fi.riista.feature.gamediary.GameSpecies;

import java.math.BigDecimal;

public final class MooselikePrice {

    private static final MooselikePrice MOOSE_PRICE =
            new MooselikePrice(new BigDecimal("120.00"), new BigDecimal("50.00"));

    private static final MooselikePrice DEER_PRICE =
            new MooselikePrice(new BigDecimal("17.00"), new BigDecimal("8.00"));

    public static MooselikePrice get(final GameSpecies gameSpecies) {
        return get(gameSpecies.getOfficialCode());
    }

    public static MooselikePrice get(final int gameSpeciesCode) {
        switch (gameSpeciesCode) {
            case GameSpecies.OFFICIAL_CODE_MOOSE:
                return MOOSE_PRICE;
            case GameSpecies.OFFICIAL_CODE_FALLOW_DEER:
            case GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER:
            case GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER:
                return DEER_PRICE;
            default:
                throw new IllegalArgumentException("Unknown gameSpeciesCode: " + gameSpeciesCode);
        }
    }

    private MooselikePrice(final BigDecimal adultPrice,
                           final BigDecimal youngPrice) {
        this.adultPrice = adultPrice;
        this.youngPrice = youngPrice;
    }

    private final BigDecimal adultPrice;
    private final BigDecimal youngPrice;

    public BigDecimal getAdultPrice() {
        return adultPrice;
    }

    public BigDecimal getYoungPrice() {
        return youngPrice;
    }
}
