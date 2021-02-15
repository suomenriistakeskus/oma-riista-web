package fi.riista.feature.gamediary.observation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.HasAuthorAndActor;
import fi.riista.feature.gamediary.HasHuntingDayId;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import static java.util.Objects.requireNonNull;

public class ObservationDTO extends ObservationDTOBase implements HasAuthorAndActor, HasHuntingDayId {

    private boolean moderatorOverride;

    @Valid
    private PersonWithHunterNumberDTO authorInfo;

    @Valid
    private PersonWithHunterNumberDTO actorInfo;

    private Long rhyId;

    private Long huntingDayId;

    @Valid
    private OrganisationNameDTO groupOfHuntingDay;

    @Valid
    private PersonWithHunterNumberDTO approverToHuntingDay;

    private LocalDateTime pointOfTimeApprovedToHuntingDay;

    @JsonIgnore
    private boolean updateableOnlyByCarnivoreAuthority;

    @AssertTrue
    public boolean isHuntingDayIdConsistentWithHuntingState() {
        return huntingDayId == null || observedWithinHunting();
    }

    // Accessors -->

    @Override
    public ObservationSpecVersion getObservationSpecVersion() {
        return ObservationSpecVersion.MOST_RECENT;
    }

    public boolean isModeratorOverride() {
        return moderatorOverride;
    }

    public void setModeratorOverride(final boolean moderatorOverride) {
        this.moderatorOverride = moderatorOverride;
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

    public Long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final Long rhyId) {
        this.rhyId = rhyId;
    }

    @Override
    public Long getHuntingDayId() {
        return huntingDayId;
    }

    public void setHuntingDayId(final Long huntingDayId) {
        this.huntingDayId = huntingDayId;
    }

    public OrganisationNameDTO getGroupOfHuntingDay() {
        return groupOfHuntingDay;
    }

    public void setGroupOfHuntingDay(final OrganisationNameDTO groupOfHuntingDay) {
        this.groupOfHuntingDay = groupOfHuntingDay;
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

    @JsonProperty
    public boolean isUpdateableOnlyByCarnivoreAuthority() {
        return updateableOnlyByCarnivoreAuthority;
    }

    @JsonIgnore
    public void setUpdateableOnlyByCarnivoreAuthority(final boolean updateableOnlyByCarnivoreAuthority) {
        this.updateableOnlyByCarnivoreAuthority = updateableOnlyByCarnivoreAuthority;
    }

    // Builder -->

    public static Builder<?> builder() {
        return new ConcreteBuilder();
    }

    // Allows sub-classing for tests and adding new fluent interface style methods.
    public static abstract class Builder<SELF extends Builder<SELF>>
            extends ObservationDTOBase.Builder<ObservationDTO, SELF> {

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

        public SELF withGroupOfHuntingDay(@Nullable final Organisation group) {
            if (group != null) {
                dto.setGroupOfHuntingDay(OrganisationNameDTO.create(group));
            }
            return self();
        }

        public SELF withApproverToHuntingDay(@Nullable final Person person) {
            if (person != null) {
                dto.setApproverToHuntingDay(PersonWithHunterNumberDTO.create(person));
            }
            return self();
        }

        @Override
        protected ObservationDTO createDTO() {
            return new ObservationDTO();
        }
    }

    private static final class ConcreteBuilder extends Builder<ConcreteBuilder> {
        @Override
        protected ConcreteBuilder self() {
            return this;
        }
    }
}
