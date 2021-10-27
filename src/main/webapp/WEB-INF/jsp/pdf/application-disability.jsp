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
        <div class="col-1">
            <p>
                <em>Vastaanottaja / Mottagare</em><br/>
                SUOMEN RIISTAKESKUS<br>
                Sompiontie 1<br>
                00730 HELSINKI<br>
                029 431 2001
            </p>

            <h1>
                <c:out value="${fn:toUpperCase(model.applicationName)}"/>
            </h1>
        </div>
        <div class="col-2">
            <%@include file="classified.jsp" %>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <%-- TODO: Once localization texts are agreed upon, either combine or create own keys for mammal application--%>
            <fmt:message key="bird.application.heading.applicant"/>
        </div>
        <div class="col-2">
            <table>
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
            <fmt:message key="disability.application.heading.basicInfo"/>
        </div>
        <div class="col-2">
            <c:if test="${model.basicInfo.useMotorVehicle == true}">
                <fmt:message key="disability.application.useMotorVehicle"></fmt:message>
            </c:if>
        </div>
        <div class="col-2">
            <c:if test="${model.basicInfo.useVehicleForWeaponTransport == true}">
                <fmt:message key="disability.application.useVehicleForWeaponTransport"></fmt:message>
            </c:if>
        </div>
        <div class="col-2">
            <table>
                <tbody>
                    <td>
                        <fmt:message key="disability.application.time"></fmt:message>
                    </td>
                    <td align="right">
                        <joda:format value="${model.basicInfo.beginDate}" pattern="d.M.yyyy"/>
                        &dash;
                        <joda:format value="${model.basicInfo.endDate}" pattern="d.M.yyyy"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="disability.application.heading.justification.vehicle"/>
        </div>
        <div class="col-2">
            <table>
                <tbody>
                <c:forEach var="vehicle" items="${model.justification.vehicles}" varStatus="s">
                <tr>
                    <td style="width: 40%">
                        <c:choose>
                            <c:when test="${vehicle.type == 'AUTO'}">
                                <fmt:message key="PermitApplicationVehicleType.AUTO"></fmt:message>
                            </c:when>
                            <c:when test="${vehicle.type == 'MOOTTORIKELKKA'}">
                                <fmt:message key="PermitApplicationVehicleType.MOOTTORIKELKKA"></fmt:message>
                            </c:when>
                            <c:when test="${vehicle.type == 'MONKIJA'}">
                                <fmt:message key="PermitApplicationVehicleType.MONKIJA"></fmt:message>
                            </c:when>
                            <c:when test="${vehicle.type == 'MUU'}">
                                <fmt:message key="PermitApplicationVehicleType.MUU"></fmt:message>
                            </c:when>
                        </c:choose>
                    </td>
                    <td style="width: 60%">
                        <c:if test="${vehicle.type == 'MUU'}">
                            <c:out value="${vehicle.description}"/>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <td style="width: 40%">
                        <fmt:message key="disability.application.heading.vehicle.justification"></fmt:message>
                    </td>
                    <td style="width: 60%">
                        <c:out value="${vehicle.justification}"/>
                    </td>
                </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="disability.application.heading.justification.huntingType"/>
        </div>
        <div class="col-2">
            <table>
                <tbody>
                <c:forEach var="huntingTypeInfo" items="${model.justification.huntingTypeInfos}" varStatus="s">
                <tr>
                    <td style="width: 40%">
                        <c:choose>
                            <c:when test="${huntingTypeInfo.huntingType == 'PIENRIISTA'}">
                                <fmt:message key="HuntingType.PIENRIISTA"></fmt:message>
                            </c:when>
                            <c:when test="${huntingTypeInfo.huntingType == 'HIRVIELAIMET'}">
                                <fmt:message key="HuntingType.HIRVIELAIMET"></fmt:message>
                            </c:when>
                            <c:when test="${huntingTypeInfo.huntingType == 'SUURPEDOT'}">
                                <fmt:message key="HuntingType.SUURPEDOT"></fmt:message>
                            </c:when>
                            <c:when test="${huntingTypeInfo.huntingType == 'MUU'}">
                                <fmt:message key="HuntingType.MUU"></fmt:message>
                            </c:when>
                        </c:choose>
                    </td>
                    <td style="width: 60%">
                        <c:if test="${huntingTypeInfo.huntingType == 'MUU'}">
                            <c:out value="${huntingTypeInfo.huntingTypeDescription}"/>
                        </c:if>
                    </td>

                </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="disability.application.heading.justification"/>
        </div>
        <div class="col-2">
            <c:out value="${model.justification.justification}"/>
        </div>
    </div>

    <c:if test="${model.otherAttachments.size() > 0}">
        <div class="layout-container">
            <div class="col-1">
                <fmt:message key="bird.application.heading.attachments"/>
            </div>
            <div class="col-2">
                <ul>
                    <c:forEach var="a" items="${model.otherAttachments}" varStatus="s">
                        <li>
                            <p><c:out value="${a.name}"/></p>
                            <c:if test="${fn:length(a.additionalInfo) > 0}">
                                <p><c:out value="${a.additionalInfo}"/></p>
                            </c:if>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </c:if>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="bird.application.heading.delivery"/>
        </div>
        <div class="col-2">
            <c:if test="${model.deliveryAddress != null}">
                <p><fmt:message key="pdf.application.delivery.recipient"/></p>
                <p>
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
                </p>
            </c:if>
            <c:if test="${model.deliveryByMail == true}">
                <p><fmt:message key="pdf.application.delivery.letter"/></p>
            </c:if>

            <c:if test="${model.deliveryByMail == false}">
                <p><fmt:message key="pdf.application.delivery.email"/></p>
            </c:if>

            <hr/>

            <p>
                <fmt:message key="pdf.application.delivery.language"/>&nbsp;
                <c:if test="${model.decisionLanguage == 'sv'}">
                    <fmt:message key="pdf.application.delivery.language.swedish"/>
                </c:if>
                <c:if test="${model.decisionLanguage != 'sv'}">
                    <fmt:message key="pdf.application.delivery.language.finnish"/>
                </c:if>
            </p>
        </div>
    </div>
</div>
</body>
</html>
