package fi.riista.feature.common.entity;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.util.Objects;

public abstract class BaseEntityDTO<PK extends Serializable> implements Authorizable, HasID<PK> {

    public BaseEntityDTO() {
    }

    public BaseEntityDTO(@Nonnull final BaseEntityDTO<PK> other) {
        Objects.requireNonNull(other);

        setId(other.getId());
        setRev(other.getRev());
    }

    @Override
    public abstract PK getId();

    public abstract void setId(final PK id);

    public abstract Integer getRev();

    public abstract void setRev(final Integer rev);

    @Override
    public String toString() {
        String idPart = this.getId() == null ? ":<transient object>" : (":<" + this.getId().toString() + ">");
        return this.getClass().getName() + idPart;
    }
}
