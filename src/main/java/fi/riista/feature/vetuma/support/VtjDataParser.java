package fi.riista.feature.vetuma.support;

import com.google.common.base.Strings;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public final class VtjDataParser {

    public static VtjData parse(String vtjDataXml) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = null;

        try {
            parser = factory.createXMLStreamReader(new StringReader(vtjDataXml));

            return new VtjDataParser(parser).parse();

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);

        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (XMLStreamException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private final XMLStreamReader parser;
    private final VtjData vtjData;

    private boolean currentIsNativeLanguage;
    private boolean currentIsLocalAddress = false;
    private boolean currentIsForeignAddress = false;

    private VtjDataParser(final XMLStreamReader parser) {
        this.parser = parser;
        this.vtjData = new VtjData();
    }

    public VtjData parse() throws XMLStreamException {
        while (parser.hasNext()) {
            int event = parser.next();
            switch (event) {
                case START_ELEMENT:
                    handleStartElement();
                    break;
                case END_ELEMENT:
                    handleEndElement();
                    break;
                default:
                    // Nothing to do
            }
        }

        return vtjData;
    }

    private void handleStartElement() throws XMLStreamException {
        final String localName = parser.getLocalName();

        if ("Paluukoodi".equals(localName)) {
            vtjData.setPaluukoodi(parseText(parser));
        } else if ("NykyinenSukunimi".equals(localName)) {
            vtjData.setSukunimi(parseText(parser));
        } else if ("NykyisetEtunimet".equals(localName)) {
            vtjData.setEtunimet(parseText(parser));
        } else if ("Kuntanumero".equals(localName)) {
            vtjData.setKuntanumero(parseText(parser));
        } else if ("KuntaS".equals(localName)) {
            vtjData.setKuntaS(parseText(parser));
        } else if ("KuntaR".equals(localName)) {
            vtjData.setKuntaR(parseText(parser));
        } else if ("Kuolinpvm".equals(localName)) {
            vtjData.setKuollut(!Strings.isNullOrEmpty(parseText(parser)));
        } else if ("SuomenKansalaisuusTietokoodi".equals(localName)) {
            vtjData.setSuomenKansalainen("1".equals(parseText(parser)));
        } else if ("Aidinkieli".equals(localName)) {
            // äidinkieli
            currentIsNativeLanguage = true;
        } else if (currentIsNativeLanguage && "Kielikoodi".equals(localName)) {
            vtjData.setKielikoodi(parseText(parser));
        } else if ("VakinainenKotimainenLahiosoite".equals(localName)) {
            // kotimainen osoite
            currentIsLocalAddress = true;
        } else if (currentIsLocalAddress && "LahiosoiteS".equals(localName)) {
            vtjData.setLahiosoiteS(parseText(parser));
        } else if (currentIsLocalAddress && "LahiosoiteR".equals(localName)) {
            vtjData.setLahiosoiteR(parseText(parser));
        } else if (currentIsLocalAddress && "Postinumero".equals(localName)) {
            vtjData.setPostinumero(parseText(parser));
        } else if (currentIsLocalAddress && "PostitoimipaikkaS".equals(localName)) {
            vtjData.setPostitoimipaikkaS(parseText(parser));
        } else if (currentIsLocalAddress && "PostitoimipaikkaR".equals(localName)) {
            vtjData.setPostitoimipaikkaR(parseText(parser));
        } else if ("VakinainenUlkomainenLahiosoite".equals(localName)) {
            // ulkomainen osoite
            currentIsForeignAddress = true;
        } else if (currentIsForeignAddress && "UlkomainenLahiosoite".equals(localName)) {
            vtjData.getUlkomainenOsoite().setLahiosoite(parseText(parser));
        } else if (currentIsForeignAddress && "UlkomainenPaikkakuntaJaValtioSelvakielinen".equals(localName)) {
            vtjData.getUlkomainenOsoite().setPaikkakuntaJaValtio(parseText(parser));
        } else if (currentIsForeignAddress && "Valtiokoodi3".equals(localName)) {
            vtjData.getUlkomainenOsoite().setValtiokoodi(parseText(parser));
        }
    }

    private void handleEndElement() {
        final String localName = parser.getLocalName();

        if ("Aidinkieli".equals(localName)) {
            currentIsNativeLanguage = false;
        } else if ("VakinainenKotimainenLahiosoite".equals(localName)) {
            currentIsLocalAddress = false;
        } else if ("VakinainenUlkomainenLahiosoite".equals(localName)) {
            currentIsForeignAddress = false;
        }
    }

    private static String parseText(XMLStreamReader parser) throws XMLStreamException {
        StringBuilder sb = new StringBuilder(32);
        int depth = 1;

        while (parser.hasNext() && depth > 0) {
            switch (parser.next()) {
                case START_ELEMENT:
                    depth++;
                    break;
                case END_ELEMENT:
                    depth--;
                    break;
                case CHARACTERS:
                case CDATA:
                    if (!parser.isWhiteSpace()) {
                        sb.append(parser.getText());
                    }
                    break;
                default:
                    // Nothing to do
            }
        }
        // Trim and return null for empty String
        String result = sb.toString().trim();
        return Strings.isNullOrEmpty(result) ? null : result;
    }
}
