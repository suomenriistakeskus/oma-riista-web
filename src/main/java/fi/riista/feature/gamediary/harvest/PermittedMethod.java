package fi.riista.feature.gamediary.harvest;

import com.google.common.base.Strings;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class PermittedMethod implements Serializable {

    /**
     * Description is free text, so very probably it contains comma (,) therefore we use separator string.
     */
    public static String DESCRIPTION_SEPARATOR = "#!SEPARATOR!#";

    enum Method {
        // Ääntä synnyttävä koneellinen laite
        TAPE_RECORDERS("B014"),

        //Varishäkki/Lokkiloukku
        TRAPS("B015"),

        // Muu
        OTHER("9999");
        private String code;

        Method(String code) {
            this.code = code;
        }

        String getCode() {
            return this.code;
        }
    }

    @Column(name = "permittedMethodTapeRecorders", nullable = false)
    private boolean tapeRecorders;

    @Column(name = "permittedMethodTraps", nullable = false)
    private boolean traps;

    @Column(name = "permittedMethodOther", nullable = false)
    private boolean other;

    /**
     * Description of which "other" method was used
     */
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    @Column(length = 255, name = "permittedMethodDescription")
    private String description;

    public PermittedMethod() {
    }

    @AssertTrue
    public boolean isDescriptionValid() {
        return other ?
                !Strings.isNullOrEmpty(description) && !description.contains(DESCRIPTION_SEPARATOR)
                : description == null;
    }

    public String getTapeRecordersCode() {
        return tapeRecorders ? Method.TAPE_RECORDERS.code : null;
    }

    public String getTrapsCode() {
        return traps ? Method.TRAPS.code : null;
    }

    public String getOtherCode() {
        return other ? Method.OTHER.code : null;
    }

    // getters and setters

    public boolean isTapeRecorders() {
        return tapeRecorders;
    }

    public void setTapeRecorders(boolean tapeRecorders) {
        this.tapeRecorders = tapeRecorders;
    }

    public boolean isTraps() {
        return traps;
    }

    public void setTraps(boolean traps) {
        this.traps = traps;
    }

    public boolean isOther() {
        return other;
    }

    public void setOther(boolean other) {
        this.other = other;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
