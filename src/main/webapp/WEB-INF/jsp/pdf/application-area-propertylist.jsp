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
    <title><fmt:message key="HarvestPermitAreaMmlPdf.title"/></title>
    <base href="/static/propertylist-pdf/">
    <link href="style.css" rel="stylesheet"/>
</head>
<body>
<div class="layout-container">

    <table id="propertylist">
        <thead>
        <tr>
            <td>
                <strong><fmt:message key="HarvestPermitAreaMmlPdf.kiinteistoTunnus"/></strong>
            </td>
            <td>
                <strong><fmt:message key="HarvestPermitAreaMmlPdf.palstaId"/></strong>
            </td>
            <td>
                <strong><fmt:message key="HarvestPermitAreaMmlPdf.area"/></strong>
            </td>
            <td>
                <strong><fmt:message key="HarvestPermitAreaMmlPdf.name"/></strong>
            </td>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="p" items="${propertyList}" varStatus="s">
            <tr>
                <td>
                    <c:out value="${p.tunnus}"/>
                </td>
                <td>
                    <c:out value="${p.id}"/>
                </td>
                <td>
                    <fmt:formatNumber value="${p.area}" minFractionDigits="2" maxFractionDigits="2"/>
                </td>
                <td>
                    <c:out value="${p.name}"/>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
