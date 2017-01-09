package fi.riista.feature.gamediary.harvest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.dto.CodesetEntryDTO;
import fi.riista.feature.common.dto.DoNotValidate;
import fi.riista.feature.gamediary.HasAuthorAndActor;
import fi.riista.feature.gamediary.HasHuntingDayId;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.season.HarvestQuotaDTO;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsDTO;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;

public class HarvestDTO extends HarvestDTOBase implements HasAuthorAndActor, HasHuntingDayId {

    @Valid
    private PersonWithHunterNumberDTO authorInfo;

    @Valid
    private PersonWithHunterNumberDTO actorInfo;

    private boolean reportedForMe;
    private boolean authoredByMe;
    private boolean moderatorOverride;

    @JsonProperty(value = "totalSpecimenAmount")
    @Range(min = Harvest.MIN_AMOUNT, max = Harvest.MAX_AMOUNT)
    private int amount;

    private boolean readOnly;

    private Long harvestReportId;

    @JsonIgnore
    @DoNotValidate
    private CodesetEntryDTO gameSpecies;

    // TODO: This should not be part of HarvestDTO.
    @Valid
    private HarvestReportFieldsDTO fields;

    private HuntingAreaType huntingAreaType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String huntingParty;

    private Double huntingAreaSize;

    private HuntingMethod huntingMethod;

    private Boolean reportedWithPhoneCall;

    @DoNotValidate
    private HarvestQuotaDTO harvestQuota;

    private Long rhyId;

    private Long huntingDayId;

    @Valid
    private PermittedMethod permittedMethod;

    @DoNotValidate
    private HuntingClubDTO huntingClub;

    @Valid
    private OrganisationNameDTO groupOfHuntingDay;

    @Valid
    private PersonWithHunterNumberDTO approverToHuntingDay;

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

    public boolean isReportedForMe() {
        return reportedForMe;
    }

    public void setReportedForMe(boolean reportedForMe) {
        this.reportedForMe = reportedForMe;
    }

    public boolean isAuthoredByMe() {
        return authoredByMe;
    }

    public void setAuthoredByMe(boolean authoredByMe) {
        this.authoredByMe = authoredByMe;
    }

    public boolean isModeratorOverride() {
        return moderatorOverride;
    }

    public void setModeratorOverride(final boolean moderatorOverride) {
        this.moderatorOverride = moderatorOverride;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Long getHarvestReportId() {
        return harvestReportId;
    }

    public void setHarvestReportId(Long harvestReportId) {
        this.harvestReportId = harvestReportId;
    }

    @JsonProperty
    public CodesetEntryDTO getGameSpecies() {
        return gameSpecies;
    }

    @JsonIgnore
    public void setGameSpecies(final CodesetEntryDTO gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public HarvestReportFieldsDTO getFields() {
        return fields;
    }

    public void setFields(HarvestReportFieldsDTO fields) {
        this.fields = fields;
    }

    public HuntingAreaType getHuntingAreaType() {
        return huntingAreaType;
    }

    public void setHuntingAreaType(HuntingAreaType huntingAreaType) {
        this.huntingAreaType = huntingAreaType;
    }

    public String getHuntingParty() {
        return huntingParty;
    }

    public void setHuntingParty(String huntingParty) {
        this.huntingParty = huntingParty;
    }

    public Double getHuntingAreaSize() {
        return huntingAreaSize;
    }

    public void setHuntingAreaSize(Double huntingAreaSize) {
        this.huntingAreaSize = huntingAreaSize;
    }

    public HuntingMethod getHuntingMethod() {
        return huntingMethod;
    }

    public void setHuntingMethod(HuntingMethod huntingMethod) {
        this.huntingMethod = huntingMethod;
    }

    public Boolean getReportedWithPhoneCall() {
        return reportedWithPhoneCall;
    }

    public void setReportedWithPhoneCall(Boolean reportedWithPhoneCall) {
        this.reportedWithPhoneCall = reportedWithPhoneCall;
    }

    public HarvestQuotaDTO getHarvestQuota() {
        return harvestQuota;
    }

    public void setHarvestQuota(final HarvestQuotaDTO harvestQuota) {
        this.harvestQuota = harvestQuota;
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

    public void setHuntingDayId(Long huntingDayId) {
        this.huntingDayId = huntingDayId;
    }

    public PermittedMethod getPermittedMethod() {
        return permittedMethod;
    }

    public void setPermittedMethod(PermittedMethod permittedMethod) {
        this.permittedMethod = permittedMethod;
    }

    public HuntingClubDTO getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(HuntingClubDTO huntingClub) {
        this.huntingClub = huntingClub;
    }

    public OrganisationNameDTO getGroupOfHuntingDay() {
        return groupOfHuntingDay;
    }

    public void setGroupOfHuntingDay(OrganisationNameDTO groupOfHuntingDay) {
        this.groupOfHuntingDay = groupOfHuntingDay;
    }

    public PersonWithHunterNumberDTO getApproverToHuntingDay() {
        return approverToHuntingDay;
    }

    public void setApproverToHuntingDay(PersonWithHunterNumberDTO approverToHuntingDay) {
        this.approverToHuntingDay = approverToHuntingDay;
    }

    public LocalDateTime getPointOfTimeApprovedToHuntingDay() {
        return pointOfTimeApprovedToHuntingDay;
    }

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

        @Override
        public SELF populateWith(@Nonnull final GameSpecies species) {
            return super.populateWith(species).chain(self -> {
                dto.setGameSpecies(new CodesetEntryDTO(species.getOfficialCode(), species.getNameLocalisation()));
            });
        }

        // ASSOCIATIONS MUST NOT BE TRAVERSED IN THIS METHOD (except for identifiers that are
        // part of Harvest itself).
        @Override
        public SELF populateWith(@Nonnull final Harvest harvest) {
            return super.populateWith(harvest)
                    .withAmount(harvest.getAmount())
                    .chain(self -> {
                        dto.setHuntingAreaType(harvest.getHuntingAreaType());
                        dto.setHuntingAreaSize(harvest.getHuntingAreaSize());
                        dto.setHuntingParty(harvest.getHuntingParty());
                        dto.setHuntingMethod(harvest.getHuntingMethod());
                        dto.setReportedWithPhoneCall(harvest.getReportedWithPhoneCall());
                        dto.setPermittedMethod(harvest.getPermittedMethod());

                        dto.setRhyId(F.getId(harvest.getRhy()));
                        dto.setHuntingDayId(F.getId(harvest.getHuntingDayOfGroup()));
                        dto.setPointOfTimeApprovedToHuntingDay(
                                DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTimeApprovedToHuntingDay()));
                        dto.setModeratorOverride(harvest.isModeratorOverride());
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
