<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false"
         trimDirectiveWhitespaces="true" %>
<%@ page import="fi.riista.feature.permit.decision.derogation.pdf.AnnualRenewalPermitPdfModelDTO" %>
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
    <title><c:out value="${model.permitNumber}"/></title>
    <base href="/static/annual-renewal/">
    <link href="annual-renewal.css" rel="stylesheet"/>
</head>
<body>

<%
    final AnnualRenewalPermitPdfModelDTO model = (AnnualRenewalPermitPdfModelDTO) request.getAttribute("model");
%>
<div class="header-container">
    <div class="header-logo">
        <img width="280" alt="image" src="01_clean.jpg"/>
    </div>

    <div class="header-text">
        <joda:format value="${model.renewalDate}" pattern="d.M.YYYY" />
    </div>

    <hr class="header-separator">
</div>

<div class="container">

    <h1>Anmälningsförfarande</h1>

    <p>
        Finlands viltcentral har genom sitt beslut <joda:format value="${model.decisionDate}" pattern="d.M.YYYY" /> nr
        ${model.decisionNumber} godkänt ert deltagande i anmälningsförfarande i
        enlighet med 41 b 3 mom. i jaktlagen.
    </p>

    <p>
        I beslutet om godkännande har Finlands viltcentral bekräftat de fågelarter som anmälningsförfarandet gäller samt
        fastställt de maximala årliga fångstantalen per fågelart. En aktör som har godkänts för anmälningsförfarande ska
        varje år lämna in en fångstanmälan till Finlands viltcentral.
    </p>

    <p>
        Den fångst som har erhållits år ${model.permitYear} anmäls med nummer ${model.permitNumber}.Det rekommenderas att anmälan
        görs i tjänsten Oma riista.
        Fångstanmälan görs också i det fall att ingen fångst har erhållits.
        Noggrannare anvisningar om hur man gör fångstanmälan finns på adressen
        <a href="https://riista.fi/sv/jakt/bytesanmalan/icke-fredade-faglar/">https://riista.fi/sv/jakt/bytesanmalan/icke-fredade-faglar/</a>
    </p>

    <p>
        Om skyddsåtgärderna avslutas eller ändras väsentligt, ska detta meddelas Finlands viltcentral,
        som bedömer om anmälningsförfarandet ska fortsätta gälla.
    </p>


    <p>
        Med vänliga hälsningar,<br>
        Finlands viltcentral
    </p>
</div>
<div class="footer">
    <%@include file="footer-sv.jsp" %>
</div>
</body>
</html>
