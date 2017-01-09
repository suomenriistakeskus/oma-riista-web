package fi.riista.feature.sso.dto;

public enum ExternalAuthenticationStatusCode {
    INVALID_CREDENTIALS,
    REMOTE_ADDRESS_BLOCKED,
    TWO_FACTOR_AUTHENTICATION_REQUIRED,
    SMS_SEND_FAILURE,
    UNKNOWN_ERROR
}
