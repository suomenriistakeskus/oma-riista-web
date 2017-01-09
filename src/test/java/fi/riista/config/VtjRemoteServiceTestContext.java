package fi.riista.config;

import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.integration.vtj.VtjConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@Configuration
@Import(VtjConfig.class)
@ComponentScan({"fi.riista.integration.vtj"})
public class VtjRemoteServiceTestContext {
    @Bean
    public PersonRepository personRepository() {
        return mock(PersonRepository.class);
    }

    @Bean
    public UserRepository userRepository() {
        return mock(UserRepository.class);
    }

    @Bean
    public MunicipalityRepository municipalityRepository() {
        return mock(MunicipalityRepository.class);
    }

    @Bean
    public ActiveUserService activeUserService() {
        return mock(ActiveUserService.class);
    }

    @Bean
    public AccountAuditService accountAuditService() {
        return mock(AccountAuditService.class);
    }
}
