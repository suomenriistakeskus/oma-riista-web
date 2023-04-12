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
    <title><fmt:message key="HuntingControlPdfReport.title"/></title>
    <base href="/static/hunting-control-report/">
    <link href="style.css" rel="stylesheet"/>
</head>
<body>


<div class="container">
    <div class="header">
        <h1><fmt:message key="HuntingControlPdfReport.title"/></h1>
        ${model.rkaName}, ${model.rhyName}&nbsp;<joda:format value="${model.reportStartDate}" pattern="dd.MM.YYYY"/> - <joda:format value="${model.reportEndDate}" pattern="dd.MM.YYYY"/><br>
        <fmt:message key="HuntingControlPdfReport.printDate"/>&nbsp;<joda:format value="${model.currentDate}" pattern="dd.MM.YYYY"/>
    </div>

    <c:if test="${model.map64Encoded != null}">
        <div class="img-container">
            <img class="event" alt="event-map" src="data:image/jpeg;base64,${model.map64Encoded}" />
            <div class="copyright">
                Maastokartta <joda:format value="${model.currentDate}" pattern="MM/YYYY"/> &copy; Maanmittauslaitos
            </div>
        </div>
    </c:if>

    <table>
        <thead>
        <tr>
            <td style="width: 17%; font-weight: bold"><fmt:message key="HuntingControlPdfReport.status"/></td>
            <td style="width: 15%; font-weight: bold"><fmt:message key="HuntingControlPdfReport.time"/></td>
            <td style="width: 23%; font-weight: bold"><fmt:message key="HuntingControlPdfReport.type"/></td>
            <td style="width: 17%; font-weight: bold"><fmt:message key="HuntingControlPdfReport.inspectors"/></td>
            <td style="width: 13%; font-weight: bold"><fmt:message key="HuntingControlPdfReport.cooperation"/></td>
            <td style="width: 10%; font-weight: bold"><fmt:message key="HuntingControlPdfReport.customers"/></td>
            <td style="width: 5%; font-weight: bold; hyphens: auto"><fmt:message key="HuntingControlPdfReport.proofOrders"/></td>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${model.events}" var="event">
        <tr>
            <c:choose>
                <c:when test="${event.status == 'PROPOSED'}">
                    <td class="status proposed"><fmt:message key="HuntingControlEventStatus.${event.status}"/></td>
                </c:when>
                <c:when test="${event.status == 'REJECTED'}">
                    <td class="status rejected"><fmt:message key="HuntingControlEventStatus.${event.status}"/></td>
                </c:when>
                <c:otherwise>
                    <td class="status accepted"><fmt:message key="HuntingControlEventStatus.${event.status}"/></td>
                </c:otherwise>
            </c:choose>
            <td>
                <joda:format value="${event.date}" pattern="dd.MM.YYYY"/><br>
                <joda:format value="${event.beginTime}" pattern="HH:mm"/> - <joda:format value="${event.endTime}" pattern="HH:mm"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${event.eventType != null}">
                        <fmt:message key="HuntingControlEventType.${event.eventType}"/>
                    </c:when>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${fn:length(event.inspectors) > 0}">
                        <c:forEach items="${event.inspectors}" var="inspector">
                            <c:out value="${inspector.firstName} ${inspector.lastName}"/><br>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        ${event.otherParticipants}
                    </c:otherwise>
                 </c:choose>
            </td>
            <td>
                <c:forEach items="${event.cooperationTypes}" var="cooperationType">
                    <fmt:message key="HuntingControlCooperationType.${cooperationType}"/><br>
                </c:forEach>
            </td>
            <td>
                ${event.customers}
            </td>
            <td>
                ${event.proofOrders}
            </td>
        </tr>
        </c:forEach>
        </tbody>
    </table>

</div>

</body>
</html>
