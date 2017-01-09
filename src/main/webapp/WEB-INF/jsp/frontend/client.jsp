<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="true" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <meta name="description" content="'Oma riista' on Suomen riistakeskuksen helppokäyttöinen sähköisen asioinnin palvelu metsästäjille ja rhy toiminnanohjaajille.">
    <meta name="author" content="Suomen riistakeskus"/>
    <link rel="shortcut icon" href="/favicon.ico?v=1" />
    <title>'Oma riista' on Suomen riistakeskuksen helppokäyttöinen sähköisen asioinnin palvelu metsästäjille ja rhy toiminnanohjaajille.</title>
    <spring:eval var="rev" expression="@runtimeEnvironmentUtil.revision"/>
    <spring:eval var="environmentId" expression="@runtimeEnvironmentUtil.environmentId"/>
    <spring:eval var="isProductionEnvironment" expression="@runtimeEnvironmentUtil.productionEnvironment"/>
    <c:set var="sourcePrefix" value="${contextPath}/v/${rev}"/>
    <link rel="stylesheet" href="/v/${styleVersion}/css/app.css">
    <script>
        WebFontConfig = {
            google: { families: [ 'Open+Sans:400,700', 'Roboto+Slab:700' ] }
        };

        (function(d) {
            var wf = d.createElement('script'), s = d.scripts[0];
            wf.src = 'https://ajax.googleapis.com/ajax/libs/webfont/1.6.26/webfont.js';
            s.parentNode.insertBefore(wf, s);
        })(document);
        <%@ include file="/frontend/js/lib/angular-loader.min.js" %>
        <%@ include file="/frontend/js/lib/script.min.js" %>
        ;$script([
            '/v/${vendorAngularVersion}/js/vendor.angular.min.js',
            '/v/${vendorOtherVersion}/js/vendor.other.min.js',
            '/v/${appVersion}/js/app.min.js',
            '/v/${templatesVersion}/js/templates.js'
        ], function () {
            angular.module('app.metadata', [])
                .constant('environmentId', '${environmentId}')
                .constant('isProductionEnvironment', ${isProductionEnvironment})
                .constant('appRevision', '${rev}')
                .constant('versionUrlPrefix', '${sourcePrefix}');
            angular.bootstrap(document, ['app'], {
                strictDi: true
            });
        });
    </script>
</head>
<body>
<div class="hidden">'Oma riista' on Suomen riistakeskuksen helppokäyttöinen sähköisen asioinnin palvelu metsästäjille ja rhy toiminnanohjaajille.</div>
<noscript>
    <div class="no-js">
        <div class="well">
            <p>Selaimesi ei tue JavaScriptia tai olet poistanut JavaScript tuen käytöstä.</p>

            <p>Tämän sivuston käyttäminen vaatii JavaScript tuen. Jos nykyinen selaimesi ei tue JavaScriptia, lataa
                jokin alla olevista selaimista.</p>
        </div>

        <div class="well">
            <p>Din webbläsare stöder inte JavaScript eller stödet för JavaScript är avstängt.</p>

            <p>För användning av den här sidan krävs att JavaScript stöds. Om din nuvarande webbläsare inte stöder
                JavaScript, ladda ner i någon webbläsare nedan. </p>
        </div>

        <div class="well">
            <p>Your browser does not support JavaScript or you have disabled JavaScript support.</p>

            <p>This web-site requires JavaScript support. If your current browser doesn't support JavaScript please
                download some other browser from the links below.</p>
        </div>

        <ul class="list-unstyled well">
            <li><a href="http://www.google.com/chrome">Google Chrome</a></li>
            <li><a href="http://www.apple.com/safari/">Safari</a></li>
            <li><a href="http://www.mozilla.org/en-US/firefox/new/">Mozilla Firefox</a></li>
        </ul>
    </div>
</noscript>

<!--[if lt IE 11]>
<div class="alert alert-danger">
    <h4><span class="glyphicon glyphicon-warning-sign"> </span>Tämän sivuston käyttäminen ei onnistu käyttämälläsi selaimella, selaimesi on vanhentunut. Ole hyvä ja päivitä selaimesi, tai lataa jokin alla olevista selaimista.</h4>
    <ul class="list-unstyled">
        <li><a href="http://www.google.com/chrome">Google Chrome</a></li>
        <li><a href="http://www.apple.com/safari/">Safari</a></li>
        <li><a href="http://www.mozilla.org/en-US/firefox/new/">Mozilla Firefox</a></li>
    </ul>
</div>
<![endif]-->

<off-canvas-container></off-canvas-container>

<site-nav></site-nav>

<div class="wrapper" ng-cloak>
    <span id="scrollToTop"></span>

    <div class="site-background"></div>

    <div class="main-content" ui-view autoscroll="false"></div>
</div>

<footer ng-cloak class="ng-cloak"
        riista-footer-css
        ng-include="'layout/footer.html'">
</footer>

<c:if test="${isProductionEnvironment}">
<!-- BEGIN GA -->
<script>
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    ga('create', 'UA-51418132-1', 'riista.fi');
    ga('send', 'pageview');

</script>
<!-- END GA -->

<!-- BEGIN Snoobi v1.4 -->
<script type="text/javascript" src="https://eu1.snoobi.com/snoop.php?tili=riista_fi"></script>
<!-- END Snoobi v1.4 -->
</c:if>

</body>
</html>
