package fi.riista.feature.permit.application.derogation.forbidden;

import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.FIELD)
public class DerogationPermitApplicationForbiddenMethods {

    @Column(name = "deviate_section_32", columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection32;

    @Column(name = "deviate_section_33", columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection33;

    @Column(name = "deviate_section_34", columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection34;

    @Column(name = "deviate_section_35", columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection35;

    @Column(name = "deviate_section_51", columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deviateSection51;

    @Column(name = "use_traps", nullable = false)
    private boolean traps;

    @Column(name = "use_tape_recorders", nullable = false)
    private boolean tapeRecorders;

    public String getDeviateSection32() {
        return deviateSection32;
    }

    public void setDeviateSection32(String deviateSection32) {
        this.deviateSection32 = deviateSection32;
    }

    public String getDeviateSection33() {
        return deviateSection33;
    }

    public void setDeviateSection33(String deviateSection33) {
        this.deviateSection33 = deviateSection33;
    }

    public String getDeviateSection34() {
        return deviateSection34;
    }

    public void setDeviateSection34(String deviateSection34) {
        this.deviateSection34 = deviateSection34;
    }

    public String getDeviateSection35() {
        return deviateSection35;
    }

    public void setDeviateSection35(String deviateSection35) {
        this.deviateSection35 = deviateSection35;
    }

    public String getDeviateSection51() {
        return deviateSection51;
    }

    public void setDeviateSection51(String deviateSection51) {
        this.deviateSection51 = deviateSection51;
    }

    public boolean isTraps() {
        return traps;
    }

    public void setTraps(boolean traps) {
        this.traps = traps;
    }

    public boolean isTapeRecorders() {
        return tapeRecorders;
    }

    public void setTapeRecorders(boolean tapeRecorders) {
        this.tapeRecorders = tapeRecorders;
    }
}
