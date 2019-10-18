<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<div id="administrativeCourt" class="attachment">
    <h1>VALITUSOSOITUS</h1>

    <h2>(Riistahallintolaki 31.1 §)</h2>
    <p>Tähän päätökseen tyytymätön voi hakea siihen muutosta valittamalla hallinto-oikeuteen kirjallisella
        valituksella.</p>

    <h2>Valituskirjelmän sisältö ja allekirjoittaminen</h2>
    <p>Valituskirjelmässä, joka osoitetaan hallinto-oikeudelle, on ilmoitettava seuraavat asiat:</p>
    <ul>
        <li>
            valittajan nimi ja kotikunta
        </li>
        <li>
            päätös, johon haetaan muutosta
        </li>
        <li>
            miltä kohdin päätökseen haetaan muutosta ja mitä muutoksia siihen vaaditaan tehtäväksi sekä perusteet,
            joilla muutosta vaaditaan
        </li>
        <li>
            postiosoite ja puhelinnumero, joihin asiaa koskevat ilmoitukset valittajalle voidaan toimittaa.
        </li>
    </ul>
    <p>Jos valittajan puhevaltaa käyttää hänen laillinen edustajansa tai asiamiehensä tai jos valituksen laatijana on
        joku muu henkilö, valituskirjelmässä on ilmoitettava myös tämän nimi ja kotikunta.</p>
    <p>Valittajan, laillisen edustajan tai asiamiehen on allekirjoitettava valituskirjelmä, ellei valituskirjelmää
        ole lähetetty sähköisesti (faksi tai sähköposti).</p>

    <h2>Valituskirjelmän liitteet</h2>
    <p>Valituskirjelmään on liitettävä:</p>
    <ul>
        <li>
            päätös, johon haetaan muutosta valittamalla, alkuperäisenä tai jäljennöksenä
        </li>
        <li>
            todistus siitä, minä päivänä päätös on annettu tiedoksi, tai muu selvitys valitusajan alkamisen ajankohdasta
        </li>
        <li>
            asiakirjat, joihin valittaja vetoaa vaatimuksensa tueksi, jollei niitä ole jo aikaisemmin toimitettu
            viranomaiselle
        </li>
        <li>
            asiamiehen valtakirja, jollei asiamiehenä toimi asianajaja tai yleinen oikeusavustaja.
        </li>
    </ul>
    <h2>Valitusaika</h2>
    <p>Valitus on tehtävä 30 päivän kuluessa päätöksen tiedoksisaannista. Valitusaikaa laskettaessa tiedoksisaantipäivää
        ei oteta lukuun.</p>
    <p>Tiedoksisaantipäivä lasketaan seuraavasti:</p>
    <ul>
        <li>
            Jos päätös on luovutettu asianomaiselle, asianomaisen asiamiehelle tai lähetille, tiedoksisaantipäivä
            ilmenee päätöksessä olevasta leimasta.
        </li>
        <li>
            Jos päätös on lähetetty postitse saantitodistusta vastaan, tiedoksisaantipäivä ilmenee saantitodistuksesta.
        </li>
        <li>
            Jos päätös on postitettu tavallisena kirjeenä sen katsotaan tulleen tiedoksi seitsemäntenä päivänä
            postituspäivästä, jollei muuta ilmene.
        </li>
        <li>
            Jos päätös on toimitettu tiedoksi muulla tavalla jollekin muulle henkilölle kuin päätöksen saajalle
            (sijaistiedoksianto), katsotaan päätöksen saajan saaneen päätöksen tiedoksi kolmantena päivänä tiedoksianto-
            tai saantitodistuksen osoittamasta päivästä.
        </li>
        <li>
            Jos päätös tai kuulutus sen nähtävänä pitämisestä on julkaistu virallisessa lehdessä tai julkisella
            kuulutuksella ilmoitustaululla, katsotaan tiedoksisaannin tapahtuneen seitsemäntenä päivänä siitä päivästä,
            jolloin kuulutus virallisessa lehdessä on julkaistu tai kuulutus pantu ilmoitustaululle.
        </li>
    </ul>

    <h2>Valituskirjelmän toimittaminen</h2>
    <p>
        Valituskirjelmän voi toimittaa hallinto-oikeudelle henkilökohtaisesti, postitse maksettuna postilähetyksenä,
        telekopiona, sähköpostilla tai asiamiestä tai lähettiä käyttäen. Postittaminen tai muu toimittaminen tapahtuu
        lähettäjän vastuulla. Valitusasiakirjojen tulee olla perillä valitusajan viimeisenä päivänä ennen viraston
        aukioloajan päättymistä. Jos valitusajan viimeinen päivä on pyhäpäivä, lauantai, itsenäisyyspäivä, vapunpäivä,
        jouluaatto tai juhannusaatto, valitusaika jatkuu kuitenkin vielä seuraavan arkipäivän viraston aukioloajan
        päättymiseen.
    </p>

    <h2>Käsittelymaksu</h2>
    <p>Hallinto-oikeus perii pääsääntöisesti valitusasian käsittelystä oikeudenkäyntimaksua 250 euroa
        tuomioistuinmaksulain (1455/2015) nojalla.</p>

    <h2>Hallinto-oikeuden osoite</h2>
    <p><c:out value="${model.document.administrativeCourt}" escapeXml="false"/></p>
</div>
