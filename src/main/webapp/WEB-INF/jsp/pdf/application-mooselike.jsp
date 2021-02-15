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
                <br>
                <span><c:out value="${model.contactPerson.phoneNumber}"/></span>
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
                    <c:if test="${fn:length(spa.description) > 0}">
                        <tr>
                            <td></td>
                            <td>
                                <em>
                                    <c:out value="${spa.description}"/>
                                </em>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="mooselike.application.heading.partners"/>
        </div>
        <div class="col-2">
            <h2><fmt:message key="mooselike.application.partners.intro"/></h2>

            <table class="data-table">
                <tbody>
                <c:forEach var="club" items="${model.partners}" varStatus="s">
                    <tr>
                        <td>
                            <c:out value="${club.officialCode}"/>
                        </td>
                        <td align="right">
                            <c:out value="${club.nameFI}"/>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="mooselike.application.heading.area"/>
        </div>
        <div class="col-2">
            <h2><fmt:message key="mooselike.application.area.intro"/></h2>

            <table class="data-table">
                <tbody>
                <tr>
                    <td><fmt:message key="mooselike.application.area.land"/></td>
                    <td align="right">
                        <fmt:formatNumber value="${model.landAreaSize/10000.0} " maxFractionDigits="0"/>
                        &nbsp;
                        <fmt:message key="pdf.application.ha"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="mooselike.application.area.water"/></td>
                    <td align="right">
                        <fmt:formatNumber value="${model.waterAreaSize/10000.0} " maxFractionDigits="0"/>
                        &nbsp;
                        <fmt:message key="pdf.application.ha"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="mooselike.application.area.total"/></td>
                    <td align="right">
                        <fmt:formatNumber value="${model.totalAreaSize/10000.0} " maxFractionDigits="0"/>
                        &nbsp;
                        <fmt:message key="pdf.application.ha"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="mooselike.application.area.state"/></td>
                    <td align="right">
                        <fmt:formatNumber value="${model.stateLandAreaSize/10000.0} " maxFractionDigits="0"/>
                        &nbsp;
                        <fmt:message key="pdf.application.ha"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="mooselike.application.area.private"/></td>
                    <td align="right">
                        <fmt:formatNumber value="${model.privateLandAreaSize/10000.0} " maxFractionDigits="0"/>
                        &nbsp;
                        <fmt:message key="pdf.application.ha"/>
                    </td>
                </tr>
                </tbody>
            </table>

            <c:if test="${model.rhys != null && model.rhys.size() > 0}">
                <hr/>

                <h2><fmt:message key="mooselike.application.area.rhy"/></h2>

                <c:forEach var="rhy" items="${model.rhys}" varStatus="s">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <td colspan="4">
                                <c:out value="${rhy.rhy.nameFI}"/>
                            </td>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td></td>
                            <td align="right"></td>
                            <td align="right"><fmt:message key="mooselike.application.area.rhy.state"/></td>
                            <td align="right"><fmt:message key="mooselike.application.area.rhy.private"/></td>
                        </tr>
                        <tr>
                            <td>
                                <p><fmt:message key="mooselike.application.area.rhy.land"/></p>
                                <p><fmt:message key="mooselike.application.area.rhy.water"/></p>
                                <p><fmt:message key="mooselike.application.area.rhy.total"/></p>
                            </td>
                            <td align="right">
                                <p>
                                    <fmt:formatNumber value="${rhy.bothSize.land/10000.0} " maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                                <p>
                                    <fmt:formatNumber value="${rhy.bothSize.water/10000.0} " maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                                <p>
                                    <fmt:formatNumber value="${rhy.bothSize.total/10000.0}" maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                            </td>
                            <td align="right">
                                <p>
                                    <fmt:formatNumber value="${rhy.stateSize.land/10000.0} " maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                                <p>
                                    <fmt:formatNumber value="${rhy.stateSize.water/10000.0} " maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                                <p>
                                    <fmt:formatNumber value="${rhy.stateSize.total/10000.0} " maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                            </td>
                            <td align="right">
                                <p><fmt:formatNumber value="${rhy.privateSize.land/10000.0} " maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                                <p><fmt:formatNumber value="${rhy.privateSize.water/10000.0} " maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                                <p><fmt:formatNumber value="${rhy.privateSize.total/10000.0} " maxFractionDigits="0"/>
                                    &nbsp;
                                    <fmt:message key="pdf.application.ha"/></p>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </c:forEach>
            </c:if>

            <c:if test="${model.htas != null && model.htas.size() > 0}">
                <hr/>

                <h2><fmt:message key="mooselike.application.area.hta"/></h2>

                <table class="data-table">
                    <tbody>
                    <c:forEach var="hta" items="${model.htas}" varStatus="s">
                        <tr>
                            <td>
                                <c:out value="${hta.hta.nameFI}"/>
                            </td>
                            <td align="right">
                                <fmt:formatNumber value="${hta.computedAreaSize/10000.0} " maxFractionDigits="0"/>
                                &nbsp;
                                <fmt:message key="pdf.application.ha"/>
                                <br/>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <table class="data-table">
                <thead>
                <tr>
                    <th>
                        <p><fmt:message key="mooselike.application.area.union.title"/></p>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <c:out value="${model.unionAreaId}"/>
                    </td>
                </tr>
                </tbody>
            </table>

            <c:if test="${model.areaPartners != null && model.areaPartners.size() > 0}">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>
                            <p><fmt:message key="mooselike.application.area.partners.title"/></p>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="areaPartner" items="${model.areaPartners}" varStatus="s">
                        <tr>
                            <c:choose>
                                <c:when test="${model.locale == 'sv'}">
                                    <td>
                                        <c:out value="${areaPartner.club.nameSV}"/>
                                    </td>
                                </c:when>
                                <c:otherwise>
                                    <td>
                                        <c:out value="${areaPartner.club.nameFI}"/>
                                    </td>
                                </c:otherwise>
                            </c:choose>
                            <td>
                                <c:out value="${areaPartner.sourceArea.externalId}"/>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </div>

    <c:if test="${model.freeHunting}">
        <div class="layout-container">
            <div class="col-1">
                <fmt:message key="mooselike.application.heading.shooters"/>
            </div>
            <div class="col-2">
                <h2><fmt:message key="mooselike.application.shooter.intro"/></h2>

                <table class="data-table">
                    <tbody>
                    <tr>
                        <td>
                            <fmt:message key="mooselike.application.shooter.only.club"/>
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterOnlyClub}"/>
                            &nbsp;
                            <fmt:message key="pdf.application.pcs"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <fmt:message key="mooselike.application.shooter.other.club.passive"/>
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterOtherClubPassive}"/>
                            &nbsp;
                            <fmt:message key="pdf.application.pcs"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <fmt:message key="mooselike.application.shooter.other.club.active"/>
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterOtherClubActive}"/>
                            &nbsp;
                            <fmt:message key="pdf.application.pcs"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <fmt:message key="mooselike.application.shooter.total"/>
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterOnlyClub + model.shooterOtherClubPassive}"/>
                            &nbsp;
                            <fmt:message key="pdf.application.pcs"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="mooselike.application.heading.attachments"/>
        </div>
        <div class="col-2">
            <h2><fmt:message key="mooselike.application.attachments.summary"/></h2>

            <table class="data-table">
                <tbody>
                <c:if test="${model.mhAreaPermits.size() > 0}">
                    <tr>
                        <td>
                            <fmt:message key="mooselike.application.attachments.area"/>
                        </td>
                        <td align="right">
                            <c:out value="${model.mhAreaPermits.size()}"/>
                            &nbsp;
                            <fmt:message key="pdf.application.pcs"/>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${model.shooterLists.size() > 0}">
                    <tr>
                        <td>
                            <fmt:message key="mooselike.application.attachments.summary.shooter"/>
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterLists.size()}"/>&nbsp;
                            <fmt:message key="pdf.application.pcs"/>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td>
                        <fmt:message key="mooselike.application.attachments.summary.other"/>
                    </td>
                    <td align="right">
                        <c:out value="${model.otherAttachments.size()}"/>&nbsp;
                        <fmt:message key="pdf.application.pcs"/>
                    </td>
                </tr>
                </tbody>
            </table>

            <c:if test="${model.mhAreaPermits.size() > 0}">
                <h2><fmt:message key="mooselike.application.attachments.area"/></h2>

                <table class="data-table">
                    <tbody>
                    <c:forEach var="a" items="${model.mhAreaPermits}" varStatus="s">
                        <tr>
                            <td>
                                <c:out value="${a}"/>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>

            <c:if test="${model.shooterLists.size() > 0}">
                <hr/>

                <h2><fmt:message key="mooselike.application.attachments.shooter"/></h2>

                <table class="data-table">
                    <tbody>
                    <c:forEach var="a" items="${model.shooterLists}" varStatus="s">
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

                <h2><fmt:message key="mooselike.application.attachments.other"/></h2>

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

    <div class="layout-container">
        <div class="col-1">
            <fmt:message key="mooselike.application.heading.delivery"/>
        </div>
        <div class="col-2">
            <c:if test="${model.deliveryByMail == true}">
                <p><fmt:message key="pdf.application.delivery.letter"/></p>
            </c:if>

            <c:if test="${model.deliveryByMail == false}">
                <p><fmt:message key="pdf.application.delivery.email"/></p>
            </c:if>

            <hr/>

            <c:if test="${model.email1 != null || model.email2 != null}">
                <p><fmt:message key="pdf.application.delivery.extra"/>:</p>

                <table class="data-table">
                    <tbody>
                    <c:if test="${model.email1 != null}">
                        <tr>
                            <td>
                                <c:out value="${model.email1}"/>
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${model.email2 != null}">
                        <tr>
                            <td>
                                <c:out value="${model.email2}"/>
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </c:if>
        </div>
    </div>
</div>
</body>
</html>
