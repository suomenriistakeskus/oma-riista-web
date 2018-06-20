package fi.riista.config;

import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.integration.vtj.VtjConfig;
import fi.riista.test.MockitoFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(VtjConfig.class)
@ComponentScan("fi.riista.integration.vtj")
public class VtjRemoteServiceTestContext {
    @Bean
    public FactoryBean<PersonRepository> personRepository() {
        return new MockitoFactoryBean<>(PersonRepository.class);
    }

    @Bean
    public FactoryBean<UserRepository> userRepository() {
        return new MockitoFactoryBean<>(UserRepository.class);
    }

    @Bean
    public FactoryBean<MunicipalityRepository> municipalityRepository() {
        return new MockitoFactoryBean<>(MunicipalityRepository.class);
    }

    @Bean
    public FactoryBean<ActiveUserService> activeUserService() {
        return new MockitoFactoryBean<>(ActiveUserService.class);
    }

    @Bean
    public FactoryBean<AccountAuditService> accountAuditService() {
        return new MockitoFactoryBean<>(AccountAuditService.class);
    }
}
