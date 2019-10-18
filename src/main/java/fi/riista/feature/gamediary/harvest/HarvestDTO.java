package fi.riista.feature.gamediary.harvest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.HasAuthorAndActor;
import fi.riista.feature.gamediary.HasHuntingDayId;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestAreaDTO;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.validation.DoNotValidate;
import fi.riista.validation.XssSafe;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;

public class HarvestDTO extends HarvestDTOBase implements HasAuthorAndActor, HasHuntingDayId {

    @Valid
    private PersonWithHunterNumberDTO authorInfo;

    @Valid
    private PersonWithHunterNumberDTO actorInfo;

    @JsonIgnore
    private LocalDateTime harvestReportDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String harvestReportMemo;

    private boolean moderatorOverride;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String moderatorFullName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String moderatorReasonForChange;

    @JsonProperty(value = "totalSpecimenAmount")
    @Min(Harvest.MIN_AMOUNT)
    @Max(Harvest.MAX_AMOUNT)
    private int amount;

    @JsonIgnore
    @DoNotValidate
    private HarvestAreaDTO harvestArea;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HarvestLukeStatus lukeStatus;

    @XssSafe
    @JsonIgnore
    private String propertyIdentifier;

    @JsonIgnore
    private Long rhyId;

    private Long huntingDayId;

    @Valid
    private PermittedMethod permittedMethod;

    @DoNotValidate
    @JsonIgnore
    private HuntingClubDTO huntingClub;

    @Valid
    @JsonIgnore
    private OrganisationNameDTO groupOfHuntingDay;

    @Valid
    @JsonIgnore
    private PersonWithHunterNumberDTO approverToHuntingDay;

    @JsonIgnore
    private LocalDateTime pointOfTimeApprovedToHuntingDay;

    public HarvestDTO() {
        // In non-mobile context this should never be null.
        setSpecimens(new ArrayList<>());
    }

    @AssertTrue
    public boolean isAmountValid() {
        return getSpecimens().size() <= getAmount();
    }

    @AssertTrue
    public boolean isPointOfTimeInPast() {
        return getPointOfTime().isBefore(DateUtil.localDateTime());
    }

    // Accessors ->

    @Override
    public final HarvestSpecVersion getHarvestSpecVersion() {
        return HarvestSpecVersion.MOST_RECENT;
    }

    @Override
    public PersonWithHunterNumberDTO getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(PersonWithHunterNumberDTO authorInfo) {
        this.authorInfo = authorInfo;
    }

    @Override
    public PersonWithHunterNumberDTO getActorInfo() {
        return actorInfo;
    }

    public void setActorInfo(PersonWithHunterNumberDTO actorInfo) {
        this.actorInfo = actorInfo;
    }

    @JsonProperty
    public boolean isModeratorOverride() {
        return moderatorOverride;
    }

    @JsonIgnore
    public void setModeratorOverride(final boolean moderatorOverride) {
        this.moderatorOverride = moderatorOverride;
    }

    @JsonProperty
    public LocalDateTime getHarvestReportDate() {
        return harvestReportDate;
    }

    @JsonIgnore
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setHarvestReportDate(final LocalDateTime harvestReportDate) {
        this.harvestReportDate = harvestReportDate;
    }

    public String getHarvestReportMemo() {
        return harvestReportMemo;
    }

    public void setHarvestReportMemo(final String harvestReportMemo) {
        this.harvestReportMemo = harvestReportMemo;
    }

    public String getModeratorReasonForChange() {
        return moderatorReasonForChange;
    }

    public void setModeratorReasonForChange(final String moderatorReasonForChange) {
        this.moderatorReasonForChange = moderatorReasonForChange;
    }

    public String getModeratorFullName() {
        return moderatorFullName;
    }

    public void setModeratorFullName(final String moderatorFullName) {
        this.moderatorFullName = moderatorFullName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public HarvestAreaDTO getHarvestArea() {
        return harvestArea;
    }

    @JsonIgnore
    public void setHarvestArea(final HarvestAreaDTO harvestArea) {
        this.harvestArea = harvestArea;
    }

    public HarvestLukeStatus getLukeStatus() {
        return lukeStatus;
    }

    public void setLukeStatus(final HarvestLukeStatus lukeStatus) {
        this.lukeStatus = lukeStatus;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    @JsonIgnore
    public void setPropertyIdentifier(final String propertyIdentifier) {
        this.propertyIdentifier = propertyIdentifier;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long getRhyId() {
        return rhyId;
    }

    @JsonIgnore
    public void setRhyId(final Long rhyId) {
        this.rhyId = rhyId;
    }

    @Override
    public Long getHuntingDayId() {
        return huntingDayId;
    }

    public void setHuntingDayId(Long huntingDayId) {
        this.huntingDayId = huntingDayId;
    }

    public PermittedMethod getPermittedMethod() {
        return permittedMethod;
    }

    public void setPermittedMethod(PermittedMethod permittedMethod) {
        this.permittedMethod = permittedMethod;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public HuntingClubDTO getHuntingClub() {
        return huntingClub;
    }

    @JsonIgnore
    public void setHuntingClub(HuntingClubDTO huntingClub) {
        this.huntingClub = huntingClub;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public OrganisationNameDTO getGroupOfHuntingDay() {
        return groupOfHuntingDay;
    }

    @JsonIgnore
    public void setGroupOfHuntingDay(OrganisationNameDTO groupOfHuntingDay) {
        this.groupOfHuntingDay = groupOfHuntingDay;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public PersonWithHunterNumberDTO getApproverToHuntingDay() {
        return approverToHuntingDay;
    }

    @JsonIgnore
    public void setApproverToHuntingDay(PersonWithHunterNumberDTO approverToHuntingDay) {
        this.approverToHuntingDay = approverToHuntingDay;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public LocalDateTime getPointOfTimeApprovedToHuntingDay() {
        return pointOfTimeApprovedToHuntingDay;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setPointOfTimeApprovedToHuntingDay(LocalDateTime pointOfTimeApprovedToHuntingDay) {
        this.pointOfTimeApprovedToHuntingDay = pointOfTimeApprovedToHuntingDay;
    }

    // Builder -->

    public static Builder<?> builder() {
        return new ConcreteBuilder();
    }

    // Allows sub-classing for tests and adding new fluent interface style methods.
    public static abstract class Builder<SELF extends Builder<SELF>> extends HarvestDTOBase.Builder<HarvestDTO, SELF> {

        public SELF withAmount(final int amount) {
            dto.setAmount(amount);
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

        public SELF withHarvestArea(@Nullable final HarvestArea harvestArea) {
            if (harvestArea != null) {
                dto.setHarvestArea(HarvestAreaDTO.create(harvestArea));
            }
            return self();
        }

        public SELF withModeratorChangeReason(@Nullable final String reason) {
            dto.setModeratorReasonForChange(reason);
            return self();
        }

        // ASSOCIATIONS MUST NOT BE TRAVERSED IN THIS METHOD (except for identifiers that are
        // part of Harvest itself).
        @Override
        public SELF populateWith(@Nonnull final Harvest harvest) {
            return super.populateWith(harvest)
                    .withAmount(harvest.getAmount())
                    .chain(self -> {
                        dto.setPermittedMethod(harvest.getPermittedMethod());
                        dto.setLukeStatus(harvest.getLukeStatus());
                        dto.setPropertyIdentifier(harvest.getPropertyIdentifier() != null
                                ? harvest.getPropertyIdentifier().getDelimitedValue() : null);
                        dto.setRhyId(F.getId(harvest.getRhy()));
                        dto.setHuntingDayId(F.getId(harvest.getHuntingDayOfGroup()));
                        dto.setPointOfTimeApprovedToHuntingDay(
                                DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTimeApprovedToHuntingDay()));
                        dto.setModeratorOverride(harvest.isModeratorOverride());
                        dto.setHarvestReportDate(harvest.getHarvestReportDate() != null
                                ? harvest.getHarvestReportDate().toLocalDateTime() : null);
                    });
        }

        @Override
        protected HarvestDTO createDTO() {
            return new HarvestDTO();
        }
    }

    private static final class ConcreteBuilder extends Builder<ConcreteBuilder> {
        @Override
        protected ConcreteBuilder self() {
            return this;
        }
    }
}
