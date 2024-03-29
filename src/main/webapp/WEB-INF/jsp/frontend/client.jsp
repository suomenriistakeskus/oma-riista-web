<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1, maximum-scale=1">
    <meta name="description" content="'Oma riista' on Suomen riistakeskuksen helppokäyttöinen sähköisen asioinnin palvelu metsästäjille ja rhy toiminnanohjaajille.">
    <meta name="author" content="Suomen riistakeskus"/>
    <meta name="google" content="notranslate">
    <link rel="apple-touch-icon" sizes="180x180" href="/static/apple-touch-icon.png?v=2">
    <link rel="icon" type="image/png" sizes="32x32" href="/static/favicon-32x32.png?v=2">
    <link rel="icon" type="image/png" sizes="16x16" href="/static/favicon-16x16.png?v=2">
    <link rel="manifest" href="/static/site.webmanifest?v=2">
    <link rel="mask-icon" href="/static/safari-pinned-tab.svg?v=2" color="#00a300">
    <link rel="shortcut icon" href="/static/favicon.ico?v=2">
    <meta name="apple-mobile-web-app-title" content="Oma riista">
    <meta name="application-name" content="Oma riista">
    <meta name="msapplication-TileColor" content="#00a300">
    <meta name="msapplication-config" content="/static/browserconfig.xml?v=2">
    <meta name="theme-color" content="#00a300">
    <title>'Oma riista' on Suomen riistakeskuksen helppokäyttöinen sähköisen asioinnin palvelu metsästäjille ja rhy toiminnanohjaajille.</title>
    <spring:eval var="rev" expression="@runtimeEnvironmentUtil.revision"/>
    <spring:eval var="commitId" expression="@runtimeEnvironmentUtil.commitId"/>
    <spring:eval var="environmentId" expression="@runtimeEnvironmentUtil.environmentId"/>
    <spring:eval var="isProductionEnvironment" expression="@runtimeEnvironmentUtil.productionEnvironment"/>
    <spring:eval var="mmlOpenAPIKey" expression="@runtimeEnvironmentUtil.mmlOpenAPIKey"/>
    <spring:eval var="ddClientToken" expression="@runtimeEnvironmentUtil.ddClientToken"/>
    <c:set var="sourcePrefix" value="${contextPath}/v/${rev}"/>

    <script type="text/javascript" src="https://www.datadoghq-browser-agent.com/datadog-logs-v4.js"></script>
    <script>
        window.DD_LOGS && DD_LOGS.init({
            clientToken: '${ddClientToken}',
            site: 'datadoghq.eu',
            service: 'oma-riista-web',
            env: '${environmentId}',
            version: '${commitId}',
            forwardErrorsToLogs: true,
            sampleRate: 100,
            beforeSend: (log) => {
                // Copy stack from _customDataHolder
                if (log._customDataHolder && !log.error.stack) {
                    log.error['stack'] = log._customDataHolder.stack;
                    delete log._customDataHolder;
                }
                // If exception is from angular, it should have been already logged, we can skip console log.
                if (log.error && log.error.origin === 'console' && log.error.stack && _.isString(log.error.stack) && log.error.stack.indexOf('at angular.') !== -1) {
                    return false;
                }
            }
        });
    </script>
    <script>
        <%@ include file="/frontend/js/lib/angular-loader.min.js" %>
        <%@ include file="/frontend/js/lib/loadjs.min.js" %>
        loadjs([
            'css!/v/${styleVersion}/css/app.css',
            '/v/${vendorOtherVersion}/js/vendor.other.min.js',
            '/v/${vendorAngularVersion}/js/vendor.angular.min.js',
            '/v/${templatesVersion}/js/templates.js',
            '/v/${appVersion}/js/app.min.js'
        ], 'app', {
            numRetries: 1
        });
        loadjs.ready(['app'], {
            success: function () {
                Dropzone.autoDiscover = false;

                angular.module('app.metadata', [])
                    .constant('environmentId', '${environmentId}')
                    .constant('isProductionEnvironment', ${isProductionEnvironment})
                    .constant('appRevision', '${rev}')
                    .constant('versionUrlPrefix', '${sourcePrefix}')
                    .constant('mmlOpenAPIKey', '${mmlOpenAPIKey}');
                angular.bootstrap(document, ['app'], {
                    strictDi: true
                });
            },
            error: function(depsNotFound) {
                if (window.DD_LOGS) {
                    window.DD_LOGS.logger.error("depsNotFound: " + depsNotFound);
                }

                Dropzone.autoDiscover = false;
                angular.module('app.metadata', [])
                    .constant('environmentId', '${environmentId}')
                    .constant('isProductionEnvironment', ${isProductionEnvironment})
                    .constant('appRevision', '${rev}')
                    .constant('versionUrlPrefix', '${sourcePrefix}')
                    .constant('mmlOpenAPIKey', '${mmlOpenAPIKey}');
                angular.bootstrap(document, ['app'], { strictDi: true });
            }
        });
    </script>
</head>
<body class="site-background">
<div style="display: none">
    'Oma riista' on Suomen riistakeskuksen helppokäyttöinen sähköisen asioinnin palvelu metsästäjille ja rhy
    toiminnanohjaajille.
</div>
<div style="display: none" ng-controller="IdleController"></div>
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

<div class="wrapper">
    <div class="main-content" ui-view autoscroll="false"></div>
</div>
<div riista-spacing-when-footer-hidden-css class="footer-spacing"></div>
<footer riista-footer-css
        ng-include="'layout/footer.html'">
</footer>

</body>
</html>
