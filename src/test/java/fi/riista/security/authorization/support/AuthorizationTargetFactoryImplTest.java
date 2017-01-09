package fi.riista.security.authorization.support;

import fi.riista.feature.account.user.SystemUserDTO;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.domain.Persistable;

public class AuthorizationTargetFactoryImplTest {
    private AuthorizationTargetFactoryImpl factory = new AuthorizationTargetFactoryImpl();

    @Test
    public void testForNullReference() {
        Assert.assertNull("Should not return target", factory.create(null));
    }

    @Test
    public void testCreateForString() {
        Assert.assertNotNull("Should support String argument", factory.create("user:1234"));
        Assert.assertNotNull("Should support String argument without key", factory.create("user:"));
        Assert.assertNotNull("Should support String argument without delimiter", factory.create("user"));
    }

    @Test
    public void testCreateForEntity() {
        Assert.assertNotNull("Should support entity", factory.create(new SystemUser()));
    }

    @Test
    public void testCreateForDTO() {
        Assert.assertNotNull("Should support DTO", factory.create(new SystemUserDTO()));
    }

    @Test
    public void testToString() {
        EntityAuthorizationTarget target = factory.create("user:1234");
        Assert.assertEquals("Wrong message", "AuthorizationTargetImpl{type=user, id=1234, class=null}", target.toString());
    }

    @Test
    public void testCreateForGenericObject() {
        Assert.assertNotNull("Should support any object", factory.create(new GeneralClassWithIdProperty()));
        Assert.assertNotNull("Should support any object", factory.create(new Object()));
        Assert.assertNotNull("Should support any object", factory.create(new Double(1)));
    }

    @Test
    public void testCreateForGenericClass() {
        Assert.assertNotNull("Should support any class", factory.create(Object.class));
        Assert.assertNotNull("Should support any class", factory.create(SystemUser.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testWithInterface() {
        final Double object = new Double(Math.PI);
        EntityAuthorizationTarget mock = Mockito.mock(EntityAuthorizationTarget.class);

        Mockito.when(mock.getAuthorizationTarget(ArgumentMatchers.any())).thenReturn(object);
        Mockito.when(mock.getAuthorizationTargetClass()).thenReturn((Class) Double.class);
        Mockito.when(mock.getAuthorizationTargetId()).thenReturn(new Long(123));
        Mockito.when(mock.getAuthorizationTargetName()).thenReturn("mockDouble");

        EntityAuthorizationTarget target = factory.create(mock);

        Assert.assertEquals("Wrong type", "mockDouble", target.getAuthorizationTargetName());
        Assert.assertEquals("Wrong id", new Long(123), target.getAuthorizationTargetId());
        Assert.assertEquals("Should have correct class", Double.class, target.getAuthorizationTargetClass());
        Assert.assertTrue("Should have reference", target.getAuthorizationTarget(Double.class) == object);
    }

    @Test
    public void testCreateForReference() {
        EntityAuthorizationTarget target = factory.create("randomEntity", new Long(958));

        Assert.assertEquals("Wrong type", "randomEntity", target.getAuthorizationTargetName());
        Assert.assertEquals("Wrong id", new Long(958), target.getAuthorizationTargetId());
        Assert.assertNull("Should have unknown class", target.getAuthorizationTargetClass());
        Assert.assertNull("Should have no reference", target.getAuthorizationTarget(String.class));
    }

    @Test
    public void testForPersistentEntity() {
        SystemUser user = new SystemUser();
        user.setId(new Long(512));

        EntityAuthorizationTarget target = AuthorizationTargetFactoryImpl.forPersistentEntity(user);

        Assert.assertEquals("Wrong type", SystemUser.class.getCanonicalName(), target.getAuthorizationTargetName());
        Assert.assertEquals("Wrong id", new Long(512), target.getAuthorizationTargetId());
        Assert.assertEquals("Should have correct class", SystemUser.class, target.getAuthorizationTargetClass());
        Assert.assertTrue("Should have reference", target.getAuthorizationTarget(SystemUser.class) == user);
    }

    @Test
    public void testForDTO() {
        SystemUserDTO userDTO = new SystemUserDTO();
        userDTO.setId(new Long(256));

        EntityAuthorizationTarget target = AuthorizationTargetFactoryImpl.forDTO(userDTO);

        Assert.assertEquals("Wrong type", SystemUserDTO.class.getCanonicalName(), target.getAuthorizationTargetName());
        Assert.assertEquals("Wrong id", new Long(256), target.getAuthorizationTargetId());
        Assert.assertEquals("Should have correct class", SystemUserDTO.class, target.getAuthorizationTargetClass());
        Assert.assertTrue("Should have reference", target.getAuthorizationTarget(SystemUserDTO.class) == userDTO);
    }

    @Test
    public void testGetReferenceWithImplementedInterface() {
        EntityAuthorizationTarget target = factory.create(new SystemUser());

        Assert.assertNotNull("Should give reference", target.getAuthorizationTarget(BaseEntity.class));
        Assert.assertNotNull("Should give reference", target.getAuthorizationTarget(Persistable.class));
        Assert.assertNotNull("Should give reference", target.getAuthorizationTarget(Object.class));
    }

    @Test
    public void testGetReferenceWithDifferentTargetClass() {
        EntityAuthorizationTarget target = factory.create(new SystemUser());

        Assert.assertNull("Should not give reference", target.getAuthorizationTarget(Double.class));
    }

    @Test
    public void testDecodeFromString() {
        EntityAuthorizationTarget target = AuthorizationTargetFactoryImpl.decodeFromString("userEntity:31243");

        Assert.assertEquals("Wrong type", "userEntity", target.getAuthorizationTargetName());
        Assert.assertEquals("Wrong id", new Long(31243), target.getAuthorizationTargetId());
        Assert.assertNull("Should have unknown class", target.getAuthorizationTargetClass());
        Assert.assertNull("Should have no reference", target.getAuthorizationTarget(String.class));
    }

    @Test
    public void testDecodeFromStringWithoutPrimaryKey() {
        EntityAuthorizationTarget target = AuthorizationTargetFactoryImpl.decodeFromString("userEntity");

        Assert.assertEquals("Wrong type", "userEntity", target.getAuthorizationTargetName());
        Assert.assertNull("Should not have id", target.getAuthorizationTargetId());
        Assert.assertNull("Should have unknown class", target.getAuthorizationTargetClass());
        Assert.assertNull("Should have no reference", target.getAuthorizationTarget(String.class));
    }

    @Test
    public void testDecodeFromStringWithoutTypeOrPrimaryKey() {
        Assert.assertNull("Should not return target", AuthorizationTargetFactoryImpl.decodeFromString(""));
        Assert.assertNull("Should not return target", AuthorizationTargetFactoryImpl.decodeFromString("   "));
        Assert.assertNull("Should not return target", AuthorizationTargetFactoryImpl.decodeFromString(":"));
    }

    @Test
    public void testCreateForGeneralObject() {
        final Double object = new Double(Math.PI);
        EntityAuthorizationTarget target = AuthorizationTargetFactoryImpl.forObject(object);

        Assert.assertEquals("Wrong type", "java.lang.Double", target.getAuthorizationTargetName());
        Assert.assertNull("Should not have id", target.getAuthorizationTargetId());
        Assert.assertEquals("Should have correct class", Double.class, target.getAuthorizationTargetClass());
        Assert.assertTrue("Should have reference", target.getAuthorizationTarget(Double.class) == object);
    }

    @Test
    public void testCreateForGeneralClass() {
        AuthorizationTargetImpl<Class<?>> target = AuthorizationTargetFactoryImpl.forClass(SystemUser.class);

        Assert.assertEquals("Wrong type", "SystemUser", target.getAuthorizationTargetName());
        Assert.assertNull("Should not have id", target.getAuthorizationTargetId());
        Assert.assertEquals("Should have correct class", SystemUser.class, target.getAuthorizationTargetClass());
        Assert.assertNull("Should have no reference", target.getAuthorizationTarget(SystemUser.class));
    }

    @Test
    public void testCreateForGeneralObjectWithIdProperty() {
        final GeneralClassWithIdProperty object = new GeneralClassWithIdProperty();

        EntityAuthorizationTarget target = AuthorizationTargetFactoryImpl.forObject(object);

        Assert.assertEquals("Wrong type", this.getClass().getCanonicalName() + ".GeneralClassWithIdProperty",
                target.getAuthorizationTargetName());
        Assert.assertEquals("Wrong id", new Long(256), target.getAuthorizationTargetId());
        Assert.assertEquals("Should have correct class", GeneralClassWithIdProperty.class, target.getAuthorizationTargetClass());
        Assert.assertTrue("Should have reference", target.getAuthorizationTarget(GeneralClassWithIdProperty.class) == object);
    }

    public static class GeneralClassWithIdProperty {
        public Long getId() {
            return 256l;
        }
    }
}
