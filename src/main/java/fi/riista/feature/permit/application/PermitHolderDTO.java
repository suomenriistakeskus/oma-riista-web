package fi.riista.feature.permit.application;

import fi.riista.validation.Validators;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class PermitHolderDTO implements Serializable {
    @NotNull
    private PermitHolder.PermitHolderType type;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String code;

    @AssertTrue
    public boolean isValidPermitHolderCode() {
        if (type == null) {
            return true;
        }

        switch (type) {
            case PERSON:
            case OTHER:
                return true;
            case BUSINESS:
            case RY:
                return Validators.isValidBusinessId(code);
        }

        return false;
    }

    public PermitHolderDTO() {
    }

    private PermitHolderDTO(final String name,
                            final String code,
                            final PermitHolder.PermitHolderType type) {
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public static PermitHolderDTO createFrom(final PermitHolder permitHolder) {
        return permitHolder != null
                ? new PermitHolderDTO(permitHolder.getName(), permitHolder.getCode(), permitHolder.getType())
                : null;
    }

    public PermitHolder toEntity() {
        return PermitHolder.create(name, code, type);
    }

    public static PermitHolderDTO create(final String name, final String code, final PermitHolder.PermitHolderType type) {
        return new PermitHolderDTO(name, code, type);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
