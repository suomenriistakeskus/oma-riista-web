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
    <title><c:out value="${model.heading.title} ${model.permitNumber}"/></title>
    <base href="/static/decision-pdf/">
    <link href="style.css" rel="stylesheet"/>
    <script>
        function getParameterByName(name, url) {
            name = name.replace(/[\[\]]/g, "\\$&");
            var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
                results = regex.exec(url);
            if (!results) return null;
            if (!results[2]) return '';
            return decodeURIComponent(results[2].replace(/\+/g, " "));
        }

        window.onload = function () {
            var sectionId = getParameterByName('sectionId', window.location.href);

            if (sectionId) {
                var elem = document.getElementById(sectionId);

                if (elem) {
                    var bbox = elem.getBoundingClientRect();
                    window.scrollTo(0, bbox.top);
                }
            }
        };
    </script>
</head>
<body>
<div id="main-container">
    <div class="header-container" id="general">
        <div class="col-1">
            <div class="logo"></div>
            <p>
                <c:choose>
                    <c:when test="${model.swedish}">
                        <em>Lähettäjä / Avsändare</em><br/>
                        FINLANDS VILTCENTRAL<br/>
                        Sompiovägen 1<br/>
                        00730 HELSINGFORS<br/>
                        029 431 2001
                    </c:when>
                    <c:otherwise>
                        <em>Lähettäjä / Avsändare</em><br/>
                        SUOMEN RIISTAKESKUS<br/>
                        Sompiontie 1<br/>
                        00730 HELSINKI<br/>
                        029 431 2001
                    </c:otherwise>
                </c:choose>
            </p>
        </div>
        <div class="col-2">
            <p><c:out value="${model.heading.title}"/></p>

            <table id="header-sub-table">
                <thead>
                <tr>
                    <td>
                        <em><c:out value="${model.heading.decisionDate}"/></em>
                    </td>
                    <td>
                        <em><c:out value="${model.heading.decisionNumber}"/></em>
                    </td>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <joda:format value="${model.publishDate}" pattern="dd.MM.YYYY"/>
                    </td>
                    <td>
                        <c:out value="${model.permitNumber}"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="header-container">
        <div class="col-1 extend">
            <p>

            <em>Vastaanottaja / Mottagare</em><br/>
            XXXXXXXXXXXXXXX
            <br>
            XXXXXXXXXXXXXXX
            <br>
            XXXXXXXXXXXXXXX
            <br>
            XXXXXXXXXXXXXXX

            </p>

            <h1>
                <c:out value="${model.heading.decisionName}"/>
            </h1>
        </div>
    </div>


    <div class="layout-container">
        <div class="col-1">
            <c:out value="${model.heading.applicant}"/>
        </div>
        <div class="col-2">
            <p>
                XXXXXXXXXXXXXXX
            </p>
        </div>
    </div>

    <div class="layout-container" id="application">
        <div class="col-1">
            <c:out value="${model.heading.application}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.application}" escapeXml="false"/>
        </div>
    </div>

    <c:if test="${model.includeApplicationReasoning}">
        <div class="layout-container" id="applicationReasoning">
            <div class="col-1">
                <c:out value="${model.heading.applicationReasoning}"/>
            </div>
            <div class="col-2">
                <c:out value="${model.document.applicationReasoning}" escapeXml="false"/>
            </div>
        </div>
    </c:if>

    <c:if test="${model.includeProcessing}">
        <div class="layout-container" id="processing">
            <div class="col-1">
                <c:out value="${model.heading.processing}"/>
            </div>
            <div class="col-2">
                <c:out value="${model.document.processing}" escapeXml="false"/>
            </div>
        </div>
    </c:if>

    <div class="layout-container" id="decision">
        <div class="col-1">
            <c:out value="${model.heading.decision}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.decision}" escapeXml="false"/>
            <c:out value="${model.document.decisionExtra}" escapeXml="false"/>
        </div>
    </div>

    <c:if test="${model.includeRestriction}">
        <div class="layout-container" id="restriction">
            <div class="col-1">
                <c:out value="${model.heading.restriction}"/>
            </div>
            <div class="col-2">
                <c:out value="${model.document.restriction}" escapeXml="false"/>
                <c:out value="${model.document.restrictionExtra}" escapeXml="false"/>
            </div>
        </div>
    </c:if>

    <div class="layout-container" id="decisionReasoning">
        <div class="col-1">
            <c:out value="${model.heading.decisionReasoning}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.decisionReasoning}" escapeXml="false"/>
        </div>
    </div>

    <c:if test="${model.includeExecution}">
        <div class="layout-container" id="execution">
            <div class="col-1">
                <c:out value="${model.heading.execution}"/>
            </div>
            <div class="col-2">
                <c:out value="${model.document.execution}" escapeXml="false"/>
            </div>
        </div>
    </c:if>

    <div class="layout-container" id="legalAdvice">
        <div class="col-1">
            <c:out value="${model.heading.legalAdvice}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.legalAdvice}" escapeXml="false"/>
        </div>
    </div>

    <c:if test="${model.includeNotificationObligation}">
        <div class="layout-container" id="notificationObligation">
            <div class="col-1">
                <c:out value="${model.heading.notificationObligation}"/>
            </div>
            <div class="col-2">
                <c:out value="${model.document.notificationObligation}" escapeXml="false"/>
            </div>
        </div>
    </c:if>

    <div class="layout-container" id="appeal">
        <div class="col-1">
            <c:out value="${model.heading.appeal}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.appeal}" escapeXml="false"/>
        </div>
    </div>

    <div class="layout-container" id="additionalInfo">
        <div class="col-1">
            <c:out value="${model.heading.additionalInfo}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.additionalInfo}" escapeXml="false"/>
        </div>
    </div>

    <div class="layout-container" id="delivery">
        <div class="col-1">
            <c:out value="${model.heading.delivery}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.delivery}" escapeXml="false"/>
        </div>
    </div>

    <c:if test="${model.includePayment}">
        <div class="layout-container" id="payment">
            <div class="col-1">
                <c:out value="${model.heading.payment}"/>
            </div>
            <div class="col-2">
                <c:out value="${model.document.payment}" escapeXml="false"/>
            </div>
        </div>
    </c:if>

    <c:if test="${model.includeAttachments}">
        <div class="layout-container" id="attachments">
            <div class="col-1">
                <c:out value="${model.heading.attachments}"/>
            </div>
            <div class="col-2">
                <c:out value="${model.document.attachments}" escapeXml="false"/>
            </div>
        </div>
    </c:if>
</div>

<c:choose>
    <c:when test="${model.swedish}">
        <%@include file="decision-appeal-sv.jsp" %>
        <%@include file="decision-correction-sv.jsp" %>
    </c:when>
    <c:otherwise>
        <%@include file="decision-appeal-fi.jsp" %>
        <%@include file="decision-correction-fi.jsp" %>
    </c:otherwise>
</c:choose>

</body>
</html>
