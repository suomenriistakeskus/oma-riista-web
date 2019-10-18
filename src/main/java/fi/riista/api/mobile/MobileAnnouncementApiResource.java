package fi.riista.api.mobile;

import fi.riista.config.Constants;
import fi.riista.feature.announcement.show.MobileAnnouncementDTO;
import fi.riista.feature.announcement.show.MobileAnnouncementFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.huntingYearBeginDate;

@RestController
@RequestMapping(value = "/api/mobile/v2/announcement")
public class MobileAnnouncementApiResource {

    @Resource
    private MobileAnnouncementFeature mobileAnnouncementFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MobileAnnouncementDTO> listAnnouncements(
            @PageableDefault(size = 500, sort = "id", direction = Sort.Direction.ASC) Pageable pageRequest,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") final LocalDateTime since) {
        if (since != null) {
            final DateTime dateTime = since.toDateTime(Constants.DEFAULT_TIMEZONE);

            return mobileAnnouncementFeature.listMobileAnnouncements(dateTime, pageRequest);

        } else {
            final LocalDate beginDate = huntingYearBeginDate(huntingYear() - 1);
            final DateTime dateTime = beginDate.toDateTimeAtStartOfDay();

            return mobileAnnouncementFeature.listMobileAnnouncements(dateTime, pageRequest);
        }
    }

}
