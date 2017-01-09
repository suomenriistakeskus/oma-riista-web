package fi.riista.feature.organization.occupation;

import java.util.Comparator;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

public final class OccupationSort {

    public static final Comparator<Occupation> BY_TYPE = comparing(o -> o.getOccupationType().getOrder());
    public static final Comparator<Occupation> BY_CALL_ORDER = comparing(o -> o.getCallOrder(), nullsLast(naturalOrder()));
    public static final Comparator<Occupation> BY_LAST_NAME = comparing(o -> o.getPerson().getLastName());
    public static final Comparator<Occupation> BY_FIRST_NAME = comparing(o -> o.getPerson().getFirstName());
    public static final Comparator<Occupation> BY_BYNAME = comparing(o -> o.getPerson().getByName());

    public static final Comparator<Occupation> BY_CALL_ORDER_ONLY_FOR_APPLICABLE_TYPES = (o1, o2) -> {
        final OccupationType type1 = o1.getOccupationType();
        final OccupationType type2 = o2.getOccupationType();
        if (type1 == type2 && type1.isCallOrderPossible()) {
            return BY_CALL_ORDER.compare(o1, o2);
        }
        return 0;
    };

    private OccupationSort() {
        throw new AssertionError();
    }
}
