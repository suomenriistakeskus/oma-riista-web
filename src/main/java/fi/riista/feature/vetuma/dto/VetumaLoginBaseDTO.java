package fi.riista.feature.vetuma.dto;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import fi.riista.config.Constants;
import fi.riista.util.Times;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public abstract class VetumaLoginBaseDTO implements Serializable {

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormat.forPattern("yyyyMMddHHmmssSSS").withZone(Constants.DEFAULT_TIMEZONE);

    // Order number: 1
    protected String RCVID;

    // Order number: 3
    protected String TIMESTMP;

    // Order number: 4
    protected String SO;

    // Order number: 9
    protected String LG;

    // Order number: 10
    protected String RETURL;

    // Order number: 11
    protected String CANURL;

    // Order number: 12
    protected String ERRURL;

    // Order number: 15
    protected String MAC;

    // Order number: 19
    protected String EXTRADATA;

    // Order number: 34
    protected String TRID;

    public boolean isExpiredNow(final int expirationSeconds) {
        return TIMESTMP == null ||
                Times.secondsBetween(this.getTimestamp(), DateTime.now()) > expirationSeconds;
    }

    public boolean verifyMac(String sharedSecretKey) {
        return computeMac(sharedSecretKey).equals(MAC);
    }

    public final String computeMac(String sharedSecretKey) {
        Objects.requireNonNull(sharedSecretKey, "sharedSecretKey must not be null");

        String dataToBeDigested = getDigestInput(sharedSecretKey);

        HashFunction hf = Hashing.sha256();
        HashCode digest = hf.newHasher()
            .putString(dataToBeDigested, Charsets.ISO_8859_1)
            .hash();

        return BaseEncoding.base16().encode(digest.asBytes());
    }

    protected abstract List<String> getDigestInputFields();

    private String getDigestInput(String sharedSecretKey) {
        List<String> fields = Lists.newArrayList(getDigestInputFields());

        fields.add(constructSharedSecret(sharedSecretKey));

        return Joiner.on('&').useForNull("").join(fields) + '&';
    }

    private String constructSharedSecret(String sharedSecretKey) {
        return RCVID + '-' + sharedSecretKey;
    }

    public String getRCVID() {
        return RCVID;
    }

    public void setRCVID(String rcvId) {
        this.RCVID = rcvId;
    }

    public String getTIMESTMP() {
        return TIMESTMP;
    }

    public DateTime getTimestamp() {
        return DATETIME_FORMATTER.parseDateTime(TIMESTMP);
    }

    public void setTIMESTMP(String timestamp) {
        this.TIMESTMP = timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        setTIMESTMP(DATETIME_FORMATTER.print(timestamp.withZone(Constants.DEFAULT_TIMEZONE).toLocalDateTime()));
    }

    public String getSO() {
        return SO;
    }

    public void setSO(String so) {
        this.SO = so;
    }

    public String getLG() {
        return LG;
    }

    public void setLG(String lang) {
        this.LG = lang;
    }

    public String getRETURL() {
        return RETURL;
    }

    public void setRETURL(String returnUrl) {
        this.RETURL = returnUrl;
    }

    public String getCANURL() {
        return CANURL;
    }

    public void setCANURL(String cancelUrl) {
        this.CANURL = cancelUrl;
    }

    public String getERRURL() {
        return ERRURL;
    }

    public void setERRURL(String errorUrl) {
        this.ERRURL = errorUrl;
    }

    public String getMAC() {
        return MAC;
    }

    public String setMAC(String mac) {
        return this.MAC = mac;
    }

    public String getEXTRADATA() {
        return EXTRADATA;
    }

    public void setEXTRADATA(String extraData) {
        this.EXTRADATA = extraData;
    }

    public String getTRID() {
        return TRID;
    }

    public void setTRID(String trid) {
        this.TRID = trid;
    }

}
