package fi.riista.feature.metrics;

import java.util.List;

public class AdminMetricsDTO {
    private long countNormalUser;
    private long countPerson;

    private long countNormalUserWithOccupationAndPassword;
    private long countAllPeopleWithOccupation;

    private long countModeratorWithPassword;
    private long countAllModerators;

    private long countRHYToiminnanohjaajaWithPassword;
    private long countAllRHYToiminnanohjaaja;

    private long countHarvest;
    private long countHarvestSpecimens;
    private long countHarvestFromMobile;
    private long countHarvestFromWeb;

    private long countObservation;
    private long countObservationSpecimens;
    private long countObservationFromMobile;
    private long countObservationFromWeb;

    private long countHarvestReport;
    private long countHarvestReportApproved;

    private long countPdfCertificateUsers;
    private long countPdfCertificateDownloads;

    private long countForeignPdfCertificateUsers;
    private long countForeignPdfCertificateDownloads;

    private long countHunterPaymentPdfUsers;
    private long countHunterPaymentPdfDownloads;

    private long countSrvaEvent;
    private long countSrvaEventSpecimen;
    private long countSrvaEventFromMobile;
    private long countSrvaEventFromWeb;
    private long countSrvaEventApproved;
    private long countSrvaEventUnfinished;
    private long countSrvaEventSpecimenApproved;
    private long countSrvaEventSpecimenUnfinished;
    private long countSrvaEventSpecimenRejected;
    private long countSrvaEventRejected;
    private List<AdminMooselikeHuntingMetrics> mooselikeHuntingMetrics;

    public long getCountNormalUser() {
        return countNormalUser;
    }

    public void setCountNormalUser(long countNormalUser) {
        this.countNormalUser = countNormalUser;
    }

    public long getCountPerson() {
        return countPerson;
    }

    public void setCountPerson(long countPerson) {
        this.countPerson = countPerson;
    }

    public long getCountNormalUserWithOccupationAndPassword() {
        return countNormalUserWithOccupationAndPassword;
    }

    public void setCountNormalUserWithOccupationAndPassword(long countNormalUserWithOccupationAndPassword) {
        this.countNormalUserWithOccupationAndPassword = countNormalUserWithOccupationAndPassword;
    }

    public long getCountAllPeopleWithOccupation() {
        return countAllPeopleWithOccupation;
    }

    public void setCountAllPeopleWithOccupation(long countAllPeopleWithOccupation) {
        this.countAllPeopleWithOccupation = countAllPeopleWithOccupation;
    }

    public long getCountModeratorWithPassword() {
        return countModeratorWithPassword;
    }

    public void setCountModeratorWithPassword(long countModeratorWithPassword) {
        this.countModeratorWithPassword = countModeratorWithPassword;
    }

    public long getCountAllModerators() {
        return countAllModerators;
    }

    public void setCountAllModerators(long countAllModerators) {
        this.countAllModerators = countAllModerators;
    }

    public long getCountRHYToiminnanohjaajaWithPassword() {
        return countRHYToiminnanohjaajaWithPassword;
    }

    public void setCountRHYToiminnanohjaajaWithPassword(long countRHYToiminnanohjaajaWithPassword) {
        this.countRHYToiminnanohjaajaWithPassword = countRHYToiminnanohjaajaWithPassword;
    }

    public long getCountAllRHYToiminnanohjaaja() {
        return countAllRHYToiminnanohjaaja;
    }

    public void setCountAllRHYToiminnanohjaaja(long countAllRHYToiminnanohjaaja) {
        this.countAllRHYToiminnanohjaaja = countAllRHYToiminnanohjaaja;
    }

    public long getCountHarvest() {
        return countHarvest;
    }

    public void setCountHarvest(long countHarvest) {
        this.countHarvest = countHarvest;
    }

    public long getCountHarvestSpecimens() {
        return countHarvestSpecimens;
    }

    public void setCountHarvestSpecimens(long countHarvestSpecimens) {
        this.countHarvestSpecimens = countHarvestSpecimens;
    }

    public long getCountHarvestFromMobile() {
        return countHarvestFromMobile;
    }

    public void setCountHarvestFromMobile(long countHarvestFromMobile) {
        this.countHarvestFromMobile = countHarvestFromMobile;
    }

    public long getCountHarvestFromWeb() {
        return countHarvestFromWeb;
    }

    public void setCountHarvestFromWeb(long countHarvestFromWeb) {
        this.countHarvestFromWeb = countHarvestFromWeb;
    }

    public long getCountObservation() {
        return countObservation;
    }

    public void setCountObservation(long countObservation) {
        this.countObservation = countObservation;
    }

    public long getCountObservationSpecimens() {
        return countObservationSpecimens;
    }

    public void setCountObservationSpecimens(long countObservationSpecimens) {
        this.countObservationSpecimens = countObservationSpecimens;
    }

    public long getCountObservationFromMobile() {
        return countObservationFromMobile;
    }

    public void setCountObservationFromMobile(long countObservationFromMobile) {
        this.countObservationFromMobile = countObservationFromMobile;
    }

    public long getCountObservationFromWeb() {
        return countObservationFromWeb;
    }

    public void setCountObservationFromWeb(long countObservationFromWeb) {
        this.countObservationFromWeb = countObservationFromWeb;
    }

    public long getCountHarvestReport() {
        return countHarvestReport;
    }

    public void setCountHarvestReport(long countHarvestReport) {
        this.countHarvestReport = countHarvestReport;
    }

    public long getCountHarvestReportApproved() {
        return countHarvestReportApproved;
    }

    public void setCountHarvestReportApproved(long countHarvestReportApproved) {
        this.countHarvestReportApproved = countHarvestReportApproved;
    }

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

    public long getCountSrvaEvent() {
        return countSrvaEvent;
    }

    public void setCountSrvaEvent(long countSrvaEvent) {
        this.countSrvaEvent = countSrvaEvent;
    }

    public long getCountSrvaEventSpecimen() {
        return countSrvaEventSpecimen;
    }

    public void setCountSrvaEventSpecimen(long countSrvaEventSpecimen) {
        this.countSrvaEventSpecimen = countSrvaEventSpecimen;
    }

    public long getCountSrvaEventFromMobile() {
        return countSrvaEventFromMobile;
    }

    public void setCountSrvaEventFromMobile(long countSrvaEventFromMobile) {
        this.countSrvaEventFromMobile = countSrvaEventFromMobile;
    }

    public long getCountSrvaEventFromWeb() {
        return countSrvaEventFromWeb;
    }

    public void setCountSrvaEventFromWeb(long countSrvaEventFromWeb) {
        this.countSrvaEventFromWeb = countSrvaEventFromWeb;
    }

    public long getCountSrvaEventApproved() {
        return countSrvaEventApproved;
    }

    public void setCountSrvaEventApproved(long countSrvaEventApproved) {
        this.countSrvaEventApproved = countSrvaEventApproved;
    }

    public long getCountSrvaEventUnfinished() {
        return countSrvaEventUnfinished;
    }

    public void setCountSrvaEventUnfinished(long countSrvaEventUnfinished) {
        this.countSrvaEventUnfinished = countSrvaEventUnfinished;
    }

    public void setCountSrvaEventSpecimenApproved(long countSrvaEventSpecimenApproved) {
        this.countSrvaEventSpecimenApproved = countSrvaEventSpecimenApproved;
    }

    public long getCountSrvaEventSpecimenApproved() {
        return countSrvaEventSpecimenApproved;
    }

    public void setCountSrvaEventSpecimenUnfinished(long countSrvaEventSpecimenUnfinished) {
        this.countSrvaEventSpecimenUnfinished = countSrvaEventSpecimenUnfinished;
    }

    public long getCountSrvaEventSpecimenUnfinished() {
        return countSrvaEventSpecimenUnfinished;
    }

    public void setCountSrvaEventSpecimenRejected(long countSrvaEventSpecimenRejected) {
        this.countSrvaEventSpecimenRejected = countSrvaEventSpecimenRejected;
    }

    public long getCountSrvaEventSpecimenRejected() {
        return countSrvaEventSpecimenRejected;
    }

    public void setCountSrvaEventRejected(long countSrvaEventRejected) {
        this.countSrvaEventRejected = countSrvaEventRejected;
    }

    public long getCountSrvaEventRejected() {
        return countSrvaEventRejected;
    }

    public List<AdminMooselikeHuntingMetrics> getMooselikeHuntingMetrics() {
        return mooselikeHuntingMetrics;
    }

    public void setMooselikeHuntingMetrics(List<AdminMooselikeHuntingMetrics> mooselikeHuntingMetrics) {
        this.mooselikeHuntingMetrics = mooselikeHuntingMetrics;
    }
}
