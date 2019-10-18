package fi.riista.feature.account.area.union;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class PersonalAreaUnionBasicDetailsDTO {
    private final long id;
    private final String name;
    private final String externalId;

    public PersonalAreaUnionBasicDetailsDTO(final long id, @Nonnull final String name,
                                            @Nonnull final String externalId) {
        this.id = id;
        this.name = requireNonNull(name);
        this.externalId = requireNonNull(externalId);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExternalId() {
        return externalId;
    }
}
