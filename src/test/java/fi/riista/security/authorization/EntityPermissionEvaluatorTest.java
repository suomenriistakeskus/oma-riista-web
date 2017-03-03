package fi.riista.security.authorization;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.security.authentication.TestAuthenticationTokenUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class EntityPermissionEvaluatorTest {

    private static class SimpleEntity extends BaseEntity<Long> {
        private Long id = 1L;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }
    }

    @Mock
    private EntityAuthorizationStrategy<SimpleEntity> authorizationStrategy;

    private EntityPermissionEvaluator evaluator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.evaluator = createEvaluator();
    }

    private EntityPermissionEvaluator createEvaluator() {
        when(authorizationStrategy.getEntityClass()).thenReturn(SimpleEntity.class);
        when(authorizationStrategy.hasPermission(any(SimpleEntity.class),
                eq(READ), any(Authentication.class))).thenReturn(true);
        when(authorizationStrategy.hasPermission(any(SimpleEntity.class),
                eq(UPDATE), any(Authentication.class))).thenReturn(true);
        when(authorizationStrategy.hasPermission(any(SimpleEntity.class),
                eq(DELETE), any(Authentication.class))).thenReturn(false);

        return new EntityPermissionEvaluator(Collections.singletonList(authorizationStrategy));
    }

    @Test
    public void testWithEntity() {
        final SimpleEntity entity = new SimpleEntity();

        final Authentication authentication = TestAuthenticationTokenUtil.createUserAuthentication();
        assertTrue(evaluator.hasPermission(authentication, entity, READ));
        assertTrue(evaluator.hasPermission(authentication, entity, UPDATE));
        assertFalse(evaluator.hasPermission(authentication, entity, DELETE));
    }
}
