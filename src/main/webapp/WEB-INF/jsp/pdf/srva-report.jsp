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
    <title><fmt:message key="SrvaReport.eventTitle"/></title>
    <base href="/static/srva-report/">
    <link href="style.css" rel="stylesheet"/>
</head>
<body>

<div>
    <%@include file="srva-report-header.jsp" %>
</div>

<div class="container">
    <div class="animal-image">
        <c:choose>
            <c:when test="${model.species != null}">
                <img src="../elainlajikuvat/96x96/${model.species.code}.jpg" alt="animal-photo"/>
                <h2 class="animal-title">
                    <c:choose>
                        <c:when test="${model.lang == 'fi'}">
                            <c:out value="${model.species.name.fi}"/>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${model.species.name.sv}"/>
                        </c:otherwise>
                    </c:choose>
                </h2>
            </c:when>
            <c:otherwise>
                <h2 class="animal-title">
                    <fmt:message key="SrvaReport.otherAnimal"/> - <c:out value="${model.otherSpeciesDescription}"/>
                </h2>
            </c:otherwise>
        </c:choose>
    </div>
    <hr/>

    <div class="col-1">
        <table>
            <tbody>
            <tr>
                <td><fmt:message key="SrvaReport.time"/></td>
                <td><joda:format value="${model.pointOfTime}" pattern="d.M.YYYY"/>&nbsp;
                    <joda:format value="${model.pointOfTime}" pattern="HH.mm"/></td>
            </tr>
            <tr>
                <td><fmt:message key="SrvaReport.result"/></td>
                <c:choose>
                    <c:when test="${model.eventResult == 'ANIMAL_FOUND_DEAD'}">
                        <td><fmt:message key="SrvaResultEnum.ANIMAL_FOUND_DEAD"/></td>
                    </c:when>
                    <c:when test="${model.eventResult == 'ANIMAL_FOUND_AND_TERMINATED'}">
                        <td><fmt:message key="SrvaResultEnum.ANIMAL_FOUND_AND_TERMINATED"/></td>
                    </c:when>
                    <c:when test="${model.eventResult == 'ANIMAL_FOUND_AND_NOT_TERMINATED'}">
                        <td><fmt:message key="SrvaResultEnum.ANIMAL_FOUND_AND_NOT_TERMINATED"/></td>
                    </c:when>
                    <c:when test="${model.eventResult == 'ACCIDENT_SITE_NOT_FOUND'}">
                        <td><fmt:message key="SrvaResultEnum.ACCIDENT_SITE_NOT_FOUND"/></td>
                    </c:when>
                    <c:when test="${model.eventResult == 'ANIMAL_TERMINATED'}">
                        <td><fmt:message key="SrvaResultEnum.ANIMAL_TERMINATED"/></td>
                    </c:when>
                    <c:when test="${model.eventResult == 'ANIMAL_DEPORTED'}">
                        <td><fmt:message key="SrvaResultEnum.ANIMAL_DEPORTED"/></td>
                    </c:when>
                    <c:when test="${model.eventResult == 'ANIMAL_NOT_FOUND'}">
                        <td><fmt:message key="SrvaResultEnum.ANIMAL_NOT_FOUND"/></td>
                    </c:when>
                    <c:when test="${model.eventResult == 'UNDUE_ALARM'}">
                        <td><fmt:message key="SrvaResultEnum.UNDUE_ALARM"/></td>
                    </c:when>
                </c:choose>
            </tr>
            <c:if test="${model.eventResultDetail != null}">
                <tr>
                    <td><fmt:message key="SrvaReport.resultDetail"/></td>
                    <c:choose>
                        <c:when test="${model.eventResultDetail == 'ANIMAL_CONTACTED_AND_DEPORTED'}">
                            <td><fmt:message key="SrvaEventResultDetailsEnum.ANIMAL_CONTACTED_AND_DEPORTED"/></td>
                        </c:when>
                        <c:when test="${model.eventResultDetail == 'ANIMAL_CONTACTED'}">
                            <td><fmt:message key="SrvaEventResultDetailsEnum.ANIMAL_CONTACTED"/></td>
                        </c:when>
                        <c:when test="${model.eventResultDetail == 'UNCERTAIN_RESULT'}">
                            <td><fmt:message key="SrvaEventResultDetailsEnum.UNCERTAIN_RESULT"/></td>
                        </c:when>
                    </c:choose>
                </tr>
            </c:if>
            <tr>
                <td><fmt:message key="SrvaReport.author"/></td>
                <td><c:out value="${model.author.byName}"/>&nbsp;<c:out value="${model.author.lastName}"/></td>
            </tr>
            <tr>
                <td><fmt:message key="SrvaReport.approver"/></td>
                <c:if test="${model.approver != null}">
                    <td><c:out value="${model.approver.byName}"/>&nbsp;<c:out value="${model.approver.lastName}"/></td>
                </c:if>
            </tr>
            <tr>
                <td><fmt:message key="SrvaReport.eventName"/></td>
                <c:choose>
                    <c:when test="${model.eventName == 'ACCIDENT'}">
                        <td><fmt:message key="SrvaEventNameEnum.ACCIDENT"/></td>
                    </c:when>
                    <c:when test="${model.eventName == 'DEPORTATION'}">
                        <td><fmt:message key="SrvaEventNameEnum.DEPORTATION"/></td>
                    </c:when>
                    <c:when test="${model.eventName == 'INJURED_ANIMAL'}">
                        <td><fmt:message key="SrvaEventNameEnum.INJURED_ANIMAL"/></td>
                    </c:when>
                </c:choose>
            </tr>
            <c:if test="${model.deportationOrderNumber != null}">
                <tr>
                    <td><fmt:message key="SrvaReport.deportationOrderNumber"/></td>
                    <td><c:out value="${model.deportationOrderNumber}"/></td>
                </tr>
            </c:if>
            <tr>
                <td><fmt:message key="SrvaReport.eventType"/></td>
                <c:choose>
                    <c:when test="${model.eventType == 'TRAFFIC_ACCIDENT'}">
                        <td><fmt:message key="SrvaEventTypeEnum.TRAFFIC_ACCIDENT"/></td>
                    </c:when>
                    <c:when test="${model.eventType == 'RAILWAY_ACCIDENT'}">
                        <td><fmt:message key="SrvaEventTypeEnum.RAILWAY_ACCIDENT"/></td>
                    </c:when>
                    <c:when test="${model.eventType == 'ANIMAL_NEAR_HOUSES_AREA'}">
                        <td><fmt:message key="SrvaEventTypeEnum.ANIMAL_NEAR_HOUSES_AREA"/></td>
                    </c:when>
                    <c:when test="${model.eventType == 'ANIMAL_AT_FOOD_DESTINATION'}">
                        <td><fmt:message key="SrvaEventTypeEnum.ANIMAL_AT_FOOD_DESTINATION"/></td>
                    </c:when>
                    <c:when test="${model.eventType == 'INJURED_ANIMAL'}">
                        <td><fmt:message key="SrvaEventTypeEnum.INJURED_ANIMAL"/></td>
                    </c:when>
                    <c:when test="${model.eventType == 'ANIMAL_ON_ICE'}">
                        <td><fmt:message key="SrvaEventTypeEnum.ANIMAL_ON_ICE"/></td>
                    </c:when>
                    <c:when test="${model.eventType == 'OTHER'}">
                        <td>
                            <fmt:message key="SrvaEventTypeEnum.OTHER"/>
                            <c:if test="${model.otherTypeDescription != null}">
                                <br>
                                <span class="other-description"><c:out value="${model.otherTypeDescription}"/></span>
                            </c:if>
                        </td>
                    </c:when>
                </c:choose>
            </tr>
            <c:if test="${model.eventTypeDetail != null}">
                <tr>
                    <td><fmt:message key="SrvaReport.eventTypeDetail"/></td>
                    <c:choose>
                        <c:when test="${model.eventTypeDetail == 'CARED_HOUSE_AREA'}">
                            <td><fmt:message key="SrvaEventTypeDetailsEnum.CARED_HOUSE_AREA"/></td>
                        </c:when>
                        <c:when test="${model.eventTypeDetail == 'FARM_ANIMAL_BUILDING'}">
                            <td><fmt:message key="SrvaEventTypeDetailsEnum.FARM_ANIMAL_BUILDING"/></td>
                        </c:when>
                        <c:when test="${model.eventTypeDetail == 'URBAN_AREA'}">
                            <td><fmt:message key="SrvaEventTypeDetailsEnum.URBAN_AREA"/></td>
                        </c:when>
                        <c:when test="${model.eventTypeDetail == 'CARCASS_AT_FOREST'}">
                            <td><fmt:message key="SrvaEventTypeDetailsEnum.CARCASS_AT_FOREST"/></td>
                        </c:when>
                        <c:when test="${model.eventTypeDetail == 'CARCASS_NEAR_HOUSES_AREA'}">
                            <td><fmt:message key="SrvaEventTypeDetailsEnum.CARCASS_NEAR_HOUSES_AREA"/></td>
                        </c:when>
                        <c:when test="${model.eventTypeDetail == 'GARBAGE_CAN'}">
                            <td><fmt:message key="SrvaEventTypeDetailsEnum.GARBAGE_CAN"/></td>
                        </c:when>
                        <c:when test="${model.eventTypeDetail == 'BEEHIVE'}">
                            <td><fmt:message key="SrvaEventTypeDetailsEnum.BEEHIVE"/></td>
                        </c:when>
                        <c:when test="${model.eventTypeDetail == 'OTHER'}">
                            <td>
                                <fmt:message key="SrvaEventTypeDetailsEnum.OTHER"/>
                                <c:if test="${model.otherTypeDetailDescription != null}">
                                    <br>
                                    <span class="other-description"><c:out value="${model.otherTypeDetailDescription}"/></span>
                                </c:if>
                            </td>
                        </c:when>
                    </c:choose>
                </tr>
            </c:if>
            <c:choose>
                <c:when test="${model.methods == null || model.methods.size() == 0}">
                    <tr>
                        <td><fmt:message key="SrvaReport.method"/></td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="method" items="${model.methods}" varStatus="s">
                        <tr>
                            <td>
                                <c:if test="${s.index == 0}">
                                <fmt:message key="SrvaReport.method"/>
                                </c:if>
                            </td>
                            <c:choose>
                                <c:when test="${method.name == 'DOG'}">
                                    <td><fmt:message key="SrvaMethodEnum.DOG"/></td>
                                </c:when>
                                <c:when test="${method.name == 'PAIN_EQUIPMENT'}">
                                    <td><fmt:message key="SrvaMethodEnum.PAIN_EQUIPMENT"/></td>
                                </c:when>
                                <c:when test="${method.name == 'SOUND_EQUIPMENT'}">
                                    <td><fmt:message key="SrvaMethodEnum.SOUND_EQUIPMENT"/></td>
                                </c:when>
                                <c:when test="${method.name == 'VEHICLE'}">
                                    <td><fmt:message key="SrvaMethodEnum.VEHICLE"/></td>
                                </c:when>
                                <c:when test="${method.name == 'CHASING_WITH_PEOPLE'}">
                                    <td><fmt:message key="SrvaMethodEnum.CHASING_WITH_PEOPLE"/></td>
                                </c:when>
                                <c:when test="${method.name == 'TRACED_WITH_DOG'}">
                                    <td><fmt:message key="SrvaMethodEnum.TRACED_WITH_DOG"/></td>
                                </c:when>
                                <c:when test="${method.name == 'TRACED_WITHOUT_DOG'}">
                                    <td><fmt:message key="SrvaMethodEnum.TRACED_WITHOUT_DOG"/></td>
                                </c:when>
                                <c:when test="${method.name == 'OTHER'}">
                                    <td>
                                        <fmt:message key="SrvaMethodEnum.OTHER"/>
                                        <c:if test="${model.otherMethodDescription != null}">
                                            <br>
                                            <span class="other-description"><c:out value="${model.otherMethodDescription}"/></span>
                                        </c:if>
                                    </td>
                                </c:when>
                            </c:choose>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            <tr>
                <td><fmt:message key="SrvaReport.personCount"/></td>
                <td><c:out value="${model.personCount}"/></td>
            </tr>
            <tr>
                <td><fmt:message key="SrvaReport.timeSpent"/></td>
                <td><c:out value="${model.timeSpent}"/></td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="col-2">
        <table>
            <tbody>
                <c:forEach var="specimen" items="${model.specimens}" varStatus="s">
                    <tr>
                    <td>
                        <b><fmt:message key="gender"/>: </b>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${specimen.gender == 'FEMALE'}">
                                <fmt:message key="GameGender.FEMALE"/>
                            </c:when>
                            <c:when test="${specimen.gender == 'MALE'}">
                                <fmt:message key="GameGender.MALE"/>
                            </c:when>
                            <c:when test="${specimen.gender == 'UNKNOWN'}">
                                <fmt:message key="GameGender.UNKNOWN"/>
                            </c:when>
                        </c:choose>
                    </td>
                    <td>
                        <b><fmt:message key="age"/>: </b>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${specimen.age == 'ADULT'}">
                                <fmt:message key="GameAge.ADULT"/>
                            </c:when>
                            <c:when test="${specimen.age == 'YOUNG'}">
                                <fmt:message key="GameAge.YOUNG"/>
                            </c:when>
                            <c:when test="${specimen.age == 'UNKNOWN'}">
                                <fmt:message key="GameAge.UNKNOWN"/>
                            </c:when>
                        </c:choose>
                    </td>
                    </tr>
                </c:forEach>
           </tbody>
        </table>
    </div>
</div>

<div class="additional-info">
    <div class="title"><b><fmt:message key="additionalInfo"/></b></div>
    <div><c:out value="${model.description}"/></div>
</div>

<p class="page-break">

<div>
    <%@include file="srva-report-header.jsp" %>
</div>

<div class="container">
    <div>
        <h4 class="location-title"><fmt:message key="SrvaReport.location"/>:&nbsp;
            <b>P:</b>&nbsp;<c:out value="${model.geoLocation.latitude}"/>&nbsp;
            <b>I:</b>&nbsp;<c:out value="${model.geoLocation.longitude}"/>
        </h4>
    </div>

    <div class="map-container">
        <img class="event" alt="event-map" src="data:image/jpeg;base64,${model.map64Encoded}" />
        <img class="finland" alt="finland-map" src="data:image/jpeg;base64,${model.mapFinland64Encoded}" />
        <div class="copyright">
            Maastokartta <joda:format value="${model.reportDate}" pattern="MM/YYYY"/> &copy; Maanmittauslaitos
        </div>
    </div>
</div>

<c:forEach var="imageURL" items="${model.imageURLs}" varStatus="s">
    <p class="page-break">

    <div>
        <%@include file="srva-report-header.jsp" %>
    </div>

    <div class="attachment">
            <h4 class="title"><fmt:message key="SrvaReport.attachment"/>&nbsp;<c:out value="${s.index + 1}"/></h4>
            <img class="image" src="${imageURL}" alt="attachment-image"/>
    </div>
</c:forEach>

</body>
</html>
