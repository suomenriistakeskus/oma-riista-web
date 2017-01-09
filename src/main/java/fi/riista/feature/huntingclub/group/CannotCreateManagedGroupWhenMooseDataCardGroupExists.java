package fi.riista.feature.huntingclub.group;

public class CannotCreateManagedGroupWhenMooseDataCardGroupExists extends RuntimeException {

    public CannotCreateManagedGroupWhenMooseDataCardGroupExists(final String clubCode, final int huntingYear) {
        super(String.format(
                "Cannot create a non-moose-data-card-group for club %s for hunting year %d because club already has " +
                        "a group created within moose data card import for that year",
                clubCode, huntingYear));
    }

}
