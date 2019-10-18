package fi.riista.api.pub;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.RuntimeEnvironmentUtil;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.support.ServletContextResource;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
public class FrontendController {
    private static final Logger LOG = LoggerFactory.getLogger(FrontendController.class);

    private static final String JSP_CLIENT_LOADER = "frontend/client";

    private static final Map<String, String> ASSETS = ImmutableMap.<String, String>builder()
            .put("appVersion", "/frontend/js/app.min.js")
            .put("styleVersion", "/frontend/css/app.css")
            .put("templatesVersion", "/frontend/js/templates.js")
            .put("vendorAngularVersion", "/frontend/js/vendor.angular.min.js")
            .put("vendorOtherVersion", "/frontend/js/vendor.other.min.js")
            .build();

    @Resource
    private ServletContext servletContext;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    private final LoadingCache<String, String> VERSION_CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(final String path) throws Exception {
                    final org.springframework.core.io.Resource resource = new ServletContextResource(servletContext, path);
                    try {
                        byte[] content = FileCopyUtils.copyToByteArray(resource.getInputStream());
                        return DigestUtils.md5DigestAsHex(content);
                    } catch (IOException ex) {
                        LOG.error("Could not calculate MD5 for resource: {}", path);
                        return runtimeEnvironmentUtil.getRevision();
                    }
                }
            });

    /*
     * Loader for SPA-frontend
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @CacheControl(policy = {CachePolicy.NO_CACHE, CachePolicy.NO_STORE, CachePolicy.MUST_REVALIDATE})
    public String showClient(final Model model, final HttpServletResponse response) {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        if (runtimeEnvironmentUtil.isDevelopmentEnvironment()) {
            VERSION_CACHE.invalidateAll();
        }

        ASSETS.forEach((key, value) -> model.addAttribute(key, VERSION_CACHE.getUnchecked(value)));

        return JSP_CLIENT_LOADER;
    }
}
