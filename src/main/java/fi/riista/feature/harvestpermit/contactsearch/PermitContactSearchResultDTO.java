package fi.riista.feature.harvestpermit.contactsearch;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.util.LocalisedString;

import java.util.Objects;

public class PermitContactSearchResultDTO {

    final private HarvestPermitCategory harvestPermitCategory;
    final private int huntingYear;

    final private LocalisedString rka;
    final private LocalisedString rhy;

    final private String name;
    final private String email;

    public PermitContactSearchResultDTO(final HarvestPermitCategory harvestPermitCategory,
                                        final int huntingYear,
                                        final LocalisedString rka,
                                        final LocalisedString rhy,
                                        final String firstName,
                                        final String lastName,
                                        final String email) {
        this.harvestPermitCategory = harvestPermitCategory;
        this.huntingYear = huntingYear;
        this.rka = rka;
        this.rhy = rhy;
        this.name = firstName + " " + lastName;
        this.email = email;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof PermitContactSearchResultDTO)) {
            return false;
        }

        final PermitContactSearchResultDTO thatDto = (PermitContactSearchResultDTO) that;

        return Objects.equals(this.harvestPermitCategory, thatDto.harvestPermitCategory) && this.huntingYear == thatDto.huntingYear &&
                Objects.equals(this.rka, thatDto.rka) && Objects.equals(this.rhy, thatDto.rhy) &&
                Objects.equals(this.name, thatDto.name) && Objects.equals(this.email, thatDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(harvestPermitCategory, huntingYear, rka, rhy, name, email);
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public LocalisedString getRka() {
        return rka;
    }

    public LocalisedString getRhy() {
        return rhy;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
