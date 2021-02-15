package fi.riista.feature.permit.application.dogevent.disturbance;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbance;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContact;
import fi.riista.feature.permit.application.dogevent.DogEventType;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class DogEventDisturbanceDTO implements Serializable, HasBeginAndEndDate {

    public static DogEventDisturbanceDTO createFrom(@Nonnull final DogEventDisturbance entity,
                                                    @Nonnull final List<DogEventDisturbanceContact> contacts,
                                                    final Integer speciesCode) {

        final DogEventDisturbanceDTO dto = new DogEventDisturbanceDTO();
        dto.setId(entity.getId());
        dto.setEventType(entity.getEventType());
        dto.setSkipped(entity.isSkipped());
        if (!dto.isSkipped()) {
            dto.setSpeciesCode(speciesCode);
            dto.setDogsAmount(entity.getDogsAmount());
            dto.setBeginDate(entity.getBeginDate());
            dto.setEndDate(entity.getEndDate());
            dto.setContacts(F.mapNonNullsToList(contacts, DogEventDisturbanceContactDTO::createFrom));
            dto.setEventDescription(entity.getEventDescription());
        }
        return dto;
    }

    private Long id;

    @NotNull
    private DogEventType eventType;

    private boolean skipped;

    private Integer speciesCode;

    @Min(1)
    private Integer dogsAmount;

    private LocalDate beginDate;

    private LocalDate endDate;

    @Valid
    private List<DogEventDisturbanceContactDTO> contacts;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String eventDescription;

    // Accessors


    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public DogEventType getEventType() {
        return eventType;
    }

    public void setEventType(final DogEventType eventType) {
        this.eventType = eventType;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(final boolean skipped) {
        this.skipped = skipped;
    }

    public Integer getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(final Integer speciesCode) {
        this.speciesCode = speciesCode;
    }

    public Integer getDogsAmount() {
        return dogsAmount;
    }

    public void setDogsAmount(final Integer dogsAmount) {
        this.dogsAmount = dogsAmount;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<DogEventDisturbanceContactDTO> getContacts() {
        return contacts;
    }

    public void setContacts(final List<DogEventDisturbanceContactDTO> contacts) {
        this.contacts = contacts;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(final String eventDescription) {
        this.eventDescription = eventDescription;
    }
}
