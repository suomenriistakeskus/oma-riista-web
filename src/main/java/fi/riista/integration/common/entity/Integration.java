package fi.riista.integration.common.entity;

import fi.riista.feature.common.entity.LifecycleEntity;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class Integration extends LifecycleEntity<String> {

    public static final String LH_PERMIT_IMPORT_ID = "lh_permit_import";
    public static final String EMAIL_HUNTING_LEADERS_TO_RHY_COORDINATOR_ID = "email_hunting_leaders_to_rhy_coordinator";
    public static final String RHY_ANNUAL_STATISTICS_MODERATOR_UPDATE_NOTIFICATION_ID = "rhy_annual_statistics_moderator_update_notification";
    public static final String HARVEST_REGISTRY_SYNC_ID = "harvest_registry_synchronization";

    private String id;

    @Column
    private DateTime lastRun;

    @Override
    @Id
    @Access(value = AccessType.PROPERTY)
    @Column(name = "integration_id", nullable = false)
    @Size(max = 255)
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    public DateTime getLastRun() {
        return lastRun;
    }

    public void setLastRun(final DateTime lastRun) {
        this.lastRun = lastRun;
    }
}
