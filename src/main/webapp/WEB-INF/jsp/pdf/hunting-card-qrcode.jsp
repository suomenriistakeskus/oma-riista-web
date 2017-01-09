<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${fn:length(model.qrCode) > 0}">
<div id="qrcode" style="position:absolute; top:155pt;right: 5pt;"></div>

<!-- Do not use minified qrcode, minified won't always work with non-ascii chars -->
<script type="text/javascript" src="/static/lib/qrcode.js"></script>
<script type="text/javascript">
    new QRCode(document.getElementById("qrcode"), {
        text: "${model.qrCode}",
        width: 165,
        height: 165,
        colorDark : "#000000",
        colorLight : "#ffffff",
        correctLevel : QRCode.CorrectLevel.H
    });
</script>
</c:if>
