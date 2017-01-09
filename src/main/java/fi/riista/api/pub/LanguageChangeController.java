package fi.riista.api.pub;

import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

@RestController
@RequestMapping(produces = MediaType.TEXT_PLAIN_VALUE)
public class LanguageChangeController {
    private static final Logger LOG = LoggerFactory.getLogger(LanguageChangeController.class);

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/api/v1/language", method = RequestMethod.GET)
    public String getLanguage(HttpServletRequest request) {
        final LocaleResolver localeResolver = getLocaleResolver(request);

        return localeResolver.resolveLocale(request).toLanguageTag();
    }

    @RequestMapping(value = "/api/v1/language", method = RequestMethod.POST)
    public String changeLanguage(@ModelAttribute("lang") String language,
                                 HttpServletRequest request, HttpServletResponse response) {
        final LocaleResolver localeResolver = getLocaleResolver(request);
        final Locale locale = StringUtils.parseLocaleString(language);

        if (locale != null) {
            localeResolver.setLocale(request, response, locale);

            return locale.toLanguageTag();
        }

        LOG.error("Could not parse language={}", language);

        return localeResolver.resolveLocale(request).toLanguageTag();
    }

    private static LocaleResolver getLocaleResolver(HttpServletRequest request) {
        final LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);

        if (localeResolver == null) {
            throw new IllegalStateException("No LocaleResolver found: not in a DispatcherServlet request?");
        }

        return localeResolver;
    }
}
