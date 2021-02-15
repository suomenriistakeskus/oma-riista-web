package fi.riista.feature.account.registration;

import fi.riista.feature.mail.token.EmailToken;
import org.joda.time.Duration;
import org.joda.time.Minutes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Service
public class VetumaTransactionService {
    private static final Duration SAML_TRANSACTION_TIMEOUT = Minutes.minutes(15).toStandardDuration();

    @Resource
    private VetumaTransactionRepository vetumaTransactionRepository;

    public static boolean isValidTransactionId(final @RequestParam String trid) {
        return VetumaTransaction.TRID_PATTERN.matcher(trid).matches();
    }

    @Nonnull
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public String startTransaction(final EmailToken emailToken,
                                   final HttpServletRequest request) {
        final VetumaTransaction vetumaTransaction = new VetumaTransaction(emailToken, request);
        return vetumaTransactionRepository.save(vetumaTransaction).getId();
    }

    @Nonnull
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public VetumaTransaction requirePendingTransaction(final String trid) {
        return getValidTransaction(trid, VetumaTransactionStatus.INIT);
    }

    @Nonnull
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public VetumaTransaction requireSuccessfulTransaction(final String trid) {
        return getValidTransaction(trid, VetumaTransactionStatus.SUCCESS);
    }

    @Nonnull
    private VetumaTransaction getValidTransaction(final String trid, final VetumaTransactionStatus expectedStatus) {
        if (StringUtils.isEmpty(trid)) {
            throw new VetumaTransactionException("SAML TRID is missing!");
        }

        if (!isValidTransactionId(trid)) {
            throw new VetumaTransactionException(String.format("Invalid SAML TRID=%s", trid));
        }

        final VetumaTransaction vetumaTransaction = vetumaTransactionRepository.findById(trid)
                .orElseThrow(() -> new VetumaTransactionException(String.format("SAML TRID=%s not found", trid)));

        if (vetumaTransaction.isExpiredNow(SAML_TRANSACTION_TIMEOUT)) {
            vetumaTransaction.setStatusTimeout();
            throw new VetumaTransactionException(String.format("SAML TRID=%s has expired", vetumaTransaction.getId()));
        }

        vetumaTransaction.assertTransactionStatus(expectedStatus);

        return vetumaTransaction;
    }
}
