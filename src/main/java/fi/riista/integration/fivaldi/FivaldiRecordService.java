package fi.riista.integration.fivaldi;

import com.ancientprogramming.fixedformat4j.exception.FixedFormatException;
import com.ancientprogramming.fixedformat4j.format.FixedFormatManager;
import com.ancientprogramming.fixedformat4j.format.impl.FixedFormatManagerImpl;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.riista.integration.fivaldi.FivaldiConstants.FIVALDI_COMPANY_NUMBER_PROD;
import static fi.riista.integration.fivaldi.FivaldiConstants.FIVALDI_COMPANY_NUMBER_TEST;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class FivaldiRecordService {

    private static final FixedFormatManager MANAGER = new FixedFormatManagerImpl();

    public static FivaldiRecord createRecord(final FivaldiRecordParams params) {
        final FivaldiRecord record = new FivaldiRecord(true);

        record.setCompanyNumber(params.isProduction() ? FIVALDI_COMPANY_NUMBER_PROD : FIVALDI_COMPANY_NUMBER_TEST);
        record.setCustomerId(params.getPaymentMethod().getCustomerId());

        record.setInvoiceNumber(params.getInvoiceNumber());
        record.setInvoiceSum(params.getAmount());

        final LocalDate invoiceDate = params.getInvoiceDate();
        record.getReslas().setInvoiceDate(invoiceDate);
        record.getReslas().updateSeason(invoiceDate.getYear(), invoiceDate.getMonthOfYear());

        record.getReslas().setNetDueDate(params.getNetDueDate());
        record.getReslas().setCreditorReference(params.getCreditorReferenceNumber());

        FivaldiRecordValidator.validate(record, true);

        return record;
    }

    public static String serialize(final List<FivaldiRecord> records) {
        requireNonNull(records);

        final StringBuilder buf = new StringBuilder();

        records.forEach(record -> {
            buf.append(MANAGER.export(record.getReslas())).append("\n");
            record.getRestap().forEach(restap -> buf.append(MANAGER.export(restap)).append("\n"));
        });

        return buf.toString();
    }

    public static List<FivaldiRecord> parseFromFile(@Nonnull final String filename) throws FivaldiRecordParseException {
        requireNonNull(filename);

        try {
            return Files.asCharSource(new File(filename), StandardCharsets.ISO_8859_1)
                    .readLines(new LineProcessor<List<FivaldiRecord>>() {

                        final AtomicInteger lineCounter = new AtomicInteger(1);
                        final AtomicInteger lastReslasLineNumber = new AtomicInteger();

                        final SortedMap<Integer, FivaldiReslasLine> reslasByLine = new TreeMap<>();
                        final Map<Integer, List<FivaldiRestapLine>> restapListByReslasLine = new HashMap<>();

                        @Override
                        public boolean processLine(final String line) {
                            final int lineNum = lineCounter.getAndIncrement();

                            if (line.matches("\\d{6}RESLAS.+")) {

                                reslasByLine.put(lineNum, parseReslasLine(line, lineNum));
                                lastReslasLineNumber.set(lineNum);

                            } else if (line.matches("\\d{6}RESTAP.+")) {

                                restapListByReslasLine
                                        .computeIfAbsent(lastReslasLineNumber.get(), k -> new ArrayList<>())
                                        .add(parseRestapLine(line, lineNum));

                            } else {
                                throw new FivaldiRecordParseException(format(
                                        "Invalid Fivaldi line: %d: %s", lineNum, line));
                            }

                            return true;
                        }

                        @Override
                        public List<FivaldiRecord> getResult() {
                            final List<FivaldiRecord> ret = new ArrayList<>();

                            reslasByLine.forEach((lineNum, reslas) -> {
                                ret.add(getValidatedRecord(reslas, restapListByReslasLine.get(lineNum), lineNum));
                            });

                            return ret;
                        }
                    });

        } catch (final IOException ioe) {
            throw new FivaldiRecordParseException(format("Parsing file %s failed", filename), ioe);
        }
    }

    private static FivaldiReslasLine parseReslasLine(final String line,
                                                     final int lineNum) throws FivaldiRecordParseException {
        try {
            return MANAGER.load(FivaldiReslasLine.class, line);
        } catch (final FixedFormatException e) {
            throw new FivaldiRecordParseException(format("RESLAS parse error: line %d: %s", lineNum, line), e);
        }
    }

    private static FivaldiRestapLine parseRestapLine(final String line,
                                                     final int lineNum) throws FivaldiRecordParseException {
        try {
            return MANAGER.load(FivaldiRestapLine.class, line);
        } catch (final FixedFormatException e) {
            throw new FivaldiRecordParseException(format("RESTAP parse error: line %d: %s", lineNum, line), e);
        }
    }

    private static FivaldiRecord getValidatedRecord(final FivaldiReslasLine reslas,
                                                    final List<FivaldiRestapLine> restap,
                                                    final int lineNum) {

        final FivaldiRecord record = new FivaldiRecord(reslas, restap);

        try {
            FivaldiRecordValidator.validate(record);
        } catch (final Exception e) {
            throw new FivaldiRecordParseException(
                    format("Fivaldi record starting at line %d failed validation", lineNum), e);
        }

        return record;
    }
}
