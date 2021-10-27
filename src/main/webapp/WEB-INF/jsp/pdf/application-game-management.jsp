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
                <c:out value="${fn:toUpperCase(model.applicationName)}"/>
            </h1>
        </div>
        <div class="col-2">
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
            <fmt:message key="bird.application.heading.application"/>
        </div>
        <div class="col-2">
            <h2><fmt:message key="pdf.application.intro"/>:</h2>

            <table>
                <tbody>
                <c:forEach var="spa" items="${model.speciesAmounts}" varStatus="s">
                    <tr>
                        <td>
                            <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>
                        </td>
                        <td align="right">
                            <c:choose>
                                <c:when test="${spa.specimenAmount != null}">
                                    <fmt:formatNumber value="${spa.specimenAmount}" maxFractionDigits="0"/>
                                </c:when>
                                <c:otherwise>
                                    <c:out value="-"/>
                                </c:otherwise>
                            </c:choose>
                            &nbsp;
                            <fmt:message key="pdf.application.pcs"/>
                        </td>
                        <td align="right">
                            <c:choose>
                                <c:when test="${spa.eggAmount != null}">
                                    <fmt:formatNumber value="${spa.eggAmount}" maxFractionDigits="0"/>
                                </c:when>
                                <c:otherwise>
                                    <c:out value="-"/>
                                </c:otherwise>
                            </c:choose>
                            &nbsp;
                            <fmt:message key="pdf.application.pcs"/>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="carnivore.application.heading.area"/>
        </div>
        <div class="col-2">
            <c:if test="${model.derogationPermitApplicationAreaDTO.areaDescription != null}">
                <h2><fmt:message key="carnivore.application.area.areaDescription"/></h2>
                <p>
                    <c:out value="${model.derogationPermitApplicationAreaDTO.areaDescription}"/>
                </p>
            </c:if>

            <c:if test="${model.areaAttachments.size() > 0}">
                <h2><fmt:message key="carnivore.application.area.map"/>:</h2>

                <ul>
                    <c:forEach var="a" items="${model.areaAttachments}" varStatus="s">
                        <li><c:out value="${a.name}"/></li>
                    </c:forEach>
                </ul>
            </c:if>

            <hr>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="bird.application.heading.period"/>
        </div>
        <div class="col-2">
            <p>
                <fmt:message key="bird.application.period.limited"/>
                &nbsp;
                <c:out value="${model.speciesPeriods[0].validityYears}"/>
                &nbsp;
                <fmt:message key="bird.application.period.years"/>.
            </p>
            <hr>

            <table>
                <tbody>
                <c:forEach var="spa" items="${model.speciesPeriods}" varStatus="s">
                    <tr>
                        <td>
                            <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>
                        </td>
                        <td align="right">
                            <joda:format value="${spa.beginDate}" pattern="d.M."/>
                            &dash;
                            <joda:format value="${spa.endDate}" pattern="d.M."/>
                        </td>
                    </tr>
                    <c:if test="${fn:length(spa.additionalPeriodInfo) > 0}">
                        <tr>
                            <td>&nbsp;</td>
                            <td>
                                <c:out value="${spa.additionalPeriodInfo}"/>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <c:if test="${model.forbiddenMethods.forbiddenMethodSelected}">
        <div class="layout-container">
            <div class="col-1">
                <fmt:message key="bird.application.heading.methods"/>
            </div>
            <div class="col-2">
                <ul>
                    <c:if test="${fn:length(model.forbiddenMethods.deviateSection32) > 0}">
                        <li>
                            <fmt:message key="bird.application.forbidden.32"/>

                            <br><br>
                            <c:out value="${model.forbiddenMethods.deviateSection32}"/>
                        </li>
                    </c:if>

                    <c:if test="${fn:length(model.forbiddenMethods.deviateSection33) > 0 || model.forbiddenMethods.tapeRecorders}">
                        <li>
                            <fmt:message key="bird.application.forbidden.33"/>

                            <br><br>
                            <c:out value="${model.forbiddenMethods.deviateSection33}"/>
                        </li>
                    </c:if>


                    <c:if test="${fn:length(model.forbiddenMethods.deviateSection34) > 0 || model.forbiddenMethods.traps}">
                        <li>
                            <fmt:message key="bird.application.forbidden.34"/>:

                            <br><br>
                            <c:out value="${model.forbiddenMethods.deviateSection34}"/>
                        </li>
                    </c:if>

                    <c:if test="${fn:length(model.forbiddenMethods.deviateSection35) > 0}">
                        <li>
                            <fmt:message key="bird.application.forbidden.35"/>:

                            <br><br>
                            <c:out value="${model.forbiddenMethods.deviateSection35}"/>
                        </li>
                    </c:if>

                    <c:if test="${fn:length(model.forbiddenMethods.deviateSection51) > 0}">
                        <li>
                            <fmt:message key="bird.application.forbidden.51"/>:
                            <br><br>
                            <c:out value="${model.forbiddenMethods.deviateSection51}"/>
                        </li>
                    </c:if>
                </ul>

                <p>
                    <fmt:message key="bird.application.forbidden.justification"/>:
                </p>

                <ul>
                    <c:forEach var="spa" items="${model.forbiddenMethods.speciesJustifications}" varStatus="s">
                        <li>
                            <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>:
                            <c:choose>
                                <c:when test="${spa.active}">
                                    <c:out value="${spa.justification}"/>
                                </c:when>
                                <c:otherwise>
                                    <fmt:message key="bird.application.forbidden.not.applicable"/>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </c:if>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="gamemanagement.application.heading.justification"/>
        </div>
        <div class="col-2">
            <p style="white-space: pre-wrap"><c:out value="${model.justification}"/></p>
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
