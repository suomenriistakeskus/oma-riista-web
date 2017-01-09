/*
 Copyright 2017 Finnish Wildlife Agency - Suomen Riistakeskus
 Copyright 2014 Ministry of Justice, Finland

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 NOTE: File contains modification to the original source.
*/
package fi.riista.feature.vetuma.support;

import com.google.common.base.MoreObjects;

import fi.riista.util.LocalisedString;

public class VtjData {

    private String etunimet;

    private String sukunimi;

    private String lahiosoiteS;

    private String lahiosoiteR;

    private String postinumero;

    private String postitoimipaikkaS;

    private String postitoimipaikkaR;

    private String kielikoodi;

    private String kuntanumero;

    private String kuntaS;

    private String kuntaR;

    private boolean suomenKansalainen;

    private boolean kuollut;

    private String paluukoodi;

    private UlkomainenOsoite ulkomainenOsoite = new UlkomainenOsoite();

    public VtjData() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("etunimet", etunimet)
                .add("sukunimi", sukunimi)
                .add("kuntanumero", kuntanumero)
                .add("kuntaS", kuntaS)
                .add("kuntaR", kuntaR)
                .add("suomenKansalainen", suomenKansalainen)
                .add("kuollut", kuollut)
                .add("paluukoodi", paluukoodi)
                .toString();
    }

    public LocalisedString getKotikunta() {
        return LocalisedString.of(kuntaS, kuntaR);
    }

    public LocalisedString getLahiosoite() {
        return LocalisedString.of(lahiosoiteS, lahiosoiteR);
    }

    public LocalisedString getPostitoimipaikka() {
        return LocalisedString.of(postitoimipaikkaS, postitoimipaikkaR);
    }

    public boolean hasSuomalainenOsoite() {
        return lahiosoiteS != null || lahiosoiteR != null;
    }

    public boolean isSuomenKansalainen() {
        return suomenKansalainen;
    }

    public void setSuomenKansalainen(boolean suomenKansalainen) {
        this.suomenKansalainen = suomenKansalainen;
    }

    public boolean isKuollut() {
        return kuollut;
    }

    public void setKuollut(boolean kuollut) {
        this.kuollut = kuollut;
    }

    public String getEtunimet() {
        return etunimet;
    }

    public void setEtunimet(String etunimet) {
        this.etunimet = etunimet;
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }

    public String getKuntanumero() {
        return kuntanumero;
    }

    public void setKuntanumero(String kuntanumero) {
        this.kuntanumero = kuntanumero;
    }

    public String getKuntaS() {
        return kuntaS;
    }

    public void setKuntaS(String kuntaS) {
        this.kuntaS = kuntaS;
    }

    public String getKuntaR() {
        return kuntaR;
    }

    public void setKuntaR(String kuntaR) {
        this.kuntaR = kuntaR;
    }

    public String getPaluukoodi() {
        return paluukoodi;
    }

    public void setPaluukoodi(String paluukoodi) {
        this.paluukoodi = paluukoodi;
    }

    public String getLahiosoiteS() {
        return lahiosoiteS;
    }

    public void setLahiosoiteS(String lahiosoiteS) {
        this.lahiosoiteS = lahiosoiteS;
    }

    public String getLahiosoiteR() {
        return lahiosoiteR;
    }

    public void setLahiosoiteR(String lahiosoiteR) {
        this.lahiosoiteR = lahiosoiteR;
    }

    public String getPostinumero() {
        return postinumero;
    }

    public void setPostinumero(String postinumero) {
        this.postinumero = postinumero;
    }

    public String getPostitoimipaikkaS() {
        return postitoimipaikkaS;
    }

    public void setPostitoimipaikkaS(String postitoimipaikkaS) {
        this.postitoimipaikkaS = postitoimipaikkaS;
    }

    public String getPostitoimipaikkaR() {
        return postitoimipaikkaR;
    }

    public void setPostitoimipaikkaR(String postitoimipaikkaR) {
        this.postitoimipaikkaR = postitoimipaikkaR;
    }

    public String getKielikoodi() {
        return kielikoodi;
    }

    public void setKielikoodi(String kielikoodi) {
        this.kielikoodi = kielikoodi;
    }

    public UlkomainenOsoite getUlkomainenOsoite() {
        return ulkomainenOsoite;
    }

    public void setUlkomainenOsoite(UlkomainenOsoite ulkomainenOsoite) {
        this.ulkomainenOsoite = ulkomainenOsoite;
    }

    public static class UlkomainenOsoite {

        private String lahiosoite;
        private String paikkakuntaJaValtio;
        private String valtiokoodi;

        public String getLahiosoite() {
            return lahiosoite;
        }

        public void setLahiosoite(String lahiosoite) {
            this.lahiosoite = lahiosoite;
        }

        public String getPaikkakuntaJaValtio() {
            return paikkakuntaJaValtio;
        }

        public void setPaikkakuntaJaValtio(String paikkakuntaJaValtio) {
            this.paikkakuntaJaValtio = paikkakuntaJaValtio;
        }

        public String getValtiokoodi() {
            return valtiokoodi;
        }

        public void setValtiokoodi(String valtiokoodi) {
            this.valtiokoodi = valtiokoodi;
        }

        public boolean hasOsoite() {
            return lahiosoite != null;
        }
    }
}
