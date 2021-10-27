package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

public class OtherwiseDeceasedExcelFeatureTest extends EmbeddedDatabaseTest {

   @Resource
   OtherwiseDeceasedExcelFeature excelFeature;

   private SystemUser privilegedUser;
   private int currentYear;

   @Before
   public void setUp() {
       privilegedUser = createNewModerator(SystemUserPrivilege.MUUTOIN_KUOLLEET);
       currentYear = DateUtil.currentYear();
   }

   @Test(expected = AccessDeniedException.class)
   public void exportToExcel_failsWithoutPrivilege() {
       onSavedAndAuthenticated(createNewModerator(), () -> excelFeature.exportToExcel(filterForYear(currentYear), Locales.FI));
   }

    @Test
    public void exportToExcel_willSucceedWhenModeratorHasPrivileges() {
        onSavedAndAuthenticated(privilegedUser, () -> {
            excelFeature.exportToExcel(filterForYear(DateUtil.currentYear()), Locales.FI);
        });
    }

    private OtherwiseDeceasedFilterDTO filterForYear(final int year) {
        final OtherwiseDeceasedFilterDTO filter = new OtherwiseDeceasedFilterDTO();
        filter.setBeginDate(DateUtil.beginOfCalendarYear(year).toLocalDate());
        filter.setEndDate(DateUtil.beginOfCalendarYear(year).plusYears(1).minusMillis(1).toLocalDate());
        return filter;
    }
}
