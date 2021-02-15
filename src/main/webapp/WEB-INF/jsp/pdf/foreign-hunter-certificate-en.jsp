<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false" trimDirectiveWhitespaces="true" %>
<%@ page import="fi.riista.feature.account.certificate.HunterForeignCertificateDTO" %>
<%@ page import="org.joda.time.LocalDate" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>Certificate</title>
    <base href="/static/hunter-certificate/">
    <link href="certificate.css" rel="stylesheet"/>
</head>
<body>

<%!
    public String getOrdinalSuffix(LocalDate now) {
        String dayOfMonth = now.dayOfMonth().getAsText();
        String suffix = "";
        if (dayOfMonth.endsWith("1")) suffix = "st";
        if (dayOfMonth.endsWith("2")) suffix = "nd";
        if (dayOfMonth.endsWith("3")) suffix = "rd";
        if (dayOfMonth.endsWith("0") || dayOfMonth.endsWith("4") || dayOfMonth.endsWith("5") || dayOfMonth.endsWith("6")
                || dayOfMonth.endsWith("7") || dayOfMonth.endsWith("8") || dayOfMonth.endsWith("9")) suffix = "th";
        return suffix;
    }
%>
<%
    HunterForeignCertificateDTO model = (HunterForeignCertificateDTO) request.getAttribute("model");
%>

<div class="container">
    <div class="header-logo">
        <img width="280" alt="image" src="01.jpg"/>
    </div>

    <div style="float:right; position:relative; width: 200pt; padding-top:10pt;">
        <h1>Certificate of hunting card</h1>
        <h1>The
            <joda:format value="${model.currentDate}" pattern="d" locale="en-GB"/>
            <%=getOrdinalSuffix(model.getCurrentDate())%>
            <joda:format value="${model.currentDate}" pattern=" MMMM YYYY" locale="en-GB"/>
        </h1>
    </div>

    <p style="padding-top:120pt">
        We hereby confirm that
        <strong>${model.lastName}&nbsp;${model.firstName}</strong>
        (born <strong> <joda:format value="${model.dateOfBirth}" pattern="dd.MM.yyyy" /></strong>,
         register number of the hunter: <strong>${model.hunterNumber}</strong>)
        has passed The Finnish Hunters’ examination and he/she has a valid hunting card for hunting season

        <strong><joda:format value="${model.huntingCardStart}" pattern="dd.MM.yyyy" /></strong> &dash;
        <strong><joda:format value="${model.huntingCardEnd}" pattern="dd.MM.yyyy" /></strong>.

        (payment <strong><joda:format value="${model.paymentDate}" pattern="dd.MM.yyyy" /></strong>)
    </p>

    <h1>Finnish hunting card</h1>

    <div style="padding-left: 70pt">
        <p>
            Every hunter in Finland must pass The hunters’ examination to get a hunting card. The hunters’ examination is a
            written test with 60 questions.
        </p>

        <p>
            There are eight parts in the examination and the training course for it:
        </p>

        <ul>
            <li><span>hunting legislation in Finland and other regulations of hunting</span></li>
            <li><span>identification of game animals</span></li>
            <li><span>game ecology and game management</span></li>
            <li><span>ethical and sustainable hunting</span></li>
            <li><span>firearms and cartridges (includes also hunting bows and arrows)</span></li>
            <li><span>hunting safety</span></li>
            <li><span>hunting equipments and hunting methods</span></li>
            <li><span>handling of quarry</span></li>
        </ul>

        <p style="padding-top: 40pt;">
            Sincerely yours,
        </p>

        <p class="s1" style="padding-top: 20pt;">
            FINNISH WILDLIFE AGENCY
        </p>

        <h1 style="padding-top: 30pt">
            Sauli Härkönen<br/>
        </h1>

        <h2 style="padding-top:0">
            Director for Public Administration Tasks
        </h2>

        <p style="padding-top: 15pt; font-size:12pt;">
            SUOMEN RIISTAKESKUS<br/>
            SOMPIONTIE 1<br/>
            00730 HELSINKI<br/>
        </p>
   </div>

    <div class="footer" style="margin-top: 170pt">
        <%@include file="footer-en.jsp"%>
    </div>

    <div class="header-logo">
        <img width="280" alt="image" src="01.jpg"/>
    </div>

    <h1 style="padding-top: 40pt;">
        Certificate of insurance
    </h1>

    <div style="font-size:10pt; line-height: 12pt">
        <div style="float:left;">
            Insurer<br/>
            Policy number<br/>
            Policyholder<br/>
            Policy period<br/>
            Insured<br/>
            Territorial Scope<br/>
        </div>

        <div style="padding-left:136pt">
            LocalTapiola General Mutual Insurance Company, Finland<br/>
            312-0042105-H<br/>
            Suomen riistakeskus (Finlands viltcentral), Finnish Wildlife Agency<br/>
            1<sup>st</sup>, August <joda:format value="${model.huntingCardStart}" pattern="yyyy" />
            –
            31<sup>st</sup>, July <joda:format value="${model.huntingCardEnd}" pattern="yyyy" /><br/>
            Certified hunters who hold a valid hunting card<br/>
            Nordic Countries, EU member states and Switzerland up to 60 days hunting trips<br/>
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:40pt;font-size: 10pt">
        <div style="float:left;">
            Type of insurance<br/>
            Limit of liability<br/>
            Main exclusions<br/>
        </div>

        <div style="padding-left:110pt;">
            Hunter’s Third Party Liability Insurance<br/>
            EUR 1,200,000 in personal injury<br/>
            Property damages
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt;font-size: 10pt">
        <div style="float:left;">
            Type of insurance<br/>
            Limit of liability
        </div>

        <div style="padding-left:110pt;">
            Hunter’s Personal Accident Insurance<br/>
            EUR 10,000 in personal injury<br/>
            EUR 50.000 in permanent total disability<br/>
            EUR 30.000 in death
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt;font-size: 10pt">
        <div style="float:left;">
            Type of insurance<br/>
            Limit of liability
        </div>

        <div style="padding-left:110pt">
            Official’s Personal Accident Insurance<br/>
            EUR 10,000 in personal injury<br/>
            EUR 50.000 in permanent total disability<br/>
            EUR 30.000 in death
        </div>
    </div>

    <p style="padding-left: 136pt;padding-top:40pt;font-size: 10pt;line-height: 10pt">
        LOCALTAPIOLA GENERAL MUTUAL INSURANCE COMPANY<br/>
        Corporate Property and Business Insurance Services
    </p>

    <p style="padding-left: 136pt;font-style: italic;font-size: 10pt;line-height: 10pt">
        This certificate is issued as a matter of information only and confers no rights upon the certificate holder. This
        is an extract of the original insurance policy between policyholder and LocalTapiola General Mutual Insurance
        Company which take precedence should there be any differences between the policy and the extract.
    </p>

    <div class="footer" style="margin-top: 320pt">
        <%@include file="footer-en.jsp"%>
    </div>

    <div class="header-logo">
        <img width="280" alt="image" src="01.jpg"/>
    </div>

    <h1 style="padding-top: 30pt;padding-left: 21pt;">
        Information in the Hunting Card and in the Certificate of Passed Shooting Test (In Finnish and in English):
    </h1>

    <h1 style="padding-left: 21pt;padding-top:20pt;">
        IN HUNTING CARD
    </h1>

    <table border="0" width="80%" style="margin-left: 60pt;">
        <th>
            <tr>
                <td width="50%" style="text-decoration: underline;">In Finnish:</td>
                <td width="50%" style="text-decoration: underline;">In English:</td>
            </tr>
        </th>
        <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
        <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
        <tr>
            <td>Metsästyskortti</td>
            <td>Hunting card</td>
        </tr>
        <tr>
            <td>Metsästäjän nimi</td>
            <td>Hunter`s name</td>
        </tr>
        <tr>
            <td>Syntymäaika</td>
            <td>Date of birth</td>
        </tr>
        <tr>
            <td>Yhdistys</td>
            <td>Game Management Association</td>
        </tr>
        <tr>
            <td>Yhdistysnumero</td>
            <td>Register number of the GMA</td>
        </tr>
        <tr>
            <td>Metsästäjänumero</td>
            <td>Register number of the Hunter</td>
        </tr>
        <tr>
            <td>Jakeluosoite</td>
            <td>Street address</td>
        </tr>

        <tr>
            <td>Kotipaikka</td>
            <td>Domicile</td>
        </tr>

        <tr>
            <td>Postinumero ja -toimipaikka</td>
            <td>City and Zip code</td>
        </tr>

    </table>

    <h1 style="padding-left: 20pt; padding-top:40pt;">
        IN CERTIFICATE OF PASSED SHOOTING TEST
    </h1>

    <table border="0" width="80%" style="margin-left: 60pt;">
        <tr>
            <td width="50%">Ampumakokeen suorituskortti</td>
            <td width="50%">Certificate of the passed shooting test</td>
        </tr>
        <tr>
            <td>Nimi</td>
            <td>Name</td>
        </tr>
        <tr>
            <td>Syntymäaika</td>
            <td>Date of birth</td>
        </tr>
        <tr>
            <td>Metsästäjänumero</td>
            <td>Register number of the Hunter</td>
        </tr>
        <tr>
            <td>on suorittanut metsästyslain</td>
            <td>...has passed the shooting test required</td>
        </tr>
        <tr>
            <td>edellyttämän ampumakokeen</td>
            <td>in the Hunting Act (place, date)</td>
        </tr>
        <tr>
            <td>Suoritus on voimassa __/__ saakka</td>
            <td>The test is valid until __/__</td>
        </tr>
        <tr>
            <td>Ammunnan valvoja</td>
            <td>Supervisor of the test (signature)</td>
        </tr>
        <tr>
            <td>Leima</td>
            <td>Stamp</td>
        </tr>
        <tr>
            <td>Suoritus on voimassa kolme vuot-</td>
            <td>The test result is valid for three years</td>
        </tr>
        <tr>
            <td>ta suorittamisajankohdasta lukien</td>
            <td>from the date of test</td>
        </tr>
    </table>

    <div class="footer" style="margin-top: 460pt">
        <%@include file="footer-en.jsp"%>
    </div>
</div>
</body>
</html>
