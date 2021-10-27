package fi.riista.feature.huntingclub.hunting.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.HasAuthorAndActor;
import fi.riista.feature.gamediary.HasHuntingDayId;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import static java.util.Objects.requireNonNull;

public class MobileGroupObservationDTO extends MobileObservationDTO implements HasHuntingDayId, HasAuthorAndActor {

    @ApiModelProperty(required = false)
    private Long huntingDayId;

    @ApiModelProperty(required = true)
    @Valid
    private PersonWithHunterNumberDTO authorInfo;

    @ApiModelProperty(required = true)
    @Valid
    private PersonWithHunterNumberDTO actorInfo;

    @ApiModelProperty(required = false)
    @Valid
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private PersonWithHunterNumberDTO approverToHuntingDay;

    @ApiModelProperty(required = false)
    private LocalDateTime pointOfTimeApprovedToHuntingDay;

    @Override
    public Long getHuntingDayId() {
        return huntingDayId;
    }

    public void setHuntingDayId(final Long huntingDayId) {
        this.huntingDayId = huntingDayId;
    }

    public PersonWithHunterNumberDTO getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(final PersonWithHunterNumberDTO authorInfo) {
        this.authorInfo = authorInfo;
    }

    public PersonWithHunterNumberDTO getActorInfo() {
        return actorInfo;
    }

    public void setActorInfo(final PersonWithHunterNumberDTO actorInfo) {
        this.actorInfo = actorInfo;
    }

    public PersonWithHunterNumberDTO getApproverToHuntingDay() {
        return approverToHuntingDay;
    }

    public void setApproverToHuntingDay(final PersonWithHunterNumberDTO approverToHuntingDay) {
        this.approverToHuntingDay = approverToHuntingDay;
    }

    public LocalDateTime getPointOfTimeApprovedToHuntingDay() {
        return pointOfTimeApprovedToHuntingDay;
    }

    public void setPointOfTimeApprovedToHuntingDay(final LocalDateTime pointOfTimeApprovedToHuntingDay) {
        this.pointOfTimeApprovedToHuntingDay = pointOfTimeApprovedToHuntingDay;
    }

    // Builder -->

    public static Builder<?, ?> builder(@Nonnull final ObservationBaseFields baseFields) {
        return new ConcreteBuilder(baseFields);
    }

    // Allows sub-classing for tests and adding new fluent interface style methods.
    public static abstract class Builder<DTO extends MobileGroupObservationDTO, SELF extends Builder<DTO, SELF>>
            extends MobileObservationDTO.Builder<DTO, SELF> {

        protected Builder(@Nonnull final ObservationBaseFields baseFields) {
            super(baseFields);
        }

        public SELF withHuntingDayId(final Long huntingDayId) {
            dto.setHuntingDayId(huntingDayId);
            return self();
        }

        public SELF withAuthorInfo(@Nonnull final Person person) {
            requireNonNull(person);
            dto.setAuthorInfo(PersonWithHunterNumberDTO.create(person));
            return self();
        }

        public SELF withActorInfo(@Nonnull final Person person) {
            requireNonNull(person);
            dto.setActorInfo(PersonWithHunterNumberDTO.create(person));
            return self();
        }

        public SELF withApproverToHuntingDay(final Person person) {
            if (person != null) {
                dto.setApproverToHuntingDay(PersonWithHunterNumberDTO.create(person));
            }
            return self();
        }
    }

    private static final class ConcreteBuilder extends Builder<MobileGroupObservationDTO, ConcreteBuilder> {

        public ConcreteBuilder(@Nonnull final ObservationBaseFields baseFields) {
            super(baseFields);
        }

        @Override
        protected ConcreteBuilder self() {
            return this;
        }

        @Override
        protected MobileGroupObservationDTO createDTO() {
            return new MobileGroupObservationDTO();
        }
    }

}
