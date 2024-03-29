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
    <title>JAKTKORT</title>
    <base href="/static/hunter-certificate/">
    <link href="certificate.css" rel="stylesheet"/>
    <link href="hunting-card.css" rel="stylesheet"/>
</head>
<body>

<!-- Page 1 -->
<div>
    <%@include file="hunting-card-header-sv.jsp"%>

    <div class="container">
        <%@include file="hunting-card-qrcode.jsp"%>

        <br/>

        <p>INTYG ÖVER BETALD JAKTVÅRDSAVGIFT</p>

        <h1>
            JAKTKORT<br/>
            <joda:format value="${model.huntingCardStart}" pattern="dd.MM.YYYY" />
            &dash;
            <joda:format value="${model.huntingCardEnd}" pattern="dd.MM.YYYY" />
        </h1>

        <dl>
            <dt>
                Jägarens namn:
            </dt>
            <dd>
                ${model.firstName}&nbsp;${model.lastName}
            </dd>

            <dt>
                Distributionsadress:
            </dt>
            <dd>
                ${model.streetAddress}<br/>
                ${model.postalCode}&nbsp;${model.postOffice}
            </dd>

            <dt>
                Hemort:
            </dt>
            <dd>
                <c:choose>
                    <c:when test="${model.homeMunicipalityName != null}">
                        <strong>
                                ${model.homeMunicipalityName}&nbsp;
                                ${model.homeMunicipalityCode}
                        </strong>
                    </c:when>
                    <c:otherwise>
                        <strong class="indent-right">Okänd</strong>
                    </c:otherwise>
                </c:choose>
            </dd>

            <dt>
                Betalningsdag:
            </dt>
            <dd>
                <c:choose>
                    <c:when test="${model.paymentDate != null}">
                        <joda:format value="${model.paymentDate}" pattern="dd.MM.YYYY" />
                    </c:when>
                    <c:otherwise>
                        <span style="color:red">EJ BETALD</span>
                    </c:otherwise>
                </c:choose>
            </dd>

            <dt>
                Jägarnummer:
            </dt>
            <dd>
                ${model.hunterNumber}
            </dd>

            <dt>
                Födelsetid:
            </dt>
            <dd>
                <joda:format value="${model.dateOfBirth}" pattern="dd.MM.YYYY" />
            </dd>

            <dt>
                Jaktvårdsförening och nr:
            </dt>
            <dd>
                <c:choose>
                    <c:when test="${model.rhyName != null}">
                        ${model.rhyName}
                        <c:if test="${model.rhyOfficialCode != null}">&nbsp;(${model.rhyOfficialCode})</c:if>
                    </c:when>
                    <c:otherwise>
                        Inte medlem
                    </c:otherwise>
                </c:choose>
            </dd>
        </dl>

        <hr/>

        <c:if test="${fn:length(model.occupationsPage1) > 0}">
            <h2>UPPGIFTER INOM JAKTVÅRDSFÖRENINGEN:</h2>

            <table width="100%">
                <tbody>
                <c:forEach items="${model.occupationsPage1}" var="o">
                    <tr>
                        <td class="occupation-name">
                            <strong>${o.occupationName}</strong><br/>
                                ${o.organisationName}&nbsp;(${o.organisationOfficialCode})
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${o.beginDate == null && o.endDate == null}">
                                    I kraft
                                </c:when>
                                <c:when test="${o.endDate == null}">
                                    I kraft
                                </c:when>
                                <c:when test="${o.beginDate == null}">
                                    Till och med <joda:format value="${o.endDate}" pattern="d.M.YYYY" />
                                </c:when>
                                <c:otherwise>
                                    <joda:format value="${o.beginDate}" pattern="d.M.YYYY" />
                                    &dash;
                                    <joda:format value="${o.endDate}" pattern="d.M.YYYY" />
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <hr/>
        </c:if>

        <c:if test="${model.isMultipage == false}">
            <c:if test="${fn:length(model.shootingTests) > 0}">
                <h2>SKJUTPROV:</h2>

                <table width="100%">
                    <tbody>
                    <c:forEach items="${model.shootingTests}" var="t">
                        <tr>
                            <td class="shooting-test-entry">
                                <div>${t.typeName}</div>
                                <div>${t.rhyName}&nbsp;(${t.rhyCode})</div>
                            </td>
                            <td>
                                I kraft
                                <joda:format value="${t.begin}" pattern="d.M.YYYY" />
                                &dash;
                                <joda:format value="${t.end}" pattern="d.M.YYYY" />
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <hr/>
            </c:if>

            <h2 style="padding-top: 0;">OBS!</h2>

            <div>
                <p class="small-print">
                    I detta kvitto nämnd jägare är ansvarsförsäkrad från och med betalningsdagen till slutet av jaktåret.
                    Försäkringen gäller i Norden, EU-länderna, Schweiz och Storbritannien på jaktresor som varar högst 60 dygn.
                    Försäkringen täcker skador som orsakats någon annan med skjutvapen – dock inte sakskador. Den till
                    försäkringen hörande jägarens privatolycksfallsförsäkring täcker skador som orsakats jägaren själv på grund
                    av att vapnet brunnit av eller exploderat. Jägare som erlagt viltvårdsavgift och som på uppdrag av polisen i
                    Finland utför eftersök av skadat djur omfattas av försäkringen liksom dennes hund som utför
                    eftersöksuppdraget. Då skada skett kontakta utan dröjsmål LokalTapiola, Ansvarsskador och hundskador
                    inträffade i SRVA-verksamhet tel. 09 453 4150 och Jägarens och funktionärens olycksfallsskador
                    tel. 09 453 3666. Om du vill byta jaktvårdsförening, skall du meddela detta till jägarregistret med telefon,
                    e-post eller per brev. I alla ärenden som berör jaktkortet och postning av Jägaren-tidningen betjänas Ni av:
                </p>

                <address>
                    Jägarregistret<br/>
                    PB 22<br/>
                    00331 Helsingfors<br/>
                    tel 029 431 2002 (vardagar november-juni: kl. 9-16 och juli-oktober: kl. 8-17)<br/>
                    e-mail: metsastajarekisteri@riista.fi
                </address>
            </div>
        </c:if>
    </div>

    <div class="footer" style="margin-top: 700pt">
        <%@include file="footer-sv.jsp"%>
    </div>
</div>

<!-- Page 2 -->
<c:if test="${model.isMultipage == true}">
<div>
    <%@include file="hunting-card-header-sv.jsp"%>

    <div class="container">
        <c:if test="${fn:length(model.occupationsPage2) > 0}">
            <h2>UPPGIFTER INOM JAKTVÅRDSFÖRENINGEN:</h2>

            <table width="100%">
                <tbody>
                <c:forEach items="${model.occupationsPage2}" var="o">
                    <tr>
                        <td class="occupation-name">
                            <strong>${o.occupationName}</strong><br/>
                                ${o.organisationName}&nbsp;(${o.organisationOfficialCode})
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${o.beginDate == null && o.endDate == null}">
                                    I kraft
                                </c:when>
                                <c:when test="${o.endDate == null}">
                                    I kraft
                                </c:when>
                                <c:when test="${o.beginDate == null}">
                                    Till och med <joda:format value="${o.endDate}" pattern="d.M.YYYY" />
                                </c:when>
                                <c:otherwise>
                                    <joda:format value="${o.beginDate}" pattern="d.M.YYYY" />
                                    &dash;
                                    <joda:format value="${o.endDate}" pattern="d.M.YYYY" />
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <hr/>
        </c:if>

        <c:if test="${fn:length(model.shootingTests) > 0}">
            <h2>SKJUTPROV:</h2>

            <table width="100%">
                <tbody>
                <c:forEach items="${model.shootingTests}" var="t">
                    <tr>
                        <td class="shooting-test-entry">
                            <div>${t.typeName}</div>
                            <div>${t.rhyName}&nbsp;(${t.rhyCode})</div>
                        </td>
                        <td>
                            I kraft
                            <joda:format value="${t.begin}" pattern="d.M.YYYY" />
                            &dash;
                            <joda:format value="${t.end}" pattern="d.M.YYYY" />
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <hr/>
        </c:if>

        <h2 style="padding-top: 0;">OBS!</h2>

        <div>
            <p class="small-print">
                I detta kvitto nämnd jägare är ansvarsförsäkrad från och med betalningsdagen till slutet av jaktåret.
                Försäkringen gäller i Norden, EU-länderna, Schweiz och Storbritannien på jaktresor som varar högst 60 dygn.
                Försäkringen täcker skador som orsakats någon annan med skjutvapen – dock inte sakskador. Den till
                försäkringen hörande jägarens privatolycksfallsförsäkring täcker skador som orsakats jägaren själv på grund
                av att vapnet brunnit av eller exploderat. Jägare som erlagt viltvårdsavgift och som på uppdrag av polisen i
                Finland utför eftersök av skadat djur omfattas av försäkringen liksom dennes hund som utför
                eftersöksuppdraget. Då skada skett kontakta utan dröjsmål LokalTapiola, Ansvarsskador och hundskador
                inträffade i SRVA-verksamhet tel. 09 453 4150 och Jägarens och funktionärens olycksfallsskador
                tel. 09 453 3666. Om du vill byta jaktvårdsförening, skall du meddela detta till jägarregistret med telefon,
                e-post eller per brev. I alla ärenden som berör jaktkortet och postning av Jägaren-tidningen betjänas Ni av:
            </p>

            <address>
                Jägarregistret<br/>
                PB 22<br/>
                00331 Helsingfors<br/>
                tel 029 431 2002 (vardagar november-juni: kl. 9-16 och juli-oktober: kl. 8-17)<br/>
                e-mail: metsastajarekisteri@riista.fi
            </address>
        </div>
    </div>

    <div class="footer" style="margin-top: 700pt">
        <%@include file="footer-sv.jsp"%>
    </div>
</div>
</c:if>

</body>
</html>
