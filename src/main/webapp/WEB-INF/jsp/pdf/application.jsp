<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<!DOCTYPE html>
<html lang="fi">
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Hakemus <c:out value="${model.applicationNumber}"/></title>
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
            <p>HAKEMUS</p>

            <table id="header-sub-table">
                <thead>
                <tr>
                    <td>
                        <em>Pvm</em>
                    </td>
                    <td>
                        <em>Klo</em>
                    </td>
                    <td>
                        <em>Nro</em>
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
                        <c:out value="${model.applicationNumber}"/>
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
            HAKIJA
        </div>
        <div class="col-2">
            <table class="data-table">
                <tbody>
                <c:if test="${model.permitHolder != null}">
                    <tr>
                        <td>Luvansaajan asiakasnumero</td>
                        <td align="right">
                            <c:out value="${model.permitHolder.officialCode}"/>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td>Luvansaajan nimi</td>
                    <td align="right">
                        <c:if test="${model.permitHolder != null}">
                            <c:out value="${model.permitHolder.nameFI}"/>
                        </c:if>

                        <c:if test="${model.permitHolder == null}">
                            <span><c:out value="${model.contactPerson.firstName}"/></span>
                            <span><c:out value="${model.contactPerson.lastName}"/></span>
                        </c:if>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="layout-container">
        <div class="col-1">
            HAKEMUS
        </div>
        <div class="col-2">
            <h2>Hakija on hakenut Suomen riistakeskukselta lupaa seuraavasti:</h2>

            <table class="data-table">
                <tbody>
                <c:forEach var="spa" items="${model.speciesAmounts}" varStatus="s">
                    <tr>
                        <td>
                            <c:out value="${spa.name}"/>
                        </td>
                        <td align="right">
                            <c:out value="${spa.amount}"/>
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
            OSAKKAAT
        </div>
        <div class="col-2">
            <h2>Lista hakemuksen osakkaista</h2>

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
            HAKEMUSALUE
        </div>
        <div class="col-2">
            <h2>Hakemusalueen yhteenveto</h2>

            <table class="data-table">
                <tbody>
                <tr>
                    <th>Maapinta-ala</th>
                    <td align="right">
                        <fmt:formatNumber value="${model.landAreaSize/10000.0} " maxFractionDigits="0"/> ha
                    </td>
                </tr>
                <tr>
                    <th>Vesipinta-ala</th>
                    <td align="right">
                        <fmt:formatNumber value="${model.waterAreaSize/10000.0} " maxFractionDigits="0"/> ha
                    </td>
                </tr>
                <tr>
                    <th>Kokonaispinta-ala</th>
                    <td align="right">
                        <fmt:formatNumber value="${model.totalAreaSize/10000.0} " maxFractionDigits="0"/> ha
                    </td>
                </tr>
                <tr>
                    <th>Valtionmaiden maapinta-ala</th>
                    <td align="right">
                        <fmt:formatNumber value="${model.stateLandAreaSize/10000.0} " maxFractionDigits="0"/> ha
                    </td>
                </tr>
                <tr>
                    <th>Yksityismaiden maapinta-ala</th>
                    <td align="right">
                        <fmt:formatNumber value="${model.privateLandAreaSize/10000.0} " maxFractionDigits="0"/> ha
                    </td>
                </tr>
                </tbody>
            </table>

            <c:if test="${model.rhys != null && model.rhys.size() > 0}">
                <hr/>

                <h2>Hakemusalue sijaitsee seuraavien riistanhoitoyhdistysten alueella</h2>

                <c:forEach var="rhy" items="${model.rhys}" varStatus="s">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th colspan="4">
                                <strong>
                                    <c:out value="${rhy.rhy.nameFI}"/>
                                </strong>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td></td>
                            <td align="right"></td>
                            <td align="right"><strong>Valtio</strong></td>
                            <td align="right"><strong>Yksityinen</strong></td>
                        </tr>
                        <tr>
                            <th>
                                <p><strong>Maa</strong></p>
                                <p><strong>Vesi</strong></p>
                                <p><strong>Yhteensä</strong></p>
                            </th>
                            <td align="right">
                                <p><fmt:formatNumber value="${rhy.bothSize.land/10000.0} " maxFractionDigits="0"/>
                                    ha</p>
                                <p><fmt:formatNumber value="${rhy.bothSize.water/10000.0} " maxFractionDigits="0"/>
                                    ha</p>
                                <p><fmt:formatNumber value="${rhy.bothSize.total/10000.0}" maxFractionDigits="0"/>
                                    ha</p>
                            </td>
                            <td align="right">
                                <p><fmt:formatNumber value="${rhy.stateSize.land/10000.0} " maxFractionDigits="0"/>
                                    ha</p>
                                <p><fmt:formatNumber value="${rhy.stateSize.water/10000.0} " maxFractionDigits="0"/>
                                    ha</p>
                                <p><fmt:formatNumber value="${rhy.stateSize.total/10000.0} " maxFractionDigits="0"/>
                                    ha</p>
                            </td>
                            <td align="right">
                                <p><fmt:formatNumber value="${rhy.privateSize.land/10000.0} " maxFractionDigits="0"/>
                                    ha</p>
                                <p><fmt:formatNumber value="${rhy.privateSize.water/10000.0} " maxFractionDigits="0"/>
                                    ha</p>
                                <p><fmt:formatNumber value="${rhy.privateSize.total/10000.0} " maxFractionDigits="0"/>
                                    ha</p>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </c:forEach>
            </c:if>

            <c:if test="${model.htas != null && model.htas.size() > 0}">
                <hr/>

                <h2>Hakemusalue sijaitsee seuraavien hirvitalousalueiden alueella</h2>

                <table class="data-table">
                    <tbody>
                    <c:forEach var="hta" items="${model.htas}" varStatus="s">
                        <tr>
                            <td>
                                <c:out value="${hta.hta.nameFI}"/>
                            </td>
                            <td align="right">
                                <fmt:formatNumber value="${hta.computedAreaSize/10000.0} " maxFractionDigits="0"/>
                                ha<br/>
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
                AMPUJIEN MÄÄRÄ ML 8 §
            </div>
            <div class="col-2">
                <h2>Ampujien määrä, joilla on ampumakoe voimassa tai vanhenee kuluvana metsästysvuonna.</h2>

                <table class="data-table">
                    <tbody>
                    <tr>
                        <td>
                            Ampujat, jotka <strong>eivät kuulu muuhun</strong> pyyntilupaa hakevaan
                            seuraan / seurueeseen.
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterOnlyClub}"/>
                            kpl
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Ampujat, jotka kuuluvat muuhun hirveä metsästävään
                            seuraan / seurueeseen, mutta <strong>eivät metsästä siellä</strong> tulevana
                            metsästyskautena.
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterOtherClubPassive}"/>
                            kpl
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Ampujat, jotka kuuluvat muuhun hirveä metsästävään
                            seuraan / seurueeseen, ja <strong>metsästävät siellä</strong> tulevana metsästyskautena.
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterOtherClubActive}"/>
                            kpl
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Lupamäärään vaikuttavien ampujien määrä yhteensä
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterOnlyClub + model.shooterOtherClubPassive}"/>
                            kpl
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <div class="layout-container">
        <div class="col-1">
            LIITTEET
        </div>
        <div class="col-2">
            <h2>Yhteenveto</h2>

            <table class="data-table">
                <tbody>
                <c:if test="${model.mhAreaPermits.size() > 0}">
                    <tr>
                        <td>
                            Aluelupa ML 8 §
                        </td>
                        <td align="right">
                            <c:out value="${model.mhAreaPermits.size()}"/> kpl
                        </td>
                    </tr>
                </c:if>
                <c:if test="${model.shooterLists.size() > 0}">
                    <tr>
                        <td>
                            Ampujaluettelo ML 8 §
                        </td>
                        <td align="right">
                            <c:out value="${model.shooterLists.size()}"/> kpl
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td>
                        Muita liitetiedostoja
                    </td>
                    <td align="right">
                        <c:out value="${model.otherAttachments.size()}"/> kpl
                    </td>
                </tr>
                </tbody>
            </table>

            <c:if test="${model.mhAreaPermits.size() > 0}">
                <h2>Aluelupa ML 8 §</h2>

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

                <h2>Ampujaluettelot ML 8 §</h2>

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

                <h2>Muut liitteet</h2>

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
            TOIMITUS
        </div>
        <div class="col-2">
            <c:if test="${model.deliveryByMail == true}">
                <p>Päätös toimitetaan luvan yhteyshenkilölle postitse.</p>
            </c:if>

            <c:if test="${model.deliveryByMail == false}">
                <p>Päätös toimitetaan luvan yhteyshenkilölle sähköisesti.</p>
            </c:if>

            <hr/>

            <c:if test="${model.email1 != null || model.email2 != null}">
                <p>Päätös toimitetaan lisäksi tiedoksi seuraaviin sähköpostiosoitteisiin:</p>

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
