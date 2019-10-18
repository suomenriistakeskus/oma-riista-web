package fi.riista.feature.common.entity;

import fi.riista.security.UserInfo;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.domain.Persistable;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * This class is an abstract superclass for all Entity classes in the
 * application. This class defines variables which are common for all entity
 * classes.
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class BaseEntity<PK extends Serializable> implements HasID<PK>, Persistable<PK> {

    @Column(nullable = false)
    @Version
    protected Integer consistencyVersion;

    public BaseEntity() {
        super();
    }

    /**
     * AccessType.PROPERTY allows safe getId() call on
     * associated entities without unnecessary entity lazy-fetch.
     * NOTE: Annotation must be applied to sub-class property.
     * <p/>
     */
    @Override
    public abstract PK getId();

    public abstract void setId(PK id);

    @Override
    @Transient
    public boolean isNew() {
        return null == getId();
    }

    /**
     * Get the concurrency version number for this entity. The concurrency
     * version is a number which is used for optimistic locking in the database.
     *
     * @return Current consistency version
     */
    public Integer getConsistencyVersion() {
        return consistencyVersion;
    }

    /**
     * Set the concurrency version number for this entity. Usually, this method
     * should never be called.
     *
     * @param consistencyVersion New consistency version
     */
    public void setConsistencyVersion(Integer consistencyVersion) {
        this.consistencyVersion = consistencyVersion;
    }

    @Override
    public String toString() {
        return getEntityClassName() +
                (this.getId() == null
                        ? ":<transient object>"
                        : ":<" + getId() + ">");
    }

    private String getEntityClassName() {
        return this instanceof HibernateProxy
                ? getClassWithoutInitializingProxy(this).getName()
                : this.getClass().getName();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (that == null || !isCompatibleClass(that)) {
            return false;
        }

        final Persistable<?> thatPersistable = (Persistable<?>) that;

        return null != this.getId() && this.getId().equals(thatPersistable.getId());
    }

    private boolean isCompatibleClass(final Object that) {
        /**
         * Hibernate specific work-around: Object class cannot be compared directly.
         * Association lazy-fetch for @ManyToOne and @OneToOne use Hibernate proxy-objects,
         * which need to be ignored.
         */

        final Class<?> thatClass = getClassWithoutInitializingProxy(that);
        final Class<?> thisClass = getClassWithoutInitializingProxy(this);

        return thisClass.isAssignableFrom(thatClass) || thatClass.isAssignableFrom(thisClass);
    }

    public static Class<?> getClassWithoutInitializingProxy(Object object) {
        if (object instanceof HibernateProxy) {
            final HibernateProxy proxy = (HibernateProxy) object;
            return proxy.getHibernateLazyInitializer().getPersistentClass();
        }
        return object.getClass();
    }

    @Override
    public int hashCode() {
        /**
         * WARNING: Do not use collection relying on hashCode() such as
         * HashMap etc. before entity has been persisted. This implementation
         * breaks the constraint of hashCode immutability!
         *
         * Alternative: Generate UUID in constructor for hashCode().
         */
        return null == getId() ? 0 : 17 + getId().hashCode() * 31;
    }

    protected static long getActiveUserId() {
        return UserInfo.extractUserIdForEntity(SecurityContextHolder.getContext().getAuthentication());
    }
}
