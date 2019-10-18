
package fi.riista.integration.habides.export.derogations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.HashCode2;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString2;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="species" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="speciesGroup" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="coversAllSpecies" type="{}nullableBoolean"/&gt;
 *         &lt;element name="sensitive" type="{}nullableBoolean"/&gt;
 *         &lt;element name="licenseValidFrom" type="{}nullableDate"/&gt;
 *         &lt;element name="licenseValidUntil" type="{}nullableDate"/&gt;
 *         &lt;element name="licensingAuthority" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element ref="{}regions"/&gt;
 *         &lt;element name="location" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="derogationJustifications" type="{}derogationJustificationsType"/&gt;
 *         &lt;element ref="{}reasons"/&gt;
 *         &lt;element name="derogationJustificationDetails" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="strictlySupervisedConditions" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="selectiveBasis" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="smallNumberIndividuals" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element ref="{}activities"/&gt;
 *         &lt;element ref="{}additionalActivities"/&gt;
 *         &lt;element name="activitiesFurtherDetails" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element ref="{}methods"/&gt;
 *         &lt;element name="furtherDetails" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="modesOfTransport" type="{}modesOfTransportType"/&gt;
 *         &lt;element name="licensed" type="{}derogationEntity"/&gt;
 *         &lt;element name="actuallyTaken" type="{}derogationEntity"/&gt;
 *         &lt;element name="allMeasuresTaken" type="{}nullableBoolean"/&gt;
 *         &lt;element name="EUAllMeasuresTaken" type="{}nullableBoolean"/&gt;
 *         &lt;element name="detrimentalToPopulation" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="alternativeToDerogation" type="{}nullableBoolean"/&gt;
 *         &lt;element name="alternativesAssessed" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="supervisoryMeasure" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="comments" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="country" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="directive" type="{}directiveType" /&gt;
 *       &lt;attribute name="derogation_reference" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="user_derogation_ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="userIdentity" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="status" type="{}statusType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "species",
    "speciesGroup",
    "coversAllSpecies",
    "sensitive",
    "licenseValidFrom",
    "licenseValidUntil",
    "licensingAuthority",
    "regions",
    "location",
    "derogationJustifications",
    "reasons",
    "derogationJustificationDetails",
    "strictlySupervisedConditions",
    "selectiveBasis",
    "smallNumberIndividuals",
    "activities",
    "additionalActivities",
    "activitiesFurtherDetails",
    "methods",
    "furtherDetails",
    "modesOfTransport",
    "licensed",
    "actuallyTaken",
    "allMeasuresTaken",
    "euAllMeasuresTaken",
    "detrimentalToPopulation",
    "alternativeToDerogation",
    "alternativesAssessed",
    "supervisoryMeasure",
    "comments"
})
@XmlRootElement(name = "derogation")
public class DERO_Derogation implements Equals2, HashCode2, ToString2
{

    @XmlElement(required = true)
    protected String species;
    @XmlElement(required = true)
    protected String speciesGroup;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String coversAllSpecies;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String sensitive;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String licenseValidFrom;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String licenseValidUntil;
    @XmlElement(required = true)
    protected String licensingAuthority;
    @XmlElement(required = true)
    protected DERO_Regions regions;
    @XmlElement(required = true)
    protected String location;
    @XmlElement(required = true)
    protected DERO_DerogationJustificationsType derogationJustifications;
    @XmlElement(required = true)
    protected DERO_Reasons reasons;
    @XmlElement(required = true)
    protected String derogationJustificationDetails;
    @XmlElement(required = true)
    protected String strictlySupervisedConditions;
    @XmlElement(required = true)
    protected String selectiveBasis;
    @XmlElement(required = true)
    protected String smallNumberIndividuals;
    @XmlElement(required = true)
    protected DERO_Activities activities;
    @XmlElement(required = true)
    protected DERO_AdditionalActivities additionalActivities;
    @XmlElement(required = true)
    protected String activitiesFurtherDetails;
    @XmlElement(required = true)
    protected DERO_Methods methods;
    @XmlElement(required = true)
    protected String furtherDetails;
    @XmlElement(required = true)
    protected DERO_ModesOfTransportType modesOfTransport;
    @XmlElement(required = true)
    protected DERO_DerogationEntity licensed;
    @XmlElement(required = true)
    protected DERO_DerogationEntity actuallyTaken;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String allMeasuresTaken;
    @XmlElement(name = "EUAllMeasuresTaken", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String euAllMeasuresTaken;
    @XmlElement(required = true)
    protected String detrimentalToPopulation;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String alternativeToDerogation;
    @XmlElement(required = true)
    protected String alternativesAssessed;
    @XmlElement(required = true)
    protected String supervisoryMeasure;
    @XmlElement(required = true)
    protected String comments;
    @XmlAttribute(name = "country")
    protected String country;
    @XmlAttribute(name = "directive")
    protected DERO_DirectiveType directive;
    @XmlAttribute(name = "derogation_reference")
    protected String derogationReference;
    @XmlAttribute(name = "user_derogation_ref")
    protected String userDerogationRef;
    @XmlAttribute(name = "userIdentity")
    protected String userIdentity;
    @XmlAttribute(name = "status")
    protected String status;

    /**
     * Gets the value of the species property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Sets the value of the species property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecies(String value) {
        this.species = value;
    }

    /**
     * Gets the value of the speciesGroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpeciesGroup() {
        return speciesGroup;
    }

    /**
     * Sets the value of the speciesGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpeciesGroup(String value) {
        this.speciesGroup = value;
    }

    /**
     * Gets the value of the coversAllSpecies property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoversAllSpecies() {
        return coversAllSpecies;
    }

    /**
     * Sets the value of the coversAllSpecies property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoversAllSpecies(String value) {
        this.coversAllSpecies = value;
    }

    /**
     * Gets the value of the sensitive property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSensitive() {
        return sensitive;
    }

    /**
     * Sets the value of the sensitive property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSensitive(String value) {
        this.sensitive = value;
    }

    /**
     * Gets the value of the licenseValidFrom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLicenseValidFrom() {
        return licenseValidFrom;
    }

    /**
     * Sets the value of the licenseValidFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLicenseValidFrom(String value) {
        this.licenseValidFrom = value;
    }

    /**
     * Gets the value of the licenseValidUntil property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLicenseValidUntil() {
        return licenseValidUntil;
    }

    /**
     * Sets the value of the licenseValidUntil property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLicenseValidUntil(String value) {
        this.licenseValidUntil = value;
    }

    /**
     * Gets the value of the licensingAuthority property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLicensingAuthority() {
        return licensingAuthority;
    }

    /**
     * Sets the value of the licensingAuthority property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLicensingAuthority(String value) {
        this.licensingAuthority = value;
    }

    /**
     * 
     *                             regions(s) - If the derogation only applies to certain parts of a country, please indicate
     *                             all NUTS 2 level
     *                             regions of the Country where the derogation is applicable. Allowed NUTS 2 level regions are
     *                             defined at:
     *                             http://dd.eionet.europa.eu/vocabulary/common/nuts/view
     *                         
     * 
     * @return
     *     possible object is
     *     {@link DERO_Regions }
     *     
     */
    public DERO_Regions getRegions() {
        return regions;
    }

    /**
     * Sets the value of the regions property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_Regions }
     *     
     */
    public void setRegions(DERO_Regions value) {
        this.regions = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the derogationJustifications property.
     * 
     * @return
     *     possible object is
     *     {@link DERO_DerogationJustificationsType }
     *     
     */
    public DERO_DerogationJustificationsType getDerogationJustifications() {
        return derogationJustifications;
    }

    /**
     * Sets the value of the derogationJustifications property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_DerogationJustificationsType }
     *     
     */
    public void setDerogationJustifications(DERO_DerogationJustificationsType value) {
        this.derogationJustifications = value;
    }

    /**
     * legal justification for granting the derogation - selection box with values
     *                             defined at:
     *                             - Birds directive: http://dd.eionet.europa.eu/vocabulary/habides/birdslegalbasis/view
     *                             - Habitats directive: http://dd.eionet.europa.eu/vocabulary/habides/habitatslegalbasis/view
     *                         
     * 
     * @return
     *     possible object is
     *     {@link DERO_Reasons }
     *     
     */
    public DERO_Reasons getReasons() {
        return reasons;
    }

    /**
     * Sets the value of the reasons property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_Reasons }
     *     
     */
    public void setReasons(DERO_Reasons value) {
        this.reasons = value;
    }

    /**
     * Gets the value of the derogationJustificationDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDerogationJustificationDetails() {
        return derogationJustificationDetails;
    }

    /**
     * Sets the value of the derogationJustificationDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDerogationJustificationDetails(String value) {
        this.derogationJustificationDetails = value;
    }

    /**
     * Gets the value of the strictlySupervisedConditions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrictlySupervisedConditions() {
        return strictlySupervisedConditions;
    }

    /**
     * Sets the value of the strictlySupervisedConditions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrictlySupervisedConditions(String value) {
        this.strictlySupervisedConditions = value;
    }

    /**
     * Gets the value of the selectiveBasis property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSelectiveBasis() {
        return selectiveBasis;
    }

    /**
     * Sets the value of the selectiveBasis property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSelectiveBasis(String value) {
        this.selectiveBasis = value;
    }

    /**
     * Gets the value of the smallNumberIndividuals property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmallNumberIndividuals() {
        return smallNumberIndividuals;
    }

    /**
     * Sets the value of the smallNumberIndividuals property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmallNumberIndividuals(String value) {
        this.smallNumberIndividuals = value;
    }

    /**
     * main activity covered by the derogation - selection box with values defined
     *                             at:
     *                             - Birds directive: http://dd.eionet.europa.eu/vocabulary/habides/birdsmainactivities/view
     *                             - Habitats directive:
     *                             http://dd.eionet.europa.eu/vocabulary/habides/habitatsmainactivities/view
     *                         
     * 
     * @return
     *     possible object is
     *     {@link DERO_Activities }
     *     
     */
    public DERO_Activities getActivities() {
        return activities;
    }

    /**
     * Sets the value of the activities property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_Activities }
     *     
     */
    public void setActivities(DERO_Activities value) {
        this.activities = value;
    }

    /**
     * additional activities covered by the derogation (optional) - selection box
     *                             with values defined at:
     *                             - Birds directive: http://dd.eionet.europa.eu/vocabulary/habides/birdsmainactivities/view
     *                             - Habitats directive:
     *                             http://dd.eionet.europa.eu/vocabulary/habides/habitatsmainactivities/view
     *                         
     * 
     * @return
     *     possible object is
     *     {@link DERO_AdditionalActivities }
     *     
     */
    public DERO_AdditionalActivities getAdditionalActivities() {
        return additionalActivities;
    }

    /**
     * Sets the value of the additionalActivities property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_AdditionalActivities }
     *     
     */
    public void setAdditionalActivities(DERO_AdditionalActivities value) {
        this.additionalActivities = value;
    }

    /**
     * Gets the value of the activitiesFurtherDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivitiesFurtherDetails() {
        return activitiesFurtherDetails;
    }

    /**
     * Sets the value of the activitiesFurtherDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivitiesFurtherDetails(String value) {
        this.activitiesFurtherDetails = value;
    }

    /**
     * mean, arrangement or method covered by the derogation - selection box with
     *                             values defined at:
     *                             - Birds directive: http://dd.eionet.europa.eu/vocabulary/habides/birdsmethods/view
     *                             - Habitats directive: http://dd.eionet.europa.eu/vocabulary/habides/habitatsmethods/view
     *                         
     * 
     * @return
     *     possible object is
     *     {@link DERO_Methods }
     *     
     */
    public DERO_Methods getMethods() {
        return methods;
    }

    /**
     * Sets the value of the methods property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_Methods }
     *     
     */
    public void setMethods(DERO_Methods value) {
        this.methods = value;
    }

    /**
     * Gets the value of the furtherDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFurtherDetails() {
        return furtherDetails;
    }

    /**
     * Sets the value of the furtherDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFurtherDetails(String value) {
        this.furtherDetails = value;
    }

    /**
     * Gets the value of the modesOfTransport property.
     * 
     * @return
     *     possible object is
     *     {@link DERO_ModesOfTransportType }
     *     
     */
    public DERO_ModesOfTransportType getModesOfTransport() {
        return modesOfTransport;
    }

    /**
     * Sets the value of the modesOfTransport property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_ModesOfTransportType }
     *     
     */
    public void setModesOfTransport(DERO_ModesOfTransportType value) {
        this.modesOfTransport = value;
    }

    /**
     * Gets the value of the licensed property.
     * 
     * @return
     *     possible object is
     *     {@link DERO_DerogationEntity }
     *     
     */
    public DERO_DerogationEntity getLicensed() {
        return licensed;
    }

    /**
     * Sets the value of the licensed property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_DerogationEntity }
     *     
     */
    public void setLicensed(DERO_DerogationEntity value) {
        this.licensed = value;
    }

    /**
     * Gets the value of the actuallyTaken property.
     * 
     * @return
     *     possible object is
     *     {@link DERO_DerogationEntity }
     *     
     */
    public DERO_DerogationEntity getActuallyTaken() {
        return actuallyTaken;
    }

    /**
     * Sets the value of the actuallyTaken property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_DerogationEntity }
     *     
     */
    public void setActuallyTaken(DERO_DerogationEntity value) {
        this.actuallyTaken = value;
    }

    /**
     * Gets the value of the allMeasuresTaken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllMeasuresTaken() {
        return allMeasuresTaken;
    }

    /**
     * Sets the value of the allMeasuresTaken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllMeasuresTaken(String value) {
        this.allMeasuresTaken = value;
    }

    /**
     * Gets the value of the euAllMeasuresTaken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEUAllMeasuresTaken() {
        return euAllMeasuresTaken;
    }

    /**
     * Sets the value of the euAllMeasuresTaken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEUAllMeasuresTaken(String value) {
        this.euAllMeasuresTaken = value;
    }

    /**
     * Gets the value of the detrimentalToPopulation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDetrimentalToPopulation() {
        return detrimentalToPopulation;
    }

    /**
     * Sets the value of the detrimentalToPopulation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDetrimentalToPopulation(String value) {
        this.detrimentalToPopulation = value;
    }

    /**
     * Gets the value of the alternativeToDerogation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlternativeToDerogation() {
        return alternativeToDerogation;
    }

    /**
     * Sets the value of the alternativeToDerogation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlternativeToDerogation(String value) {
        this.alternativeToDerogation = value;
    }

    /**
     * Gets the value of the alternativesAssessed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlternativesAssessed() {
        return alternativesAssessed;
    }

    /**
     * Sets the value of the alternativesAssessed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlternativesAssessed(String value) {
        this.alternativesAssessed = value;
    }

    /**
     * Gets the value of the supervisoryMeasure property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupervisoryMeasure() {
        return supervisoryMeasure;
    }

    /**
     * Sets the value of the supervisoryMeasure property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupervisoryMeasure(String value) {
        this.supervisoryMeasure = value;
    }

    /**
     * Gets the value of the comments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the value of the comments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComments(String value) {
        this.comments = value;
    }

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the directive property.
     * 
     * @return
     *     possible object is
     *     {@link DERO_DirectiveType }
     *     
     */
    public DERO_DirectiveType getDirective() {
        return directive;
    }

    /**
     * Sets the value of the directive property.
     * 
     * @param value
     *     allowed object is
     *     {@link DERO_DirectiveType }
     *     
     */
    public void setDirective(DERO_DirectiveType value) {
        this.directive = value;
    }

    /**
     * Gets the value of the derogationReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDerogationReference() {
        return derogationReference;
    }

    /**
     * Sets the value of the derogationReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDerogationReference(String value) {
        this.derogationReference = value;
    }

    /**
     * Gets the value of the userDerogationRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserDerogationRef() {
        return userDerogationRef;
    }

    /**
     * Sets the value of the userDerogationRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserDerogationRef(String value) {
        this.userDerogationRef = value;
    }

    /**
     * Gets the value of the userIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserIdentity() {
        return userIdentity;
    }

    /**
     * Sets the value of the userIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserIdentity(String value) {
        this.userIdentity = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    public DERO_Derogation withSpecies(String value) {
        setSpecies(value);
        return this;
    }

    public DERO_Derogation withSpeciesGroup(String value) {
        setSpeciesGroup(value);
        return this;
    }

    public DERO_Derogation withCoversAllSpecies(String value) {
        setCoversAllSpecies(value);
        return this;
    }

    public DERO_Derogation withSensitive(String value) {
        setSensitive(value);
        return this;
    }

    public DERO_Derogation withLicenseValidFrom(String value) {
        setLicenseValidFrom(value);
        return this;
    }

    public DERO_Derogation withLicenseValidUntil(String value) {
        setLicenseValidUntil(value);
        return this;
    }

    public DERO_Derogation withLicensingAuthority(String value) {
        setLicensingAuthority(value);
        return this;
    }

    public DERO_Derogation withRegions(DERO_Regions value) {
        setRegions(value);
        return this;
    }

    public DERO_Derogation withLocation(String value) {
        setLocation(value);
        return this;
    }

    public DERO_Derogation withDerogationJustifications(DERO_DerogationJustificationsType value) {
        setDerogationJustifications(value);
        return this;
    }

    public DERO_Derogation withReasons(DERO_Reasons value) {
        setReasons(value);
        return this;
    }

    public DERO_Derogation withDerogationJustificationDetails(String value) {
        setDerogationJustificationDetails(value);
        return this;
    }

    public DERO_Derogation withStrictlySupervisedConditions(String value) {
        setStrictlySupervisedConditions(value);
        return this;
    }

    public DERO_Derogation withSelectiveBasis(String value) {
        setSelectiveBasis(value);
        return this;
    }

    public DERO_Derogation withSmallNumberIndividuals(String value) {
        setSmallNumberIndividuals(value);
        return this;
    }

    public DERO_Derogation withActivities(DERO_Activities value) {
        setActivities(value);
        return this;
    }

    public DERO_Derogation withAdditionalActivities(DERO_AdditionalActivities value) {
        setAdditionalActivities(value);
        return this;
    }

    public DERO_Derogation withActivitiesFurtherDetails(String value) {
        setActivitiesFurtherDetails(value);
        return this;
    }

    public DERO_Derogation withMethods(DERO_Methods value) {
        setMethods(value);
        return this;
    }

    public DERO_Derogation withFurtherDetails(String value) {
        setFurtherDetails(value);
        return this;
    }

    public DERO_Derogation withModesOfTransport(DERO_ModesOfTransportType value) {
        setModesOfTransport(value);
        return this;
    }

    public DERO_Derogation withLicensed(DERO_DerogationEntity value) {
        setLicensed(value);
        return this;
    }

    public DERO_Derogation withActuallyTaken(DERO_DerogationEntity value) {
        setActuallyTaken(value);
        return this;
    }

    public DERO_Derogation withAllMeasuresTaken(String value) {
        setAllMeasuresTaken(value);
        return this;
    }

    public DERO_Derogation withEUAllMeasuresTaken(String value) {
        setEUAllMeasuresTaken(value);
        return this;
    }

    public DERO_Derogation withDetrimentalToPopulation(String value) {
        setDetrimentalToPopulation(value);
        return this;
    }

    public DERO_Derogation withAlternativeToDerogation(String value) {
        setAlternativeToDerogation(value);
        return this;
    }

    public DERO_Derogation withAlternativesAssessed(String value) {
        setAlternativesAssessed(value);
        return this;
    }

    public DERO_Derogation withSupervisoryMeasure(String value) {
        setSupervisoryMeasure(value);
        return this;
    }

    public DERO_Derogation withComments(String value) {
        setComments(value);
        return this;
    }

    public DERO_Derogation withCountry(String value) {
        setCountry(value);
        return this;
    }

    public DERO_Derogation withDirective(DERO_DirectiveType value) {
        setDirective(value);
        return this;
    }

    public DERO_Derogation withDerogationReference(String value) {
        setDerogationReference(value);
        return this;
    }

    public DERO_Derogation withUserDerogationRef(String value) {
        setUserDerogationRef(value);
        return this;
    }

    public DERO_Derogation withUserIdentity(String value) {
        setUserIdentity(value);
        return this;
    }

    public DERO_Derogation withStatus(String value) {
        setStatus(value);
        return this;
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
            String theSpecies;
            theSpecies = this.getSpecies();
            strategy.appendField(locator, this, "species", buffer, theSpecies, (this.species!= null));
        }
        {
            String theSpeciesGroup;
            theSpeciesGroup = this.getSpeciesGroup();
            strategy.appendField(locator, this, "speciesGroup", buffer, theSpeciesGroup, (this.speciesGroup!= null));
        }
        {
            String theCoversAllSpecies;
            theCoversAllSpecies = this.getCoversAllSpecies();
            strategy.appendField(locator, this, "coversAllSpecies", buffer, theCoversAllSpecies, (this.coversAllSpecies!= null));
        }
        {
            String theSensitive;
            theSensitive = this.getSensitive();
            strategy.appendField(locator, this, "sensitive", buffer, theSensitive, (this.sensitive!= null));
        }
        {
            String theLicenseValidFrom;
            theLicenseValidFrom = this.getLicenseValidFrom();
            strategy.appendField(locator, this, "licenseValidFrom", buffer, theLicenseValidFrom, (this.licenseValidFrom!= null));
        }
        {
            String theLicenseValidUntil;
            theLicenseValidUntil = this.getLicenseValidUntil();
            strategy.appendField(locator, this, "licenseValidUntil", buffer, theLicenseValidUntil, (this.licenseValidUntil!= null));
        }
        {
            String theLicensingAuthority;
            theLicensingAuthority = this.getLicensingAuthority();
            strategy.appendField(locator, this, "licensingAuthority", buffer, theLicensingAuthority, (this.licensingAuthority!= null));
        }
        {
            DERO_Regions theRegions;
            theRegions = this.getRegions();
            strategy.appendField(locator, this, "regions", buffer, theRegions, (this.regions!= null));
        }
        {
            String theLocation;
            theLocation = this.getLocation();
            strategy.appendField(locator, this, "location", buffer, theLocation, (this.location!= null));
        }
        {
            DERO_DerogationJustificationsType theDerogationJustifications;
            theDerogationJustifications = this.getDerogationJustifications();
            strategy.appendField(locator, this, "derogationJustifications", buffer, theDerogationJustifications, (this.derogationJustifications!= null));
        }
        {
            DERO_Reasons theReasons;
            theReasons = this.getReasons();
            strategy.appendField(locator, this, "reasons", buffer, theReasons, (this.reasons!= null));
        }
        {
            String theDerogationJustificationDetails;
            theDerogationJustificationDetails = this.getDerogationJustificationDetails();
            strategy.appendField(locator, this, "derogationJustificationDetails", buffer, theDerogationJustificationDetails, (this.derogationJustificationDetails!= null));
        }
        {
            String theStrictlySupervisedConditions;
            theStrictlySupervisedConditions = this.getStrictlySupervisedConditions();
            strategy.appendField(locator, this, "strictlySupervisedConditions", buffer, theStrictlySupervisedConditions, (this.strictlySupervisedConditions!= null));
        }
        {
            String theSelectiveBasis;
            theSelectiveBasis = this.getSelectiveBasis();
            strategy.appendField(locator, this, "selectiveBasis", buffer, theSelectiveBasis, (this.selectiveBasis!= null));
        }
        {
            String theSmallNumberIndividuals;
            theSmallNumberIndividuals = this.getSmallNumberIndividuals();
            strategy.appendField(locator, this, "smallNumberIndividuals", buffer, theSmallNumberIndividuals, (this.smallNumberIndividuals!= null));
        }
        {
            DERO_Activities theActivities;
            theActivities = this.getActivities();
            strategy.appendField(locator, this, "activities", buffer, theActivities, (this.activities!= null));
        }
        {
            DERO_AdditionalActivities theAdditionalActivities;
            theAdditionalActivities = this.getAdditionalActivities();
            strategy.appendField(locator, this, "additionalActivities", buffer, theAdditionalActivities, (this.additionalActivities!= null));
        }
        {
            String theActivitiesFurtherDetails;
            theActivitiesFurtherDetails = this.getActivitiesFurtherDetails();
            strategy.appendField(locator, this, "activitiesFurtherDetails", buffer, theActivitiesFurtherDetails, (this.activitiesFurtherDetails!= null));
        }
        {
            DERO_Methods theMethods;
            theMethods = this.getMethods();
            strategy.appendField(locator, this, "methods", buffer, theMethods, (this.methods!= null));
        }
        {
            String theFurtherDetails;
            theFurtherDetails = this.getFurtherDetails();
            strategy.appendField(locator, this, "furtherDetails", buffer, theFurtherDetails, (this.furtherDetails!= null));
        }
        {
            DERO_ModesOfTransportType theModesOfTransport;
            theModesOfTransport = this.getModesOfTransport();
            strategy.appendField(locator, this, "modesOfTransport", buffer, theModesOfTransport, (this.modesOfTransport!= null));
        }
        {
            DERO_DerogationEntity theLicensed;
            theLicensed = this.getLicensed();
            strategy.appendField(locator, this, "licensed", buffer, theLicensed, (this.licensed!= null));
        }
        {
            DERO_DerogationEntity theActuallyTaken;
            theActuallyTaken = this.getActuallyTaken();
            strategy.appendField(locator, this, "actuallyTaken", buffer, theActuallyTaken, (this.actuallyTaken!= null));
        }
        {
            String theAllMeasuresTaken;
            theAllMeasuresTaken = this.getAllMeasuresTaken();
            strategy.appendField(locator, this, "allMeasuresTaken", buffer, theAllMeasuresTaken, (this.allMeasuresTaken!= null));
        }
        {
            String theEUAllMeasuresTaken;
            theEUAllMeasuresTaken = this.getEUAllMeasuresTaken();
            strategy.appendField(locator, this, "euAllMeasuresTaken", buffer, theEUAllMeasuresTaken, (this.euAllMeasuresTaken!= null));
        }
        {
            String theDetrimentalToPopulation;
            theDetrimentalToPopulation = this.getDetrimentalToPopulation();
            strategy.appendField(locator, this, "detrimentalToPopulation", buffer, theDetrimentalToPopulation, (this.detrimentalToPopulation!= null));
        }
        {
            String theAlternativeToDerogation;
            theAlternativeToDerogation = this.getAlternativeToDerogation();
            strategy.appendField(locator, this, "alternativeToDerogation", buffer, theAlternativeToDerogation, (this.alternativeToDerogation!= null));
        }
        {
            String theAlternativesAssessed;
            theAlternativesAssessed = this.getAlternativesAssessed();
            strategy.appendField(locator, this, "alternativesAssessed", buffer, theAlternativesAssessed, (this.alternativesAssessed!= null));
        }
        {
            String theSupervisoryMeasure;
            theSupervisoryMeasure = this.getSupervisoryMeasure();
            strategy.appendField(locator, this, "supervisoryMeasure", buffer, theSupervisoryMeasure, (this.supervisoryMeasure!= null));
        }
        {
            String theComments;
            theComments = this.getComments();
            strategy.appendField(locator, this, "comments", buffer, theComments, (this.comments!= null));
        }
        {
            String theCountry;
            theCountry = this.getCountry();
            strategy.appendField(locator, this, "country", buffer, theCountry, (this.country!= null));
        }
        {
            DERO_DirectiveType theDirective;
            theDirective = this.getDirective();
            strategy.appendField(locator, this, "directive", buffer, theDirective, (this.directive!= null));
        }
        {
            String theDerogationReference;
            theDerogationReference = this.getDerogationReference();
            strategy.appendField(locator, this, "derogationReference", buffer, theDerogationReference, (this.derogationReference!= null));
        }
        {
            String theUserDerogationRef;
            theUserDerogationRef = this.getUserDerogationRef();
            strategy.appendField(locator, this, "userDerogationRef", buffer, theUserDerogationRef, (this.userDerogationRef!= null));
        }
        {
            String theUserIdentity;
            theUserIdentity = this.getUserIdentity();
            strategy.appendField(locator, this, "userIdentity", buffer, theUserIdentity, (this.userIdentity!= null));
        }
        {
            String theStatus;
            theStatus = this.getStatus();
            strategy.appendField(locator, this, "status", buffer, theStatus, (this.status!= null));
        }
        return buffer;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final DERO_Derogation that = ((DERO_Derogation) object);
        {
            String lhsSpecies;
            lhsSpecies = this.getSpecies();
            String rhsSpecies;
            rhsSpecies = that.getSpecies();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "species", lhsSpecies), LocatorUtils.property(thatLocator, "species", rhsSpecies), lhsSpecies, rhsSpecies, (this.species!= null), (that.species!= null))) {
                return false;
            }
        }
        {
            String lhsSpeciesGroup;
            lhsSpeciesGroup = this.getSpeciesGroup();
            String rhsSpeciesGroup;
            rhsSpeciesGroup = that.getSpeciesGroup();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "speciesGroup", lhsSpeciesGroup), LocatorUtils.property(thatLocator, "speciesGroup", rhsSpeciesGroup), lhsSpeciesGroup, rhsSpeciesGroup, (this.speciesGroup!= null), (that.speciesGroup!= null))) {
                return false;
            }
        }
        {
            String lhsCoversAllSpecies;
            lhsCoversAllSpecies = this.getCoversAllSpecies();
            String rhsCoversAllSpecies;
            rhsCoversAllSpecies = that.getCoversAllSpecies();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "coversAllSpecies", lhsCoversAllSpecies), LocatorUtils.property(thatLocator, "coversAllSpecies", rhsCoversAllSpecies), lhsCoversAllSpecies, rhsCoversAllSpecies, (this.coversAllSpecies!= null), (that.coversAllSpecies!= null))) {
                return false;
            }
        }
        {
            String lhsSensitive;
            lhsSensitive = this.getSensitive();
            String rhsSensitive;
            rhsSensitive = that.getSensitive();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "sensitive", lhsSensitive), LocatorUtils.property(thatLocator, "sensitive", rhsSensitive), lhsSensitive, rhsSensitive, (this.sensitive!= null), (that.sensitive!= null))) {
                return false;
            }
        }
        {
            String lhsLicenseValidFrom;
            lhsLicenseValidFrom = this.getLicenseValidFrom();
            String rhsLicenseValidFrom;
            rhsLicenseValidFrom = that.getLicenseValidFrom();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "licenseValidFrom", lhsLicenseValidFrom), LocatorUtils.property(thatLocator, "licenseValidFrom", rhsLicenseValidFrom), lhsLicenseValidFrom, rhsLicenseValidFrom, (this.licenseValidFrom!= null), (that.licenseValidFrom!= null))) {
                return false;
            }
        }
        {
            String lhsLicenseValidUntil;
            lhsLicenseValidUntil = this.getLicenseValidUntil();
            String rhsLicenseValidUntil;
            rhsLicenseValidUntil = that.getLicenseValidUntil();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "licenseValidUntil", lhsLicenseValidUntil), LocatorUtils.property(thatLocator, "licenseValidUntil", rhsLicenseValidUntil), lhsLicenseValidUntil, rhsLicenseValidUntil, (this.licenseValidUntil!= null), (that.licenseValidUntil!= null))) {
                return false;
            }
        }
        {
            String lhsLicensingAuthority;
            lhsLicensingAuthority = this.getLicensingAuthority();
            String rhsLicensingAuthority;
            rhsLicensingAuthority = that.getLicensingAuthority();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "licensingAuthority", lhsLicensingAuthority), LocatorUtils.property(thatLocator, "licensingAuthority", rhsLicensingAuthority), lhsLicensingAuthority, rhsLicensingAuthority, (this.licensingAuthority!= null), (that.licensingAuthority!= null))) {
                return false;
            }
        }
        {
            DERO_Regions lhsRegions;
            lhsRegions = this.getRegions();
            DERO_Regions rhsRegions;
            rhsRegions = that.getRegions();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "regions", lhsRegions), LocatorUtils.property(thatLocator, "regions", rhsRegions), lhsRegions, rhsRegions, (this.regions!= null), (that.regions!= null))) {
                return false;
            }
        }
        {
            String lhsLocation;
            lhsLocation = this.getLocation();
            String rhsLocation;
            rhsLocation = that.getLocation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "location", lhsLocation), LocatorUtils.property(thatLocator, "location", rhsLocation), lhsLocation, rhsLocation, (this.location!= null), (that.location!= null))) {
                return false;
            }
        }
        {
            DERO_DerogationJustificationsType lhsDerogationJustifications;
            lhsDerogationJustifications = this.getDerogationJustifications();
            DERO_DerogationJustificationsType rhsDerogationJustifications;
            rhsDerogationJustifications = that.getDerogationJustifications();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "derogationJustifications", lhsDerogationJustifications), LocatorUtils.property(thatLocator, "derogationJustifications", rhsDerogationJustifications), lhsDerogationJustifications, rhsDerogationJustifications, (this.derogationJustifications!= null), (that.derogationJustifications!= null))) {
                return false;
            }
        }
        {
            DERO_Reasons lhsReasons;
            lhsReasons = this.getReasons();
            DERO_Reasons rhsReasons;
            rhsReasons = that.getReasons();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "reasons", lhsReasons), LocatorUtils.property(thatLocator, "reasons", rhsReasons), lhsReasons, rhsReasons, (this.reasons!= null), (that.reasons!= null))) {
                return false;
            }
        }
        {
            String lhsDerogationJustificationDetails;
            lhsDerogationJustificationDetails = this.getDerogationJustificationDetails();
            String rhsDerogationJustificationDetails;
            rhsDerogationJustificationDetails = that.getDerogationJustificationDetails();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "derogationJustificationDetails", lhsDerogationJustificationDetails), LocatorUtils.property(thatLocator, "derogationJustificationDetails", rhsDerogationJustificationDetails), lhsDerogationJustificationDetails, rhsDerogationJustificationDetails, (this.derogationJustificationDetails!= null), (that.derogationJustificationDetails!= null))) {
                return false;
            }
        }
        {
            String lhsStrictlySupervisedConditions;
            lhsStrictlySupervisedConditions = this.getStrictlySupervisedConditions();
            String rhsStrictlySupervisedConditions;
            rhsStrictlySupervisedConditions = that.getStrictlySupervisedConditions();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "strictlySupervisedConditions", lhsStrictlySupervisedConditions), LocatorUtils.property(thatLocator, "strictlySupervisedConditions", rhsStrictlySupervisedConditions), lhsStrictlySupervisedConditions, rhsStrictlySupervisedConditions, (this.strictlySupervisedConditions!= null), (that.strictlySupervisedConditions!= null))) {
                return false;
            }
        }
        {
            String lhsSelectiveBasis;
            lhsSelectiveBasis = this.getSelectiveBasis();
            String rhsSelectiveBasis;
            rhsSelectiveBasis = that.getSelectiveBasis();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "selectiveBasis", lhsSelectiveBasis), LocatorUtils.property(thatLocator, "selectiveBasis", rhsSelectiveBasis), lhsSelectiveBasis, rhsSelectiveBasis, (this.selectiveBasis!= null), (that.selectiveBasis!= null))) {
                return false;
            }
        }
        {
            String lhsSmallNumberIndividuals;
            lhsSmallNumberIndividuals = this.getSmallNumberIndividuals();
            String rhsSmallNumberIndividuals;
            rhsSmallNumberIndividuals = that.getSmallNumberIndividuals();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "smallNumberIndividuals", lhsSmallNumberIndividuals), LocatorUtils.property(thatLocator, "smallNumberIndividuals", rhsSmallNumberIndividuals), lhsSmallNumberIndividuals, rhsSmallNumberIndividuals, (this.smallNumberIndividuals!= null), (that.smallNumberIndividuals!= null))) {
                return false;
            }
        }
        {
            DERO_Activities lhsActivities;
            lhsActivities = this.getActivities();
            DERO_Activities rhsActivities;
            rhsActivities = that.getActivities();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "activities", lhsActivities), LocatorUtils.property(thatLocator, "activities", rhsActivities), lhsActivities, rhsActivities, (this.activities!= null), (that.activities!= null))) {
                return false;
            }
        }
        {
            DERO_AdditionalActivities lhsAdditionalActivities;
            lhsAdditionalActivities = this.getAdditionalActivities();
            DERO_AdditionalActivities rhsAdditionalActivities;
            rhsAdditionalActivities = that.getAdditionalActivities();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "additionalActivities", lhsAdditionalActivities), LocatorUtils.property(thatLocator, "additionalActivities", rhsAdditionalActivities), lhsAdditionalActivities, rhsAdditionalActivities, (this.additionalActivities!= null), (that.additionalActivities!= null))) {
                return false;
            }
        }
        {
            String lhsActivitiesFurtherDetails;
            lhsActivitiesFurtherDetails = this.getActivitiesFurtherDetails();
            String rhsActivitiesFurtherDetails;
            rhsActivitiesFurtherDetails = that.getActivitiesFurtherDetails();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "activitiesFurtherDetails", lhsActivitiesFurtherDetails), LocatorUtils.property(thatLocator, "activitiesFurtherDetails", rhsActivitiesFurtherDetails), lhsActivitiesFurtherDetails, rhsActivitiesFurtherDetails, (this.activitiesFurtherDetails!= null), (that.activitiesFurtherDetails!= null))) {
                return false;
            }
        }
        {
            DERO_Methods lhsMethods;
            lhsMethods = this.getMethods();
            DERO_Methods rhsMethods;
            rhsMethods = that.getMethods();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "methods", lhsMethods), LocatorUtils.property(thatLocator, "methods", rhsMethods), lhsMethods, rhsMethods, (this.methods!= null), (that.methods!= null))) {
                return false;
            }
        }
        {
            String lhsFurtherDetails;
            lhsFurtherDetails = this.getFurtherDetails();
            String rhsFurtherDetails;
            rhsFurtherDetails = that.getFurtherDetails();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "furtherDetails", lhsFurtherDetails), LocatorUtils.property(thatLocator, "furtherDetails", rhsFurtherDetails), lhsFurtherDetails, rhsFurtherDetails, (this.furtherDetails!= null), (that.furtherDetails!= null))) {
                return false;
            }
        }
        {
            DERO_ModesOfTransportType lhsModesOfTransport;
            lhsModesOfTransport = this.getModesOfTransport();
            DERO_ModesOfTransportType rhsModesOfTransport;
            rhsModesOfTransport = that.getModesOfTransport();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "modesOfTransport", lhsModesOfTransport), LocatorUtils.property(thatLocator, "modesOfTransport", rhsModesOfTransport), lhsModesOfTransport, rhsModesOfTransport, (this.modesOfTransport!= null), (that.modesOfTransport!= null))) {
                return false;
            }
        }
        {
            DERO_DerogationEntity lhsLicensed;
            lhsLicensed = this.getLicensed();
            DERO_DerogationEntity rhsLicensed;
            rhsLicensed = that.getLicensed();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "licensed", lhsLicensed), LocatorUtils.property(thatLocator, "licensed", rhsLicensed), lhsLicensed, rhsLicensed, (this.licensed!= null), (that.licensed!= null))) {
                return false;
            }
        }
        {
            DERO_DerogationEntity lhsActuallyTaken;
            lhsActuallyTaken = this.getActuallyTaken();
            DERO_DerogationEntity rhsActuallyTaken;
            rhsActuallyTaken = that.getActuallyTaken();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "actuallyTaken", lhsActuallyTaken), LocatorUtils.property(thatLocator, "actuallyTaken", rhsActuallyTaken), lhsActuallyTaken, rhsActuallyTaken, (this.actuallyTaken!= null), (that.actuallyTaken!= null))) {
                return false;
            }
        }
        {
            String lhsAllMeasuresTaken;
            lhsAllMeasuresTaken = this.getAllMeasuresTaken();
            String rhsAllMeasuresTaken;
            rhsAllMeasuresTaken = that.getAllMeasuresTaken();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "allMeasuresTaken", lhsAllMeasuresTaken), LocatorUtils.property(thatLocator, "allMeasuresTaken", rhsAllMeasuresTaken), lhsAllMeasuresTaken, rhsAllMeasuresTaken, (this.allMeasuresTaken!= null), (that.allMeasuresTaken!= null))) {
                return false;
            }
        }
        {
            String lhsEUAllMeasuresTaken;
            lhsEUAllMeasuresTaken = this.getEUAllMeasuresTaken();
            String rhsEUAllMeasuresTaken;
            rhsEUAllMeasuresTaken = that.getEUAllMeasuresTaken();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "euAllMeasuresTaken", lhsEUAllMeasuresTaken), LocatorUtils.property(thatLocator, "euAllMeasuresTaken", rhsEUAllMeasuresTaken), lhsEUAllMeasuresTaken, rhsEUAllMeasuresTaken, (this.euAllMeasuresTaken!= null), (that.euAllMeasuresTaken!= null))) {
                return false;
            }
        }
        {
            String lhsDetrimentalToPopulation;
            lhsDetrimentalToPopulation = this.getDetrimentalToPopulation();
            String rhsDetrimentalToPopulation;
            rhsDetrimentalToPopulation = that.getDetrimentalToPopulation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "detrimentalToPopulation", lhsDetrimentalToPopulation), LocatorUtils.property(thatLocator, "detrimentalToPopulation", rhsDetrimentalToPopulation), lhsDetrimentalToPopulation, rhsDetrimentalToPopulation, (this.detrimentalToPopulation!= null), (that.detrimentalToPopulation!= null))) {
                return false;
            }
        }
        {
            String lhsAlternativeToDerogation;
            lhsAlternativeToDerogation = this.getAlternativeToDerogation();
            String rhsAlternativeToDerogation;
            rhsAlternativeToDerogation = that.getAlternativeToDerogation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "alternativeToDerogation", lhsAlternativeToDerogation), LocatorUtils.property(thatLocator, "alternativeToDerogation", rhsAlternativeToDerogation), lhsAlternativeToDerogation, rhsAlternativeToDerogation, (this.alternativeToDerogation!= null), (that.alternativeToDerogation!= null))) {
                return false;
            }
        }
        {
            String lhsAlternativesAssessed;
            lhsAlternativesAssessed = this.getAlternativesAssessed();
            String rhsAlternativesAssessed;
            rhsAlternativesAssessed = that.getAlternativesAssessed();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "alternativesAssessed", lhsAlternativesAssessed), LocatorUtils.property(thatLocator, "alternativesAssessed", rhsAlternativesAssessed), lhsAlternativesAssessed, rhsAlternativesAssessed, (this.alternativesAssessed!= null), (that.alternativesAssessed!= null))) {
                return false;
            }
        }
        {
            String lhsSupervisoryMeasure;
            lhsSupervisoryMeasure = this.getSupervisoryMeasure();
            String rhsSupervisoryMeasure;
            rhsSupervisoryMeasure = that.getSupervisoryMeasure();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "supervisoryMeasure", lhsSupervisoryMeasure), LocatorUtils.property(thatLocator, "supervisoryMeasure", rhsSupervisoryMeasure), lhsSupervisoryMeasure, rhsSupervisoryMeasure, (this.supervisoryMeasure!= null), (that.supervisoryMeasure!= null))) {
                return false;
            }
        }
        {
            String lhsComments;
            lhsComments = this.getComments();
            String rhsComments;
            rhsComments = that.getComments();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "comments", lhsComments), LocatorUtils.property(thatLocator, "comments", rhsComments), lhsComments, rhsComments, (this.comments!= null), (that.comments!= null))) {
                return false;
            }
        }
        {
            String lhsCountry;
            lhsCountry = this.getCountry();
            String rhsCountry;
            rhsCountry = that.getCountry();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "country", lhsCountry), LocatorUtils.property(thatLocator, "country", rhsCountry), lhsCountry, rhsCountry, (this.country!= null), (that.country!= null))) {
                return false;
            }
        }
        {
            DERO_DirectiveType lhsDirective;
            lhsDirective = this.getDirective();
            DERO_DirectiveType rhsDirective;
            rhsDirective = that.getDirective();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "directive", lhsDirective), LocatorUtils.property(thatLocator, "directive", rhsDirective), lhsDirective, rhsDirective, (this.directive!= null), (that.directive!= null))) {
                return false;
            }
        }
        {
            String lhsDerogationReference;
            lhsDerogationReference = this.getDerogationReference();
            String rhsDerogationReference;
            rhsDerogationReference = that.getDerogationReference();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "derogationReference", lhsDerogationReference), LocatorUtils.property(thatLocator, "derogationReference", rhsDerogationReference), lhsDerogationReference, rhsDerogationReference, (this.derogationReference!= null), (that.derogationReference!= null))) {
                return false;
            }
        }
        {
            String lhsUserDerogationRef;
            lhsUserDerogationRef = this.getUserDerogationRef();
            String rhsUserDerogationRef;
            rhsUserDerogationRef = that.getUserDerogationRef();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "userDerogationRef", lhsUserDerogationRef), LocatorUtils.property(thatLocator, "userDerogationRef", rhsUserDerogationRef), lhsUserDerogationRef, rhsUserDerogationRef, (this.userDerogationRef!= null), (that.userDerogationRef!= null))) {
                return false;
            }
        }
        {
            String lhsUserIdentity;
            lhsUserIdentity = this.getUserIdentity();
            String rhsUserIdentity;
            rhsUserIdentity = that.getUserIdentity();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "userIdentity", lhsUserIdentity), LocatorUtils.property(thatLocator, "userIdentity", rhsUserIdentity), lhsUserIdentity, rhsUserIdentity, (this.userIdentity!= null), (that.userIdentity!= null))) {
                return false;
            }
        }
        {
            String lhsStatus;
            lhsStatus = this.getStatus();
            String rhsStatus;
            rhsStatus = that.getStatus();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "status", lhsStatus), LocatorUtils.property(thatLocator, "status", rhsStatus), lhsStatus, rhsStatus, (this.status!= null), (that.status!= null))) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy2 strategy) {
        int currentHashCode = 1;
        {
            String theSpecies;
            theSpecies = this.getSpecies();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "species", theSpecies), currentHashCode, theSpecies, (this.species!= null));
        }
        {
            String theSpeciesGroup;
            theSpeciesGroup = this.getSpeciesGroup();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "speciesGroup", theSpeciesGroup), currentHashCode, theSpeciesGroup, (this.speciesGroup!= null));
        }
        {
            String theCoversAllSpecies;
            theCoversAllSpecies = this.getCoversAllSpecies();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "coversAllSpecies", theCoversAllSpecies), currentHashCode, theCoversAllSpecies, (this.coversAllSpecies!= null));
        }
        {
            String theSensitive;
            theSensitive = this.getSensitive();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "sensitive", theSensitive), currentHashCode, theSensitive, (this.sensitive!= null));
        }
        {
            String theLicenseValidFrom;
            theLicenseValidFrom = this.getLicenseValidFrom();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "licenseValidFrom", theLicenseValidFrom), currentHashCode, theLicenseValidFrom, (this.licenseValidFrom!= null));
        }
        {
            String theLicenseValidUntil;
            theLicenseValidUntil = this.getLicenseValidUntil();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "licenseValidUntil", theLicenseValidUntil), currentHashCode, theLicenseValidUntil, (this.licenseValidUntil!= null));
        }
        {
            String theLicensingAuthority;
            theLicensingAuthority = this.getLicensingAuthority();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "licensingAuthority", theLicensingAuthority), currentHashCode, theLicensingAuthority, (this.licensingAuthority!= null));
        }
        {
            DERO_Regions theRegions;
            theRegions = this.getRegions();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "regions", theRegions), currentHashCode, theRegions, (this.regions!= null));
        }
        {
            String theLocation;
            theLocation = this.getLocation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "location", theLocation), currentHashCode, theLocation, (this.location!= null));
        }
        {
            DERO_DerogationJustificationsType theDerogationJustifications;
            theDerogationJustifications = this.getDerogationJustifications();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "derogationJustifications", theDerogationJustifications), currentHashCode, theDerogationJustifications, (this.derogationJustifications!= null));
        }
        {
            DERO_Reasons theReasons;
            theReasons = this.getReasons();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "reasons", theReasons), currentHashCode, theReasons, (this.reasons!= null));
        }
        {
            String theDerogationJustificationDetails;
            theDerogationJustificationDetails = this.getDerogationJustificationDetails();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "derogationJustificationDetails", theDerogationJustificationDetails), currentHashCode, theDerogationJustificationDetails, (this.derogationJustificationDetails!= null));
        }
        {
            String theStrictlySupervisedConditions;
            theStrictlySupervisedConditions = this.getStrictlySupervisedConditions();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "strictlySupervisedConditions", theStrictlySupervisedConditions), currentHashCode, theStrictlySupervisedConditions, (this.strictlySupervisedConditions!= null));
        }
        {
            String theSelectiveBasis;
            theSelectiveBasis = this.getSelectiveBasis();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "selectiveBasis", theSelectiveBasis), currentHashCode, theSelectiveBasis, (this.selectiveBasis!= null));
        }
        {
            String theSmallNumberIndividuals;
            theSmallNumberIndividuals = this.getSmallNumberIndividuals();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "smallNumberIndividuals", theSmallNumberIndividuals), currentHashCode, theSmallNumberIndividuals, (this.smallNumberIndividuals!= null));
        }
        {
            DERO_Activities theActivities;
            theActivities = this.getActivities();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "activities", theActivities), currentHashCode, theActivities, (this.activities!= null));
        }
        {
            DERO_AdditionalActivities theAdditionalActivities;
            theAdditionalActivities = this.getAdditionalActivities();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "additionalActivities", theAdditionalActivities), currentHashCode, theAdditionalActivities, (this.additionalActivities!= null));
        }
        {
            String theActivitiesFurtherDetails;
            theActivitiesFurtherDetails = this.getActivitiesFurtherDetails();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "activitiesFurtherDetails", theActivitiesFurtherDetails), currentHashCode, theActivitiesFurtherDetails, (this.activitiesFurtherDetails!= null));
        }
        {
            DERO_Methods theMethods;
            theMethods = this.getMethods();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "methods", theMethods), currentHashCode, theMethods, (this.methods!= null));
        }
        {
            String theFurtherDetails;
            theFurtherDetails = this.getFurtherDetails();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "furtherDetails", theFurtherDetails), currentHashCode, theFurtherDetails, (this.furtherDetails!= null));
        }
        {
            DERO_ModesOfTransportType theModesOfTransport;
            theModesOfTransport = this.getModesOfTransport();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "modesOfTransport", theModesOfTransport), currentHashCode, theModesOfTransport, (this.modesOfTransport!= null));
        }
        {
            DERO_DerogationEntity theLicensed;
            theLicensed = this.getLicensed();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "licensed", theLicensed), currentHashCode, theLicensed, (this.licensed!= null));
        }
        {
            DERO_DerogationEntity theActuallyTaken;
            theActuallyTaken = this.getActuallyTaken();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "actuallyTaken", theActuallyTaken), currentHashCode, theActuallyTaken, (this.actuallyTaken!= null));
        }
        {
            String theAllMeasuresTaken;
            theAllMeasuresTaken = this.getAllMeasuresTaken();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "allMeasuresTaken", theAllMeasuresTaken), currentHashCode, theAllMeasuresTaken, (this.allMeasuresTaken!= null));
        }
        {
            String theEUAllMeasuresTaken;
            theEUAllMeasuresTaken = this.getEUAllMeasuresTaken();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "euAllMeasuresTaken", theEUAllMeasuresTaken), currentHashCode, theEUAllMeasuresTaken, (this.euAllMeasuresTaken!= null));
        }
        {
            String theDetrimentalToPopulation;
            theDetrimentalToPopulation = this.getDetrimentalToPopulation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "detrimentalToPopulation", theDetrimentalToPopulation), currentHashCode, theDetrimentalToPopulation, (this.detrimentalToPopulation!= null));
        }
        {
            String theAlternativeToDerogation;
            theAlternativeToDerogation = this.getAlternativeToDerogation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "alternativeToDerogation", theAlternativeToDerogation), currentHashCode, theAlternativeToDerogation, (this.alternativeToDerogation!= null));
        }
        {
            String theAlternativesAssessed;
            theAlternativesAssessed = this.getAlternativesAssessed();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "alternativesAssessed", theAlternativesAssessed), currentHashCode, theAlternativesAssessed, (this.alternativesAssessed!= null));
        }
        {
            String theSupervisoryMeasure;
            theSupervisoryMeasure = this.getSupervisoryMeasure();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "supervisoryMeasure", theSupervisoryMeasure), currentHashCode, theSupervisoryMeasure, (this.supervisoryMeasure!= null));
        }
        {
            String theComments;
            theComments = this.getComments();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "comments", theComments), currentHashCode, theComments, (this.comments!= null));
        }
        {
            String theCountry;
            theCountry = this.getCountry();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "country", theCountry), currentHashCode, theCountry, (this.country!= null));
        }
        {
            DERO_DirectiveType theDirective;
            theDirective = this.getDirective();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "directive", theDirective), currentHashCode, theDirective, (this.directive!= null));
        }
        {
            String theDerogationReference;
            theDerogationReference = this.getDerogationReference();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "derogationReference", theDerogationReference), currentHashCode, theDerogationReference, (this.derogationReference!= null));
        }
        {
            String theUserDerogationRef;
            theUserDerogationRef = this.getUserDerogationRef();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "userDerogationRef", theUserDerogationRef), currentHashCode, theUserDerogationRef, (this.userDerogationRef!= null));
        }
        {
            String theUserIdentity;
            theUserIdentity = this.getUserIdentity();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "userIdentity", theUserIdentity), currentHashCode, theUserIdentity, (this.userIdentity!= null));
        }
        {
            String theStatus;
            theStatus = this.getStatus();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "status", theStatus), currentHashCode, theStatus, (this.status!= null));
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
