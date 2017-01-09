package fi.riista.feature.account.audit;

import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.account.user.ActiveUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @Mock
    private ActiveUserService activeUserService;

    @Test
    public void testExtraFailsWithNullKey() {
        try {
            auditService.extra(null, new Object());
            fail();
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().startsWith("null key in entry:"));
        }
    }

    @Test
    public void testExtraFailsWithNullValue() {
        try {
            auditService.extra("foo", null);
            fail();
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().startsWith("null value in entry: foo=null"));
        }
    }

    @Test
    public void testLogDoesntFailOnNulls() {
        auditService.log((String) null, (Object) null);
    }

    @Test
    public void testLogWithExtraBuilderDoesntFailOnNulls() {
        auditService.log((String) null, (Object) null, auditService.extra("", ""));
    }

    @Test
    public void testLogWithExtraMapDoesntFailOnNulls() {
        Map<String, Object> map = new HashMap<>();
        map.put(null, null);
        auditService.log((String) null, (Object) null, map);
    }
}
