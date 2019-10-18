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

<div class="container">
    <div class="header-logo">
        <img width="280" alt="image" src="riista-logo.jpg"/>
    </div>


    <div class="header-text">
        TODISTUS<br/>
        <c:if test="${model.derogation != null}">
            metsästyslain 41 a §:n 3 momentti<br/>
        </c:if>
        <c:if test="${model.derogation == null}">
            metsästyslain 41 a §:n 4 momentti<br/>
        </c:if>
        <br/>
        <joda:format value="${model.currentDate}" pattern="d.M.YYYY"/>
    </div>


    <br/>
    <br/>

    <h1>
        TODISTUS<br/>
    </h1>

    <br/>
    <!-- Derogation -->
    <c:if test="${model.derogation != null}">
        <p>
            Suomen riistakeskus todistaa, että saajalla on ollut Suomen riistakeskuksen myöntämä poikkeuslupa kyseisen
            eläimen pyyntiin.
        </p>

        <br/>
        <br/>


        <dl>
            <dt>
                Poikkeusluvan myöntäjä:
            </dt>
            <dd>
                Suomen riistakeskus
            </dd>
            <dt>
                Poikkeusluvan saaja:
            </dt>
            <dd>${model.derogation.permitHolderName}

            </dd>
            <dt>
                Poikkeusluvan myöntöpäivä:
            </dt>
            <dd><joda:format value="${model.derogation.publishDate}" pattern="d.M.YYYY"/>

            </dd>
            <dt>
                Poikkeusluvan nro:
            </dt>
            <dd>
                    ${model.derogation.permitNumber}
            </dd>
        </dl>
    </c:if>

    <!-- Quota -->
    <c:if test="${model.derogation == null}">
        <p>
            Suomen riistakeskus todistaa, että todistuksen saaja on saanut kyseisen eläimen
            saaliiksi alueellisen kiintiön nojalla.
        </p>
        <br/>
        <br/>
    </c:if>
    <dl>
        <dt>
            Saaliin saaja:
        </dt>
        <dd>
            ${model.shooterName}
        </dd>

        <dt>
            Metsästäjänumero:
        </dt>
        <dd>
            ${model.hunterNumber}<br/>
        </dd>

        <dt>
            Saalis saatu:
        </dt>
        <dd>
            <joda:format value="${model.pointOfTime}" pattern="d.M.YYYY"/>
            klo <joda:format value="${model.pointOfTime}" pattern="HH.mm"/>
        </dd>

        <dt>
            Kaatopaikan riistanhoitoyhdistys:
        </dt>
        <dd>
            ${model.rhy}<br/>
        </dd>

        <dt>
            Kaatopaikan koordinaatit:
        </dt>
        <dd>
            ETRS-TM35FIN (P/I): ${model.latitude} / ${model.longitude}
        </dd>

        <dt>
            Saaliseläin:
        </dt>
        <dd>
            ${model.species}<br/>
            sukupuoli: ${model.gender}<br/>
            paino: <fmt:formatNumber type="number" maxFractionDigits="2" value="${model.weight}"/> kg
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
        Todistus on allekirjoitettu koneellisesti riistahallintolain (158/2011) 8 §:n 5 momentin nojalla.<br/>
    </p>

    <br/>

    <div class="footer" style="margin-top: 330pt">
        <%@include file="footer-fi.jsp" %>
    </div>
</div>
</body>
</html>
