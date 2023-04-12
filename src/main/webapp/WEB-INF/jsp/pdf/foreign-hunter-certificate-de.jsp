<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false"
         trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>Zertifikat</title>
    <base href="/static/hunter-certificate/">
    <link href="foreign-hunter-certificate.css" rel="stylesheet"/>
</head>
<body>

<%@include file="header-de.jsp" %>
<div class="container">
    <p style="padding-top:20pt;">
        Wir bestätigen hiermit, daß
        ${model.lastName}&nbsp;${model.firstName}
        (geb <joda:format value="${model.dateOfBirth}" pattern="dd.MM.yyyy"/>,
        Registernummer vom Jäger: ${model.hunterNumber}),
        die Finnische Jägerprüfung abgelegt hat und einen gültigen Jagdschein für die Saison
        <joda:format value="${model.huntingCardStart}" pattern="dd.MM.yyyy"/> &dash;
        <joda:format value="${model.huntingCardEnd}" pattern="dd.MM.yyyy"/> hat.
        (bez. <joda:format value="${model.paymentDate}" pattern="dd.MM.yyyy"/>)
    </p>

    <h1 style="padding: 5pt 0">Finnischer Jagdschein</h1>

    <div>
        <p>
            In Finnland muss jeder Jäger erfolgreich eine Jägerprüfung absolvieren, um einen Jagdschein zu erhalten.
            Die Jägerprüfung wird schriftlich abgenommen und umfasst 60 Fragen.
        </p>

        <p style="padding-top:20pt">
            Die Prüfung und der Vorbereitungskurs dazu ist in die folgenden acht Teilbereiche untergliedert:
        </p>

        <ul>
            <li><span>Jagdgesetzgebung und andere Vorschriften bei der Jagd in Finnland</span></li>
            <li><span>Erkennung von jagbarem Wild</span></li>
            <li><span>Wildökologie und -management</span></li>
            <li><span>Ethik und Nachhaltigkeit bei der Jagd</span></li>
            <li><span>Schusswaffen und Munition (einschließlich Jagdbögen und -pfeile)</span></li>
            <li><span>Sicherheitsmaßnahmen bei der Jagd</span></li>
            <li><span>Jagdausrüstungen und Jagdmethoden</span></li>
            <li><span>Umgang mit erlegtem Wild</span></li>
        </ul>

        <p style="margin-top: 20pt">
            Der Jagdschein umfaßt eine Jägerversicherung,
            die bei der Jagd durch eine Schußwaffe entstandene Personenschäden deckt.
            Die Versicherung ist ein Jahr gültig (<joda:format value="${model.huntingCardStart}" pattern="dd.MM.yyyy"/>
            &dash; <joda:format value="${model.huntingCardEnd}" pattern="dd.MM.yyyy"/>) in Finnland,
            Skandinavien, Schweiz, das Vereinigte Königreich Großbritannien und Nordirland und in den Mitgliedsländern
            der
            Europäischen Union.
        </p>

        <h1>Schießprüfung</h1>

        <p>
            Schalenwild- und Bärenjäger haben eine gebührenpflichtige Schießprüfung abzulegen, die von Hegeringen
            abgenommen
            wird. Die Schießprüfung für Schalenwildjäger umfaßt vier Schüsse auf die stehende Elchscheibe aus 75 Meter
            Entfernung. Alle vier Schüsse müssen im Treffbereich der Elchscheibe liegen (Durchmesser 23 cm). Die
            Schießprüfung
            für Bärenjäger umfaßt vier Schüsse auf die stehende Bärenscheibe aus 75 Meter Entfernung. Alle vier Schüsse
            müssen
            im Treffbereich der Bärenscheibe liegen (Durchmesser 17 cm). Für die bestandene Prüfung wird dem Schützen
            ein
            Zeugnis, die Schießprüfungskarte ausgehändigt, welche drei Jahre gültig ist.
        </p>

        <p style="padding-top: 20pt;">
            Hochachtungsvoll,<br/>
        </p>

        <p class="s1" style="padding-top: 8pt;">
            FINNISCHE WILDLIFE-AGENTUR
        </p>

        <h1 style="padding: 15pt 0 0;">
            Sauli Härkönen
        </h1>

        <h2 style="padding-top: 0">
            Direktor für öffentliche Verwaltungsaufgaben
        </h2>

        <p style="padding-top: 15pt; font-size:12pt;">
            SUOMEN RIISTAKESKUS<br/>
            SOMPIONTIE 1<br/>
            00730 HELSINKI<br/>
        </p>

    </div>
</div>
<div class="footer" style="margin-top: 200pt">
    <%@include file="footer-de.jsp" %>
</div>

<%@include file="header-de.jsp" %>
<div class="container">

    <h1 style="padding-top: 20pt;">
        Versicherungsschein
    </h1>

    <div style="font-size:10pt; line-height: 12pt">
        <div style="float:left;">
            Versicherer<br/>
            Versicherungsnehmer<br/>
            Laufzeit<br/>
            Versicherte Personen<br/>
            Räumlicher Geltungsbereich
        </div>

        <div style="padding-left:136pt">
            LocalTapiola General Mutual Insurance Company, Finnland<br/>
            Suomen riistakeskus (Finlands viltcentral), Finnish Wildlife Agency<br/>
            1. August <joda:format value="${model.huntingCardStart}" pattern="yyyy"/>
            bis 31. Juli <joda:format value="${model.huntingCardEnd}" pattern="yyyy"/><br/>
            Jäger mit gültigem Jagdschein<br/>
            Nordische Länder, EU-Mitgliedsstaaten, Schweiz und Groβbritannien für Jagdausflüge mit einer Dauer von
            biz
            zu 60 Tagen:<br/>
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt">
        <div style="float:left;">
            Versicherungsnummer<br/>
            Versicherungsart<br/>
            Haftungshöchstbetrag<br/>
            Hauptausschlüsse<br/>
        </div>

        <div style="padding-left:110pt;">
            312-0042105-H<br/>
            Haftpflichtversicherung für Jäger<br/>
            EUR 1,200,000 bei Personenschäden<br/>
            Sachschäden
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt">
        <div style="float:left;">
            Versicherungsnummer<br/>
            Versicherungsart<br/>
            Haftungshöchstbetrag<br/>
        </div>

        <div style="padding-left:110pt;">
            353-4344930-S<br/>
            Unfallversicherung für Offizielle<br/>
            EUR 10,000 bei Personenschäden<br/>
            EUR 50,000 bei dauernder Erwerbsunfähigkeit<br/>
            EUR 30,000 im Todesfall
        </div>
    </div>

    <div style="padding-top: 20pt; font-size:10pt; line-height: 12pt">
        <div style="float:left;">
            Räumlicher Geltungsbereich
        </div>

        <div style="padding-left:136pt">
            Nordische Länder, EU-Mitgliedsstaaten, Schweiz und Groβbritannien für Jagdausflüge mit einer Dauer von
            biz
            zu 60 Tagen:<br/>
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt">
        <div style="float:left;">
            Versicherungsnummer<br/>
            Versicherungsart<br/>
            Haftungshöchstbetrag
        </div>

        <div style="padding-left:110pt">
            353-4344930-S<br/>
            Unfallversicherung für Offizielle<br/>
            EUR 10,000 bei Personenschäden<br/>
            EUR 50,000 bei dauernder Erwerbsunfähigkeit<br/>
            EUR 30,000 im Todesfall
        </div>
    </div>

    <p style="padding-left: 136pt;padding-top:20pt;line-height: 10pt">
        LOCALTAPIOLA GENERAL MUTUAL INSURANCE COMPANY<br/>
        Groβkunden Service
    </p>

    <p style="padding-left: 136pt;font-style: italic;line-height: 10pt;margin-top: 20px">
        Dieser Versicherungsschein dient lediglich zur Information und überträgt keine Rechte auf
        den Versicherungsscheininhaber. Es handelt sich hierbei um einen Auszug aus dem Original
        des Versicherungsvertrags zwischen dem Versicherungsnehmer und der LocalTapiola
        General Mutual Insurance Company, der im Fall von Abweichungen zwischen dem Vertrag
        und dem Auszug Vorrang hat.
    </p>

</div>
<div class="footer" style="margin-top: 380pt">
    <%@include file="footer-de.jsp" %>
</div>

<%@include file="header-de.jsp" %>
<div class="container">

    <h1 style="padding-top: 20pt;padding-left: 21pt;">
        Übersetzung wichtiger Begriffe im finnischen Jagdschein und Schießprüfung (Originaltext auf finnisch und
        schwedisch):
    </h1>

    <h1 style="padding-left: 21pt;padding-top:20pt; padding-bottom: 15pt;">
        IN DER JAGD KARTE
    </h1>

    <table border="0" width="100%" style="margin-left: 20pt;">
        <th>
            <tr>
                <td width="33%" style="text-decoration: underline;">Auf finnisch:</td>
                <td width="33%" style="text-decoration: underline;">Auf schwedisch:</td>
                <td width="33%" style="text-decoration: underline;">Auf deutsch:</td>
            </tr>
        </th>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>Metsästyskortti</td>
            <td>Jaktkort</td>
            <td>Jagdschein</td>
        </tr>
        <tr>
            <td>Metsästäjän nimi</td>
            <td>Jägarens namn</td>
            <td>Name, Vorname</td>
        </tr>
        <tr>
            <td>Syntymäaika</td>
            <td>Födelsetid</td>
            <td>Geburtsdatum</td>
        </tr>
        <tr>
            <td>Yhdistys</td>
            <td>Jaktvårdsförening</td>
            <td>Jagdverein</td>
        </tr>
        <tr>
            <td>Yhdistysnumero</td>
            <td>Föreningsnummer</td>
            <td>Registernummer vom Verein</td>
        </tr>
        <tr>
            <td>Metsästäjänumero</td>
            <td>Jägarnummer</td>
            <td>Registernummer vom Jäger</td>
        </tr>
        <tr>
            <td>Jakeluosoite</td>
            <td>Utdelningsadress</td>
            <td>Anschrift</td>
        </tr>

        <tr>
            <td>Kotipaikka</td>
            <td>Hemort</td>
            <td>Wohnort</td>
        </tr>
        <tr>
            <td>Postinumero ja -toimipaikka</td>
            <td>Postnummer och -anstalt</td>
            <td>PLZ, Postort</td>
        </tr>
        <tr>
            <td>Maksettu</td>
            <td>Betalt</td>
            <td>Bezahlt</td>
        </tr>

    </table>

    <h1 style="padding-left: 20pt; padding-top:20pt; padding-bottom: 15pt;">
        ZERTIFIKAT DER BESTEHENDEN SCHIESSPRÜFUNG
    </h1>

    <table border="0" width="100%" style="margin-left: 20pt;">
        <th>
            <tr>
                <td width="33%" style="text-decoration: underline;">In Finnish:</td>
                <td width="33%" style="text-decoration: underline;">In Swedish:</td>
                <td width="33%" style="text-decoration: underline;">In English:</td>
            </tr>
        </th>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td width="33%">Ampumakoetodistus</td>
            <td width="33%">Intyg över avlagt skjutprov</td>
            <td width="33%">Schießprüfungskarte</td>
        </tr>
        <tr>
            <td>Nimi</td>
            <td>Namn</td>
            <td>Name, Vorname</td>
        </tr>
        <tr>
            <td>Metsästäjänumero</td>
            <td>Jägarnummer</td>
            <td>Registernummer vom Jäger</td>
        </tr>
        <tr>
            <td>Kansalaisuus</td>
            <td>Medborgarskap</td>
            <td>Staatsbürgerschaft</td>
        </tr>
        <tr>
            <td>...on suorittanut metsästyslain</td>
            <td>...har avlagt det i jaktlagen</td>
            <td>...hat ein Scießprüfung</td>
        </tr>
        <tr>
            <td>edellyttämän ampumakokeen</td>
            <td>nämnda skjutprovet för</td>
            <td>bestehen</td>
        </tr>
        <tr>
            <td>Paikka ja aika</td>
            <td>Plats och tid</td>
            <td>Ort und Zeit</td>
        </tr>
        <tr>
            <td>Suoritus on voimassa</td>
            <td>Provet gäller till</td>
            <td>Prüfung ist gültig bis</td>
        </tr>
        <tr>
            <td>__/__20__ saakka</td>
            <td>den __/__20__</td>
            <td>zum __/__20__</td>
        </tr>
        <tr>
            <td>Ampumakokeen vastaanottaja</td>
            <td>Examinator för skjutprov</td>
            <td>Schießprüfung Aufseher</td>
        </tr>
        <tr>
            <td>Leima</td>
            <td>Stämpel</td>
            <td>Briefmarke</td>
        </tr>
    </table>
</div>
<div class="footer" style="margin-top: 560pt">
    <%@include file="footer-de.jsp" %>
</div>
</body>
</html>
