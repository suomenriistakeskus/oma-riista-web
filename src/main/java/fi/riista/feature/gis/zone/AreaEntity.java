package fi.riista.feature.gis.zone;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.util.LocalisedString;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@MappedSuperclass
@Access(value = AccessType.FIELD)
public abstract class AreaEntity<T extends Serializable> extends LifecycleEntity<T> {

    public abstract GISZone getZone();

    public abstract void setZone(GISZone zone);

    public Optional<Set<Long>> getZoneIdSet() {
        return Optional.ofNullable(getZone()).map(HasID::getId).map(Collections::singleton);
    }

    public abstract LocalisedString getNameLocalisation();

    public Optional<Date> findZoneModificationTime() {
        return Optional.ofNullable(getZone()).map(zone -> zone.getLifecycleFields().getModificationTime());
    }

    @Nullable // before persisted
    public Date getLatestCombinedModificationTime() {
        final Optional<Date> zoneMtime = findZoneModificationTime();

        return Optional.ofNullable(getModificationTime())
                .map(areaMtime -> zoneMtime.filter(areaMtime::before).orElse(areaMtime))
                .orElseGet(() -> zoneMtime.orElse(null));
    }
}
