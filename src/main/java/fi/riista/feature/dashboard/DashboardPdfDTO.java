package fi.riista.feature.dashboard;

public class DashboardPdfDTO {
    private long countPdfCertificateUsers;
    private long countPdfCertificateDownloads;

    private long countForeignPdfCertificateUsers;
    private long countForeignPdfCertificateDownloads;

    private long countHunterPaymentPdfUsers;
    private long countHunterPaymentPdfDownloads;

    public long getCountPdfCertificateUsers() {
        return countPdfCertificateUsers;
    }

    public void setCountPdfCertificateUsers(long countPdfCertificateUsers) {
        this.countPdfCertificateUsers = countPdfCertificateUsers;
    }

    public long getCountPdfCertificateDownloads() {
        return countPdfCertificateDownloads;
    }

    public void setCountPdfCertificateDownloads(long countPdfCertificateDownloads) {
        this.countPdfCertificateDownloads = countPdfCertificateDownloads;
    }

    public long getCountForeignPdfCertificateUsers() {
        return countForeignPdfCertificateUsers;
    }

    public void setCountForeignPdfCertificateUsers(long countForeignPdfCertificateUsers) {
        this.countForeignPdfCertificateUsers = countForeignPdfCertificateUsers;
    }

    public long getCountForeignPdfCertificateDownloads() {
        return countForeignPdfCertificateDownloads;
    }

    public void setCountForeignPdfCertificateDownloads(long countForeignPdfCertificateDownloads) {
        this.countForeignPdfCertificateDownloads = countForeignPdfCertificateDownloads;
    }

    public long getCountHunterPaymentPdfUsers() {
        return countHunterPaymentPdfUsers;
    }

    public void setCountHunterPaymentPdfUsers(long countHunterPaymentPdfUsers) {
        this.countHunterPaymentPdfUsers = countHunterPaymentPdfUsers;
    }

    public long getCountHunterPaymentPdfDownloads() {
        return countHunterPaymentPdfDownloads;
    }

    public void setCountHunterPaymentPdfDownloads(long countHunterPaymentPdfDownloads) {
        this.countHunterPaymentPdfDownloads = countHunterPaymentPdfDownloads;
    }
}
