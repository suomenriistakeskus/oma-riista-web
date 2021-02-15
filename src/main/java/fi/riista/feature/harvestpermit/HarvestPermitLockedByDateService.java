package fi.riista.feature.harvestpermit;

import fi.riista.config.Constants;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.joda.time.LocalDate;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.util.DateUtil.today;

@Component
public class HarvestPermitLockedByDateService {

    private static boolean isTodayPastHuntingYearLockedDate(final int huntingYear) {
        return today().isAfter(new LocalDate(huntingYear + 1, 3, 31));
    }

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private Environment springEnvironment;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    private boolean disableForTesting = false;

    private boolean lockTestEnabled() {
        if (springEnvironment.acceptsProfiles(Profiles.of(Constants.EMBEDDED_DATABASE))) {
            return !disableForTesting;
        }

        return runtimeEnvironmentUtil.isProductionEnvironment();
    }

    public void disableLockingForTests() {
        disableForTesting = true;
    }

    public void normalLocking() {
        disableForTesting = false;
    }

    public boolean isDateLockedForHuntingYear(final int huntingYear) {
        return lockTestEnabled() && isTodayPastHuntingYearLockedDate(huntingYear);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isPermitLockedByDateForHuntingYear(final @Nonnull HarvestPermit permit, final int huntingYear) {
        return lockTestEnabled() && permit.isMooselikePermitType() && isTodayPastHuntingYearLockedDate(huntingYear);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isPermitLocked(final @Nonnull HarvestPermitSpeciesAmount speciesAmount) {
        return isPermitLockedByDateForHuntingYear(speciesAmount.getHarvestPermit(), speciesAmount.resolveHuntingYear());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isPermitLocked(final @Nonnull HuntingClubGroup group) {
        return group.getHarvestPermit() != null && isPermitLockedByDateForHuntingYear(group.getHarvestPermit(), group.getHuntingYear());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isMoosePermitLockDatePassed(final HarvestPermit permit) {
        return harvestPermitSpeciesAmountRepository
                .findMooseAmounts(permit) // in real world scenarios returns only one result
                .stream()
                .anyMatch(this::isPermitLocked);
    }
}
