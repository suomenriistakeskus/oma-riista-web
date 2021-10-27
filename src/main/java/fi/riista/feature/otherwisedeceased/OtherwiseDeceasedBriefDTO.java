package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.DtoUtil;
import org.joda.time.LocalDateTime;

/**
 * Only the information for the listing and filtering
 */
public class OtherwiseDeceasedBriefDTO extends BaseEntityDTO<Long> {

    // Factories

    public static OtherwiseDeceasedBriefDTO create(final OtherwiseDeceased entity,
                                                   final GameSpecies gameSpecies,
                                                   final Municipality municipality,
                                                   final Organisation rhy,
                                                   final Organisation rka) {
        final OtherwiseDeceasedBriefDTO dto = new OtherwiseDeceasedBriefDTO();
        DtoUtil.copyBaseFields(entity, dto);

        final OrganisationNameDTO municipalityDTO = new OrganisationNameDTO();
        municipalityDTO.setNameFI(municipality.getNameLocalisation().getFinnish());
        municipalityDTO.setNameSV(municipality.getNameLocalisation().getSwedish());

        dto.setPointOfTime(entity.getPointOfTime().toLocalDateTime());
        dto.setGameSpeciesCode(gameSpecies.getOfficialCode());
        dto.setMunicipality(municipalityDTO);
        dto.setRhy(OrganisationNameDTO.createWithOfficialCode(rhy));
        dto.setRka(OrganisationNameDTO.createWithOfficialCode(rka));
        dto.setCause(entity.getCause());
        dto.setSource(entity.getSource());
        dto.setRejected(entity.isRejected());

        return dto;
    }

    // Attributes

    private Long id;

    private Integer rev;

    private LocalDateTime pointOfTime;

    private int gameSpeciesCode;

    private OrganisationNameDTO municipality;

    private OrganisationNameDTO rhy;

    private OrganisationNameDTO rka;

    private OtherwiseDeceasedCause cause;

    private OtherwiseDeceasedSource source;

    private boolean rejected;

    // Constructors

    private OtherwiseDeceasedBriefDTO() {
    }

    // Accessors

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

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
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

    public OtherwiseDeceasedCause getCause() {
        return cause;
    }

    public void setCause(final OtherwiseDeceasedCause cause) {
        this.cause = cause;
    }

    public OtherwiseDeceasedSource getSource() {
        return source;
    }

    public void setSource(final OtherwiseDeceasedSource source) {
        this.source = source;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(final boolean rejected) {
        this.rejected = rejected;
    }
}
