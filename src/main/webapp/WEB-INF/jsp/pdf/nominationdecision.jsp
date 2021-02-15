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
    <title><c:out value="${model.heading.title} ${model.documentNumber}"/></title>
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
                        <em><c:out value="${model.heading.documentNumber}"/></em>
                    </td>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <joda:format value="${model.publishDate}" pattern="dd.MM.YYYY"/>
                    </td>
                    <td>
                        <c:out value="${model.documentNumber}"/>
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
                <c:if test="${model.deliveryAddress == null}">
                    <span><c:out value="${model.contactPerson.firstName}"/></span>
                    <span><c:out value="${model.contactPerson.lastName}"/></span>
                    <br>
                    <span><c:out value="${model.contactPerson.address.streetAddress}"/></span>
                    <br>
                    <span><c:out value="${model.contactPerson.address.postalCode}"/></span>
                    <span><c:out value="${model.contactPerson.address.city}"/></span>
                </c:if>
                <c:if test="${model.deliveryAddress != null}">
                    <span><c:out value="${model.deliveryAddress.recipient}"/></span>
                    <br>
                    <span><c:out value="${model.deliveryAddress.streetAddress}"/></span>
                    <br>
                    <span><c:out value="${model.deliveryAddress.postalCode}"/></span>
                    <span><c:out value="${model.deliveryAddress.city}"/></span>
                    <c:if test="${model.deliveryAddress.country != null}">
                        <br>
                        <span><c:out value="${model.deliveryAddress.country}"/></span>
                    </c:if>
                </c:if>
            </p>

            <h1>
                <c:out value="${model.heading.decisionName}"/>
            </h1>
        </div>
    </div>

    <div class="layout-container" id="proposal">
        <div class="col-1">
            <c:out value="${model.heading.proposal}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.proposal}" escapeXml="false"/>
        </div>
    </div>

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
        </div>
    </div>

    <div class="layout-container" id="decisionReasoning">
        <div class="col-1">
            <c:out value="${model.heading.decisionReasoning}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.decisionReasoning}" escapeXml="false"/>
        </div>
    </div>

    <div class="layout-container" id="legalAdvice">
        <div class="col-1">
            <c:out value="${model.heading.legalAdvice}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.legalAdvice}" escapeXml="false"/>
        </div>
    </div>


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

    <div class="layout-container" id="payment">
        <div class="col-1">
            <c:out value="${model.heading.payment}"/>
        </div>
        <div class="col-2">
            <c:out value="${model.document.payment}" escapeXml="false"/>
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
<%--    Two separate correction clauses for nomination decision--%>
    <c:when test="${model.swedish}">
        <%@include file="nominationdecision-correction-sv.jsp" %>
        <%@include file="decision-correction-sv.jsp" %>
    </c:when>
    <c:otherwise>
        <%@include file="nominationdecision-correction-fi.jsp" %>
        <%@include file="decision-correction-fi.jsp" %>
    </c:otherwise>
</c:choose>

</body>
</html>
