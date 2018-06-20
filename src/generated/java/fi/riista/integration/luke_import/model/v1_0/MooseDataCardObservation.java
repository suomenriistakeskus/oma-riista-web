
package fi.riista.integration.luke_import.model.v1_0;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import fi.riista.integration.support.DayAndMonthAdapter;
import org.joda.time.LocalDate;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString2;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;


/**
 * <p>Java class for _Päivittäiset_hirvihavainnotType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="_Päivittäiset_hirvihavainnotType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd}DateAndLocationType"/&gt;
 *         &lt;element name="_AU" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_N0" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_N1" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_N2" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_N3" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_T" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="_Y" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
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
@XmlType(name = "_P\u00e4ivitt\u00e4iset_hirvihavainnotType", namespace = "http://www.abbyy.com/FlexiCapture/Schemas/Export/Hirvitietokortti.xsd", propOrder = {
    "date",
    "latitude",
    "longitude",
    "au",
    "n0",
    "n1",
    "n2",
    "n3",
    "t",
    "y"
})
public class MooseDataCardObservation implements Cloneable, Copyable, PartialCopyable, DateAndLocation, ToString2
{

    @XmlElement(name = "_P\u00e4iv\u00e4m\u00e4\u00e4r\u00e4", required = true, type = String.class)
    @XmlJavaTypeAdapter(DayAndMonthAdapter.class)
    protected LocalDate date;
    @XmlElement(name = "_Koordinaatit_P", required = true)
    protected String latitude;
    @XmlElement(name = "_Koordinaatit_I", required = true)
    protected String longitude;
    @XmlElement(name = "_AU", required = true, type = Integer.class, nillable = true)
    protected Integer au;
    @XmlElement(name = "_N0", required = true, type = Integer.class, nillable = true)
    protected Integer n0;
    @XmlElement(name = "_N1", required = true, type = Integer.class, nillable = true)
    protected Integer n1;
    @XmlElement(name = "_N2", required = true, type = Integer.class, nillable = true)
    protected Integer n2;
    @XmlElement(name = "_N3", required = true, type = Integer.class, nillable = true)
    protected Integer n3;
    @XmlElement(name = "_T", required = true, type = Integer.class, nillable = true)
    protected Integer t;
    @XmlElement(name = "_Y", required = true, type = Integer.class, nillable = true)
    protected Integer y;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public MooseDataCardObservation() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a MooseDataCardObservation copying the state of another MooseDataCardObservation
     * 
     * @param _other
     *     The original MooseDataCardObservation from which to copy state.
     */
    public MooseDataCardObservation(final MooseDataCardObservation _other) {
        this.date = _other.date;
        this.latitude = _other.latitude;
        this.longitude = _other.longitude;
        this.au = _other.au;
        this.n0 = _other.n0;
        this.n1 = _other.n1;
        this.n2 = _other.n2;
        this.n3 = _other.n3;
        this.t = _other.t;
        this.y = _other.y;
    }

    /**
     * Instantiates a MooseDataCardObservation copying the state of another MooseDataCardObservation
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original MooseDataCardObservation from which to copy state.
     */
    public MooseDataCardObservation(final MooseDataCardObservation _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree datePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("date"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(datePropertyTree!= null):((datePropertyTree == null)||(!datePropertyTree.isLeaf())))) {
            this.date = _other.date;
        }
        final PropertyTree latitudePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("latitude"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(latitudePropertyTree!= null):((latitudePropertyTree == null)||(!latitudePropertyTree.isLeaf())))) {
            this.latitude = _other.latitude;
        }
        final PropertyTree longitudePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("longitude"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(longitudePropertyTree!= null):((longitudePropertyTree == null)||(!longitudePropertyTree.isLeaf())))) {
            this.longitude = _other.longitude;
        }
        final PropertyTree auPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("au"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(auPropertyTree!= null):((auPropertyTree == null)||(!auPropertyTree.isLeaf())))) {
            this.au = _other.au;
        }
        final PropertyTree n0PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("n0"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(n0PropertyTree!= null):((n0PropertyTree == null)||(!n0PropertyTree.isLeaf())))) {
            this.n0 = _other.n0;
        }
        final PropertyTree n1PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("n1"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(n1PropertyTree!= null):((n1PropertyTree == null)||(!n1PropertyTree.isLeaf())))) {
            this.n1 = _other.n1;
        }
        final PropertyTree n2PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("n2"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(n2PropertyTree!= null):((n2PropertyTree == null)||(!n2PropertyTree.isLeaf())))) {
            this.n2 = _other.n2;
        }
        final PropertyTree n3PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("n3"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(n3PropertyTree!= null):((n3PropertyTree == null)||(!n3PropertyTree.isLeaf())))) {
            this.n3 = _other.n3;
        }
        final PropertyTree tPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("t"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(tPropertyTree!= null):((tPropertyTree == null)||(!tPropertyTree.isLeaf())))) {
            this.t = _other.t;
        }
        final PropertyTree yPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("y"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(yPropertyTree!= null):((yPropertyTree == null)||(!yPropertyTree.isLeaf())))) {
            this.y = _other.y;
        }
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(LocalDate value) {
        this.date = value;
    }

    /**
     * Gets the value of the latitude property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Sets the value of the latitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLatitude(String value) {
        this.latitude = value;
    }

    /**
     * Gets the value of the longitude property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * Sets the value of the longitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLongitude(String value) {
        this.longitude = value;
    }

    /**
     * Gets the value of the au property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAU() {
        return au;
    }

    /**
     * Sets the value of the au property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAU(Integer value) {
        this.au = value;
    }

    /**
     * Gets the value of the n0 property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getN0() {
        return n0;
    }

    /**
     * Sets the value of the n0 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setN0(Integer value) {
        this.n0 = value;
    }

    /**
     * Gets the value of the n1 property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getN1() {
        return n1;
    }

    /**
     * Sets the value of the n1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setN1(Integer value) {
        this.n1 = value;
    }

    /**
     * Gets the value of the n2 property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getN2() {
        return n2;
    }

    /**
     * Sets the value of the n2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setN2(Integer value) {
        this.n2 = value;
    }

    /**
     * Gets the value of the n3 property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getN3() {
        return n3;
    }

    /**
     * Sets the value of the n3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setN3(Integer value) {
        this.n3 = value;
    }

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setT(Integer value) {
        this.t = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setY(Integer value) {
        this.y = value;
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
            LocalDate theDate;
            theDate = this.getDate();
            strategy.appendField(locator, this, "date", buffer, theDate, (this.date!= null));
        }
        {
            String theLatitude;
            theLatitude = this.getLatitude();
            strategy.appendField(locator, this, "latitude", buffer, theLatitude, (this.latitude!= null));
        }
        {
            String theLongitude;
            theLongitude = this.getLongitude();
            strategy.appendField(locator, this, "longitude", buffer, theLongitude, (this.longitude!= null));
        }
        {
            Integer theAU;
            theAU = this.getAU();
            strategy.appendField(locator, this, "au", buffer, theAU, (this.au!= null));
        }
        {
            Integer theN0;
            theN0 = this.getN0();
            strategy.appendField(locator, this, "n0", buffer, theN0, (this.n0 != null));
        }
        {
            Integer theN1;
            theN1 = this.getN1();
            strategy.appendField(locator, this, "n1", buffer, theN1, (this.n1 != null));
        }
        {
            Integer theN2;
            theN2 = this.getN2();
            strategy.appendField(locator, this, "n2", buffer, theN2, (this.n2 != null));
        }
        {
            Integer theN3;
            theN3 = this.getN3();
            strategy.appendField(locator, this, "n3", buffer, theN3, (this.n3 != null));
        }
        {
            Integer theT;
            theT = this.getT();
            strategy.appendField(locator, this, "t", buffer, theT, (this.t!= null));
        }
        {
            Integer theY;
            theY = this.getY();
            strategy.appendField(locator, this, "y", buffer, theY, (this.y!= null));
        }
        return buffer;
    }

    public MooseDataCardObservation withDate(LocalDate value) {
        setDate(value);
        return this;
    }

    public MooseDataCardObservation withLatitude(String value) {
        setLatitude(value);
        return this;
    }

    public MooseDataCardObservation withLongitude(String value) {
        setLongitude(value);
        return this;
    }

    public MooseDataCardObservation withAU(Integer value) {
        setAU(value);
        return this;
    }

    public MooseDataCardObservation withN0(Integer value) {
        setN0(value);
        return this;
    }

    public MooseDataCardObservation withN1(Integer value) {
        setN1(value);
        return this;
    }

    public MooseDataCardObservation withN2(Integer value) {
        setN2(value);
        return this;
    }

    public MooseDataCardObservation withN3(Integer value) {
        setN3(value);
        return this;
    }

    public MooseDataCardObservation withT(Integer value) {
        setT(value);
        return this;
    }

    public MooseDataCardObservation withY(Integer value) {
        setY(value);
        return this;
    }

    @Override
    public MooseDataCardObservation clone() {
        final MooseDataCardObservation _newObject;
        try {
            _newObject = ((MooseDataCardObservation) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return _newObject;
    }

    @Override
    public MooseDataCardObservation createCopy() {
        final MooseDataCardObservation _newObject;
        try {
            _newObject = ((MooseDataCardObservation) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.date = this.date;
        _newObject.latitude = this.latitude;
        _newObject.longitude = this.longitude;
        _newObject.au = this.au;
        _newObject.n0 = this.n0;
        _newObject.n1 = this.n1;
        _newObject.n2 = this.n2;
        _newObject.n3 = this.n3;
        _newObject.t = this.t;
        _newObject.y = this.y;
        return _newObject;
    }

    @Override
    public MooseDataCardObservation createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final MooseDataCardObservation _newObject;
        try {
            _newObject = ((MooseDataCardObservation) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree datePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("date"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(datePropertyTree!= null):((datePropertyTree == null)||(!datePropertyTree.isLeaf())))) {
            _newObject.date = this.date;
        }
        final PropertyTree latitudePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("latitude"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(latitudePropertyTree!= null):((latitudePropertyTree == null)||(!latitudePropertyTree.isLeaf())))) {
            _newObject.latitude = this.latitude;
        }
        final PropertyTree longitudePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("longitude"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(longitudePropertyTree!= null):((longitudePropertyTree == null)||(!longitudePropertyTree.isLeaf())))) {
            _newObject.longitude = this.longitude;
        }
        final PropertyTree auPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("au"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(auPropertyTree!= null):((auPropertyTree == null)||(!auPropertyTree.isLeaf())))) {
            _newObject.au = this.au;
        }
        final PropertyTree n0PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("n0"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(n0PropertyTree!= null):((n0PropertyTree == null)||(!n0PropertyTree.isLeaf())))) {
            _newObject.n0 = this.n0;
        }
        final PropertyTree n1PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("n1"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(n1PropertyTree!= null):((n1PropertyTree == null)||(!n1PropertyTree.isLeaf())))) {
            _newObject.n1 = this.n1;
        }
        final PropertyTree n2PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("n2"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(n2PropertyTree!= null):((n2PropertyTree == null)||(!n2PropertyTree.isLeaf())))) {
            _newObject.n2 = this.n2;
        }
        final PropertyTree n3PropertyTree = ((_propertyTree == null)?null:_propertyTree.get("n3"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(n3PropertyTree!= null):((n3PropertyTree == null)||(!n3PropertyTree.isLeaf())))) {
            _newObject.n3 = this.n3;
        }
        final PropertyTree tPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("t"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(tPropertyTree!= null):((tPropertyTree == null)||(!tPropertyTree.isLeaf())))) {
            _newObject.t = this.t;
        }
        final PropertyTree yPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("y"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(yPropertyTree!= null):((yPropertyTree == null)||(!yPropertyTree.isLeaf())))) {
            _newObject.y = this.y;
        }
        return _newObject;
    }

    @Override
    public MooseDataCardObservation copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public MooseDataCardObservation copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends MooseDataCardObservation.Selector<MooseDataCardObservation.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static MooseDataCardObservation.Select _root() {
            return new MooseDataCardObservation.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> date = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> latitude = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> longitude = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> au = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> n0 = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> n1 = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> n2 = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> n3 = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> t = null;
        private com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> y = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.date!= null) {
                products.put("date", this.date.init());
            }
            if (this.latitude!= null) {
                products.put("latitude", this.latitude.init());
            }
            if (this.longitude!= null) {
                products.put("longitude", this.longitude.init());
            }
            if (this.au!= null) {
                products.put("au", this.au.init());
            }
            if (this.n0 != null) {
                products.put("n0", this.n0 .init());
            }
            if (this.n1 != null) {
                products.put("n1", this.n1 .init());
            }
            if (this.n2 != null) {
                products.put("n2", this.n2 .init());
            }
            if (this.n3 != null) {
                products.put("n3", this.n3 .init());
            }
            if (this.t!= null) {
                products.put("t", this.t.init());
            }
            if (this.y!= null) {
                products.put("y", this.y.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> date() {
            return ((this.date == null)?this.date = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "date"):this.date);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> latitude() {
            return ((this.latitude == null)?this.latitude = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "latitude"):this.latitude);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> longitude() {
            return ((this.longitude == null)?this.longitude = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "longitude"):this.longitude);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> au() {
            return ((this.au == null)?this.au = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "au"):this.au);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> n0() {
            return ((this.n0 == null)?this.n0 = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "n0"):this.n0);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> n1() {
            return ((this.n1 == null)?this.n1 = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "n1"):this.n1);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> n2() {
            return ((this.n2 == null)?this.n2 = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "n2"):this.n2);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> n3() {
            return ((this.n3 == null)?this.n3 = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "n3"):this.n3);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> t() {
            return ((this.t == null)?this.t = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "t"):this.t);
        }

        public com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>> y() {
            return ((this.y == null)?this.y = new com.kscs.util.jaxb.Selector<TRoot, MooseDataCardObservation.Selector<TRoot, TParent>>(this._root, this, "y"):this.y);
        }

    }

}
