package fi.riista.security.authorization;

import fi.riista.security.authorization.api.AuthorizationTargetFactory;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.spi.AuthorizationAuditListener;
import fi.riista.security.authorization.spi.EntityAuthorizationStrategy;
import fi.riista.security.authorization.support.EntityAuthorizationStrategyRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class EntityPermissionEvaluatorTest {

    @Mock
    private EntityAuthorizationStrategyRegistry strategyRegistry;

    @Mock
    private EntityAuthorizationStrategy authorizationStrategy;

    @Mock
    private AuthorizationTargetFactory targetFactory;

    @Mock
    private EntityAuthorizationTarget target;

    @Mock
    private AuthorizationAuditListener auditListener;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private EntityPermissionEvaluator createEvaluator() {
        EntityPermissionEvaluator evaluator = new EntityPermissionEvaluator();

        ReflectionTestUtils.setField(evaluator, "authorizationStrategyRegistry", strategyRegistry);
        ReflectionTestUtils.setField(evaluator, "authorizationTargetFactory", targetFactory);
        ReflectionTestUtils.setField(evaluator, "auditListener", auditListener);

        when(strategyRegistry.lookupAuthorizationStrategy(any(EntityAuthorizationTarget.class))).thenReturn(authorizationStrategy);

        when(authorizationStrategy.hasPermission(any(EntityAuthorizationTarget.class),
                eq("read"), any(Authentication.class))).thenReturn(true);
        when(authorizationStrategy.hasPermission(any(EntityAuthorizationTarget.class),
                eq("update"), any(Authentication.class))).thenReturn(true);
        when(authorizationStrategy.hasPermission(any(EntityAuthorizationTarget.class),
                eq("write"), any(Authentication.class))).thenReturn(false);

        when(targetFactory.create(any())).thenReturn(target);
        when(targetFactory.create(anyString(), any(Serializable.class))).thenReturn(target);

        return evaluator;
    }

    private static Authentication createAuthentication() {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        return new TestingAuthenticationToken("testUser", "testPassword", authorities);
    }

    @Test
    public void testWithObject() {
        EntityPermissionEvaluator evaluator = createEvaluator();

        Assert.assertTrue(evaluator.hasPermission(createAuthentication(), Math.PI, "read"));
        Assert.assertTrue(evaluator.hasPermission(createAuthentication(), Math.PI, "update"));
        Assert.assertFalse(evaluator.hasPermission(createAuthentication(), Math.PI, "write"));
    }

    @Test
    public void testWithUnknownTarget() {
        EntityPermissionEvaluator evaluator = createEvaluator();

        when(targetFactory.create(any())).thenReturn(null);

        Assert.assertFalse(evaluator.hasPermission(createAuthentication(), Math.PI, "read"));
        Assert.assertFalse(evaluator.hasPermission(createAuthentication(), Math.PI, "update"));
        Assert.assertFalse(evaluator.hasPermission(createAuthentication(), Math.PI, "write"));
    }

    @Test
    public void testWithReference() {
        EntityPermissionEvaluator evaluator = createEvaluator();

        Assert.assertTrue(evaluator.hasPermission(createAuthentication(), 1L, "targetObjectType", "read"));
        Assert.assertTrue(evaluator.hasPermission(createAuthentication(), 1L, "targetObjectType", "update"));
        Assert.assertFalse(evaluator.hasPermission(createAuthentication(), 1L, "targetObjectType", "write"));
    }

    @Test
    public void testWithUnknownReference() {
        EntityPermissionEvaluator evaluator = createEvaluator();

        when(targetFactory.create(anyString(), any(Serializable.class))).thenReturn(null);

        Assert.assertFalse(evaluator.hasPermission(createAuthentication(), 1L, "targetObjectType", "read"));
        Assert.assertFalse(evaluator.hasPermission(createAuthentication(), 1L, "targetObjectType", "update"));
        Assert.assertFalse(evaluator.hasPermission(createAuthentication(), 1L, "targetObjectType", "write"));
    }

    private void verifyAuditListener(final String permission, boolean expectedResult) {
        EntityPermissionEvaluator evaluator = createEvaluator();

        // Invoke
        final Authentication authentication = createAuthentication();
        evaluator.hasPermission(authentication, Math.PI, permission);

        // Verify listener called
        Mockito.verify(auditListener, Mockito.times(1)).onAccessDecision(
                eq(expectedResult), eq(permission),
                same(target),
                same(authentication));

        Mockito.verifyNoMoreInteractions(auditListener);
    }

    @Test
    public void testAuditListenerIsCalledOnGranted() {
        verifyAuditListener("read", true);
    }

    @Test
    public void testAuditListenerIsCalledOnDeny() {
        verifyAuditListener("write", false);
    }
}

