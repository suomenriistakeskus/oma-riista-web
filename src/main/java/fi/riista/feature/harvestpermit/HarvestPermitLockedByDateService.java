package fi.riista.feature.harvestpermit;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class HarvestPermitLockedByDateService {

    private boolean disabled = false;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    public boolean isPermitLockedByDateForHuntingYear(final HarvestPermit permit, final int huntingYear) {
        if (disabled) {
            return returnDisabled();
        }
        return Optional.ofNullable(permit)
                .map(p -> p.isMooselikePermitType() && isDateLockedForHuntingYear(huntingYear))
                .orElse(false);
    }

    public boolean isDateLockedForHuntingYear(final int huntingYear) {
        if (disabled) {
            return returnDisabled();
        }
        final LocalDate lockDate = new LocalDate(huntingYear + 1, 3, 31);
        return DateUtil.today().isAfter(lockDate);
    }

    private boolean returnDisabled() {
        if (runtimeEnvironmentUtil.isIntegrationTestEnvironment() || runtimeEnvironmentUtil.isDevelopmentEnvironment()) {
            return false;
        }
        throw new IllegalStateException("Functionality is disabled");
    }

    public void disableLockingForTests() {
        disabled = true;
    }

    public void normalLocking() {
        disabled = false;
    }
}
