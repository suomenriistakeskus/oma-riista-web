package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

public class MobileAccountV2DTO extends MobileAccountDTO {

    public static MobileAccountV2DTO create(
            @Nonnull final String username,
            @Nonnull final Person person,
            @Nonnull final Address address,
            @Nonnull final Riistanhoitoyhdistys rhy,
            @Nonnull final SortedSet<Integer> harvestYears,
            @Nonnull final SortedSet<Integer> observationYears,
            @Nonnull final List<MobileOccupationDTO> occupations) {

        // Instead of traversing entity graph here, all the needed entities are
        // lifted up as parameters in order to not introduce hidden N+1 issues.

        final SortedSet<Integer> gameDiaryYears = new TreeSet<>();
        gameDiaryYears.addAll(harvestYears);
        gameDiaryYears.addAll(observationYears);

        final MobileAccountV2DTO dto = new MobileAccountV2DTO();
        dto.populateWith(username, person, address, rhy, gameDiaryYears, occupations);
        dto.getHarvestYears().addAll(harvestYears);
        dto.getObservationYears().addAll(observationYears);
        dto.setEnableSrva(Optional.ofNullable(person.isEnableSrva()).orElse(false));
        return dto;
    }

    private final SortedSet<Integer> harvestYears = new TreeSet<>();
    private final SortedSet<Integer> observationYears = new TreeSet<>();
    private boolean enableSrva;

    private MobileAccountV2DTO() {
    }

    // Accessors -->

    public SortedSet<Integer> getHarvestYears() {
        return harvestYears;
    }

    public SortedSet<Integer> getObservationYears() {
        return observationYears;
    }

    public boolean isEnableSrva() {
        return enableSrva;
    }

    public void setEnableSrva(boolean enableSrva) {
        this.enableSrva = enableSrva;
    }
}
