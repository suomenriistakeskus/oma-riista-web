package fi.riista.api.external;

import fi.riista.feature.vetuma.VetumaLoginFeature;
import fi.riista.feature.vetuma.dto.VetumaLoginResponseDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

@Controller
public class VetumaResponseController {

    private static final Logger LOG = LoggerFactory.getLogger(VetumaResponseController.class);

    // User is forwarded back from Vetuma using respective URL for different situations.
    public static final String CALLBACK_VETUMA_SUCCESS = "/vetuma/login-response-success";
    public static final String CALLBACK_VETUMA_CANCEL = "/vetuma/login-response-cancel";
    public static final String CALLBACK_VETUMA_ERROR = "/vetuma/login-response-error";

    // After Vetuma response data and status code has been processed, the user is redirected to Angular UI
    public static final String REDIRECT_SUCCESS = "redirect:/#/register/from-vetuma/success";
    public static final String REDIRECT_CANCEL = "redirect:/#/register/from-vetuma/cancel";
    public static final String REDIRECT_ERROR = "redirect:/#/register/from-vetuma/failure";

    @Resource
    private VetumaLoginFeature feature;

    @ExceptionHandler(Exception.class)
    public String handleAllErrors(Exception ex) {
        LOG.error("Unknown error while processing response from Vetuma", ex);

        return REDIRECT_ERROR;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.POST,
            value = CALLBACK_VETUMA_SUCCESS, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String success(final VetumaLoginResponseDTO response) {
        LOG.debug("Vetuma login successful for TRID={}", response.getTRID());

        if (feature.handleSuccess(response)) {
            return REDIRECT_SUCCESS + "/" + response.getTRID();
        }

        return REDIRECT_ERROR;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.POST,
            value = CALLBACK_VETUMA_CANCEL, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String cancel(final VetumaLoginResponseDTO response) {
        LOG.debug("Vetuma login cancelled, response: " + response.toString());

        feature.handleCancel(response);

        return REDIRECT_CANCEL;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.POST,
            value = CALLBACK_VETUMA_ERROR, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String error(final VetumaLoginResponseDTO response) {
        LOG.error("Vetuma login failed, response: " + response.toString());

        feature.handleError(response);

        return REDIRECT_ERROR;
    }
}
