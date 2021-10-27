package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OtherwiseDeceasedDTO extends BaseEntityDTO<Long>  {

    public static OtherwiseDeceasedDTO create(@Nonnull final OtherwiseDeceased entity,
                                              final List<OtherwiseDeceasedAttachmentDTO> attachments,
                                              final List<OtherwiseDeceasedChangeDTO> changes) {

        return create(entity, entity.getSpecies(), entity.getMunicipality(), entity.getRhy(), entity.getRka(),
                      attachments, changes);
    }

    public static OtherwiseDeceasedDTO create(@Nonnull final OtherwiseDeceased entity,
                                              @Nonnull final GameSpecies species,
                                              @Nonnull final Municipality municipality,
                                              @Nonnull final Organisation rhy,
                                              @Nonnull final Organisation rka,
                                              final List<OtherwiseDeceasedAttachmentDTO> attachments,
                                              final List<OtherwiseDeceasedChangeDTO> changes) {
        requireNonNull(entity);
        requireNonNull(species);
        requireNonNull(municipality);
        requireNonNull(rhy);
        requireNonNull(rka);

        final OtherwiseDeceasedDTO dto = new OtherwiseDeceasedDTO();
        DtoUtil.copyBaseFields(entity, dto);

        final OrganisationNameDTO municipalityDTO = new OrganisationNameDTO();
        municipalityDTO.setNameFI(municipality.getNameLocalisation().getFinnish());
        municipalityDTO.setNameSV(municipality.getNameLocalisation().getSwedish());

        dto.setPointOfTime(entity.getPointOfTime().toLocalDateTime());
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());
        dto.setWeight(entity.getWeight());
        dto.setNoExactLocation(entity.getNoExactLocation());
        dto.setRejected(entity.isRejected());
        dto.setCause(entity.getCause());
        dto.setCauseOther(entity.getCauseOther());
        dto.setSource(entity.getSource());
        dto.setSourceOther(entity.getSourceOther());
        dto.setDescription(entity.getDescription());
        dto.setAdditionalInfo(entity.getAdditionalInfo());
        dto.setMunicipality(municipalityDTO);
        dto.setRhy(OrganisationNameDTO.createWithOfficialCode(rhy));
        dto.setRka(OrganisationNameDTO.createWithOfficialCode(rka));
        dto.setGeoLocation(entity.getGeoLocation());
        dto.setAttachments(attachments);
        dto.setChangeHistory(changes);

        return dto;
    }

    private Long id;

    private Integer rev;

    private LocalDateTime pointOfTime;

    private int gameSpeciesCode;

    private GameAge age;

    private GameGender gender;

    private Double weight;

    private boolean noExactLocation;

    private boolean rejected;

    private OtherwiseDeceasedCause cause;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String causeOther;

    private OtherwiseDeceasedSource source;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String sourceOther;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    /** Location fields */

    @Valid
    private OrganisationNameDTO municipality;

    @Valid
    private OrganisationNameDTO rhy;

    @Valid
    private OrganisationNameDTO rka;

    @Valid
    private GeoLocation geoLocation;

    /** Attachments */

    @Valid
    private List<OtherwiseDeceasedAttachmentDTO> attachments;

    /** Change log */

    @Valid
    private List<OtherwiseDeceasedChangeDTO> changeHistory;

    /** Fill in only when updating */

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String reasonForChange;

    // Accessories

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

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(final GameAge age) {
        this.age = age;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(final GameGender gender) {
        this.gender = gender;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(final Double weight) {
        this.weight = weight;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public OtherwiseDeceasedCause getCause() {
        return cause;
    }

    public void setCause(final OtherwiseDeceasedCause cause) {
        this.cause = cause;
    }

    public String getCauseOther() {
        return causeOther;
    }

    public void setCauseOther(final String causeOther) {
        this.causeOther = causeOther;
    }

    public OtherwiseDeceasedSource getSource() {
        return source;
    }

    public void setSource(final OtherwiseDeceasedSource source) {
        this.source = source;
    }

    public String getSourceOther() {
        return sourceOther;
    }

    public void setSourceOther(final String sourceOther) {
        this.sourceOther = sourceOther;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public boolean isNoExactLocation() {
        return noExactLocation;
    }

    public void setNoExactLocation(final boolean noExactLocation) {
        this.noExactLocation = noExactLocation;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(final boolean rejected) {
        this.rejected = rejected;
    }

    public OrganisationNameDTO getMunicipality() {
        return municipality;
    }

    public void setMunicipality(final OrganisationNameDTO municipality) {
        this.municipality = municipality;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public void setRhy(final OrganisationNameDTO rhy) {
        this.rhy = rhy;
    }

    public OrganisationNameDTO getRka() {
        return rka;
    }

    public void setRka(final OrganisationNameDTO rka) {
        this.rka = rka;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public List<OtherwiseDeceasedAttachmentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<OtherwiseDeceasedAttachmentDTO> attachments) {
        this.attachments = attachments;
    }

    public List<OtherwiseDeceasedChangeDTO> getChangeHistory() {
        return changeHistory;
    }

    public void setChangeHistory(final List<OtherwiseDeceasedChangeDTO> changeHistory) {
        this.changeHistory = changeHistory;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public void setReasonForChange(final String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }
}
