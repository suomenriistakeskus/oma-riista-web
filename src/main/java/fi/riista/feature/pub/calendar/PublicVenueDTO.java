package fi.riista.feature.pub.calendar;

import fi.riista.feature.organization.calendar.Venue;

public class PublicVenueDTO {

    public static PublicVenueDTO create(Venue venue){
        PublicVenueDTO dto = new PublicVenueDTO();
        dto.setName(venue.getName());
        dto.setStreetAddress(venue.getAddress().getStreetAddress());
        dto.setPostalCode(venue.getAddress().getPostalCode());
        dto.setCity(venue.getAddress().getCity());
        dto.setCountry(venue.getAddress().getCountry());
        return dto;
    }

    private String name;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String country;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
