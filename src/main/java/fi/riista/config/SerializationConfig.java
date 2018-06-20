package fi.riista.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.RuntimeEnvironmentUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class SerializationConfig {

    @Bean
    public CustomJacksonObjectMapper globalJacksonObjectMapper(final RuntimeEnvironmentUtil environmentUtil) {
        return new CustomJacksonObjectMapper(!environmentUtil.isProductionEnvironment());
    }

    @Bean
    public XmlMapper globalJacksonXmlMapper() {
        return Jackson2ObjectMapperBuilder.xml()
                .defaultUseWrapper(false)
                .indentOutput(true)
                .modulesToInstall(new JaxbAnnotationModule())
                .build();
    }
}
