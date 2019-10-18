package fi.riista.feature.permit.application.bird.forbidden;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class BirdPermitApplicationForbiddenMethodsDTO {

    public static BirdPermitApplicationForbiddenMethodsDTO createFrom(final @Nonnull BirdPermitApplicationForbiddenMethods entity,
                                                                      final @Nonnull List<BirdPermitApplicationForbiddenMethodsSpeciesDTO> justificationList) {
        requireNonNull(entity);
        requireNonNull(justificationList);

        return new BirdPermitApplicationForbiddenMethodsDTO(
                entity.getDeviateSection32(),
                entity.getDeviateSection33(),
                entity.getDeviateSection34(),
                entity.getDeviateSection35(),
                entity.getDeviateSection51(),
                entity.isTraps(),
                entity.isTapeRecorders(),
                justificationList);
    }

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection32;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection33;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection34;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection35;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection51;

    private boolean traps;

    private boolean tapeRecorders;

    @Valid
    private List<BirdPermitApplicationForbiddenMethodsSpeciesDTO> speciesJustifications;

    public BirdPermitApplicationForbiddenMethodsDTO() {

    }

    public BirdPermitApplicationForbiddenMethodsDTO(final String deviateSection32,
                                                    final String deviateSection33,
                                                    final String deviateSection34,
                                                    final String deviateSection35,
                                                    final String deviateSection51,
                                                    final boolean traps,
                                                    final boolean tapeRecorders,
                                                    final List<BirdPermitApplicationForbiddenMethodsSpeciesDTO> justificationList) {
        this.deviateSection32 = deviateSection32;
        this.deviateSection33 = deviateSection33;
        this.deviateSection34 = deviateSection34;
        this.deviateSection35 = deviateSection35;
        this.deviateSection51 = deviateSection51;
        this.traps = traps;
        this.tapeRecorders = tapeRecorders;
        this.speciesJustifications = justificationList;
    }

    public BirdPermitApplicationForbiddenMethods toEntity() {
        final BirdPermitApplicationForbiddenMethods entity = new BirdPermitApplicationForbiddenMethods();
        entity.setDeviateSection32(deviateSection32);
        entity.setDeviateSection33(deviateSection33);
        entity.setDeviateSection34(deviateSection34);
        entity.setDeviateSection35(deviateSection35);
        entity.setDeviateSection51(deviateSection51);
        entity.setTraps(traps);
        entity.setTapeRecorders(tapeRecorders);
        return entity;
    }

    // For JSP only
    @JsonIgnore
    public boolean isForbiddenMethodSelected() {
        return StringUtils.hasText(deviateSection32) ||
                StringUtils.hasText(deviateSection33) ||
                StringUtils.hasText(deviateSection34) ||
                StringUtils.hasText(deviateSection35) ||
                StringUtils.hasText(deviateSection51) ||
                traps || tapeRecorders;
    }

    public String getDeviateSection32() {
        return deviateSection32;
    }

    public void setDeviateSection32(final String deviateSection32) {
        this.deviateSection32 = deviateSection32;
    }

    public String getDeviateSection33() {
        return deviateSection33;
    }

    public void setDeviateSection33(final String deviateSection33) {
        this.deviateSection33 = deviateSection33;
    }

    public String getDeviateSection34() {
        return deviateSection34;
    }

    public void setDeviateSection34(final String deviateSection34) {
        this.deviateSection34 = deviateSection34;
    }

    public String getDeviateSection35() {
        return deviateSection35;
    }

    public void setDeviateSection35(final String deviateSection35) {
        this.deviateSection35 = deviateSection35;
    }

    public String getDeviateSection51() {
        return deviateSection51;
    }

    public void setDeviateSection51(final String deviateSection51) {
        this.deviateSection51 = deviateSection51;
    }

    public boolean isTraps() {
        return traps;
    }

    public void setTraps(final boolean traps) {
        this.traps = traps;
    }

    public boolean isTapeRecorders() {
        return tapeRecorders;
    }

    public void setTapeRecorders(final boolean tapeRecorders) {
        this.tapeRecorders = tapeRecorders;
    }

    public List<BirdPermitApplicationForbiddenMethodsSpeciesDTO> getSpeciesJustifications() {
        return speciesJustifications;
    }

    public void setSpeciesJustifications(final List<BirdPermitApplicationForbiddenMethodsSpeciesDTO> speciesJustifications) {
        this.speciesJustifications = speciesJustifications;
    }
}
