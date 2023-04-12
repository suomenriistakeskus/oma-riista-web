<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" %>
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
    <title>Todistus</title>
    <base href="/static/legal-harvest-certificate/">
    <link href="style.css" rel="stylesheet"/>
</head>
<body>

<div class="header-container">
    <div class="header-logo">
        <img width="280" alt="image" src="riistakeskus-logo.png"/>
    </div>


    <div class="header-text">
        INTYG<br/>
        <c:if test="${model.derogation != null}">
            jaktlagen 41 a § 3 momentet<br/>
        </c:if>
        <c:if test="${model.derogation == null}">
            jaktlagen 41 a § 4 momentet<br/>
        </c:if>
        <br/>
        <joda:format value="${model.currentDate}" pattern="d.M.YYYY"/>
    </div>

    <hr class="header-separator">
</div>

<div class="container">
    <br/>
    <br/>

    <h1>
        BYTESINTYG<br/>
    </h1>

    <br/>

    <!-- Derogation -->
    <c:if test="${model.derogation != null}">
        <p>
            Finlands viltcentral intygar att intygets mottagare har haft av Finlands viltcentral beviljad dispens för
            fångst av nämnda djur.
        </p>

        <br/>
        <br/>

        <dl>
            <dt>
                Dispensen utställd av:
            </dt>
            <dd>
                Finlands viltcentral
            </dd>
            <dt>
                Dispensens mottagare:
            </dt>
            <dd>${model.derogation.permitHolderName}

            </dd>
            <dt>
                Datum för beviljande av dispens:
            </dt>
            <dd><joda:format value="${model.derogation.publishDate}" pattern="d.M.YYYY"/>

            </dd>
            <dt>
                Dispensens nummer:
            </dt>
            <dd>
                    ${model.derogation.permitNumber}
            </dd>
        </dl>
    </c:if>

    <!-- Quota -->
    <c:if test="${model.derogation == null}">
        <p>
            Finlands viltcentral intygar att intygets mottagare har fällt nämnda djur som byte med stöd av den regionala
            kvoten.
        </p>
        <br/>
        <br/>
    </c:if>

    <dl>
        <dt>
            Bytet fällt av:
        </dt>
        <dd>
            ${model.shooterName}
        </dd>

        <dt>
            Jägarnummer:
        </dt>
        <dd>
            ${model.hunterNumber}<br/>
        </dd>

        <dt>
            Bytet fällt:
        </dt>
        <dd>
            <joda:format value="${model.pointOfTime}" pattern="d.M.YYYY"/>
            kl <joda:format value="${model.pointOfTime}" pattern="HH.mm"/>
        </dd>

        <dt>
            Jaktvårdsförening där bytet fällts:
        </dt>
        <dd>
            ${model.rhy}<br/>
        </dd>

        <dt>
            Koordinater (där bytet fällts):
        </dt>
        <dd>
            ETRS-TM35FIN (N/Ö): ${model.latitude} / ${model.longitude}
        </dd>

        <dt>
            Bytesdjur:
        </dt>
        <dd>
            ${model.species}<br/>
            kön: ${model.gender}<br/>
            vikt: <fmt:formatNumber type="number" maxFractionDigits="2" value="${model.weight}"/> kg
        </dd>
    </dl>

    <br/>
    <br/>
    <br/>

    <p>
        <joda:format value="${model.currentDate}" pattern="d.M.YYYY"/>
    </p>

    <br/>

    <p>
        <strong>${model.approver}</strong>
        <br/>
        <br/>
        Intyget är undertecknat maskinellt.<br/>
    </p>

    <br/>
</div>

<div class="footer" style="margin-top: 330pt">
    <%@include file="footer-sv.jsp" %>
</div>

</body>
</html>
