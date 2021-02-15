package fi.riista.feature.permit.application.dogevent.disturbance;

import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContact;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DogEventDisturbanceContactDTO implements Serializable {

    public static List<DogEventDisturbanceContactDTO> createFrom(@Nonnull final List<DogEventDisturbanceContact> entities) {

        requireNonNull(entities);
        return entities.stream()
                .map(DogEventDisturbanceContactDTO::createFrom)
                .collect(toList());
    }

    public static DogEventDisturbanceContactDTO createFrom(@Nonnull final DogEventDisturbanceContact entity) {

        requireNonNull(entity);
        final DogEventDisturbanceContactDTO dto = new DogEventDisturbanceContactDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getContactName());
        dto.setMail(entity.getContactMail());
        dto.setPhone(entity.getContactPhone());
        return dto;
    }

    private Long id;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @Email
    @Size(max = 255)
    private String mail;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "[+]?[ 0-9]+")
    private String phone;

    // Accessors

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(final String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }
}
