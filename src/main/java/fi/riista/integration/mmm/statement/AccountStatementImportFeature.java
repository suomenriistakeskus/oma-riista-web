package fi.riista.integration.mmm.statement;

import com.google.common.collect.Range;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.config.Constants;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import fi.riista.integration.mmm.transfer.AccountTransferBatch;
import fi.riista.integration.mmm.transfer.AccountTransferBatchRepository;
import fi.riista.integration.mmm.transfer.AccountTransferRepository;
import fi.riista.integration.mmm.transfer.QAccountTransferBatch;
import fi.riista.util.DateUtil;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.InMemoryDestFile;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.max;
import static fi.riista.util.DateUtil.today;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service
public class AccountStatementImportFeature {

    private static final Logger LOG = LoggerFactory.getLogger(AccountStatementImportFeature.class);

    private static final String DIRECTORY = "/siirto/";
    private static final Charset CHARSET = Charset.forName("ISO-8859-1");

    private static final Pattern FILENAME_PATTERN = Pattern.compile("hirviit(\\d{2})(\\d{2})(\\d{2})");

    private static final Pattern FILENAME_WITH_FILE_NUMBER_PATTERN =
            Pattern.compile(String.format("%s_%s", FILENAME_PATTERN.toString(), "(\\d+)"));
    @Resource
    private AccountTransferRepository transferRepo;

    @Resource
    private AccountTransferBatchRepository batchRepo;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Value("${mmm.accountstatement.import.ipaddress}")
    private String ipAddress;

    @Value("${mmm.accountstatement.import.username}")
    private String username;

    @Value("${mmm.accountstatement.import.password}")
    private String password;

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void importAccountTransfers(@Nonnull final AccountStatement statement) {
        requireNonNull(statement);

        final LocalDate date = statement.getFilenameDate();
        final Integer fileNumber = statement.getFileNumber();

        if (batchRepo.findByFilenameDateAndFileNumber(date, fileNumber).isPresent()) {
            throw new AccountStatementImportException(
                    format("Account statement for date %s is already imported", date));
        }

        try {
            final AccountTransferBatch batch =
                    new AccountTransferBatch(date, statement.getFilename(), statement.getFilenameDate(), fileNumber);
            batchRepo.save(batch);

            final List<AccountStatementLine> lines = statement.getLines();

            lines.forEach(line -> {
                final AccountTransfer transfer = convert(line);
                transfer.setBatch(batch);
                transferRepo.save(transfer);
            });

            LOG.info("Successfully imported {} account transfers from statement for date {}.", lines.size(), date);

        } catch (final RuntimeException e) {
            throw new AccountStatementImportException(
                    format("Importing account statement for date %s failed", date), e);
        }
    }

    private static AccountTransfer convert(final AccountStatementLine line) {
        final AccountTransfer transfer = new AccountTransfer();
        transfer.setCreditorIban(line.getCreditorAccountNumberAsIban());
        transfer.setTransactionDate(line.getTransactionDate());
        transfer.setBookingDate(line.getBookingDate());
        transfer.setDebtorName(line.getDebtorNameAbbrv());
        transfer.setAmount(line.getAmount());
        transfer.setCreditorReference(CreditorReference.fromNullable(line.getCreditorReference()));
        transfer.setAccountServiceReference(line.getAccountServiceReference());
        return transfer;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public List<AccountStatement> fetchAccountStatements() {
        return fetchAccountStatements(this.ipAddress, this.username, this.password, findImportedFiles());
    }

    private static List<AccountStatement> fetchAccountStatements(final String ipAddress,
                                                                 final String username,
                                                                 final String password,
                                                                 final Map<LocalDate, Integer> fetchedFiles) {

        requireNonNull(ipAddress, "ipAddress is null");
        requireNonNull(username, "username is null");
        requireNonNull(password, "password is null");

        try (final SSHClient ssh = new SSHClient()) {

            // TODO Verify host key
            ssh.addHostKeyVerifier(new PromiscuousVerifier());

            ssh.connect(ipAddress, 22);
            ssh.authPassword(username, password);

            try (final SFTPClient sftp = ssh.newSFTPClient()) {

                final List<RemoteResourceInfo> resources = sftp.ls(DIRECTORY, RemoteResourceInfo::isRegularFile);

                final List<Tuple3<String, LocalDate, Integer>> filesToFetch =
                        findFilesToBeFetched(resources, fetchedFiles);

                if (filesToFetch.isEmpty()) {
                    LOG.info("No new account statement files present");
                    return Collections.emptyList();
                }

                final String filenames = filesToFetch.stream().map(Tuple3::_1).collect(joining(", "));
                LOG.info("Found {} account statement files to be fetched: {}", filesToFetch.size(), filenames);

                final ByteArrayOutputStream out = new ByteArrayOutputStream();

                final InMemoryDestFile inMemory = new InMemoryDestFile() {
                    @Override
                    public OutputStream getOutputStream() {
                        return out;
                    }
                };

                return filesToFetch
                        .stream()
                        .flatMap(tuple -> {
                            final String filename = tuple._1;
                            final String path = DIRECTORY + filename;

                            try {
                                sftp.get(path, inMemory);

                                final String fileContent = new String(out.toByteArray(), CHARSET);

                                final AccountStatement statement = AccountStatementParser.parseFile(fileContent);
                                statement.setFilename(filename);
                                statement.setFilenameDate(tuple._2);
                                statement.setFileNumber(tuple._3);

                                return Stream.of(statement);

                            } catch (final IOException ex) {
                                LOG.warn("Could not fetch account statement file '{}'", path, ex);
                            } catch (final AccountStatementParseException ex2) {
                                LOG.warn("Parsing account statement file '{}' failed", path, ex2);
                            } finally {
                                out.reset();
                            }

                            return Stream.empty();
                        })
                        .collect(toList());
            }

        } catch (final IOException e) {
            throw new AccountStatementImportException("Connection to account statement server failed", e);
        }
    }

    // Package private for testing
    /*package*/ static List<Tuple3<String, LocalDate, Integer>> findFilesToBeFetched(final List<RemoteResourceInfo> resources,
                                                                                 final Map<LocalDate, Integer> fetchedFiles) {
        // Earliest date to consider
        final LocalDate earliestValidDate = getSearchPeriod().lowerEndpoint();

        return resources.stream()
                .flatMap(resource -> extractFileMetadata(resource, earliestValidDate)
                        // Filter out if earlies valid date is after the resource date
                        .filter(tuple-> !earliestValidDate.isAfter(tuple._1()))
                        // Include only if valid or greater file number that previously imported for that date
                        .filter(tuple -> fetchedFiles.getOrDefault(tuple._1(), -1) < tuple._2())
                        .map(date -> Tuple.of(resource.getName(), date._1(), date._2()))
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .sorted(getDateAndFileNumberComparator())
                .collect(toList());
    }

    private static Comparator<Tuple3<String, LocalDate, Integer>> getDateAndFileNumberComparator() {
        final Comparator<Tuple3<String, LocalDate, Integer>> dateComparator = comparing(Tuple3::_2);
        final Comparator<Tuple3<String, LocalDate, Integer>> fullComparator = dateComparator.thenComparing(Tuple3::_3);
        return fullComparator;
    }

    private static Optional<Tuple2<LocalDate, Integer>> extractFileMetadata(final RemoteResourceInfo resource,
                                                                            final LocalDate earliestValidDate) {

        final String filename = resource.getName();
        final Matcher legacyFormatMatcher = FILENAME_PATTERN.matcher(filename);
        final Matcher matcher = FILENAME_WITH_FILE_NUMBER_PATTERN.matcher(filename);

        if (legacyFormatMatcher.matches()) {
            try {
                final int day = Integer.parseInt(legacyFormatMatcher.group(1));
                final int month = Integer.parseInt(legacyFormatMatcher.group(2));
                final int year = 2000 + Integer.parseInt(legacyFormatMatcher.group(3));

                return Optional.of(Tuple.of(new LocalDate(year, month, day), 0));

            } catch (final IllegalArgumentException e) {
                LOG.warn("Incorrect date in filename: '{}'", filename, e);
            }
        } else if (matcher.matches()) {
            final int day = Integer.parseInt(matcher.group(1));
            final int month = Integer.parseInt(matcher.group(2));
            final int year = 2000 + Integer.parseInt(matcher.group(3));
            final int fileNumber = Integer.parseInt(matcher.group(4));

            return Optional.of(Tuple.of(new LocalDate(year, month, day), fileNumber));
        } else {
            // Log warn message if new files with unknown filename pattern are detected.

            final long mtime = resource.getAttributes().getMtime();
            final LocalDate dateFromMtime = new DateTime(mtime * 1000L, Constants.DEFAULT_TIMEZONE).toLocalDate();

            if (!dateFromMtime.isBefore(earliestValidDate)) {
                LOG.warn("Filename not matching expected pattern: '{}'", filename);
            }
        }

        return Optional.empty();
    }

    private Map<LocalDate, Integer> findImportedFiles() {
        final Range<LocalDate> searchPeriod = getSearchPeriod();

        return findImportedFilesBetweenDates(searchPeriod.lowerEndpoint(), searchPeriod.upperEndpoint());

    }

    public static Range<LocalDate> getSearchPeriod() {
        final LocalDate searchPeriodEnd = today();
        final LocalDate searchPeriodBegin = searchPeriodEnd.minusDays(90);
        return DateUtil.rangeFrom(searchPeriodBegin, searchPeriodEnd );
    }

    private Map<LocalDate, Integer> findImportedFilesBetweenDates(final LocalDate beginDate, final LocalDate endDate) {
        final QAccountTransferBatch BATCH = QAccountTransferBatch.accountTransferBatch;

        return queryFactory
                .from(BATCH)
                .where(BATCH.filenameDate.between(beginDate, endDate))
                .transform(groupBy(BATCH.filenameDate).as(max(BATCH.fileNumber.coalesce(0))));
    }
}
