<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false"
         trimDirectiveWhitespaces="true" %>
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
    <link href="foreign-hunter-certificate.css" rel="stylesheet"/>
</head>
<body>

<%
    final HunterForeignCertificateDTO model = (HunterForeignCertificateDTO) request.getAttribute("model");
%>
<%@include file="header-en.jsp" %>

<div class="container">
    <p style="padding-top:20pt;">
        We hereby confirm that
        ${model.lastName}&nbsp;${model.firstName}
        (born <joda:format value="${model.dateOfBirth}" pattern="dd.MM.yyyy"/>,
        register number of the hunter: ${model.hunterNumber})
        has passed The Finnish Hunters’ examination and he/she has a valid hunting card for hunting season

        <joda:format value="${model.huntingCardStart}" pattern="dd.MM.yyyy"/> &dash;
        <joda:format value="${model.huntingCardEnd}" pattern="dd.MM.yyyy"/>.

        (payment <joda:format value="${model.paymentDate}" pattern="dd.MM.yyyy"/>)
    </p>

    <h1>Finnish hunting card</h1>

    <div>
        <p>
            Every hunter in Finland must pass The hunters’ examination to get a hunting card. The hunters’ examination
            is a
            written test with 60 questions.
        </p>

        <p style="padding-top:20pt">
            There are eight parts in the examination and the training course for it:
        </p>

        <ul>
            <li><span>hunting legislation in Finland and other regulations of hunting</span></li>
            <li><span>identification of game animals</span></li>
            <li><span>game ecology and game management</span></li>
            <li><span>ethical and sustainable hunting</span></li>
            <li><span>firearms and cartridges (includes also hunting bows and arrows)</span></li>
            <li><span>hunting safety</span></li>
            <li><span>hunting equipment and hunting methods</span></li>
            <li><span>handling of quarry</span></li>
        </ul>
        <p style="padding-top:20pt">
            A hunting licence includes hunter’s insurance covering personal injuries caused by a firearm while hunting.
            The insurance is valid for one year (<joda:format value="${model.huntingCardStart}" pattern="dd.MM.yyyy"/>
            &dash; <joda:format value="${model.huntingCardEnd}" pattern="dd.MM.yyyy"/>) in Finland, Scandinavia,
            Switzerland,
            the United Kingdom of Great Britain and Northern Ireland and European Union Member States.
        </p>

        <h1>Shooting test</h1>
        <p>
            Hunters of cloven-hoofed game and bear must take a paid shooting test arranged by a game management
            association.
            The shooting test for ungulate hunters involves taking four shots at a standing elk target at a distance of
            75 metres.
            All four shots must be within the hit area of the elk target (diameter 23 cm).
            The shooting test for bear hunters consists of taking four shots at a standing bear target at a distance of
            75 metres.
            All four shots must be within the hit area of the bear target (diameter 17 cm).
            A shooting test certificate valid for three years from the date of completing the test is issued to
            candidates who pass the test.
        </p>

        <p style="padding-top: 20pt;">
            Sincerely yours,
        </p>

        <p class="s1" style="padding-top: 8pt;">
            FINNISH WILDLIFE AGENCY
        </p>

        <h1 style="padding: 15pt 0 0;">
            Sauli Härkönen<br/>
        </h1>

        <h2 style="padding-top: 0">
            Director for Public Administration Tasks
        </h2>

        <p style="padding-top: 15pt; font-size:12pt;">
            SUOMEN RIISTAKESKUS<br/>
            SOMPIONTIE 1<br/>
            00730 HELSINKI<br/>
        </p>
    </div>

</div>
<div class="footer" style="margin-top: 200pt">
    <%@include file="footer-en.jsp" %>
</div>

<%@include file="header-en.jsp" %>
<div class="container">

    <h1 style="padding-top: 20pt;">
        Certificate of insurance
    </h1>

    <div style="font-size:10pt; line-height: 12pt">
        <div style="float:left;">
            Insurer<br/>
            Policyholder<br/>
            Policy period<br/>
            Insured<br/>
            Territorial Scope<br/>
        </div>

        <div style="padding-left:136pt">
            LocalTapiola General Mutual Insurance Company, Finland<br/>
            Suomen riistakeskus (Finlands viltcentral), Finnish Wildlife Agency<br/>
            August 1<sup>st</sup> <joda:format value="${model.huntingCardStart}" pattern="yyyy"/>
            –
            July 31<sup>st</sup> <joda:format value="${model.huntingCardEnd}" pattern="yyyy"/><br/>
            Certified hunters who hold a valid hunting card<br/>
            Nordic Countries, EU member states, Switzerland and Great Britain up to 60 days hunting trips:<br/>
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt">
        <div style="float:left;">
            Policy number<br/>
            Type of insurance<br/>
            Limit of liability<br/>
            Main exclusions<br/>
        </div>

        <div style="padding-left:110pt;">
            312-0042105-H<br/>
            Hunter’s Third Party Liability Insurance<br/>
            EUR 1,200,000 in personal injury<br/>
            Property damages
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt">
        <div style="float:left;">
            Policy number<br/>
            Type of insurance<br/>
            Limit of liability
        </div>

        <div style="padding-left:110pt;">
            353-4344930-S<br/>
            Hunter’s Personal Accident Insurance<br/>
            EUR 10,000 in personal injury<br/>
            EUR 50,000 in permanent total disability<br/>
            EUR 30,000 in death
        </div>
    </div>

    <div style="padding-top: 20pt; font-size:10pt; line-height: 12pt">
        <div style="float:left;">
            Territorial Scope<br/>
        </div>

        <div style="padding-left:136pt">
            Nordic Countries and EU member states up to 60 days hunting trips:<br/>
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt">
        <div style="float:left;">
            Policy number<br/>
            Type of insurance<br/>
            Limit of liability
        </div>

        <div style="padding-left:110pt">
            353-4344930-S<br/>
            Official’s Personal Accident Insurance<br/>
            EUR 10,000 in personal injury<br/>
            EUR 50,000 in permanent total disability<br/>
            EUR 30,000 in death
        </div>
    </div>

    <p style="padding-left: 136pt;padding-top:20pt;line-height: 10pt">
        LOCALTAPIOLA GENERAL MUTUAL INSURANCE COMPANY<br/>
        Major Clients
    </p>

    <p style="padding-left: 136pt;font-style: italic;line-height: 10pt;margin-top: 20px">
        This certificate is issued as a matter of information only and confers no rights upon the certificate holder.
        This
        is an extract of the original insurance policy between policyholder and LocalTapiola which take precedence
        should
        there be any differences between the policy and the extract.
    </p>

</div>
<div class="footer" style="margin-top: 520pt">
    <%@include file="footer-en.jsp" %>
</div>

<%@include file="header-en.jsp" %>
<div class="container">

    <h1 style="padding-top: 20pt;padding-left: 21pt;">
        Information in the Hunting Card and in the Certificate of Passed Shooting Test (In Finnish/Swedish and in
        English):
    </h1>

    <h1 style="padding-left: 21pt;padding-top:20pt; padding-bottom: 15pt;">
        IN HUNTING CARD
    </h1>

    <table border="0" width="100%" style="margin-left: 20pt;">
        <th>
            <tr>
                <td width="33%" style="text-decoration: underline;">In Finnish:</td>
                <td width="33%" style="text-decoration: underline;">In Swedish:</td>
                <td width="33%" style="text-decoration: underline;">In English:</td>
            </tr>
        </th>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>Metsästyskortti</td>
            <td>Jaktkort</td>
            <td>Hunting card</td>
        </tr>
        <tr>
            <td>Metsästäjän nimi</td>
            <td>Jägarens namn</td>
            <td>Hunter`s name</td>
        </tr>
        <tr>
            <td>Syntymäaika</td>
            <td>Födelsetid</td>
            <td>Date of birth</td>
        </tr>
        <tr>
            <td>Yhdistys</td>
            <td>Jaktvårdsförening</td>
            <td>Game Management Association</td>
        </tr>
        <tr>
            <td>Yhdistysnumero</td>
            <td>Föreningsnummer</td>
            <td>Register number of the GMA</td>
        </tr>
        <tr>
            <td>Metsästäjänumero</td>
            <td>Jägarnummer</td>
            <td>Register number of the Hunter</td>
        </tr>
        <tr>
            <td>Jakeluosoite</td>
            <td>Utdelningsadress</td>
            <td>Street address</td>
        </tr>

        <tr>
            <td>Kotipaikka</td>
            <td>Hemort</td>
            <td>Domicile</td>
        </tr>
        <tr>
            <td>Postinumero ja -toimipaikka</td>
            <td>Postnummer och -anstalt</td>
            <td>City and Zip code</td>
        </tr>
        <tr>
            <td>Maksettu</td>
            <td>Betalt</td>
            <td>Paid</td>
        </tr>

    </table>

    <h1 style="padding-left: 20pt; padding-top:20pt; padding-bottom: 15pt;">
        IN CERTIFICATE OF PASSED SHOOTING TEST
    </h1>

    <table border="0" width="100%" style="margin-left: 20pt;">
        <th>
            <tr>
                <td width="33%" style="text-decoration: underline;">In Finnish:</td>
                <td width="33%" style="text-decoration: underline;">In Swedish:</td>
                <td width="33%" style="text-decoration: underline;">In English:</td>
            </tr>
        </th>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td width="33%">Ampumakoetodistus</td>
            <td width="33%">Intyg över avlagt skjutprov</td>
            <td width="33%">Big game shooting certificate</td>
        </tr>
        <tr>
            <td>Nimi</td>
            <td>Namn</td>
            <td>Name</td>
        </tr>
        <tr>
            <td>Metsästäjänumero</td>
            <td>Jägarnummer</td>
            <td>Register number of the Hunter</td>
        </tr>
        <tr>
            <td>Kansalaisuus</td>
            <td>Medborgarskap</td>
            <td>Nationality</td>
        </tr>
        <tr>
            <td>...on suorittanut metsästyslain</td>
            <td>...har avlagt det i jaktlagen</td>
            <td>...has passed the required big</td>
        </tr>
        <tr>
            <td>edellyttämän ampumakokeen</td>
            <td>nämnda skjutprovet för</td>
            <td>game shooting test for</td>
        </tr>
        <tr>
            <td>Paikka ja aika</td>
            <td>Plats och tid</td>
            <td>Place and date</td>
        </tr>
        <tr>
            <td>Suoritus on voimassa</td>
            <td>Provet gäller till</td>
            <td>The certificate is valid</td>
        </tr>
        <tr>
            <td>__/__20__ saakka</td>
            <td>den __/__20__</td>
            <td>until __/__20__</td>
        </tr>
        <tr>
            <td>Ampumakokeen vastaanottaja</td>
            <td>Examinator för skjutprov</td>
            <td>Shooting test official (signature)</td>
        </tr>
        <tr>
            <td>Leima</td>
            <td>Stämpel</td>
            <td>Stamp</td>
        </tr>
    </table>

</div>
<div class="footer" style="margin-top: 460pt">
    <%@include file="footer-en.jsp" %>
</div>
</body>
</html>
