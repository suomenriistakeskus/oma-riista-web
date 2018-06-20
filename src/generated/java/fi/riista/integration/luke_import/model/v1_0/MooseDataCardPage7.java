
package fi.riista.integration.luke_import.model.v1_0;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString2;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;


/**
 * <p>Java class for _Sivu_7Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_Sivu_7Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="_7.1" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_7.1Type"/&gt;
 *         &lt;element name="_Asiakasnumero" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Esiintyminen" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_EsiintyminenType"/&gt;
 *         &lt;element name="_MuutoksetExport" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Arvio_yksilömäärästä" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Esiintyminen1" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_EsiintyminenType"/&gt;
 *         &lt;element name="_MuutoksetExport1" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Arvio_yksilömäärästä1" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Esiintyminen2" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_EsiintyminenType"/&gt;
 *         &lt;element name="_MuutoksetExport2" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Arvio_yksilömäärästä2" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Esiintyminen3" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_EsiintyminenType"/&gt;
 *         &lt;element name="_MuutoksetExport3" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Arvio_yksilömäärästä3" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Esiintyminen4" type="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}_EsiintyminenType"/&gt;
 *         &lt;element name="_MuutoksetExport4" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="_Arvio_yksilömäärästä4" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Arvio_porsaallisten_emakoiden_määrästä" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;anyAttribute namespace='http://www.abbyy.com/FlexiCapture/Schemas/Export/AdditionalFormData.xsd'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "_Sivu_7Type", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "section_7_1",
    "huntingClubCode",
    "whiteTailedDeerAppeared",
    "trendOfWhiteTailedDeerPopulationGrowth",
    "estimatedSpecimenAmountOfWhiteTailedDeer",
    "roeDeerAppeared",
    "trendOfRoeDeerPopulationGrowth",
    "estimatedSpecimenAmountOfRoeDeer",
    "wildForestReindeerAppeared",
    "trendOfWildForestReindeerPopulationGrowth",
    "estimatedSpecimenAmountOfWildForestReindeer",
    "fallowDeerAppeared",
    "trendOfFallowDeerPopulationGrowth",
    "estimatedSpecimenAmountOfFallowDeer",
    "wildBoarAppeared",
    "trendOfWildBoarPopulationGrowth",
    "estimatedSpecimenAmountOfWildBoar",
    "estimatedAmountOfSowsWithPiglets"
})
public class MooseDataCardPage7 implements Cloneable, Copyable, PartialCopyable, ToString2
{

    @XmlElement(name = "_7.1", required = true)
    protected MooseDataCardSection_7_1 section_7_1;
    @XmlElement(name = "_Asiakasnumero", required = true)
    protected String huntingClubCode;
    @XmlElement(name = "_Esiintyminen", required = true)
    @XmlSchemaType(name = "string")
    protected MooseDataCardGameSpeciesAppearance whiteTailedDeerAppeared;
    @XmlElement(name = "_MuutoksetExport", required = true)
    protected String trendOfWhiteTailedDeerPopulationGrowth;
    @XmlElement(name = "_Arvio_yksil\u00f6m\u00e4\u00e4r\u00e4st\u00e4", required = true, type = Integer.class, nillable = true)
    protected Integer estimatedSpecimenAmountOfWhiteTailedDeer;
    @XmlElement(name = "_Esiintyminen1", required = true)
    @XmlSchemaType(name = "string")
    protected MooseDataCardGameSpeciesAppearance roeDeerAppeared;
    @XmlElement(name = "_MuutoksetExport1", required = true)
    protected String trendOfRoeDeerPopulationGrowth;
    @XmlElement(name = "_Arvio_yksil\u00f6m\u00e4\u00e4r\u00e4st\u00e41", required = true, type = Integer.class, nillable = true)
    protected Integer estimatedSpecimenAmountOfRoeDeer;
    @XmlElement(name = "_Esiintyminen2", required = true)
    @XmlSchemaType(name = "string")
    protected MooseDataCardGameSpeciesAppearance wildForestReindeerAppeared;
    @XmlElement(name = "_MuutoksetExport2", required = true)
    protected String trendOfWildForestReindeerPopulationGrowth;
    @XmlElement(name = "_Arvio_yksil\u00f6m\u00e4\u00e4r\u00e4st\u00e42", required = true, type = Integer.class, nillable = true)
    protected Integer estimatedSpecimenAmountOfWildForestReindeer;
    @XmlElement(name = "_Esiintyminen3", required = true)
    @XmlSchemaType(name = "string")
    protected MooseDataCardGameSpeciesAppearance fallowDeerAppeared;
    @XmlElement(name = "_MuutoksetExport3", required = true)
    protected String trendOfFallowDeerPopulationGrowth;
    @XmlElement(name = "_Arvio_yksil\u00f6m\u00e4\u00e4r\u00e4st\u00e43", required = true, type = Integer.class, nillable = true)
    protected Integer estimatedSpecimenAmountOfFallowDeer;
    @XmlElement(name = "_Esiintyminen4", required = true)
    @XmlSchemaType(name = "string")
    protected MooseDataCardGameSpeciesAppearance wildBoarAppeared;
    @XmlElement(name = "_MuutoksetExport4", required = true)
    protected String trendOfWildBoarPopulationGrowth;
    @XmlElement(name = "_Arvio_yksil\u00f6m\u00e4\u00e4r\u00e4st\u00e44", required = true, type = Integer.class, nillable = true)
    protected Integer estimatedSpecimenAmountOfWildBoar;
    @XmlElement(name = "_Arvio_porsaallisten_emakoiden_m\u00e4\u00e4r\u00e4st\u00e4", required = true, type = Integer.class, nillable = true)
    protected Integer estimatedAmountOfSowsWithPiglets;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardPage7() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardPage7 copying the state of another MooseDataCardPage7
     * 
     * @param _other
     *     The original MooseDataCardPage7 from which to copy state.
     */
    public MooseDataCardPage7(final MooseDataCardPage7 _other) {
        this.section_7_1 = ((_other.section_7_1 == null)?null:_other.section_7_1 .createCopy());
        this.huntingClubCode = _other.huntingClubCode;
        this.whiteTailedDeerAppeared = _other.whiteTailedDeerAppeared;
        this.trendOfWhiteTailedDeerPopulationGrowth = _other.trendOfWhiteTailedDeerPopulationGrowth;
        this.estimatedSpecimenAmountOfWhiteTailedDeer = _other.estimatedSpecimenAmountOfWhiteTailedDeer;
        this.roeDeerAppeared = _other.roeDeerAppeared;
        this.trendOfRoeDeerPopulationGrowth = _other.trendOfRoeDeerPopulationGrowth;
        this.estimatedSpecimenAmountOfRoeDeer = _other.estimatedSpecimenAmountOfRoeDeer;
        this.wildForestReindeerAppeared = _other.wildForestReindeerAppeared;
        this.trendOfWildForestReindeerPopulationGrowth = _other.trendOfWildForestReindeerPopulationGrowth;
        this.estimatedSpecimenAmountOfWildForestReindeer = _other.estimatedSpecimenAmountOfWildForestReindeer;
        this.fallowDeerAppeared = _other.fallowDeerAppeared;
        this.trendOfFallowDeerPopulationGrowth = _other.trendOfFallowDeerPopulationGrowth;
        this.estimatedSpecimenAmountOfFallowDeer = _other.estimatedSpecimenAmountOfFallowDeer;
        this.wildBoarAppeared = _other.wildBoarAppeared;
        this.trendOfWildBoarPopulationGrowth = _other.trendOfWildBoarPopulationGrowth;
        this.estimatedSpecimenAmountOfWildBoar = _other.estimatedSpecimenAmountOfWildBoar;
        this.estimatedAmountOfSowsWithPiglets = _other.estimatedAmountOfSowsWithPiglets;
    }

    /**
     * Instantiates a MooseDataCardPage7 copying the state of another MooseDataCardPage7
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardPage7 from which to copy state.
     */
    public MooseDataCardPage7(final MooseDataCardPage7 _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree section_7_1PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_7_1"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_7_1PropertyTree!= null):((section_7_1PropertyTree == null)||(!section_7_1PropertyTree.isLeaf())))) {
            this.section_7_1 = ((_other.section_7_1 == null)?null:_other.section_7_1 .createCopy(section_7_1PropertyTree, _propertyTreeUse));
        }
        final PropertyTree huntingClubCodePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubCode"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubCodePropertyTree!= null):((huntingClubCodePropertyTree == null)||(!huntingClubCodePropertyTree.isLeaf())))) {
            this.huntingClubCode = _other.huntingClubCode;
        }
        final PropertyTree whiteTailedDeerAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("whiteTailedDeerAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(whiteTailedDeerAppearedPropertyTree!= null):((whiteTailedDeerAppearedPropertyTree == null)||(!whiteTailedDeerAppearedPropertyTree.isLeaf())))) {
            this.whiteTailedDeerAppeared = _other.whiteTailedDeerAppeared;
        }
        final PropertyTree trendOfWhiteTailedDeerPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfWhiteTailedDeerPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfWhiteTailedDeerPopulationGrowthPropertyTree!= null):((trendOfWhiteTailedDeerPopulationGrowthPropertyTree == null)||(!trendOfWhiteTailedDeerPopulationGrowthPropertyTree.isLeaf())))) {
            this.trendOfWhiteTailedDeerPopulationGrowth = _other.trendOfWhiteTailedDeerPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfWhiteTailedDeerPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfWhiteTailedDeer"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfWhiteTailedDeerPropertyTree!= null):((estimatedSpecimenAmountOfWhiteTailedDeerPropertyTree == null)||(!estimatedSpecimenAmountOfWhiteTailedDeerPropertyTree.isLeaf())))) {
            this.estimatedSpecimenAmountOfWhiteTailedDeer = _other.estimatedSpecimenAmountOfWhiteTailedDeer;
        }
        final PropertyTree roeDeerAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("roeDeerAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(roeDeerAppearedPropertyTree!= null):((roeDeerAppearedPropertyTree == null)||(!roeDeerAppearedPropertyTree.isLeaf())))) {
            this.roeDeerAppeared = _other.roeDeerAppeared;
        }
        final PropertyTree trendOfRoeDeerPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfRoeDeerPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfRoeDeerPopulationGrowthPropertyTree!= null):((trendOfRoeDeerPopulationGrowthPropertyTree == null)||(!trendOfRoeDeerPopulationGrowthPropertyTree.isLeaf())))) {
            this.trendOfRoeDeerPopulationGrowth = _other.trendOfRoeDeerPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfRoeDeerPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfRoeDeer"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfRoeDeerPropertyTree!= null):((estimatedSpecimenAmountOfRoeDeerPropertyTree == null)||(!estimatedSpecimenAmountOfRoeDeerPropertyTree.isLeaf())))) {
            this.estimatedSpecimenAmountOfRoeDeer = _other.estimatedSpecimenAmountOfRoeDeer;
        }
        final PropertyTree wildForestReindeerAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("wildForestReindeerAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(wildForestReindeerAppearedPropertyTree!= null):((wildForestReindeerAppearedPropertyTree == null)||(!wildForestReindeerAppearedPropertyTree.isLeaf())))) {
            this.wildForestReindeerAppeared = _other.wildForestReindeerAppeared;
        }
        final PropertyTree trendOfWildForestReindeerPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfWildForestReindeerPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfWildForestReindeerPopulationGrowthPropertyTree!= null):((trendOfWildForestReindeerPopulationGrowthPropertyTree == null)||(!trendOfWildForestReindeerPopulationGrowthPropertyTree.isLeaf())))) {
            this.trendOfWildForestReindeerPopulationGrowth = _other.trendOfWildForestReindeerPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfWildForestReindeerPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfWildForestReindeer"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfWildForestReindeerPropertyTree!= null):((estimatedSpecimenAmountOfWildForestReindeerPropertyTree == null)||(!estimatedSpecimenAmountOfWildForestReindeerPropertyTree.isLeaf())))) {
            this.estimatedSpecimenAmountOfWildForestReindeer = _other.estimatedSpecimenAmountOfWildForestReindeer;
        }
        final PropertyTree fallowDeerAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("fallowDeerAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(fallowDeerAppearedPropertyTree!= null):((fallowDeerAppearedPropertyTree == null)||(!fallowDeerAppearedPropertyTree.isLeaf())))) {
            this.fallowDeerAppeared = _other.fallowDeerAppeared;
        }
        final PropertyTree trendOfFallowDeerPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfFallowDeerPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfFallowDeerPopulationGrowthPropertyTree!= null):((trendOfFallowDeerPopulationGrowthPropertyTree == null)||(!trendOfFallowDeerPopulationGrowthPropertyTree.isLeaf())))) {
            this.trendOfFallowDeerPopulationGrowth = _other.trendOfFallowDeerPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfFallowDeerPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfFallowDeer"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfFallowDeerPropertyTree!= null):((estimatedSpecimenAmountOfFallowDeerPropertyTree == null)||(!estimatedSpecimenAmountOfFallowDeerPropertyTree.isLeaf())))) {
            this.estimatedSpecimenAmountOfFallowDeer = _other.estimatedSpecimenAmountOfFallowDeer;
        }
        final PropertyTree wildBoarAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("wildBoarAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(wildBoarAppearedPropertyTree!= null):((wildBoarAppearedPropertyTree == null)||(!wildBoarAppearedPropertyTree.isLeaf())))) {
            this.wildBoarAppeared = _other.wildBoarAppeared;
        }
        final PropertyTree trendOfWildBoarPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfWildBoarPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfWildBoarPopulationGrowthPropertyTree!= null):((trendOfWildBoarPopulationGrowthPropertyTree == null)||(!trendOfWildBoarPopulationGrowthPropertyTree.isLeaf())))) {
            this.trendOfWildBoarPopulationGrowth = _other.trendOfWildBoarPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfWildBoarPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfWildBoar"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfWildBoarPropertyTree!= null):((estimatedSpecimenAmountOfWildBoarPropertyTree == null)||(!estimatedSpecimenAmountOfWildBoarPropertyTree.isLeaf())))) {
            this.estimatedSpecimenAmountOfWildBoar = _other.estimatedSpecimenAmountOfWildBoar;
        }
        final PropertyTree estimatedAmountOfSowsWithPigletsPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedAmountOfSowsWithPiglets"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedAmountOfSowsWithPigletsPropertyTree!= null):((estimatedAmountOfSowsWithPigletsPropertyTree == null)||(!estimatedAmountOfSowsWithPigletsPropertyTree.isLeaf())))) {
            this.estimatedAmountOfSowsWithPiglets = _other.estimatedAmountOfSowsWithPiglets;
        }
    }

    /**
     * Gets the value of the section_7_1 property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardSection_7_1 }
     *     
     */
    public MooseDataCardSection_7_1 getSection_7_1() {
        return section_7_1;
    }

    /**
     * Sets the value of the section_7_1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardSection_7_1 }
     *     
     */
    public void setSection_7_1(MooseDataCardSection_7_1 value) {
        this.section_7_1 = value;
    }

    /**
     * Gets the value of the huntingClubCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHuntingClubCode() {
        return huntingClubCode;
    }

    /**
     * Sets the value of the huntingClubCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHuntingClubCode(String value) {
        this.huntingClubCode = value;
    }

    /**
     * Gets the value of the whiteTailedDeerAppeared property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public MooseDataCardGameSpeciesAppearance getWhiteTailedDeerAppeared() {
        return whiteTailedDeerAppeared;
    }

    /**
     * Sets the value of the whiteTailedDeerAppeared property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public void setWhiteTailedDeerAppeared(MooseDataCardGameSpeciesAppearance value) {
        this.whiteTailedDeerAppeared = value;
    }

    /**
     * Gets the value of the trendOfWhiteTailedDeerPopulationGrowth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrendOfWhiteTailedDeerPopulationGrowth() {
        return trendOfWhiteTailedDeerPopulationGrowth;
    }

    /**
     * Sets the value of the trendOfWhiteTailedDeerPopulationGrowth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrendOfWhiteTailedDeerPopulationGrowth(String value) {
        this.trendOfWhiteTailedDeerPopulationGrowth = value;
    }

    /**
     * Gets the value of the estimatedSpecimenAmountOfWhiteTailedDeer property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEstimatedSpecimenAmountOfWhiteTailedDeer() {
        return estimatedSpecimenAmountOfWhiteTailedDeer;
    }

    /**
     * Sets the value of the estimatedSpecimenAmountOfWhiteTailedDeer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEstimatedSpecimenAmountOfWhiteTailedDeer(Integer value) {
        this.estimatedSpecimenAmountOfWhiteTailedDeer = value;
    }

    /**
     * Gets the value of the roeDeerAppeared property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public MooseDataCardGameSpeciesAppearance getRoeDeerAppeared() {
        return roeDeerAppeared;
    }

    /**
     * Sets the value of the roeDeerAppeared property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public void setRoeDeerAppeared(MooseDataCardGameSpeciesAppearance value) {
        this.roeDeerAppeared = value;
    }

    /**
     * Gets the value of the trendOfRoeDeerPopulationGrowth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrendOfRoeDeerPopulationGrowth() {
        return trendOfRoeDeerPopulationGrowth;
    }

    /**
     * Sets the value of the trendOfRoeDeerPopulationGrowth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrendOfRoeDeerPopulationGrowth(String value) {
        this.trendOfRoeDeerPopulationGrowth = value;
    }

    /**
     * Gets the value of the estimatedSpecimenAmountOfRoeDeer property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEstimatedSpecimenAmountOfRoeDeer() {
        return estimatedSpecimenAmountOfRoeDeer;
    }

    /**
     * Sets the value of the estimatedSpecimenAmountOfRoeDeer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEstimatedSpecimenAmountOfRoeDeer(Integer value) {
        this.estimatedSpecimenAmountOfRoeDeer = value;
    }

    /**
     * Gets the value of the wildForestReindeerAppeared property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public MooseDataCardGameSpeciesAppearance getWildForestReindeerAppeared() {
        return wildForestReindeerAppeared;
    }

    /**
     * Sets the value of the wildForestReindeerAppeared property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public void setWildForestReindeerAppeared(MooseDataCardGameSpeciesAppearance value) {
        this.wildForestReindeerAppeared = value;
    }

    /**
     * Gets the value of the trendOfWildForestReindeerPopulationGrowth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrendOfWildForestReindeerPopulationGrowth() {
        return trendOfWildForestReindeerPopulationGrowth;
    }

    /**
     * Sets the value of the trendOfWildForestReindeerPopulationGrowth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrendOfWildForestReindeerPopulationGrowth(String value) {
        this.trendOfWildForestReindeerPopulationGrowth = value;
    }

    /**
     * Gets the value of the estimatedSpecimenAmountOfWildForestReindeer property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEstimatedSpecimenAmountOfWildForestReindeer() {
        return estimatedSpecimenAmountOfWildForestReindeer;
    }

    /**
     * Sets the value of the estimatedSpecimenAmountOfWildForestReindeer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEstimatedSpecimenAmountOfWildForestReindeer(Integer value) {
        this.estimatedSpecimenAmountOfWildForestReindeer = value;
    }

    /**
     * Gets the value of the fallowDeerAppeared property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public MooseDataCardGameSpeciesAppearance getFallowDeerAppeared() {
        return fallowDeerAppeared;
    }

    /**
     * Sets the value of the fallowDeerAppeared property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public void setFallowDeerAppeared(MooseDataCardGameSpeciesAppearance value) {
        this.fallowDeerAppeared = value;
    }

    /**
     * Gets the value of the trendOfFallowDeerPopulationGrowth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrendOfFallowDeerPopulationGrowth() {
        return trendOfFallowDeerPopulationGrowth;
    }

    /**
     * Sets the value of the trendOfFallowDeerPopulationGrowth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrendOfFallowDeerPopulationGrowth(String value) {
        this.trendOfFallowDeerPopulationGrowth = value;
    }

    /**
     * Gets the value of the estimatedSpecimenAmountOfFallowDeer property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEstimatedSpecimenAmountOfFallowDeer() {
        return estimatedSpecimenAmountOfFallowDeer;
    }

    /**
     * Sets the value of the estimatedSpecimenAmountOfFallowDeer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEstimatedSpecimenAmountOfFallowDeer(Integer value) {
        this.estimatedSpecimenAmountOfFallowDeer = value;
    }

    /**
     * Gets the value of the wildBoarAppeared property.
     * 
     * @return
     *     possible object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public MooseDataCardGameSpeciesAppearance getWildBoarAppeared() {
        return wildBoarAppeared;
    }

    /**
     * Sets the value of the wildBoarAppeared property.
     * 
     * @param value
     *     allowed object is
     *     {@link MooseDataCardGameSpeciesAppearance }
     *     
     */
    public void setWildBoarAppeared(MooseDataCardGameSpeciesAppearance value) {
        this.wildBoarAppeared = value;
    }

    /**
     * Gets the value of the trendOfWildBoarPopulationGrowth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrendOfWildBoarPopulationGrowth() {
        return trendOfWildBoarPopulationGrowth;
    }

    /**
     * Sets the value of the trendOfWildBoarPopulationGrowth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrendOfWildBoarPopulationGrowth(String value) {
        this.trendOfWildBoarPopulationGrowth = value;
    }

    /**
     * Gets the value of the estimatedSpecimenAmountOfWildBoar property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEstimatedSpecimenAmountOfWildBoar() {
        return estimatedSpecimenAmountOfWildBoar;
    }

    /**
     * Sets the value of the estimatedSpecimenAmountOfWildBoar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEstimatedSpecimenAmountOfWildBoar(Integer value) {
        this.estimatedSpecimenAmountOfWildBoar = value;
    }

    /**
     * Gets the value of the estimatedAmountOfSowsWithPiglets property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEstimatedAmountOfSowsWithPiglets() {
        return estimatedAmountOfSowsWithPiglets;
    }

    /**
     * Sets the value of the estimatedAmountOfSowsWithPiglets property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEstimatedAmountOfSowsWithPiglets(Integer value) {
        this.estimatedAmountOfSowsWithPiglets = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public String toString() {
        final ToStringStrategy2 strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy2 strategy) {
        strategy.appendStart(locator, this, buffer);
        appendFields(locator, buffer, strategy);
        strategy.appendEnd(locator, this, buffer);
        return buffer;
    }

    public StringBuilder appendFields(ObjectLocator locator, StringBuilder buffer, ToStringStrategy2 strategy) {
        {
            MooseDataCardSection_7_1 theSection_7_1;
            theSection_7_1 = this.getSection_7_1();
            strategy.appendField(locator, this, "section_7_1", buffer, theSection_7_1, (this.section_7_1 != null));
        }
        {
            String theHuntingClubCode;
            theHuntingClubCode = this.getHuntingClubCode();
            strategy.appendField(locator, this, "huntingClubCode", buffer, theHuntingClubCode, (this.huntingClubCode!= null));
        }
        {
            MooseDataCardGameSpeciesAppearance theWhiteTailedDeerAppeared;
            theWhiteTailedDeerAppeared = this.getWhiteTailedDeerAppeared();
            strategy.appendField(locator, this, "whiteTailedDeerAppeared", buffer, theWhiteTailedDeerAppeared, (this.whiteTailedDeerAppeared!= null));
        }
        {
            String theTrendOfWhiteTailedDeerPopulationGrowth;
            theTrendOfWhiteTailedDeerPopulationGrowth = this.getTrendOfWhiteTailedDeerPopulationGrowth();
            strategy.appendField(locator, this, "trendOfWhiteTailedDeerPopulationGrowth", buffer, theTrendOfWhiteTailedDeerPopulationGrowth, (this.trendOfWhiteTailedDeerPopulationGrowth!= null));
        }
        {
            Integer theEstimatedSpecimenAmountOfWhiteTailedDeer;
            theEstimatedSpecimenAmountOfWhiteTailedDeer = this.getEstimatedSpecimenAmountOfWhiteTailedDeer();
            strategy.appendField(locator, this, "estimatedSpecimenAmountOfWhiteTailedDeer", buffer, theEstimatedSpecimenAmountOfWhiteTailedDeer, (this.estimatedSpecimenAmountOfWhiteTailedDeer!= null));
        }
        {
            MooseDataCardGameSpeciesAppearance theRoeDeerAppeared;
            theRoeDeerAppeared = this.getRoeDeerAppeared();
            strategy.appendField(locator, this, "roeDeerAppeared", buffer, theRoeDeerAppeared, (this.roeDeerAppeared!= null));
        }
        {
            String theTrendOfRoeDeerPopulationGrowth;
            theTrendOfRoeDeerPopulationGrowth = this.getTrendOfRoeDeerPopulationGrowth();
            strategy.appendField(locator, this, "trendOfRoeDeerPopulationGrowth", buffer, theTrendOfRoeDeerPopulationGrowth, (this.trendOfRoeDeerPopulationGrowth!= null));
        }
        {
            Integer theEstimatedSpecimenAmountOfRoeDeer;
            theEstimatedSpecimenAmountOfRoeDeer = this.getEstimatedSpecimenAmountOfRoeDeer();
            strategy.appendField(locator, this, "estimatedSpecimenAmountOfRoeDeer", buffer, theEstimatedSpecimenAmountOfRoeDeer, (this.estimatedSpecimenAmountOfRoeDeer!= null));
        }
        {
            MooseDataCardGameSpeciesAppearance theWildForestReindeerAppeared;
            theWildForestReindeerAppeared = this.getWildForestReindeerAppeared();
            strategy.appendField(locator, this, "wildForestReindeerAppeared", buffer, theWildForestReindeerAppeared, (this.wildForestReindeerAppeared!= null));
        }
        {
            String theTrendOfWildForestReindeerPopulationGrowth;
            theTrendOfWildForestReindeerPopulationGrowth = this.getTrendOfWildForestReindeerPopulationGrowth();
            strategy.appendField(locator, this, "trendOfWildForestReindeerPopulationGrowth", buffer, theTrendOfWildForestReindeerPopulationGrowth, (this.trendOfWildForestReindeerPopulationGrowth!= null));
        }
        {
            Integer theEstimatedSpecimenAmountOfWildForestReindeer;
            theEstimatedSpecimenAmountOfWildForestReindeer = this.getEstimatedSpecimenAmountOfWildForestReindeer();
            strategy.appendField(locator, this, "estimatedSpecimenAmountOfWildForestReindeer", buffer, theEstimatedSpecimenAmountOfWildForestReindeer, (this.estimatedSpecimenAmountOfWildForestReindeer!= null));
        }
        {
            MooseDataCardGameSpeciesAppearance theFallowDeerAppeared;
            theFallowDeerAppeared = this.getFallowDeerAppeared();
            strategy.appendField(locator, this, "fallowDeerAppeared", buffer, theFallowDeerAppeared, (this.fallowDeerAppeared!= null));
        }
        {
            String theTrendOfFallowDeerPopulationGrowth;
            theTrendOfFallowDeerPopulationGrowth = this.getTrendOfFallowDeerPopulationGrowth();
            strategy.appendField(locator, this, "trendOfFallowDeerPopulationGrowth", buffer, theTrendOfFallowDeerPopulationGrowth, (this.trendOfFallowDeerPopulationGrowth!= null));
        }
        {
            Integer theEstimatedSpecimenAmountOfFallowDeer;
            theEstimatedSpecimenAmountOfFallowDeer = this.getEstimatedSpecimenAmountOfFallowDeer();
            strategy.appendField(locator, this, "estimatedSpecimenAmountOfFallowDeer", buffer, theEstimatedSpecimenAmountOfFallowDeer, (this.estimatedSpecimenAmountOfFallowDeer!= null));
        }
        {
            MooseDataCardGameSpeciesAppearance theWildBoarAppeared;
            theWildBoarAppeared = this.getWildBoarAppeared();
            strategy.appendField(locator, this, "wildBoarAppeared", buffer, theWildBoarAppeared, (this.wildBoarAppeared!= null));
        }
        {
            String theTrendOfWildBoarPopulationGrowth;
            theTrendOfWildBoarPopulationGrowth = this.getTrendOfWildBoarPopulationGrowth();
            strategy.appendField(locator, this, "trendOfWildBoarPopulationGrowth", buffer, theTrendOfWildBoarPopulationGrowth, (this.trendOfWildBoarPopulationGrowth!= null));
        }
        {
            Integer theEstimatedSpecimenAmountOfWildBoar;
            theEstimatedSpecimenAmountOfWildBoar = this.getEstimatedSpecimenAmountOfWildBoar();
            strategy.appendField(locator, this, "estimatedSpecimenAmountOfWildBoar", buffer, theEstimatedSpecimenAmountOfWildBoar, (this.estimatedSpecimenAmountOfWildBoar!= null));
        }
        {
            Integer theEstimatedAmountOfSowsWithPiglets;
            theEstimatedAmountOfSowsWithPiglets = this.getEstimatedAmountOfSowsWithPiglets();
            strategy.appendField(locator, this, "estimatedAmountOfSowsWithPiglets", buffer, theEstimatedAmountOfSowsWithPiglets, (this.estimatedAmountOfSowsWithPiglets!= null));
        }
        return buffer;
    }

    public MooseDataCardPage7 withSection_7_1(MooseDataCardSection_7_1 value) {
        setSection_7_1(value);
        return this;
    }

    public MooseDataCardPage7 withHuntingClubCode(String value) {
        setHuntingClubCode(value);
        return this;
    }

    public MooseDataCardPage7 withWhiteTailedDeerAppeared(MooseDataCardGameSpeciesAppearance value) {
        setWhiteTailedDeerAppeared(value);
        return this;
    }

    public MooseDataCardPage7 withTrendOfWhiteTailedDeerPopulationGrowth(String value) {
        setTrendOfWhiteTailedDeerPopulationGrowth(value);
        return this;
    }

    public MooseDataCardPage7 withEstimatedSpecimenAmountOfWhiteTailedDeer(Integer value) {
        setEstimatedSpecimenAmountOfWhiteTailedDeer(value);
        return this;
    }

    public MooseDataCardPage7 withRoeDeerAppeared(MooseDataCardGameSpeciesAppearance value) {
        setRoeDeerAppeared(value);
        return this;
    }

    public MooseDataCardPage7 withTrendOfRoeDeerPopulationGrowth(String value) {
        setTrendOfRoeDeerPopulationGrowth(value);
        return this;
    }

    public MooseDataCardPage7 withEstimatedSpecimenAmountOfRoeDeer(Integer value) {
        setEstimatedSpecimenAmountOfRoeDeer(value);
        return this;
    }

    public MooseDataCardPage7 withWildForestReindeerAppeared(MooseDataCardGameSpeciesAppearance value) {
        setWildForestReindeerAppeared(value);
        return this;
    }

    public MooseDataCardPage7 withTrendOfWildForestReindeerPopulationGrowth(String value) {
        setTrendOfWildForestReindeerPopulationGrowth(value);
        return this;
    }

    public MooseDataCardPage7 withEstimatedSpecimenAmountOfWildForestReindeer(Integer value) {
        setEstimatedSpecimenAmountOfWildForestReindeer(value);
        return this;
    }

    public MooseDataCardPage7 withFallowDeerAppeared(MooseDataCardGameSpeciesAppearance value) {
        setFallowDeerAppeared(value);
        return this;
    }

    public MooseDataCardPage7 withTrendOfFallowDeerPopulationGrowth(String value) {
        setTrendOfFallowDeerPopulationGrowth(value);
        return this;
    }

    public MooseDataCardPage7 withEstimatedSpecimenAmountOfFallowDeer(Integer value) {
        setEstimatedSpecimenAmountOfFallowDeer(value);
        return this;
    }

    public MooseDataCardPage7 withWildBoarAppeared(MooseDataCardGameSpeciesAppearance value) {
        setWildBoarAppeared(value);
        return this;
    }

    public MooseDataCardPage7 withTrendOfWildBoarPopulationGrowth(String value) {
        setTrendOfWildBoarPopulationGrowth(value);
        return this;
    }

    public MooseDataCardPage7 withEstimatedSpecimenAmountOfWildBoar(Integer value) {
        setEstimatedSpecimenAmountOfWildBoar(value);
        return this;
    }

    public MooseDataCardPage7 withEstimatedAmountOfSowsWithPiglets(Integer value) {
        setEstimatedAmountOfSowsWithPiglets(value);
        return this;
    }

    @Override
    public MooseDataCardPage7 clone() {
        final MooseDataCardPage7 _newObject;
        try {
            _newObject = ((MooseDataCardPage7) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.section_7_1 = ((this.section_7_1 == null)?null:this.section_7_1 .clone());
        return _newObject;
    }

    @Override
    public MooseDataCardPage7 createCopy() {
        final MooseDataCardPage7 _newObject;
        try {
            _newObject = ((MooseDataCardPage7) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.section_7_1 = ((this.section_7_1 == null)?null:this.section_7_1 .createCopy());
        _newObject.huntingClubCode = this.huntingClubCode;
        _newObject.whiteTailedDeerAppeared = this.whiteTailedDeerAppeared;
        _newObject.trendOfWhiteTailedDeerPopulationGrowth = this.trendOfWhiteTailedDeerPopulationGrowth;
        _newObject.estimatedSpecimenAmountOfWhiteTailedDeer = this.estimatedSpecimenAmountOfWhiteTailedDeer;
        _newObject.roeDeerAppeared = this.roeDeerAppeared;
        _newObject.trendOfRoeDeerPopulationGrowth = this.trendOfRoeDeerPopulationGrowth;
        _newObject.estimatedSpecimenAmountOfRoeDeer = this.estimatedSpecimenAmountOfRoeDeer;
        _newObject.wildForestReindeerAppeared = this.wildForestReindeerAppeared;
        _newObject.trendOfWildForestReindeerPopulationGrowth = this.trendOfWildForestReindeerPopulationGrowth;
        _newObject.estimatedSpecimenAmountOfWildForestReindeer = this.estimatedSpecimenAmountOfWildForestReindeer;
        _newObject.fallowDeerAppeared = this.fallowDeerAppeared;
        _newObject.trendOfFallowDeerPopulationGrowth = this.trendOfFallowDeerPopulationGrowth;
        _newObject.estimatedSpecimenAmountOfFallowDeer = this.estimatedSpecimenAmountOfFallowDeer;
        _newObject.wildBoarAppeared = this.wildBoarAppeared;
        _newObject.trendOfWildBoarPopulationGrowth = this.trendOfWildBoarPopulationGrowth;
        _newObject.estimatedSpecimenAmountOfWildBoar = this.estimatedSpecimenAmountOfWildBoar;
        _newObject.estimatedAmountOfSowsWithPiglets = this.estimatedAmountOfSowsWithPiglets;
        return _newObject;
    }

    @Override
    public MooseDataCardPage7 createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardPage7 _newObject;
        try {
            _newObject = ((MooseDataCardPage7) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree section_7_1PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("section_7_1"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(section_7_1PropertyTree!= null):((section_7_1PropertyTree == null)||(!section_7_1PropertyTree.isLeaf())))) {
            _newObject.section_7_1 = ((this.section_7_1 == null)?null:this.section_7_1 .createCopy(section_7_1PropertyTree, _propertyTreeUse));
        }
        final PropertyTree huntingClubCodePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("huntingClubCode"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(huntingClubCodePropertyTree!= null):((huntingClubCodePropertyTree == null)||(!huntingClubCodePropertyTree.isLeaf())))) {
            _newObject.huntingClubCode = this.huntingClubCode;
        }
        final PropertyTree whiteTailedDeerAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("whiteTailedDeerAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(whiteTailedDeerAppearedPropertyTree!= null):((whiteTailedDeerAppearedPropertyTree == null)||(!whiteTailedDeerAppearedPropertyTree.isLeaf())))) {
            _newObject.whiteTailedDeerAppeared = this.whiteTailedDeerAppeared;
        }
        final PropertyTree trendOfWhiteTailedDeerPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfWhiteTailedDeerPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfWhiteTailedDeerPopulationGrowthPropertyTree!= null):((trendOfWhiteTailedDeerPopulationGrowthPropertyTree == null)||(!trendOfWhiteTailedDeerPopulationGrowthPropertyTree.isLeaf())))) {
            _newObject.trendOfWhiteTailedDeerPopulationGrowth = this.trendOfWhiteTailedDeerPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfWhiteTailedDeerPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfWhiteTailedDeer"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfWhiteTailedDeerPropertyTree!= null):((estimatedSpecimenAmountOfWhiteTailedDeerPropertyTree == null)||(!estimatedSpecimenAmountOfWhiteTailedDeerPropertyTree.isLeaf())))) {
            _newObject.estimatedSpecimenAmountOfWhiteTailedDeer = this.estimatedSpecimenAmountOfWhiteTailedDeer;
        }
        final PropertyTree roeDeerAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("roeDeerAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(roeDeerAppearedPropertyTree!= null):((roeDeerAppearedPropertyTree == null)||(!roeDeerAppearedPropertyTree.isLeaf())))) {
            _newObject.roeDeerAppeared = this.roeDeerAppeared;
        }
        final PropertyTree trendOfRoeDeerPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfRoeDeerPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfRoeDeerPopulationGrowthPropertyTree!= null):((trendOfRoeDeerPopulationGrowthPropertyTree == null)||(!trendOfRoeDeerPopulationGrowthPropertyTree.isLeaf())))) {
            _newObject.trendOfRoeDeerPopulationGrowth = this.trendOfRoeDeerPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfRoeDeerPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfRoeDeer"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfRoeDeerPropertyTree!= null):((estimatedSpecimenAmountOfRoeDeerPropertyTree == null)||(!estimatedSpecimenAmountOfRoeDeerPropertyTree.isLeaf())))) {
            _newObject.estimatedSpecimenAmountOfRoeDeer = this.estimatedSpecimenAmountOfRoeDeer;
        }
        final PropertyTree wildForestReindeerAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("wildForestReindeerAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(wildForestReindeerAppearedPropertyTree!= null):((wildForestReindeerAppearedPropertyTree == null)||(!wildForestReindeerAppearedPropertyTree.isLeaf())))) {
            _newObject.wildForestReindeerAppeared = this.wildForestReindeerAppeared;
        }
        final PropertyTree trendOfWildForestReindeerPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfWildForestReindeerPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfWildForestReindeerPopulationGrowthPropertyTree!= null):((trendOfWildForestReindeerPopulationGrowthPropertyTree == null)||(!trendOfWildForestReindeerPopulationGrowthPropertyTree.isLeaf())))) {
            _newObject.trendOfWildForestReindeerPopulationGrowth = this.trendOfWildForestReindeerPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfWildForestReindeerPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfWildForestReindeer"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfWildForestReindeerPropertyTree!= null):((estimatedSpecimenAmountOfWildForestReindeerPropertyTree == null)||(!estimatedSpecimenAmountOfWildForestReindeerPropertyTree.isLeaf())))) {
            _newObject.estimatedSpecimenAmountOfWildForestReindeer = this.estimatedSpecimenAmountOfWildForestReindeer;
        }
        final PropertyTree fallowDeerAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("fallowDeerAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(fallowDeerAppearedPropertyTree!= null):((fallowDeerAppearedPropertyTree == null)||(!fallowDeerAppearedPropertyTree.isLeaf())))) {
            _newObject.fallowDeerAppeared = this.fallowDeerAppeared;
        }
        final PropertyTree trendOfFallowDeerPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfFallowDeerPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfFallowDeerPopulationGrowthPropertyTree!= null):((trendOfFallowDeerPopulationGrowthPropertyTree == null)||(!trendOfFallowDeerPopulationGrowthPropertyTree.isLeaf())))) {
            _newObject.trendOfFallowDeerPopulationGrowth = this.trendOfFallowDeerPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfFallowDeerPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfFallowDeer"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfFallowDeerPropertyTree!= null):((estimatedSpecimenAmountOfFallowDeerPropertyTree == null)||(!estimatedSpecimenAmountOfFallowDeerPropertyTree.isLeaf())))) {
            _newObject.estimatedSpecimenAmountOfFallowDeer = this.estimatedSpecimenAmountOfFallowDeer;
        }
        final PropertyTree wildBoarAppearedPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("wildBoarAppeared"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(wildBoarAppearedPropertyTree!= null):((wildBoarAppearedPropertyTree == null)||(!wildBoarAppearedPropertyTree.isLeaf())))) {
            _newObject.wildBoarAppeared = this.wildBoarAppeared;
        }
        final PropertyTree trendOfWildBoarPopulationGrowthPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("trendOfWildBoarPopulationGrowth"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(trendOfWildBoarPopulationGrowthPropertyTree!= null):((trendOfWildBoarPopulationGrowthPropertyTree == null)||(!trendOfWildBoarPopulationGrowthPropertyTree.isLeaf())))) {
            _newObject.trendOfWildBoarPopulationGrowth = this.trendOfWildBoarPopulationGrowth;
        }
        final PropertyTree estimatedSpecimenAmountOfWildBoarPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedSpecimenAmountOfWildBoar"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedSpecimenAmountOfWildBoarPropertyTree!= null):((estimatedSpecimenAmountOfWildBoarPropertyTree == null)||(!estimatedSpecimenAmountOfWildBoarPropertyTree.isLeaf())))) {
            _newObject.estimatedSpecimenAmountOfWildBoar = this.estimatedSpecimenAmountOfWildBoar;
        }
        final PropertyTree estimatedAmountOfSowsWithPigletsPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("estimatedAmountOfSowsWithPiglets"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(estimatedAmountOfSowsWithPigletsPropertyTree!= null):((estimatedAmountOfSowsWithPigletsPropertyTree == null)||(!estimatedAmountOfSowsWithPigletsPropertyTree.isLeaf())))) {
            _newObject.estimatedAmountOfSowsWithPiglets = this.estimatedAmountOfSowsWithPiglets;
        }
        return _newObject;
    }

    @Override
    public MooseDataCardPage7 copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardPage7 copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardPage7 .Selector<MooseDataCardPage7 .Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardPage7 .Select _root() {
            return new MooseDataCardPage7 .Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private MooseDataCardSection_7_1 .Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> section_7_1 = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> huntingClubCode = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> whiteTailedDeerAppeared = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfWhiteTailedDeerPopulationGrowth = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfWhiteTailedDeer = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> roeDeerAppeared = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfRoeDeerPopulationGrowth = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfRoeDeer = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> wildForestReindeerAppeared = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfWildForestReindeerPopulationGrowth = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfWildForestReindeer = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> fallowDeerAppeared = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfFallowDeerPopulationGrowth = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfFallowDeer = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> wildBoarAppeared = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfWildBoarPopulationGrowth = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfWildBoar = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedAmountOfSowsWithPiglets = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.section_7_1 != null) {
                products.put("section_7_1", this.section_7_1 .init());
            }
            if (this.huntingClubCode!= null) {
                products.put("huntingClubCode", this.huntingClubCode.init());
            }
            if (this.whiteTailedDeerAppeared!= null) {
                products.put("whiteTailedDeerAppeared", this.whiteTailedDeerAppeared.init());
            }
            if (this.trendOfWhiteTailedDeerPopulationGrowth!= null) {
                products.put("trendOfWhiteTailedDeerPopulationGrowth", this.trendOfWhiteTailedDeerPopulationGrowth.init());
            }
            if (this.estimatedSpecimenAmountOfWhiteTailedDeer!= null) {
                products.put("estimatedSpecimenAmountOfWhiteTailedDeer", this.estimatedSpecimenAmountOfWhiteTailedDeer.init());
            }
            if (this.roeDeerAppeared!= null) {
                products.put("roeDeerAppeared", this.roeDeerAppeared.init());
            }
            if (this.trendOfRoeDeerPopulationGrowth!= null) {
                products.put("trendOfRoeDeerPopulationGrowth", this.trendOfRoeDeerPopulationGrowth.init());
            }
            if (this.estimatedSpecimenAmountOfRoeDeer!= null) {
                products.put("estimatedSpecimenAmountOfRoeDeer", this.estimatedSpecimenAmountOfRoeDeer.init());
            }
            if (this.wildForestReindeerAppeared!= null) {
                products.put("wildForestReindeerAppeared", this.wildForestReindeerAppeared.init());
            }
            if (this.trendOfWildForestReindeerPopulationGrowth!= null) {
                products.put("trendOfWildForestReindeerPopulationGrowth", this.trendOfWildForestReindeerPopulationGrowth.init());
            }
            if (this.estimatedSpecimenAmountOfWildForestReindeer!= null) {
                products.put("estimatedSpecimenAmountOfWildForestReindeer", this.estimatedSpecimenAmountOfWildForestReindeer.init());
            }
            if (this.fallowDeerAppeared!= null) {
                products.put("fallowDeerAppeared", this.fallowDeerAppeared.init());
            }
            if (this.trendOfFallowDeerPopulationGrowth!= null) {
                products.put("trendOfFallowDeerPopulationGrowth", this.trendOfFallowDeerPopulationGrowth.init());
            }
            if (this.estimatedSpecimenAmountOfFallowDeer!= null) {
                products.put("estimatedSpecimenAmountOfFallowDeer", this.estimatedSpecimenAmountOfFallowDeer.init());
            }
            if (this.wildBoarAppeared!= null) {
                products.put("wildBoarAppeared", this.wildBoarAppeared.init());
            }
            if (this.trendOfWildBoarPopulationGrowth!= null) {
                products.put("trendOfWildBoarPopulationGrowth", this.trendOfWildBoarPopulationGrowth.init());
            }
            if (this.estimatedSpecimenAmountOfWildBoar!= null) {
                products.put("estimatedSpecimenAmountOfWildBoar", this.estimatedSpecimenAmountOfWildBoar.init());
            }
            if (this.estimatedAmountOfSowsWithPiglets!= null) {
                products.put("estimatedAmountOfSowsWithPiglets", this.estimatedAmountOfSowsWithPiglets.init());
            }
            return products;
        }

        public MooseDataCardSection_7_1 .Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> section_7_1() {
            return ((this.section_7_1 == null)?this.section_7_1 = new MooseDataCardSection_7_1 .Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "section_7_1"):this.section_7_1);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> huntingClubCode() {
            return ((this.huntingClubCode == null)?this.huntingClubCode = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "huntingClubCode"):this.huntingClubCode);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> whiteTailedDeerAppeared() {
            return ((this.whiteTailedDeerAppeared == null)?this.whiteTailedDeerAppeared = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "whiteTailedDeerAppeared"):this.whiteTailedDeerAppeared);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfWhiteTailedDeerPopulationGrowth() {
            return ((this.trendOfWhiteTailedDeerPopulationGrowth == null)?this.trendOfWhiteTailedDeerPopulationGrowth = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "trendOfWhiteTailedDeerPopulationGrowth"):this.trendOfWhiteTailedDeerPopulationGrowth);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfWhiteTailedDeer() {
            return ((this.estimatedSpecimenAmountOfWhiteTailedDeer == null)?this.estimatedSpecimenAmountOfWhiteTailedDeer = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "estimatedSpecimenAmountOfWhiteTailedDeer"):this.estimatedSpecimenAmountOfWhiteTailedDeer);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> roeDeerAppeared() {
            return ((this.roeDeerAppeared == null)?this.roeDeerAppeared = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "roeDeerAppeared"):this.roeDeerAppeared);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfRoeDeerPopulationGrowth() {
            return ((this.trendOfRoeDeerPopulationGrowth == null)?this.trendOfRoeDeerPopulationGrowth = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "trendOfRoeDeerPopulationGrowth"):this.trendOfRoeDeerPopulationGrowth);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfRoeDeer() {
            return ((this.estimatedSpecimenAmountOfRoeDeer == null)?this.estimatedSpecimenAmountOfRoeDeer = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "estimatedSpecimenAmountOfRoeDeer"):this.estimatedSpecimenAmountOfRoeDeer);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> wildForestReindeerAppeared() {
            return ((this.wildForestReindeerAppeared == null)?this.wildForestReindeerAppeared = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "wildForestReindeerAppeared"):this.wildForestReindeerAppeared);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfWildForestReindeerPopulationGrowth() {
            return ((this.trendOfWildForestReindeerPopulationGrowth == null)?this.trendOfWildForestReindeerPopulationGrowth = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "trendOfWildForestReindeerPopulationGrowth"):this.trendOfWildForestReindeerPopulationGrowth);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfWildForestReindeer() {
            return ((this.estimatedSpecimenAmountOfWildForestReindeer == null)?this.estimatedSpecimenAmountOfWildForestReindeer = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "estimatedSpecimenAmountOfWildForestReindeer"):this.estimatedSpecimenAmountOfWildForestReindeer);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> fallowDeerAppeared() {
            return ((this.fallowDeerAppeared == null)?this.fallowDeerAppeared = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "fallowDeerAppeared"):this.fallowDeerAppeared);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfFallowDeerPopulationGrowth() {
            return ((this.trendOfFallowDeerPopulationGrowth == null)?this.trendOfFallowDeerPopulationGrowth = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "trendOfFallowDeerPopulationGrowth"):this.trendOfFallowDeerPopulationGrowth);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfFallowDeer() {
            return ((this.estimatedSpecimenAmountOfFallowDeer == null)?this.estimatedSpecimenAmountOfFallowDeer = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "estimatedSpecimenAmountOfFallowDeer"):this.estimatedSpecimenAmountOfFallowDeer);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> wildBoarAppeared() {
            return ((this.wildBoarAppeared == null)?this.wildBoarAppeared = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "wildBoarAppeared"):this.wildBoarAppeared);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> trendOfWildBoarPopulationGrowth() {
            return ((this.trendOfWildBoarPopulationGrowth == null)?this.trendOfWildBoarPopulationGrowth = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "trendOfWildBoarPopulationGrowth"):this.trendOfWildBoarPopulationGrowth);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedSpecimenAmountOfWildBoar() {
            return ((this.estimatedSpecimenAmountOfWildBoar == null)?this.estimatedSpecimenAmountOfWildBoar = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "estimatedSpecimenAmountOfWildBoar"):this.estimatedSpecimenAmountOfWildBoar);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>> estimatedAmountOfSowsWithPiglets() {
            return ((this.estimatedAmountOfSowsWithPiglets == null)?this.estimatedAmountOfSowsWithPiglets = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardPage7 .Selector<TRoot, TParent>>(this._root, this, "estimatedAmountOfSowsWithPiglets"):this.estimatedAmountOfSowsWithPiglets);
        }

    }

}
