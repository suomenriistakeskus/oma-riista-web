<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<div id="administrativeCourt" class="attachment">
    <h1>VALITUSOSOITUS</h1>

    <h2>(Riistahallintolaki 31.1 §)</h2>
    <p>Tähän päätökseen saa hakea muutosta valittamalla hallinto-oikeuteen.</p>

    <h2>Valituksen sisältö</h2>
    <p>Valituksessa on ilmoitettava</p>
    <ul>
        <li>
            päätös, johon haetaan muutosta
        </li>
        <li>
            miltä kohdin päätökseen haetaan muutosta ja mitä muutoksia siihen vaaditaan tehtäväksi
        </li>
        <li>
            vaatimusten perustelut
        </li>
        <li>
            mihin valitusoikeus perustuu, jos valituksen kohteena oleva päätös ei kohdistu valittajaan
        </li>
    </ul>
    <p>
        Valituksessa on lisäksi ilmoitettava valittajan nimi ja yhteystiedot.
        Jos puhevaltaa käyttää valittajan laillinen edustaja tai asiamies, myös tämän yhteystiedot on ilmoitettava.
    </p>
    <p>
        Valituksessa on ilmoitettava myös se postiosoite ja mahdollinen muu osoite, johon oikeudenkäyntiin liittyvät
        asiakirjat voidaan lähettää (<i>prosessiosoite</i>).
    </p>

    <h2>Valituksen liitteet</h2>
    <p>Valitukseen on liitettävä:</p>
    <ul>
        <li>
            valituksen kohteena oleva päätös valitusosoituksineen
        </li>
        <li>
            selvitys siitä, milloin valittaja on saanut päätöksen tiedoksi, tai muu selvitys valitusajan alkamisen
            ajankohdasta
        </li>
        <li>
            asiakirjat, joihin valittaja vetoaa vaatimuksensa tueksi, jollei niitä ole jo aikaisemmin toimitettu
            viranomaiselle.
        </li>
        <li>
            asiamiehen valtakirja, jollei oikeudenkäynnistä hallintoasioissa annetussa laissa toisin säädetä
        </li>
    </ul>

    <h2>Valituksen tekeminen ja valitusaika</h2>

    <p>Valituksen saa tehdä sillä perusteella, että päätös on lainvastainen.</p>
    <p>
        Valitus on tehtävä kirjallisesti 30 päivän kuluessa päätöksen tiedoksisaannista. Valitusajan laskeminen alkaa
        tiedoksisaantipäivää seuraavasta päivästä.
    </p>

    <p>Tiedoksisaantipäivä lasketaan seuraavasti:</p>
    <ul>
        <li>
            jos päätös on postitettu tavallisena kirjeenä, sen katsotaan annetun tiedoksi seitsemäntenä päivänä
            postituspäivästä, jollei muuta ilmene
        </li>
        <li>
            jos päätös on annettu tiedoksi sähköisenä viestinä, sen katsotaan annetun tiedoksi kolmantena päivänä
            viestin lähettämisestä, jollei muuta näytetä
        </li>
        <li>
            jos päätös on lähetetty postitse saantitodistusta vastaan, tiedoksisaantipäivä ilmenee saantitodistuksesta
        </li>
        <li>
            jos päätös on luovutettu asianomaiselle, asianomaisen asiamiehelle tai lähetille, tiedoksisaantipäivä
            ilmenee päätöksessä olevasta leimasta
        </li>
        <li>
            jos päätös on toimitettu tiedoksi muulla tavalla jollekin muulle henkilölle kuin päätöksen saajalle
            (sijaistiedoksianto), katsotaan päätöksen saajan saaneen päätöksen tiedoksi kolmantena päivänä tiedoksianto-
            tai saantitodistuksen osoittamasta päivästä
        </li>
    </ul>

    <h2>Valituksen toimittaminen</h2>
    <p>
        Valitus on toimitettava valitusajassa toimivaltaiselle hallintotuomioistuimelle. Valituksen voi toimittaa
        hallinto-oikeudelle henkilökohtaisesti, lähetin tai asiamiehen välityksellä. Valituksen voi toimittaa myös
        postitse, sähköpostitse tai telekopiona hallinto-oikeuden kirjaamoon, tai sähköisen asiointipalvelun kautta.
        Valitusasiakirjojen on oltava perillä hallinto-oikeudessa valitusajan viimeisenä päivänä klo 16.15 mennessä.
        Postittaminen tai muu toimittaminen tapahtuu lähettäjän vastuulla. Jos valitusajan viimeinen päivä on pyhäpäivä,
        lauantai, itsenäisyyspäivä, vapunpäivä, jouluaatto tai juhannusaatto, valitusaika jatkuu kuitenkin vielä
        seuraavan arkipäivän viraston aukioloajan päättymiseen.
    </p>

    <h2>Käsittelymaksu</h2>
    <p>Hallinto-oikeuden päätöksistä peritään oikeudenkäyntimaksu tuomioistuinmaksulain (1455/2015) mukaisesti.
        Oikeudenkäyntimaksua ei peritä, jos hallinto-oikeus muuttaa valituksenalaista päätöstä muutoksenhakijan
        eduksi.
    </p>

    <h2>Hallinto-oikeuden osoite</h2>
    <p><c:out value="${model.document.administrativeCourt}" escapeXml="false"/></p>
</div>
