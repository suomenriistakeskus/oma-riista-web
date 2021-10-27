<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>Zertifikat</title>
    <base href="/static/hunter-certificate/">
    <link href="certificate.css" rel="stylesheet"/>
</head>
<body>

<div class="container">
    <div class="header-logo">
        <img width="280" alt="image" src="01.jpg"/>
        <div style="position: absolute; right: 130pt; top: 10pt">
            <h1><joda:format value="${model.currentDate}" pattern="dd.MM.YYYY" /></h1>
            <h2>Zertifikat</h2>
        </div>
    </div>

    <p style="padding-top:50pt">
        Wir bestätigen hiermit, daß
        <strong>${model.lastName}&nbsp;${model.firstName}</strong>
        (geb <strong> <joda:format value="${model.dateOfBirth}" pattern="dd.MM.yyyy" /> </strong>,
         Registernummer vom Jäger: <strong>${model.hunterNumber}</strong>),
        die Finnische Jägerprüfung abgelegt hat und einen gültigen Jagdschein für die Saison
        <strong><joda:format value="${model.huntingCardStart}" pattern="dd.MM.yyyy" /></strong> &dash;
        <strong><joda:format value="${model.huntingCardEnd}" pattern="dd.MM.yyyy" /></strong> hat.
        (bez. <strong><joda:format value="${model.paymentDate}" pattern="dd.MM.yyyy" /></strong>)
    </p>

    <h1 style="padding: 5pt 0">Finnischer Jagdschein</h1>

    <div style="padding-left: 70pt">
        <p>
            In Finnland muss jeder Jäger erfolgreich eine Jägerprüfung absolvieren, um einen Jagdschein zu erhalten.
            Die Jägerprüfung wird schriftlich abgenommen und umfasst 60 Fragen.
        </p>

        <p>
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
    </div>

    <p style="margin-top: 10pt">
        Der Jagdschein umfaßt eine <strong>Jägerversicherung</strong>,
        die bei der Jagd durch eine Schußwaffe ent- standene Personenschäden deckt.
        Die Versicherung ist ein Jahr gültig (<joda:format value="${model.huntingCardStart}" pattern="dd.MM.yyyy" />
        &dash; <joda:format value="${model.huntingCardEnd}" pattern="dd.MM.yyyy" />) in Finnland,
        Skandinavien und in den Mitgliedsländern der Europäischen Union.
    </p>

    <h1 style="padding-top: 0">Schießprüfung</h1>

    <div style="padding-left: 10pt">
        <p>
            Schalenwild- und Bärenjäger haben eine gebührenpflichtige Schießprüfung abzulegen,
            die von Hegeringen abgenommen wird. Die Schießprüfung für Schalenwildjäger umfaßt
            vier Schüsse auf die stehende Elchscheibe aus 75 Meter Entfernung.
            Alle vier Schüsse müssen im Treffbereich der Elchscheibe liegen (Durchmesser 23 cm).
            Die Schießprüfung für Bärenjäger umfaßt vier Schüsse auf die stehende Bärenscheibe
            aus 75 Meter Entfernung. Alle vier Schüsse müssen im Treffbereich der Bärenscheibe
            liegen (Durch- messer 17 cm). Für die bestandene Prüfung wird dem Schützen ein Zeugnis,
            die Schieß- prüfungskarte ausgehändigt, welche drei Jahre gültig ist.
        </p>
        <p style="padding-top: 5pt; padding-bottom: 10pt;">
            Hochachtungsvoll<br/>
        </p>

        <p style="padding-bottom: 5pt;">
            SUOMEN RIISTAKESKUS
        </p>

        <h1 style="padding-top: 10pt">
            Sauli Härkönen
        </h1>

        <h2 style="padding-top: 0">Director for Public Administration Tasks</h2>

        <p style="padding-top: 10pt; font-size:12pt;">
            SUOMEN RIISTAKESKUS<br/>
            SOMPIONTIE 1<br/>
            00730 HELSINKI<br/>
        </p>
    </div>

    <div class="footer" style="margin-top: 180pt">
        <%@include file="footer-de.jsp"%>
    </div>

    <div class="header-logo">
        <img width="280" alt="image" src="01.jpg"/>
    </div>

    <h1 style="padding-top: 40pt;">
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
            1. August <joda:format value="${model.huntingCardStart}" pattern="yyyy" />
            bis 31. Juli <joda:format value="${model.huntingCardEnd}" pattern="yyyy" /><br/>
            Jäger mit gültigem Jagdschein<br/>
            Nordische Länder, EU-Mitgliedsstaaten und Schweiz für Jagdausflüge mit einer Dauer von bis zu 60 Tagen<br/>
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:40pt;font-size: 10pt">
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

    <div style="padding-left:136pt; padding-top:20pt;font-size: 10pt">
        <div style="float:left;">
            Versicherungsnummer<br/>
            Versicherungsart<br/>
            Haftungshöchstbetrag
        </div>

        <div style="padding-left:110pt;">
            353-4344930-S<br/>
            Unfallversicherung für Jäger<br/>
            EUR 10,000 bei Personenschäden<br/>
            EUR 50,000 bei dauernder Erwerbsunfähigkeit<br/>
            EUR 30,000 im Todesfall
        </div>
    </div>

    <div style="padding-left:136pt; padding-top:20pt;font-size: 10pt">
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

    <p style="padding-left: 136pt;padding-top:40pt;font-size: 10pt;line-height: 10pt">
        LOCALTAPIOLA GENERAL MUTUAL INSURANCE COMPANY<br/>
        Groβkunden Service
    </p>

    <p style="padding-left: 136pt;font-style: italic;font-size: 10pt;line-height: 10pt;margin-top: 20px">
        Dieser Versicherungsschein dient lediglich zur Information und überträgt keine
        Rechte auf den Versicherungsscheininhaber. Es handelt sich hierbei um einen
        Auszug aus dem Original des Versicherungsvertrags zwischen dem Versicherungsnehmer
        und der LocalTapiola General Mutual Insurance Company, der im Fall
        von Abweichungen zwischen dem Vertrag und dem Auszug Vorrang hat.
    </p>

    <div class="footer" style="margin-top: 380pt">
        <%@include file="footer-de.jsp"%>
    </div>

    <div class="header-logo">
        <img width="280" alt="image" src="01.jpg"/>
    </div>

    <h1 style="padding-top: 30pt;padding-left: 21pt;">
        Übersetzung wichtiger Begriffe im finnischen Jagdschein (Originaltext auf finnisch und schwedisch):
    </h1>

    <h1 style="padding-left: 21pt;padding-top:20pt;">
        IN DER JAGD KARTE
    </h1>

    <div class="position:relative">
        <div style="padding-left: 60pt; float:left;">
            <p style="text-decoration: underline;">
                Auf finnisch:
            </p>

            <p style="padding-top: 17pt;">
                Metsästyskortti<br/>
                Kuitti riistanhoitomaksun<br/>
                suorittamisesta<br/>
                Metsästäjän nimi<br/>
                Syntymäaika<br/>
                Yhdistys<br/>
                Yhdistysnumero<br/>
                Metsästäjänumero<br/>
                Jakeluosoite<br/>
                Asuinpaikka<br/>
                Postinumero ja -toimipaikka<br/>
            </p>
        </div>

        <div style="margin-left: 250pt;">
            <p style="text-decoration: underline;">
                Auf deutsch:
            </p>

            <p style="padding-top: 17pt;">
                Jagdschein<br/>
                Quittung des<br/>
                Gebühr des Jahresjagdscheines<br/>
                Name, Vorname<br/>
                Geburtsdatum<br/>
                Jagdverein<br/>
                Registernummer vom Verein<br/>
                Registernummer vom Jäger<br/>
                Anschrift<br/>
                Wohnort<br/>
                PLZ, Postort<br/>
            </p>
        </div>
    </div>

    <div class="footer" style="margin-top: 560pt">
        <%@include file="footer-de.jsp"%>
    </div>
</div>
</body>
</html>
