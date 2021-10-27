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
    <title>Hunting Card</title>
    <base href="/static/hunter-certificate/">
    <link href="certificate.css" rel="stylesheet"/>
    <link href="hunting-card.css" rel="stylesheet"/>
</head>
<body>

<div class="container">
    <div class="header-logo">
        <img width="280" alt="image" src="01.jpg"/>
    </div>

    <%@include file="hunting-card-qrcode.jsp"%>

    <div class="header-text">
        TULOSTETTU<br/>
        OMA RIISTA -PALVELUSTA<br/>
        <joda:format value="${model.currentDate}" pattern="d.M.YYYY" />
    </div>

    <p>TODISTUS RIISTANHOITOMAKSUN SUORITTAMISESTA</p>

    <h1>
        METSÄSTYSKORTTI<br/>
        <joda:format value="${model.huntingCardStart}" pattern="dd.MM.YYYY" />
        &dash;
        <joda:format value="${model.huntingCardEnd}" pattern="dd.MM.YYYY" />
    </h1>

    <dl>
        <dt>
            Metsästäjän nimi:
        </dt>
        <dd>
           ${model.firstName}&nbsp;${model.lastName}
        </dd>

        <dt>
            Jakeluosoite:
        </dt>
        <dd>
            ${model.streetAddress}<br/>
            ${model.postalCode}&nbsp;${model.postOffice}
        </dd>

        <dt>
            Kotipaikka:
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
                    <strong class="indent-right">Ei tiedossa</strong>
                </c:otherwise>
            </c:choose>
        </dd>

        <dt>
            Maksupäivä:
        </dt>
        <dd>
            <c:choose>
                <c:when test="${model.paymentDate != null}">
                    <joda:format value="${model.paymentDate}" pattern="dd.MM.YYYY" />
                </c:when>
                <c:otherwise>
                    <span style="color:red">EI MAKSETTU</span>
                </c:otherwise>
            </c:choose>
        </dd>

        <dt>
            Metsästäjänumero:
        </dt>
        <dd>
            ${model.hunterNumber}
        </dd>

        <dt>
            Syntymäaika:
        </dt>
        <dd>
            <joda:format value="${model.dateOfBirth}" pattern="dd.MM.YYYY" />
        </dd>

        <dt>
            Riistanhoitoyhdistys ja nro:
        </dt>
        <dd>
            <c:choose>
                <c:when test="${model.rhyName != null}">
                    ${model.rhyName}
                    <c:if test="${model.rhyOfficialCode != null}">&nbsp;(${model.rhyOfficialCode})</c:if>
                </c:when>
                <c:otherwise>
                    Ei RHY jäsenyyttä
                </c:otherwise>
            </c:choose>
        </dd>
    </dl>

    <hr/>

    <c:if test="${fn:length(model.occupations) > 0}">
        <h2>TEHTÄVÄT RIISTANHOITOYHDISTYKSESSÄ:</h2>

        <table width="100%">
            <tbody>
            <c:forEach items="${model.occupations}" var="o">
                <tr>
                    <td class="occupation-name">
                        <strong>${o.occupationName}</strong><br/>
                        ${o.organisationName}&nbsp;(${o.organisationOfficialCode})
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${o.beginDate == null && o.endDate == null}">
                                Voimassa
                            </c:when>
                            <c:when test="${o.endDate == null}">
                                Voimassa
                            </c:when>
                            <c:when test="${o.beginDate == null}">
                                <joda:format value="${o.endDate}" pattern="d.M.YYYY" /> asti
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
        <h2>AMPUMAKOKEET:</h2>

        <table width="100%">
            <tbody>
            <c:forEach items="${model.shootingTests}" var="t">
                <tr>
                    <td class="shooting-test-entry">
                        <div>${t.typeName}</div>
                        <div>${t.rhyName}&nbsp;(${t.rhyCode})</div>
                    </td>
                    <td>
                        Voimassa
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

    <h2 style="padding-top: 0;">HUOM!</h2>

    <div>
        <p class="small-print">
            Tässä kuitissa mainittu metsästäjä on vastuuvakuutettu maksupäivästä lukien kuitin voimassaolon loppuun.
            Vakuutus kattaa ampuma-aseella toiselle aiheutetut vahingot - ei kuitenkaan esinevahinkoja.
            Vakuutukseen liittyvä metsästäjän yksityistapaturmavakuutus kattaa aseen laukeamisesta tai räjähtämisestä
            metsästäjälle itselleen aiheutuneet vahingot. Vakuutus on näiltä osin voimassa myös Pohjoismaissa, Sveitsissä ja
            EU-maissa. Vahingoittunutta eläintä poliisin toimeksiannosta Suomessa jäljittävä riistanhoitomaksun
            maksanut metsästäjä kuuluu vakuutuksen piiriin samoin kuin hänen jäljitystehtävää suorittava koiransa.
            Vahingon tapahduttua ottakaa viipymättä yhteys LähiTapiolaan, puh. 010 19 5105
        </p>

        <p class="small-print">
            Jos haluatte vaihtaa riistanhoitoyhdistystä, on siitä ilmoitettava metsästäjärekisteriin sähköpostilla tai postitse.
        </p>

        <p class="small-print">
            Kaikissa metsästyskorttiin ja Metsästäjä-lehden postitukseen liittyvissä asioissa Teitä palvelee:
        </p>

        <address>
            Metsästäjärekisteri<br/>
            PL 22<br/>
            00331 Helsinki<br/>
            puh 029 431 2002 (arkisin marras-kesäkuu: klo 9-16 ja heinä-lokakuu: klo 8-18)<br/>
            e-mail: metsastajarekisteri@riista.fi
        </address>
    </div>

    <div class="footer" style="margin-top: 330pt">
        <%@include file="footer-fi.jsp"%>
    </div>
</div>
</body>
</html>
