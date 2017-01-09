package fi.riista.config.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class CustomJacksonObjectMapper extends ObjectMapper {

    public CustomJacksonObjectMapper(boolean developmentEnvironment) {
        super();

        // Only map fields and ignore get/set methods
        setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        configure(SerializationFeature.INDENT_OUTPUT, developmentEnvironment);

        // Skip null map properties
        disable(SerializationFeature.WRITE_NULL_MAP_VALUES);

        // Export dates as strings
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignore unknown properties if in production
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, developmentEnvironment);
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, developmentEnvironment);

        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, !developmentEnvironment);

        // Joda time date handling
        registerModule(new JodaModule());

        // Dynamic byte-code optimization
        registerModule(new AfterburnerModule());

        // Allow returning Spring Data JPA Page<?> from controller
        final SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Page.class, PageResponse.class);
        registerModule(module);
    }
}
