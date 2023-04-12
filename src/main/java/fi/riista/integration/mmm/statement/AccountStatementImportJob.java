package fi.riista.integration.mmm.statement;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLineFeature;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "AccountStatementImportJob",
        enabledProperty = "mmm.accountstatement.import.enabled",
        cronExpression = "${mmm.accountstatement.import.schedule}")
public class AccountStatementImportJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(AccountStatementImportJob.class);

    @Resource
    private AccountStatementImportFeature statementImportFeature;

    @Resource
    private InvoicePaymentLineFeature paymentLineFeature;

    @Override
    protected void executeAsAdmin() {
        try {
            LOG.info("Starting ...");

            int numSuccessfullyImported = 0;

            for (final AccountStatement statement : statementImportFeature.fetchAccountStatements()) {
                try {
                    statementImportFeature.importAccountTransfers(statement);
                    numSuccessfullyImported++;
                } catch (final AccountStatementImportException e) {
                    LOG.error("Exception while importing account statement from file '{}'", statement.getFilename(), e);
                }
            }

            LOG.info("Imported {} account statements.", numSuccessfullyImported);

            try {
                paymentLineFeature.createInvoicePaymentLinesFromAccountTransfers();
            } catch (final Exception e) {
                LOG.error("Exception while associating account transfers with invoices", e);
            }

            LOG.info("Done.");

        } catch (final Exception e) {
            LOG.error("Account statement import job threw exception", e);
        }
    }
}
