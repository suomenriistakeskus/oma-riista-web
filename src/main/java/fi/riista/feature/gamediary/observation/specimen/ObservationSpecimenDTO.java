package fi.riista.feature.gamediary.observation.specimen;

import fi.riista.feature.gamediary.GameDiaryEntrySpecimenDTO;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.util.F;

import javax.validation.constraints.AssertTrue;

import static fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps.MAX_PAW_LENGTH_OF_LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps.MAX_PAW_WIDTH_OF_BEAR;
import static fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps.MIN_PAW_LENGTH_OF_LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps.MIN_PAW_WIDTH_OF_LARGE_CARNIVORES;
import static fi.riista.util.NumberUtils.isInRange;

public class ObservationSpecimenDTO extends GameDiaryEntrySpecimenDTO {

    private ObservedGameAge age;

    private ObservedGameState state;

    private GameMarking marking;

    private Double widthOfPaw;

    private Double lengthOfPaw;

    public ObservationSpecimenDTO() {
        super();
    }

    public ObservationSpecimenDTO(final GameGender gender, final ObservedGameAge age) {
        super(gender);
        setAge(age);
    }

    @AssertTrue
    public boolean isWidthOfPawInValidRange() {
        return widthOfPaw == null || isInRange(widthOfPaw, MIN_PAW_WIDTH_OF_LARGE_CARNIVORES, MAX_PAW_WIDTH_OF_BEAR);
    }

    @AssertTrue
    public boolean isLengthOfPawInValidRange() {
        return lengthOfPaw == null
                || isInRange(lengthOfPaw, MIN_PAW_LENGTH_OF_LARGE_CARNIVORES, MAX_PAW_LENGTH_OF_LARGE_CARNIVORES);
    }

    public boolean allBusinessFieldsNull() {
        return F.allNull(getGender(), getAge(), getState(), getMarking(), getWidthOfPaw(), getLengthOfPaw());
    }

    public void clearBusinessFields() {
        setGender(null);
        setAge(null);
        setState(null);
        setMarking(null);
        setWidthOfPaw(null);
        setLengthOfPaw(null);
    }

    // Accessors -->

    public ObservedGameAge getAge() {
        return age;
    }

    public void setAge(final ObservedGameAge age) {
        this.age = age;
    }

    public ObservedGameState getState() {
        return state;
    }

    public void setState(final ObservedGameState state) {
        this.state = state;
    }

    public GameMarking getMarking() {
        return marking;
    }

    public void setMarking(final GameMarking marking) {
        this.marking = marking;
    }

    public Double getWidthOfPaw() {
        return widthOfPaw;
    }

    public void setWidthOfPaw(final Double widthOfPaw) {
        this.widthOfPaw = widthOfPaw;
    }

    public Double getLengthOfPaw() {
        return lengthOfPaw;
    }

    public void setLengthOfPaw(final Double lengthOfPaw) {
        this.lengthOfPaw = lengthOfPaw;
    }
}
