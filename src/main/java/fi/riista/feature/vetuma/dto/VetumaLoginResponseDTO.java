/*
 Copyright 2017 Finnish Wildlife Agency - Suomen Riistakeskus
 Copyright 2014 Ministry of Justice, Finland

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 NOTE: File contains modification to the original source.
*/
package fi.riista.feature.vetuma.dto;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import fi.riista.feature.vetuma.support.VtjData;
import fi.riista.feature.vetuma.support.VtjDataParser;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VetumaLoginResponseDTO extends VetumaLoginBaseDTO {

    public enum Status {
        SUCCESSFUL,
        CANCELLED,
        REJECTED,
        ERROR,
        FAILURE
    }

    private static final String NAME_REGEXP = "ETUNIMI=([^,]*), SUKUNIMI=(.*)";

    private static final String SSN_REGEXP =
            "HETU=([0-9]{6}" + // Six digits
                    "[+-Aa]" + // - or A
                    "[0-9]{3}" + // Three digits
                    "[A-Za-z0-9])"; // One letter or digit

    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEXP);
    private static final Pattern SSN_PATTERN = Pattern.compile(SSN_REGEXP);

    // Order number: 8
    private String USERID;

    // Order number: 18
    private String SUBJECTDATA;

    // Order number: 29
    @javax.validation.constraints.Pattern(regexp = SSN_REGEXP)
    private Status STATUS;

    // Order number: 35
    private String VTJDATA;

    @Override
    protected List<String> getDigestInputFields() {
        String status = STATUS == null ? null : STATUS.name();

        // Order is important!
        return Arrays.asList(
                RCVID, TIMESTMP, SO, USERID, LG, RETURL, CANURL, ERRURL,
                SUBJECTDATA, EXTRADATA, status, TRID, VTJDATA);
    }

    public String getSsn() {
        String ssn = null;

        if (!Strings.isNullOrEmpty(EXTRADATA)) {
            Matcher matcher = SSN_PATTERN.matcher(EXTRADATA);

            if (matcher.matches()) {
                ssn = matcher.group(1).toUpperCase();
            }
        }

        return ssn;
    }

    public String[] getNames() {
        String[] names = {null, null};

        if (!Strings.isNullOrEmpty(SUBJECTDATA)) {
            Matcher matcher = NAME_PATTERN.matcher(SUBJECTDATA);

            if (matcher.matches()) {
                names[0] = Strings.emptyToNull(matcher.group(1));
                names[1] = Strings.emptyToNull(matcher.group(2));
            }
        }

        return names;
    }

    public VtjData getVtjData() {
        String xmlData = getVtjDataAsXml();

        return StringUtils.hasText(xmlData) ? VtjDataParser.parse(xmlData) : null;
    }

    public String getVtjDataAsXml() {
        if (Strings.isNullOrEmpty(VTJDATA)) {
            return null;
        }

        try {
            return URLDecoder.decode(VTJDATA, Charsets.ISO_8859_1.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("RCVID", RCVID)
                .add("TIMESTMP", TIMESTMP)
                .add("SO", SO)
                .add("LG", LG)
                .add("USERID", USERID)
                .add("RETURL", RETURL)
                .add("CANURL", CANURL)
                .add("ERRURL", ERRURL)
                .add("MAC", MAC)
                .add("SUBJECTDATA", SUBJECTDATA)
                .add("EXTRADATA", EXTRADATA)
                .add("STATUS", STATUS)
                .add("TRID", TRID)
                .add("VTJDATA", VTJDATA)
                .toString();
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String userId) {
        this.USERID = userId;
    }

    public String getSUBJECTDATA() {
        return SUBJECTDATA;
    }

    public void setSUBJECTDATA(String subjectData) {
        this.SUBJECTDATA = subjectData;
    }

    public Status getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(Status status) {
        this.STATUS = status;
    }

    public String getVTJDATA() {
        return VTJDATA;
    }

    public void setVTJDATA(String vtjData) {
        this.VTJDATA = vtjData;
    }
}
