<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>JAKTKORT</title>
    <base href="/static/foreign-hunter-certificate/">

    <link href="style.css" rel="stylesheet"/>

    <style type="text/css">
        h1 {
            font-weight: bold;
            text-decoration: none;
            font-size: 20pt;
            line-height: 26pt;
            padding: 5pt 0;
        }

        h1 small {
            font-size: 16px;
            line-height: 18px;
            font-weight: normal;
        }

        h2 {
            font-weight: bold;
            text-decoration: none;
            font-size: 11pt;
            padding-top: 0;
            padding-bottom: 5pt;
        }

        address {
            font-size: 10pt;
            line-height: 13pt;
        }

        td.occupation-name {
            padding: 5pt 0 0;
            min-width: 350pt;
        }

        dl {
            width:100%;
            overflow:hidden;
            font-size: 10pt;
            line-height: 12pt;
        }

        dt {
            float:left;
            width:35%; /* adjust the width; make sure the total of both is 100% */
            padding-top:7pt;
        }

        dd {
            float:left;
            width:65%; /* adjust the width; make sure the total of both is 100% */
            padding-top:7pt;
            font-weight: bold;
        }

        .small-print {
            font-size: 9pt;
            line-height: 11pt;
        }

        .small-print strong {
            line-height: 11pt;
        }

    </style>
</head>
<body>

<div class="container">
    <div class="header-logo">
        <img width="280" alt="image" src="01.jpg"/>
    </div>

    <%@include file="hunting-card-qrcode.jsp"%>

    <div class="header-text">
        UTSKRIFT FRÅN<br/>
        OMA RIISTA -TJÄNSTEN<br/>
        <joda:format value="${model.currentDate}" pattern="d.M.YYYY" />
    </div>

    <h1>
        <small>INTYG ÖVER BETALD JAKTVÅRDSAVGIFT</small><br/>
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

    <c:if test="${fn:length(model.occupations) > 0}">
        <h2>UPPGIFTER INOM JAKTVÅRDSFÖRENINGEN:</h2>

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

    <h2 style="padding-top: 0;">OBS!</h2>

    <div>
        <p class="small-print">
            I detta kvitto nämnd jägare är ansvarsförsäkrad från och med betalningsdagen till slutet av kvittots
            giltighetstid. Försäkringen täcker skador som orsakats någon annan med skjutvapen – dock inte sakskador.
            Den till försäkringen hörande jägarens privatolycksfallsförsäkring täcker skador som orsakats jägaren
            själv på grund av att vapnet brunnit av eller exploderat. Försäkringen är i kraft för dessa delar också
            i de nordiska länderna och i EU-länder. Jägare som erlagt viltvårdsavgift och som på uppdrag av polisen
            i Finland utför eftersök av skadat djur omfattas av försäkringen liksom dennes hund som utför
            eftersöksuppdraget. Då skada skett kontakta utan dröjsmål LokalTapiola, tel. 010 19 5105
        </p>

        <p class="small-print">
            Om du vill byta jaktvårdsförening, skall du meddela detta till jägarregistret med e-post, fax eller per brev.
        </p>

        <p class="small-print">
            I alla ärenden som berör jaktkortet och postning av Jägaren-tidningen betjänas Ni av:
        </p>

        <address>
            Jägarregistret<br/>
            PB 22<br/>
            00331 Helsingfors<br/>
            tel 029 431 2002 (vardagar 8-18)<br/>
            e-mail: metsastajarekisteri@innofactor.com
        </address>
    </div>

    <div class="footer" style="margin-top: 300pt">
        <%@include file="footer-sv.jsp"%>
    </div>
</div>
</body>
</html>
