package fi.riista.feature.account.registration;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SamlAuthenticationResult {
    private final boolean successful;
    private final String relayState;
    private final SamlUserAttributes userAttributes;
    private final List<String> errors;
    private final String lastErrorReason;

    public SamlAuthenticationResult(final SamlUserAttributes userAttributes, final String relayState) {
        this.successful = true;
        this.userAttributes = Objects.requireNonNull(userAttributes);
        this.errors = Collections.emptyList();
        this.lastErrorReason = null;
        this.relayState = relayState;
    }

    public SamlAuthenticationResult(final List<String> errors, final String lastErrorReason, final String relayState) {
        this.successful = false;
        this.userAttributes = null;
        this.errors = Objects.requireNonNull(errors);
        this.lastErrorReason = lastErrorReason;
        this.relayState = relayState;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getRelayState() {
        return relayState;
    }

    public SamlUserAttributes getUserAttributes() {
        Preconditions.checkState(isSuccessful(), "not authenticated");
        return userAttributes;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getLastErrorReason() {
        return lastErrorReason;
    }
}
