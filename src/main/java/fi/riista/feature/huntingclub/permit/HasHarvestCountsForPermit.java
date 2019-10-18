package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import io.vavr.Tuple;
import io.vavr.Tuple6;

import javax.validation.constraints.AssertTrue;

public interface HasHarvestCountsForPermit {

    int getNumberOfAdultMales();

    int getNumberOfAdultFemales();

    int getNumberOfYoungMales();

    int getNumberOfYoungFemales();

    int getNumberOfNonEdibleAdults();

    int getNumberOfNonEdibleYoungs();

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
                                        final int numberOfYoungMales, final int numberOfYoungFemales,
                                        final int numberOfNonEdibleAdults,
                                        final int numberOfNonEdibleYoungs) {
        return new HarvestCountDTO(numberOfAdultMales, numberOfAdultFemales, numberOfYoungMales, numberOfYoungFemales,
                numberOfNonEdibleAdults, 0,
                numberOfNonEdibleYoungs, 0);
    }
}
