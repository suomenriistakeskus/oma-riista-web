package fi.riista.api.external;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportFeature;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportException;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/anon/moosedatacard")
public class ExternalMooseDataCardImportApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalMooseDataCardImportApiResource.class);

    @Resource
    private MooseDataCardImportFeature importFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> importMooseDataCard(
            @RequestParam final MultipartFile xmlFile, @RequestParam final MultipartFile pdfFile) {

        LOG.debug("Moose data card upload request received via anonymous API");

        final SecurityContext sc = SecurityContextHolder.getContext();

        sc.setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Populated SecurityContextHolder with anonymous token: '" + sc.getAuthentication() + "'");
        }

        try {
            return ResponseEntity.ok(toMap(importFeature.importMooseDataCardWithSpecialPrivilege(xmlFile, pdfFile)));
        } catch (final MooseDataCardImportException e) {
            return ResponseEntity.badRequest().body(toMap(e.getMessages()));
        }
    }

    private static ImmutableMap<String, List<String>> toMap(final List<String> messages) {
        return ImmutableMap.of("messages", messages);
    }

}
