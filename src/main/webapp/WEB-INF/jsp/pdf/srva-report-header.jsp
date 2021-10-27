<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false" trimDirectiveWhitespaces="true" %>

<div class="header">
    <div class="col-1">
        <h1><fmt:message key="SrvaReport.eventTitle"/></h1>
        <h3><c:out value="${model.rhy}"/></h3>
    </div>

    <div class="col-2">
        <div>
            <h4>
                <joda:format value="${model.reportDate}" pattern="d.M.YYYY"/>
            </h4>
        </div>
        <div>
            <h4>
                <c:if test="${model.isModerator}">
                    <fmt:message key="SrvaReport.moderator"/>
                </c:if>
            </h4>
        </div>
        <div>
            <h4>
                <c:if test="${model.activeUser != null}">
                    <c:out value="${model.activeUser}"/>
                </c:if>
            </h4>
        </div>
    </div>
</div>
