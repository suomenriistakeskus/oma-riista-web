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
    <title>Shooting Test Summary</title>
    <base href="/static/shootingtest-summary/">
    <link href="style.css" rel="stylesheet"/>
</head>
<body>
<div class="main-container">
    <div class="header-container">
        <table class="extend">
            <tr>
                <td>
                    <div class="col-1">
                        <div class="logo"/>
                    </div>

                </td>
                <td class="col-2">
                    <strong>
                        <c:out value="${model.riistanhoitoyhdistysDTO.nameSV}"/></br>
                        JVF-nummer:&nbsp;<c:out value="${model.riistanhoitoyhdistysDTO.officialCode}"/>
                    </strong>
                </td>
                <td class="col-3">
                    <strong>
                        AVRÄKNINGSSAMMANDRAG</br>
                        Utskrivet: <joda:format value="${model.timestamp}" pattern="d.M.YYYY"/>
                    </strong>
                </td>
            </tr>
        </table>

    </div>
    <hr/>

    <hr/>

    <table class="summary-table">
        <tr>
            <td><strong>Tid:</strong></td>
            <td><strong><joda:format value="${model.calendarEventDTO.date}" pattern="dd.MM.YYYY"/>
                &nbsp;
                <joda:format value="${model.calendarEventDTO.beginTime}" pattern="HH"/>
                &dash;
                <joda:format value="${model.calendarEventDTO.endTime}" pattern="HH"/></strong></td>
        </tr>
        <tr>
            <td><strong>Plats:</strong></td>
            <td><strong><c:out value="${model.calendarEventDTO.venue.name}"/></strong></td>
        </tr>
        <tr>
            <td>
                <strong>Mottagare av skjutprov:</strong>
            </td>
            <td>
                <c:forEach items="${model.calendarEventDTO.getOfficials()}" var="o">
                    <strong><c:out value="${o.lastName}"/>&nbsp;<c:out value="${o.firstName}"/></strong></br>
                </c:forEach>
            </td>
        </tr>

    </table>
    </br>


    <hr/>

    <hr/>

    <h1>PRESTATIONER:</h1>


    <table class="summary-table">
        <thead>
        <tr>

            <th><strong>Namn</strong></th>
            <th><strong>Provform</strong></th>
            <th><strong>Resultat</strong></th>
            <th><strong>St</strong></th>
            <th><strong>Sammanlagt</strong></th>
            <th><strong>Betalt</strong></th>

        </tr>
        </thead>
        <tbody>
        <c:forEach items="${model.shootingTestParticipantDTOS}" var="p">
            <c:forEach items="${p.attempts}" var="att" varStatus="attemptNum">
                <tr>
                    <td>
                        <c:if test="${attemptNum.first}">
                            <strong>
                                <c:out value="${p.lastName}"/>&nbsp;
                                <c:out value="${p.firstName}"/>
                            </strong>,&nbsp;
                            <c:out value="${p.hunterNumber}"/>
                        </c:if>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${att.type.toString() == 'MOOSE'}">Älg&nbsp;/&nbsp;Hjort</c:when>
                            <c:when test="${att.type.toString() == 'ROE_DEER'}">Rådjur</c:when>
                            <c:when test="${att.type.toString() == 'BEAR'}">Björn</c:when>
                            <c:when test="${att.type.toString() == 'BOW'}">Pilbåge</c:when>
                            <c:otherwise>${att.type}</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${att.qualified}">Godkänd</c:when>
                            <c:otherwise>Ej&nbsp;godkända</c:otherwise>
                        </c:choose>

                    </td>
                    <td>
                        <c:out value="${att.attemptCount}"/>
                    </td>
                    <td>
                        <c:if test="${attemptNum.first}">
                            <c:out value="${p.totalDueAmount}"/>
                        </c:if>
                    </td>
                    <td>
                        <c:if test="${attemptNum.first}">
                            <c:out value="${p.paidAmount}"/>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </c:forEach>

        <tr class="summary-table-summary">
            <td colspan="3" class="summary-table-summary"><strong>Prestationer totalt:</strong></td>
            <td class="summary-table-summary"><strong><c:out value="${model.totalAttempts}"/></strong></td>
            <td colspan="2" class="summary-table-summary"></td>
        </tr>
        </tbody>
    </table>

    <hr>
    <hr>

    <table class="summary-table">
        <tbody>
        <tr>
            <td><strong>Erlagda betalningar totalt</strong></td>
            <td align="right"><strong><c:out value="${model.calendarEventDTO.totalPaidAmount}"/>&nbsp;€</strong></td>
        </tr>
        <tr>
            <td><strong>-&nbsp;kontant</strong></td>
            <td align="right"><strong>_______&nbsp;€</strong></td>
        </tr>
        <tr>
            <td><strong>-&nbsp;elektronisk prestation</strong></td>
            <td align="right"><strong>_______&nbsp;€</strong></td>
        </tr>
        </tbody>
    </table>

    <hr>
    <hr>

    <div>
        <table class="summary-table">
            <tbody>
            <tr>
                <td>Avräkningen överlämnats</td>
                <td>_______/_____________</td>
                <td>______________________________________________</td>
            </tr>
            <tr>
                <td></td>
                <td></td>
                <td>Avräkningens underskrift och namnförtydligande</td>
            </tr>

            <tr>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td>&nbsp;</td>
            </tr>
            <tr class="signature-line">
                <td>Avräkningen mottagen</td>
                <td>_______/_____________</td>
                <td>______________________________________________</td>
            </tr>
            <tr>
                <td></td>
                <td></td>
                <td>Mottagarens underskrift och namnförtydligande</td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

</div>

</body>
</html>
