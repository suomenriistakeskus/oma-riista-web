<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false" trimDirectiveWhitespaces="true" %>
<%@ page import="fi.riista.feature.account.certificate.HunterForeignCertificateDTO" %>
<%@ page import="org.joda.time.LocalDate" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Certificate</title>
    <base href="/static/foreign-hunter-certificate/">
    <link href="style.css" rel="stylesheet"/>
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

    <div style="float:right; position:relative; width: 200pt; padding-top:20pt;">
        <h2>Certificate of hunting card</h2>
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
        has passed The Finnish Hunters’ examination and he has a valid hunting card for hunting season

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
            There are six parts in the examination and the training course for it:
        </p>

        <ul>
            <li><span>hunting legislation in Finland and other regulations of hunting</span></li>
            <li><span>Finnish organizations in hunting</span></li>
            <li><span>hunting rights in Finland</span></li>
            <li><span>game management</span></li>
            <li><span>firearms and cartridges</span></li>
            <li><span>hunting methods, search of wounded game, handling of quarry, good hunting behaviour etc.</span></li>
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
            Nordic Countries and EU member states up to 60 days hunting trips<br/>
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

    <div class="position:relative">
        <div style="padding-left: 60pt; float:left;">
            <p style="text-decoration: underline;">
                In Finnish:
            </p>

            <p style="padding-top: 10pt;">
                Metsästyskortti<br/>
                Metsästäjän nimi<br/>
                Syntymäaika<br/>
                Yhdistys<br/>
                Yhdistysnumero<br/>
                Metsästäjänumero<br/>
                Jakeluosoite<br/>
                Kotipaikka<br/>
                Postinumero ja -toimipaikka<br/>
            </p>
        </div>

        <div style="margin-left: 260pt;">
            <p style="text-decoration: underline;">
                In English:
            </p>

            <p style="padding-top: 10pt;">
                Hunting card<br/>
                Hunter`s name<br/>
                Date of birth<br/>
                Game Management Association<br/>
                Register number of the GMA<br/>
                Register number of the Hunter<br/>
                Street address<br/>
                Domicile<br/>
                City and Zip code<br/>
            </p>
        </div>
    </div>

    <h1 style="padding-left: 20pt; padding-top:40pt;">
        IN CERTIFICATE OF PASSED SHOOTING TEST
    </h1>

    <div class="position:relative">
        <div style="padding-left: 60pt; float:left;">
            <p style="padding-top: 10pt;">
                Ampumakokeen suorituskortti<br/>
                Nimi<br/>
                Syntymäaika<br/>
                Metsästäjänumero<br/>
                on suorittanut metsästyslain<br/>
                edellyttämän ampumakokeen<br/>
                Suoritus on voimassa __/__ saakka<br/>
                Ammunnan valvoja<br/>
                Leima<br/>
                Suoritus on voimassa kolme vuot-<br/>
                ta suorittamisajankohdasta lukien
            </p>
        </div>

        <div style="margin-left: 260pt;">
            <p style="padding-top: 10pt;">
                Certificate of the passed shooting test<br/>
                Name<br/>
                Date of birth<br/>
                Register number of the Hunter<br/>
                ...has passed the shooting test required<br/>
                in the Hunting Act (place, date)<br/>
                The test is valid until __/__<br/>
                Supervisor of the test (signature)<br/>
                Stamp<br/>
                The test result is valid for three years<br/>
                from the date of test
            </p>
        </div>
    </div>

    <div class="footer" style="margin-top: 460pt">
        <%@include file="footer-en.jsp"%>
    </div>
</div>
</body>
</html>
