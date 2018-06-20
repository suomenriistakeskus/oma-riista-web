package fi.riista.feature.huntingclub.permit;

import io.vavr.Tuple;
import io.vavr.Tuple6;

import javax.validation.constraints.AssertTrue;

public interface HasHarvestCountsForPermit {

    public int getNumberOfAdultMales();

    public int getNumberOfAdultFemales();

    public int getNumberOfYoungMales();

    public int getNumberOfYoungFemales();

    public int getNumberOfNonEdibleAdults();

    public int getNumberOfNonEdibleYoungs();

    @AssertTrue
    default boolean isValid() {
        return getNumberOfNonEdibleAdults() <= getNumberOfAdultMales() + getNumberOfAdultFemales() &&
                getNumberOfNonEdibleYoungs() <= getNumberOfYoungMales() + getNumberOfYoungFemales();
    }

    default Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> asTuple() {
        return Tuple.of(getNumberOfAdultMales(), getNumberOfAdultFemales(), getNumberOfYoungMales(),
                getNumberOfYoungFemales(), getNumberOfNonEdibleAdults(), getNumberOfNonEdibleYoungs());
    }

    static HasHarvestCountsForPermit of(final int numberOfAdultMales, final int numberOfAdultFemales,
            final int numberOfYoungMales, final int numberOfYoungFemales, final int numberOfNonEdibleAdults,
            final int numberOfNonEdibleYoungs) {

        return new HasHarvestCountsForPermit() {
            @Override
            public int getNumberOfAdultMales() {
                return numberOfAdultMales;
            }

            @Override
            public int getNumberOfAdultFemales() {
                return numberOfAdultFemales;
            }

            @Override
            public int getNumberOfYoungMales() {
                return numberOfYoungMales;
            }

            @Override
            public int getNumberOfYoungFemales() {
                return numberOfYoungFemales;
            }

            @Override
            public int getNumberOfNonEdibleAdults() {
                return numberOfNonEdibleAdults;
            }

            @Override
            public int getNumberOfNonEdibleYoungs() {
                return numberOfNonEdibleYoungs;
            }
        };
    }

    static HasHarvestCountsForPermit zeros() {
        return of(0, 0, 0, 0, 0, 0);
    }
}
