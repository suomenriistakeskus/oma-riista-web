package fi.riista.feature.gamediary;

import fi.riista.feature.common.dto.BaseEntityDTO;

public abstract class GameDiaryEntrySpecimenDTO extends BaseEntityDTO<Long> {

    public Long id;

    public Integer rev;

    private GameGender gender;

    public GameDiaryEntrySpecimenDTO() {
    }

    public GameDiaryEntrySpecimenDTO(final GameGender gender) {
        setGender(gender);
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(final GameGender gender) {
        this.gender = gender;
    }
}
