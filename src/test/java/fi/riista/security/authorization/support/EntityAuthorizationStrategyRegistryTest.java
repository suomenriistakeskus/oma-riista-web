package fi.riista.security.authorization.support;

import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.spi.EntityAuthorizationStrategy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

public class EntityAuthorizationStrategyRegistryTest {

    @Mock
    private EntityAuthorizationStrategy authorizationStrategy;

    @Mock
    private EntityAuthorizationTarget target;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private EntityAuthorizationStrategyRegistry create() {
        EntityAuthorizationStrategyRegistry registry = new EntityAuthorizationStrategyRegistry();

        Mockito.when(authorizationStrategy.getEntityName()).thenReturn("java.lang.Integer");
        Mockito.when(authorizationStrategy.getSupportedTypes()).thenReturn(new Class[] { Double.class });

        ReflectionTestUtils.setField(registry, "strategies", Collections.singletonList(authorizationStrategy));

        registry.configure();

        return registry;
    }

    @Test
    public void testWithNullReference() {
        EntityAuthorizationStrategyRegistry registry = create();

        EntityAuthorizationStrategy strategy = registry.lookupAuthorizationStrategy(null);

        Assert.assertNotNull(strategy);
        Assert.assertEquals("notImplemented", strategy.getEntityName());
        Assert.assertArrayEquals(new Class[] { Void.class }, strategy.getSupportedTypes());
        Assert.assertFalse(strategy.hasPermission(null, null, null));
    }

    @Test
    public void testWithInvalidTarget() {
        EntityAuthorizationStrategyRegistry registry = create();

        EntityAuthorizationStrategy strategy = registry.lookupAuthorizationStrategy(target);

        Assert.assertNotNull(strategy);
        Assert.assertEquals("notImplemented", strategy.getEntityName());
    }

    @Test
    public void testWithEntityName() {
        Mockito.when(target.getAuthorizationTargetName()).thenReturn("java.lang.Integer");

        EntityAuthorizationStrategyRegistry registry = create();

        EntityAuthorizationStrategy strategy = registry.lookupAuthorizationStrategy(target);

        Assert.assertNotNull(strategy);
        Assert.assertTrue(strategy == authorizationStrategy);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testWithCanonicalClassName() {
        Mockito.when(target.getAuthorizationTargetClass()).thenReturn((Class) Integer.class);

        EntityAuthorizationStrategyRegistry registry = create();

        EntityAuthorizationStrategy strategy = registry.lookupAuthorizationStrategy(target);

        Assert.assertNotNull(strategy);
        Assert.assertTrue(strategy == authorizationStrategy);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testWithEntityClass() {
        Mockito.when(target.getAuthorizationTargetClass()).thenReturn((Class) Double.class);

        EntityAuthorizationStrategyRegistry registry = create();

        EntityAuthorizationStrategy strategy = registry.lookupAuthorizationStrategy(target);

        Assert.assertNotNull(strategy);
        Assert.assertTrue(strategy == authorizationStrategy);
    }

}
