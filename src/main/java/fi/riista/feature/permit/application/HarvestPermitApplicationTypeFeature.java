package fi.riista.feature.permit.application;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Component
public class HarvestPermitApplicationTypeFeature {

    @Resource
    private RequireEntityService requireEntityService;

    public List<HarvestPermitApplicationTypeDTO> listTypes() {
        return ImmutableList.of(mooselikeType2018());
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationTypeDTO findTypeForApplication(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ);
        final String permitTypeCode = application.getPermitTypeCode();
        final int huntingYear = application.getHuntingYear();
        if (HarvestPermit.MOOSELIKE_PERMIT_TYPE.equals(permitTypeCode) && huntingYear == 2018) {
            return mooselikeType2018();
        }
        throw new IllegalArgumentException("Unknown permitTypeCode:" + permitTypeCode);
    }

    private HarvestPermitApplicationTypeDTO mooselikeType2018() {
        final LocalDate begin = new LocalDate(2018, 4, 3);
        final LocalDate end = new LocalDate(2018, 4, 30);
        final LocalDate today = DateUtil.today();
        final boolean active = DateUtil.overlapsInclusive(begin, end, today);
        final BigDecimal price = PermitDecision.DECISION_PRICE_MOOSELIKE;

        return new HarvestPermitApplicationTypeDTO(2018, "100", begin, end, active, price);
    }
}
