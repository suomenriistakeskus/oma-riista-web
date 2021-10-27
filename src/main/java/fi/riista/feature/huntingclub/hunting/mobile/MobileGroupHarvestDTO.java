package fi.riista.feature.huntingclub.hunting.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.HasAuthorAndActor;
import fi.riista.feature.gamediary.HasHuntingDayId;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;

public class MobileGroupHarvestDTO extends MobileHarvestDTO implements HasHuntingDayId, HasAuthorAndActor {

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

    @Override
    public PersonWithHunterNumberDTO getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(final PersonWithHunterNumberDTO authorInfo) {
        this.authorInfo = authorInfo;
    }

    @Override
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

    public static Builder<?> builder(@Nonnull final HarvestSpecVersion specVersion) {
        return new ConcreteBuilder(specVersion);
    }

    // Allows sub-classing for tests and adding new fluent interface style methods.
    public static abstract class Builder<SELF extends Builder<SELF>>
            extends MobileHarvestDTO.Builder<MobileGroupHarvestDTO, SELF> {

        protected Builder(@Nonnull final HarvestSpecVersion specVersion) {
            super(specVersion);
        }

        public SELF withHuntingDayId(final Long huntingDayId) {
            dto.setHuntingDayId(huntingDayId);
            return self();
        }

        public SELF withAuthorInfo(@Nonnull final Person person) {
            dto.setAuthorInfo(PersonWithHunterNumberDTO.create(person));
            return self();
        }

        public SELF withActorInfo(@Nonnull final Person person) {
            dto.setActorInfo(PersonWithHunterNumberDTO.create(person));
            return self();
        }

        public SELF withApproverToHuntingDay(final Person person) {
            if (person != null) {
                dto.setApproverToHuntingDay(PersonWithHunterNumberDTO.create(person));
            }
            return self();
        }

        public SELF withPointOfTimeApprovedToHuntingDay(final LocalDateTime pointOfTimeApprovedToHuntingDay) {
            dto.setPointOfTimeApprovedToHuntingDay(pointOfTimeApprovedToHuntingDay);
            return self();
        }

        @Override
        public SELF populateWith(@Nonnull final Harvest harvest) {
            return super.populateWith(harvest).withHuntingDayId(F.getId(harvest.getHuntingDayOfGroup()))
                    .withPointOfTimeApprovedToHuntingDay(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTimeApprovedToHuntingDay()));
        }
    }

    private static final class ConcreteBuilder extends Builder<ConcreteBuilder> {

        public ConcreteBuilder(@Nonnull final HarvestSpecVersion specVersion) {
            super(specVersion);
        }

        @Override
        protected ConcreteBuilder self() {
            return this;
        }

        @Override
        protected MobileGroupHarvestDTO createDTO() {
            return new MobileGroupHarvestDTO();
        }
    }
}
