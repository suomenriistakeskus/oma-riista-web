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
                            <fmt:formatNumber value="${spa.amount}" maxFractionDigits="0"/>
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
            <fmt:message key="bird.application.heading.area"/>
        </div>
        <div class="col-2">
            <table>
                <tbody>
                <tr>
                    <td><fmt:message key="bird.application.area.name"/></td>
                    <td align="right">
                        <c:out value="${model.protectedArea.name}"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bird.application.area.address"/></td>
                    <td align="right">
                        <c:out value="${model.protectedArea.streetAddress}"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bird.application.area.postalcode"/></td>
                    <td align="right">
                        <c:out value="${model.protectedArea.postalCode}"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bird.application.area.city"/></td>
                    <td align="right">
                        <c:out value="${model.protectedArea.city}"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bird.application.area.size"/></td>
                    <td align="right">
                        <c:out value="${model.protectedArea.protectedAreSize}"/>
                        <fmt:message key="pdf.application.ha"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="bird.application.area.type"/></td>
                    <td align="right">
                        <c:choose>
                            <c:when test="${model.protectedArea.protectedAreaType == 'OTHER'}">
                                <fmt:message key="bird.application.area.type.OTHER"/>
                            </c:when>
                            <c:when test="${model.protectedArea.protectedAreaType == 'AIRPORT'}">
                                <fmt:message key="bird.application.area.type.AIRPORT"/>
                            </c:when>
                            <c:when test="${model.protectedArea.protectedAreaType == 'FOOD_PREMISES'}">
                                <fmt:message key="bird.application.area.type.FOOD_PREMISES"/>
                            </c:when>
                            <c:when test="${model.protectedArea.protectedAreaType == 'WASTE_DISPOSAL'}">
                                <fmt:message key="bird.application.area.type.WASTE_DISPOSAL"/>
                            </c:when>
                            <c:when test="${model.protectedArea.protectedAreaType == 'BERRY_FARM'}">
                                <fmt:message key="bird.application.area.type.BERRY_FARM"/>
                            </c:when>
                            <c:when test="${model.protectedArea.protectedAreaType == 'FUR_FARM'}">
                                <fmt:message key="bird.application.area.type.FUR_FARM"/>
                            </c:when>
                            <c:when test="${model.protectedArea.protectedAreaType == 'FISHERY'}">
                                <fmt:message key="bird.application.area.type.FISHERY"/>
                            </c:when>
                            <c:when test="${model.protectedArea.protectedAreaType == 'ANIMAL_SHELTER'}">
                                <fmt:message key="bird.application.area.type.ANIMAL_SHELTER"/>
                            </c:when>
                        </c:choose>
                    </td>
                </tr>
                </tbody>
            </table>

            <hr>

            <c:if test="${fn:length(model.areaAttachments) > 0}">
                <h2><fmt:message key="bird.application.area.map"/>:</h2>

                <ul>
                    <c:forEach var="a" items="${model.areaAttachments}" varStatus="s">
                        <li><c:out value="${a.name}"/></li>
                    </c:forEach>
                </ul>
            </c:if>

            <c:if test="${model.areaDescription != null}">
                <p>
                    <h2><fmt:message key="bird.application.area.areaDescription"/>:</h2>
                    <c:out value="${model.areaDescription}"></c:out>
                </p>
            </c:if>
            <hr>

            <h2>
                <fmt:message key="bird.application.area.rights"/>:
            </h2>

            <hr>

            <p>
                <c:out value="${model.protectedArea.descriptionOfRights}"/>
            </p>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="bird.application.heading.cause"/>
        </div>
        <div class="col-2">
            <ul>
                <c:if test="${model.permitCause.causePublicHealth}">
                    <li><fmt:message key="bird.application.cause.health"/></li>
                </c:if>

                <c:if test="${model.permitCause.causePublicSafety}">
                    <li><fmt:message key="bird.application.cause.safety"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeAviationSafety}">
                    <li><fmt:message key="bird.application.cause.aviation"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeCropsDamage}">
                    <li><fmt:message key="bird.application.cause.crops"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeDomesticPets}">
                    <li><fmt:message key="bird.application.cause.domestic"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeForestDamage}">
                    <li><fmt:message key="bird.application.cause.forest"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeFishing}">
                    <li><fmt:message key="bird.application.cause.fish"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeWaterSystem}">
                    <li><fmt:message key="bird.application.cause.water"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeFlora}">
                    <li><fmt:message key="bird.application.cause.flora"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeFauna}">
                    <li><fmt:message key="bird.application.cause.fauna"/></li>
                </c:if>

                <c:if test="${model.permitCause.causeResearch}">
                    <li><fmt:message key="bird.application.cause.research"/></li>
                </c:if>
            </ul>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="bird.application.heading.period"/>
        </div>
        <div class="col-2">
            <c:choose>
                <c:when test="${model.validityYears == 0}">
                    <p><fmt:message key="bird.application.period.limitless"/></p>
                </c:when>
                <c:when test="${model.validityYears > 0}">
                    <p>
                        <fmt:message key="bird.application.period.limited"/>
                        &nbsp;
                        <c:out value="${model.validityYears}"/>
                        &nbsp;
                        <fmt:message key="bird.application.period.years"/>.
                    </p>
                </c:when>
            </c:choose>

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

                            <c:if test="${model.forbiddenMethods.tapeRecorders}">
                                <br><br>
                                <fmt:message key="bird.application.forbidden.tape"/>
                            </c:if>

                            <c:if test="${fn:length(model.forbiddenMethods.deviateSection33) > 0}">
                                <br><br>
                                <fmt:message key="bird.application.forbidden.other"/>:
                                <br>
                                <c:out value="${model.forbiddenMethods.deviateSection33}"/>
                            </c:if>
                        </li>
                    </c:if>


                    <c:if test="${fn:length(model.forbiddenMethods.deviateSection34) > 0 || model.forbiddenMethods.traps}">
                        <li>
                            <fmt:message key="bird.application.forbidden.34"/>:

                            <c:if test="${model.forbiddenMethods.traps}">
                                <br><br>
                                <fmt:message key="bird.application.forbidden.traps"/>
                            </c:if>

                            <c:if test="${fn:length(model.forbiddenMethods.deviateSection34) > 0}">
                                <br><br>
                                <fmt:message key="bird.application.forbidden.other"/>:
                                <br>
                                <c:out value="${model.forbiddenMethods.deviateSection34}"/>
                            </c:if>
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
            <fmt:message key="bird.application.heading.damage"/>
        </div>
        <div class="col-2">
            <p><fmt:message key="bird.application.damage.title"/>:</p>

            <hr>

            <h2><fmt:message key="bird.application.damage.amount"/></h2>

            <ul>
                <c:forEach var="spa" items="${model.damage}" varStatus="s">
                    <li>
                        <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>:
                        <c:out value="${spa.causedDamageAmount}"/> &euro;
                    </li>
                </c:forEach>
            </ul>

            <hr>

            <h2><fmt:message key="bird.application.damage.description"/></h2>

            <ul>
                <c:forEach var="spa" items="${model.damage}" varStatus="s">
                    <li>
                        <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>:
                        <c:out value="${spa.causedDamageDescription}"/>
                    </li>
                </c:forEach>
            </ul>

            <hr>

            <p><fmt:message key="bird.application.damage.eviction.title"/></p>

            <hr>

            <h2><fmt:message key="bird.application.damage.eviction.method"/></h2>

            <ul>
                <c:forEach var="spa" items="${model.damage}" varStatus="s">
                    <li>
                        <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>:
                        <c:out value="${spa.evictionMeasureDescription}"/>
                    </li>
                </c:forEach>
            </ul>

            <hr>

            <h2><fmt:message key="bird.application.damage.eviction.effects"/></h2>

            <ul>
                <c:forEach var="spa" items="${model.damage}" varStatus="s">
                    <li>
                        <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>:
                        <c:out value="${spa.evictionMeasureEffect}"/>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="bird.application.heading.population"/>
        </div>
        <div class="col-2">
            <p><fmt:message key="bird.application.population.title"/></p>

            <hr>

            <h2><fmt:message key="bird.application.population.amount"/></h2>

            <ul>
                <c:forEach var="spa" items="${model.population}" varStatus="s">
                    <li>
                        <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>:
                        <c:out value="${spa.populationAmount}"/>
                    </li>
                </c:forEach>
            </ul>

            <hr>

            <h2><fmt:message key="bird.application.population.description"/></h2>

            <ul>
                <c:forEach var="spa" items="${model.population}" varStatus="s">
                    <li>
                        <c:out value="${speciesNames[spa.gameSpeciesCode]}"/>:
                        <c:out value="${spa.populationDescription}"/>
                    </li>
                </c:forEach>
            </ul>
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
