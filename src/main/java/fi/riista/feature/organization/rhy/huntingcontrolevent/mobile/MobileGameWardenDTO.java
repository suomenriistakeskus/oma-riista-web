package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

public class MobileGameWardenDTO {

    @NotNull
    private MobileHuntingControlInspectorDTO inspector;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    // Constructors / factories

    public static MobileGameWardenDTO create(@Nonnull final Occupation occupation,
                                             @Nonnull final Person person,
                                             @Nonnull final Occupation requesterOccupation) {
        final MobileGameWardenDTO dto = new MobileGameWardenDTO();
        // Show only dates within requester nomination dates
        dto.setBeginDate(max(occupation.getBeginDate(), requesterOccupation.getBeginDate()));
        dto.setEndDate(min(occupation.getLifecycleFields().getDeletionTime(),
                           occupation.getEndDate(),
                           requesterOccupation.getEndDate()));
        dto.setInspector(MobileHuntingControlInspectorDTO.create(person));
        return dto;
    }

    // Functions

    private static LocalDate min(final DateTime deletionTime, final LocalDate first, final LocalDate second) {
        return deletionTime == null
                ? min(first, second)
                : min(min(deletionTime.toLocalDate(), first), second);
    }

    private static LocalDate min(final LocalDate first, final LocalDate second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }

        return first.isBefore(second) ? first : second;
    }

    private static LocalDate max(final LocalDate first, final LocalDate second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }

        return first.isAfter(second) ? first : second;
    }

    // Accessors -->

    public MobileHuntingControlInspectorDTO getInspector() {
        return inspector;
    }

    public void setInspector(final MobileHuntingControlInspectorDTO inspector) {
        this.inspector = inspector;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }
}
