package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.common.entity.LifecycleEntity;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Access(AccessType.FIELD)
public class DogEventDisturbanceContact extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "dog_event_disturbance_contact_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dog_event_disturbance_id", unique = true, nullable = false)
    private DogEventDisturbance event;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(nullable = false)
    private String contactName;

    @Email
    @Size(max = 255)
    @Column
    private String contactMail;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "[+]?[ 0-9]+")
    @Column(nullable = false)
    private String contactPhone;

    // Accessors

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public DogEventDisturbance getEvent() {
        return event;
    }

    public void setEvent(final DogEventDisturbance event) {
        this.event = event;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(final String contactName) {
        this.contactName = contactName;
    }

    public String getContactMail() {
        return contactMail;
    }

    public void setContactMail(final String contactMail) {
        this.contactMail = contactMail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(final String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
