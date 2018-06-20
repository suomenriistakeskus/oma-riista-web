package fi.riista.feature.permit.invoice;

public enum InvoiceFivaldiState {

    // When invoice is not yet added to a Fivaldi batch file
    NOT_BATCHED,

    // When invoice has been added to a Fivaldi batch file
    BATCHED,

    // When Fivaldi batch file, to which the invoice is added, has been requested (downloaded)
    // at least once by some user
    DOWNLOADED
}
