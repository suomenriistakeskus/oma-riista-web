package fi.riista.security.aop;

import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.security.UserInfo;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/*
 * Custom methods to use in @PreAuthorize authorization checks.
 *
 * NOTE: This is mostly copy of the package private DefaultMethodSecurityExpressionRoot which cannot be extended.
 */
public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomMethodSecurityExpressionRoot(Authentication a) {
        super(a);
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    void setThis(Object target) {
        this.target = target;
    }

    @Override
    public Object getThis() {
        return target;
    }

    public boolean hasPrivilege(SystemUserPrivilege privilege) {
        return UserInfo.extractFrom(authentication).hasPrivilege(privilege);
    }
}
