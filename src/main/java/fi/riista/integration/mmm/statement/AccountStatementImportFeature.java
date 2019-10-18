package fi.riista.integration.mmm.statement;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.config.Constants;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import fi.riista.integration.mmm.transfer.AccountTransferBatch;
import fi.riista.integration.mmm.transfer.AccountTransferBatchRepository;
import fi.riista.integration.mmm.transfer.AccountTransferRepository;
import fi.riista.integration.mmm.transfer.QAccountTransferBatch;
import io.vavr.Tuple;
import io.vavr.Tuple2;
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
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.util.DateUtil.today;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service
public class AccountStatementImportFeature {

    private static final Logger LOG = LoggerFactory.getLogger(AccountStatementImportFeature.class);

    private static final String DIRECTORY = "/siirto/";
    private static final Charset CHARSET = Charset.forName("ISO-8859-1");

    private static final Pattern FILENAME_PATTERN = Pattern.compile("hirviit(\\d{2})(\\d{2})(\\d{2})");

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

        final LocalDate date = statement.getStatementDate();

        if (batchRepo.findByStatementDate(date).isPresent()) {
            throw new AccountStatementImportException(
                    format("Account statement for date %s is already imported", date));
        }

        try {
            final AccountTransferBatch batch =
                    new AccountTransferBatch(date, statement.getFilename(), statement.getFilenameDate());
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
        return fetchAccountStatements(this.ipAddress, this.username, this.password, findMissingFilenameDates());
    }

    private static List<AccountStatement> fetchAccountStatements(final String ipAddress,
                                                                 final String username,
                                                                 final String password,
                                                                 final List<LocalDate> missingFilenameDates) {

        requireNonNull(ipAddress, "ipAddress is null");
        requireNonNull(username, "username is null");
        requireNonNull(password, "password is null");

        if (missingFilenameDates.isEmpty()) {
            return Collections.emptyList();
        }

        try (final SSHClient ssh = new SSHClient()) {

            // TODO Verify host key
            ssh.addHostKeyVerifier(new PromiscuousVerifier());

            ssh.connect(ipAddress, 22);
            ssh.authPassword(username, password);

            try (final SFTPClient sftp = ssh.newSFTPClient()) {

                final List<RemoteResourceInfo> resources = sftp.ls(DIRECTORY, RemoteResourceInfo::isRegularFile);

                final List<Tuple2<String, LocalDate>> filesToFetch =
                        findFilesToBeFetched(resources, missingFilenameDates);

                if (filesToFetch.isEmpty()) {
                    LOG.info("No new account statement files present");
                    return Collections.emptyList();
                }

                final String filenames = filesToFetch.stream().map(Tuple2::_1).collect(joining(", "));
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

    private static List<Tuple2<String, LocalDate>> findFilesToBeFetched(final List<RemoteResourceInfo> resources,
                                                                        final List<LocalDate> missingFilenameDates) {
        final LocalDate earliestMissingDate =
                missingFilenameDates.stream().min(naturalOrder()).orElseThrow(IllegalStateException::new);

        return resources.stream()
                .flatMap(resource -> {
                    return extractFileDate(resource, earliestMissingDate)
                            .filter(missingFilenameDates::contains)
                            .map(date -> Tuple.of(resource.getName(), date))
                            .map(Stream::of)
                            .orElseGet(Stream::empty);
                })
                .sorted(comparing(Tuple2::_2))
                .collect(toList());
    }

    private static Optional<LocalDate> extractFileDate(final RemoteResourceInfo resource,
                                                       final LocalDate earliestDateExpected) {

        final String filename = resource.getName();
        final Matcher matcher = FILENAME_PATTERN.matcher(filename);

        if (matcher.matches()) {
            try {
                final int day = Integer.parseInt(matcher.group(1));
                final int month = Integer.parseInt(matcher.group(2));
                final int year = 2000 + Integer.parseInt(matcher.group(3));

                return Optional.of(new LocalDate(year, month, day));

            } catch (final IllegalArgumentException e) {
                LOG.warn("Incorrect date in filename: '{}'", filename, e);
            }
        } else {
            // Log warn message if new files with unknown filename pattern are detected.

            final long mtime = resource.getAttributes().getMtime();
            final LocalDate dateFromMtime = new DateTime(mtime * 1000L, Constants.DEFAULT_TIMEZONE).toLocalDate();

            if (!dateFromMtime.isBefore(earliestDateExpected)) {
                LOG.warn("Filename not matching expected pattern: '{}'", filename);
            }
        }

        return Optional.empty();
    }

    private List<LocalDate> findMissingFilenameDates() {
        final LocalDate searchPeriodEnd = today();
        final LocalDate searchPeriodBegin = searchPeriodEnd.minusDays(90);

        final List<LocalDate> existingFilenameDates = findImportedFilenameDates(searchPeriodBegin, searchPeriodEnd);

        return streamBusinessDays(searchPeriodBegin, searchPeriodEnd)
                .filter(date -> !existingFilenameDates.contains(date))
                .collect(toList());
    }

    private static Stream<LocalDate> streamBusinessDays(final LocalDate beginDate, final LocalDate endDate) {
        final int daysElapsed = Days.daysBetween(beginDate, endDate).getDays();

        return IntStream
                .rangeClosed(0, daysElapsed)
                .mapToObj(beginDate::plusDays)
                // Filter out Saturdays and Sundays because account statement files do not appear
                // at weekends.
                .filter(date -> date.getDayOfWeek() <= 5);
    }

    private List<LocalDate> findImportedFilenameDates(final LocalDate beginDate, final LocalDate endDate) {
        final QAccountTransferBatch BATCH = QAccountTransferBatch.accountTransferBatch;

        return queryFactory
                .select(BATCH.filenameDate)
                .from(BATCH)
                .where(BATCH.filenameDate.between(beginDate, endDate))
                .fetch();
    }
}
