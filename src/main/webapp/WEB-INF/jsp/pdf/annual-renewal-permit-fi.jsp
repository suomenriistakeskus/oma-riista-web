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

    <h1>Ilmoitusmenettely</h1>

    <p>
        Suomen riistakeskus on hyväksynyt päätöksellään <joda:format value="${model.decisionDate}" pattern="d.M.YYYY" />
        nro ${model.decisionNumber} osallistumisenne metsästyslain
        41 b §:n 3 momentin mukaiseen ilmoitusmenettelyyn.
    </p>

    <p>
        Hyväksymispäätöksessä Suomen riistakeskus on vahvistanut ne lintulajit, joita ilmoitusmenettely koskee sekä
        asettanut vuosittain metsästettäväksi sallitut enimmäismäärät lajeittain. Ilmoitusmenettelyyn hyväksytyn toimijan on
        vuosittain tehtävä saalisilmoitus Suomen riistakeskukselle.
    </p>

    <p>
        Vuonna ${model.permitYear} saatu saalis ilmoitetaan numerolla ${model.permitNumber}. Ilmoitus suositellaan tehtäväksi
        Oma riista- palvelussa.
        Saalisilmoitus on tehtävä myös siinä tapauksessa, että saalista ei ole saatu. Tarkemmat ohjeet
        saalisilmoituksen tekemiseen löytyvät osoitteesta
        <a href="https://riista.fi/metsastys/saalisilmoitukset/rauhoittamattomat-linnut">https://riista.fi/metsastys/saalisilmoitukset/rauhoittamattomat-linnut</a>.
    </p>

    <p>
        Mikäli suojattava toiminta loppuu tai merkittävästi muuttuu, on siitä ilmoitettava Suomen riistakeskukselle,
        joka tekee arvion ilmoitusmenettelyn jatkumisesta.
    </p>


    <p>
        Ystävällisin terveisin,<br>
        Suomen riistakeskus
    </p>


</div>
<div class="footer">
    <%@include file="footer-fi.jsp" %>
</div>
</body>
</html>
