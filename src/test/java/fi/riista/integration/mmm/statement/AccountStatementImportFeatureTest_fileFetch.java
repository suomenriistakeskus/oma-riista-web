package fi.riista.integration.mmm.statement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.error.NotFoundException;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import fi.riista.integration.mmm.transfer.AccountTransferBatchRepository;
import fi.riista.integration.mmm.transfer.AccountTransferRepository;
import fi.riista.integration.mmm.transfer.AccountTransfer_;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.BigDecimalComparison;
import io.vavr.Tuple3;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.PathComponents;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE_OP;
import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE_OP_2;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.ValueGenerator.zeroPaddedNumber;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang.StringUtils.leftPad;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AccountStatementImportFeatureTest_fileFetch  {

    @Test
    public void testFetchAccountStatements_noFiles() {
        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(emptyList(), emptyMap());
        assertThat(filesToFetch, is(empty()));
    }

    @Test
    public void testFetchAccountStatements_oldPattern_tooOldFile() {
        final LocalDate tooOldDate = AccountStatementImportFeature.getSearchPeriod().lowerEndpoint().minusDays(1);
        final RemoteResourceInfo resource = createResource(tooOldDate);

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), emptyMap());
        assertThat(filesToFetch, is(empty()));

    }

    @Test
    public void testFetchAccountStatements_oldPattern() {
        final LocalDate today = today();
        final RemoteResourceInfo resource = createResource(today);

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), emptyMap());
        assertThat(filesToFetch, hasSize(1));
        assertThat(filesToFetch.get(0)._2, equalTo(today));
    }

    @Test
    public void testFetchAccountStatements_oldPattern_doesNotFetchSameFileAgain() {
        final LocalDate today = today();
        final RemoteResourceInfo resource = createResource(today);

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), singletonMap(today, 0));
        assertThat(filesToFetch, is(empty()));
    }

    @Test
    public void testFetchAccountStatements_newPattern_tooOldFile() {
        final LocalDate tooOldDate = today().minusDays(91);
        final RemoteResourceInfo resource = createResource(tooOldDate, "1");

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), emptyMap());
        assertThat(filesToFetch, is(empty()));

    }

    @Test
    public void testFetchAccountStatements_newPattern() {
        final LocalDate today = today();
        final RemoteResourceInfo resource = createResource(today, "1");

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), emptyMap());
        assertThat(filesToFetch, hasSize(1));
        assertThat(filesToFetch.get(0)._2, equalTo(today));
    }

    @Test
    public void testFetchAccountStatements_newPattern_doesNotFetchSameFileAgain() {
        final LocalDate today = today();
        final RemoteResourceInfo resource = createResource(today, "1");

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), singletonMap(today, 1));
        assertThat(filesToFetch, is(empty()));
    }

    @Test
    public void testFetchAccountStatements_newPattern_newFileForSameDate() {
        final LocalDate today = today();
        final RemoteResourceInfo resource = createResource(today, "2");

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), singletonMap(today, 1));
        assertThat(filesToFetch, hasSize(1));
        assertThat(filesToFetch.get(0)._2, equalTo(today));
        assertThat(filesToFetch.get(0)._1, equalTo(resource.getName()));
    }

    @Test
    public void testFetchAccountStatements_newPattern_newFileForSameDate_zeroPaddedFileNumber() {
        final LocalDate today = today();
        final RemoteResourceInfo resource = createResource(today, "02");

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), singletonMap(today, 1));
        assertThat(filesToFetch, hasSize(1));
        assertThat(filesToFetch.get(0)._2, equalTo(today));
        assertThat(filesToFetch.get(0)._1, equalTo(resource.getName()));
    }

    @Test
    public void testFetchAccountStatements_newPattern_newFileForYesterday() {
        final LocalDate yesterday = today().minusDays(1);
        final RemoteResourceInfo resource = createResource(yesterday, "2");

        // Previously got number one for yesterday and today
        final ImmutableMap<LocalDate, Integer> fetchedMap = ImmutableMap.of(yesterday, 1, today(), 1);
        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource), fetchedMap);
        assertThat(filesToFetch, hasSize(1));
        assertThat(filesToFetch.get(0)._2, equalTo(yesterday));
        assertThat(filesToFetch.get(0)._1, equalTo(resource.getName()));
    }

    @Test
    public void testFetchAccountStatements_newPattern_sortsTheResources() {
        final LocalDate yesterday = today().minusDays(1);
        // Should com out in reversed order
        final RemoteResourceInfo resource2 = createResource(yesterday, "2");
        final RemoteResourceInfo resource1 = createResource(yesterday, "1");

        final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                AccountStatementImportFeature.findFilesToBeFetched(ImmutableList.of(resource2, resource1), emptyMap());
        assertThat(filesToFetch, hasSize(2));
        assertThat(filesToFetch.get(0)._2, equalTo(yesterday));
        assertThat(filesToFetch.get(0)._3, equalTo(1));

        assertThat(filesToFetch.get(1)._2, equalTo(yesterday));
        assertThat(filesToFetch.get(1)._3, equalTo(2));
    }

    private static RemoteResourceInfo createResource(final LocalDate localDate) {
        final String year = zeroPaddedNumber(localDate.getYear() - 2000, 2);
        final String month = zeroPaddedNumber(localDate.getMonthOfYear(), 2);
        final String day = zeroPaddedNumber(localDate.getDayOfMonth(), 2);

        final String filename = format("hirviit%s%s%s", day, month, year);
        final PathComponents pathComp = new PathComponents("/siirto", filename, "/");
        return new RemoteResourceInfo(pathComp, FileAttributes.EMPTY);
    }

    private static RemoteResourceInfo createResource(final LocalDate localDate, final String fileNumberString) {
        final String year = zeroPaddedNumber(localDate.getYear() - 2000, 2);
        final String month = zeroPaddedNumber(localDate.getMonthOfYear(), 2);
        final String day = zeroPaddedNumber(localDate.getDayOfMonth(), 2);

        final String filename = format("hirviit%s%s%s_%s", day, month, year, fileNumberString);
        final PathComponents pathComp = new PathComponents("/siirto", filename, "/");
        return new RemoteResourceInfo(pathComp, FileAttributes.EMPTY);
    }

}
