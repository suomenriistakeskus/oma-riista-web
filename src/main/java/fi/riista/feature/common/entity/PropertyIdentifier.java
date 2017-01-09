package fi.riista.feature.common.entity;

import com.google.common.base.Preconditions;
import fi.riista.validation.FinnishPropertyIdentifier;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * Kiinteistötunnus on 4-osainen. Se muodostuu 3-numeroisesta kuntanumerosta, 3-numeroisesta
 * sijaintialuenumerosta, 4-numeroisesta ryhmänumerosta ja 4-numeroisesta yksikkönumerosta.
 *
 * source: http://docs.jhs-suositukset.fi/jhs-suositukset/JHS138/JHS138.pdf
 */
@Embeddable
@Access(AccessType.FIELD)
public class PropertyIdentifier implements Serializable {

    public static String formatPropertyIdentifier(Long value) {
        return value != null ? StringUtils.leftPad(Long.toString(value), 14, '0') : null;
    }

    public static PropertyIdentifier create(Long value) {
        return value != null ? create(formatPropertyIdentifier(value)) : null;
    }

    public static PropertyIdentifier create(String value) {
        PropertyIdentifier result = new PropertyIdentifier();
        result.propertyIdentifier = Objects.requireNonNull(value);
        Preconditions.checkArgument(value.length() == 14);

        return result;
    }

    @Column
    @Size(max = 255)
    @FinnishPropertyIdentifier
    private String propertyIdentifier;

    public String getValue() {
        return this.propertyIdentifier;
    }

    public void setValue(String value) {
        this.propertyIdentifier = value;
    }

    public String getDelimitedValue() {
        return String.format("%s-%s-%s-%s",
                getKuntanumero(), getSijaintialuenumero(), getRyhmanumero(), getYksikkonumero());
    }

    // Kuntanumerona käytetään Väestörekisterikeskuksen kunnalle antamaa numeroa,
    // jonka arvo voi olla välillä 001-999.
    public String getKuntanumero() {
        return this.propertyIdentifier != null ? this.propertyIdentifier.substring(0, 3) : null;
    }

    // Sijaintialuenumerona käytetään kiinteistörekisterin pitäjän antamaa numeroa,
    // jonka arvo voi olla välillä 001-999. Asemakaava-alueella tontin ja yleisen alueen
    // sijaintialuenumero on kunnanosan numero tai muun siihen verrattavan alueen numero.
    // Muille rekisteriyksiköille käytetään kylän numeroa tai muun siihen verrattavan alueen numeroa.
    // Kiinteistörekisterissä käytetään sijaintialuenumeroita liitteen 1 mukaisesti.
    public String getSijaintialuenumero() {
        return this.propertyIdentifier != null ? this.propertyIdentifier.substring(3, 6) : null;
    }

    // Ryhmänumerona käytetään kiinteistörekisterin pitäjän antamaa numeroa,
    // jonka arvo voi olla välillä 0000-9999. Asemakaava-alueilla tontin ja yleisen alueen
    // ryhmänumero on korttelin numero tai muun siihen verrattavan alueen numero.
    // Muille rekisteriyksiköille käytetään talon numeroa tai muun siihen verrattavan
    // alueen numeroa. Kiinteistörekisterissä käytetään ryhmänumeroita liitteen 2 mukaisesti.
    public String getRyhmanumero() {
        return this.propertyIdentifier != null ? this.propertyIdentifier.substring(6, 10) : null;
    }

    // Yksikkönumeron antaa kiinteistörekisterin pitäjä ja sen arvo on välillä 0000-9999. Asemakaava-alueella
    // yksikkönumero on tonttijaon mukaisen tontin numero tai muu numero. Muille rekisteriyksiköille käytetään
    // tilan numeroa tai muuta siihen verrattavaa numeroa.
    public String getYksikkonumero() {
        return this.propertyIdentifier != null ? this.propertyIdentifier.substring(10, 14) : null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyIdentifier)) return false;
        final PropertyIdentifier that = (PropertyIdentifier) o;
        return Objects.equals(propertyIdentifier, that.propertyIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyIdentifier);
    }
}
