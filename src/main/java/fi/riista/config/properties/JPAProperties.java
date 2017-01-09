package fi.riista.config.properties;

import fi.riista.config.BatchConfig;
import fi.riista.config.Constants;
import fi.riista.config.jpa.CustomHibernateNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.loader.BatchFetchStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@PropertySource("classpath:configuration/jpa.properties")
public class JPAProperties {
    @Value("${hibernate.show_sql}")
    private boolean showSql;

    @Value("${hibernate.format_sql}")
    private boolean formatSql;

    // @see org.hibernate.cfg.AvailableSettings
    public Map<String, Object> build() {
        final Map<String, Object> props = new HashMap<>();

        props.put("hibernate.connection.charSet", Constants.DEFAULT_ENCODING);
        props.put(AvailableSettings.SHOW_SQL, showSql);
        props.put(AvailableSettings.FORMAT_SQL, formatSql);

        props.put("jadira.usertype.autoRegisterUserTypes", "true");
        props.put("jadira.usertype.currencyCode", "EUR");
        props.put("jadira.usertype.seed", "org.jadira.usertype.spi.shared.JvmTimestampSeed");
        props.put("jadira.usertype.useJdbc42Apis", "false");

        props.put(org.hibernate.jpa.AvailableSettings.VALIDATION_MODE, "callback, ddl");
        props.put(org.hibernate.jpa.AvailableSettings.PERSIST_VALIDATION_GROUP, "javax.validation.groups.Default");
        props.put(org.hibernate.jpa.AvailableSettings.UPDATE_VALIDATION_GROUP, "javax.validation.groups.Default");

        props.put(AvailableSettings.LOG_SESSION_METRICS, false);
        props.put(org.hibernate.jpa.AvailableSettings.NAMING_STRATEGY, CustomHibernateNamingStrategy.class.getName());
        props.put(org.hibernate.jpa.AvailableSettings.LOCK_TIMEOUT, "15000");
        props.put(AvailableSettings.MAX_FETCH_DEPTH, 1);
        props.put(AvailableSettings.STATEMENT_BATCH_SIZE, BatchConfig.BATCH_SIZE);
        props.put(AvailableSettings.BATCH_VERSIONED_DATA, true);
        props.put(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, 16);
        props.put(AvailableSettings.BATCH_FETCH_STYLE, BatchFetchStyle.PADDED);
        props.put(AvailableSettings.ORDER_UPDATES, true);
        props.put(AvailableSettings.ORDER_INSERTS, true);

        return props;
    }
}
