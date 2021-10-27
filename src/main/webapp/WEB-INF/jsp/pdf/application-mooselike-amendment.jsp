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
    <title><fmt:message key="pdf.application.title"/>&nbsp;<c:out value="${model.applicationNumber}"/></title>
    <base href="/static/application-pdf/">
    <link href="style.css" rel="stylesheet"/>
</head>
<body>
<div id="main-container">
    <div class="header-container">
        <div class="col-1">
            <div class="logo"></div>

            <p>
                <em>Lähettäjä / Avsändare</em><br/>
                <span><c:out value="${model.contactPerson.firstName}"/></span>
                <span><c:out value="${model.contactPerson.lastName}"/></span>
                <br>
                <span><c:out value="${model.contactPerson.address.streetAddress}"/></span>
                <br>
                <span><c:out value="${model.contactPerson.address.postalCode}"/></span>
                <span><c:out value="${model.contactPerson.address.city}"/>,</span>
                <span><c:out value="${model.contactPerson.address.country}"/></span>
            </p>
        </div>
        <div class="col-2">
            <p><fmt:message key="pdf.application.heading"/></p>

            <table id="header-sub-table">
                <thead>
                <tr>
                    <td>
                        <em><fmt:message key="pdf.application.header.date"/></em>
                    </td>
                    <td>
                        <em><fmt:message key="pdf.application.header.time"/></em>
                    </td>
                    <td>
                        <em><fmt:message key="pdf.application.header.number"/></em>
                    </td>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <joda:format value="${model.submitDate}" pattern="dd.MM.YYYY"/>
                    </td>
                    <td>
                        <joda:format value="${model.submitDate}" pattern="HH:mm"/>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${model.applicationNumber != null}">
                                <c:out value="${model.applicationNumber}"/>
                            </c:when>
                            <c:otherwise>
                                <fmt:message key="pdf.application.header.draft"/>
                            </c:otherwise>
                        </c:choose>
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
                SUOMEN RIISTAKESKUS<br>
                Sompiontie 1<br>
                00730 HELSINKI<br>
                029 431 2001
            </p>

            <h1>
                <c:out value="${model.applicationName}"/>
            </h1>
        </div>
        <div class="col-2">
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="pdf.application.heading"/>
        </div>
        <div class="col-2">
            <table class="data-table">
                <tbody>
                <c:if test="${fn:length(model.permitHolder.code) > 0}">
                    <tr>
                        <td><fmt:message key="pdf.application.holder.code"/></td>
                        <td align="right">
                            <c:out value="${model.permitHolder.code}"/>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td><fmt:message key="pdf.application.holder.name"/></td>
                    <td align="right">
                        <c:out value="${model.permitHolder.name}"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="mooselike.application.heading.applicant"/>
        </div>
        <div class="col-2">
            <h2><fmt:message key="pdf.application.intro"/>:</h2>

            <table class="data-table">
                <tbody>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.original.permit"/>
                    </td>
                    <td align="right">
                        <c:out value="${model.originalPermitNumber}"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.species"/>
                    </td>
                    <td align="right">
                        <c:out value="${model.species}"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.point.of.time"/>
                    </td>
                    <td align="right">
                        <joda:format value="${model.pointOfTime}" pattern="dd.MM.YYYY HH:mm"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.age.and.gender"/>
                    </td>
                    <td align="right">
                        <span><c:out value="${model.age}"/></span>
                        <span><c:out value="${model.gender}"/></span>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.shooter"/>
                    </td>
                    <td align="right">
                        <span><c:out value="${model.shooter.lastName}"/></span>
                        <span><c:out value="${model.shooter.firstName}"/></span>
                        <span><c:out value="${model.shooter.hunterNumber}"/></span>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.partner"/>
                    </td>
                    <td align="right">
                        <c:out value="${model.partner.nameFI}"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.location"/>
                    </td>
                    <td align="right">
                        <fmt:message key="mooselike.application.amendment.latitude"/>
                        &nbsp;
                        <c:out value="${model.geoLocation.latitude}"/>
                        &nbsp;
                        <fmt:message key="mooselike.application.amendment.longitude"/>
                        &nbsp;
                        <c:out value="${model.geoLocation.longitude}"/>
                        (ETRS-TM35FIN)
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <em>
                            <c:out value="${model.description}" escapeXml="false"/>
                        </em>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="mooselike.application.heading.attachments"/>
        </div>
        <div class="col-2">
            <h2><fmt:message key="mooselike.application.attachments.summary"/></h2>

            <table class="data-table">
                <tbody>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.statements"/>
                    </td>
                    <td align="right">
                        <c:out value="${model.officialStatements.size()}"/>
                        &nbsp;
                        <fmt:message key="pdf.application.pcs"/>
                    </td>
                </tr>

                <tr>
                    <td>
                        <fmt:message key="mooselike.application.amendment.other.attachments"/>
                    </td>
                    <td align="right">
                        <c:out value="${model.otherAttachments.size()}"/>
                        &nbsp;
                        <fmt:message key="pdf.application.pcs"/>
                    </td>
                </tr>
                </tbody>
            </table>

            <c:if test="${model.officialStatements.size() > 0}">
                <h2><fmt:message key="mooselike.application.amendment.statements"/></h2>

                <table class="data-table">
                    <tbody>
                    <c:forEach var="a" items="${model.officialStatements}" varStatus="s">
                        <tr>
                            <td>
                                <c:out value="${a}"/>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${model.otherAttachments.size() > 0}">
                <hr/>

                <h2><fmt:message key="mooselike.application.amendment.other.attachments"/></h2>

                <table class="data-table">
                    <tbody>
                    <c:forEach var="a" items="${model.otherAttachments}" varStatus="s">
                        <tr>
                            <td>
                                <c:out value="${a}"/>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </div>
</div>
</body>
</html>
