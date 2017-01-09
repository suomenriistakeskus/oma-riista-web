package fi.riista.api.admin;

import fi.riista.integration.srva.callring.SrvaUpdateCallRingFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class AdminJobController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminJobController.class);

    @Resource
    private SrvaUpdateCallRingFeature srvaUpdateCallRingFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/api/v1/admin/srva/callring-sync", method = RequestMethod.GET)
    public void execute() {
        try {
            srvaUpdateCallRingFeature.configureAll();
        } catch (final Exception ex) {
            LOG.error("SRVA callRing sync has failed", ex);
        }
    }
}
