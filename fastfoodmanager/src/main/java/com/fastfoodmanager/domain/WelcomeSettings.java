package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class WelcomeSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String siteTitle;
    private String siteSubtitle;
    private String address;
    @Column(columnDefinition = "TEXT")
    private String googleMapsUrl;
    private String siteDomain;


    @Column(columnDefinition = "TEXT")
    private String scheduleText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "welcome_carousel_images", joinColumns = @JoinColumn(name = "welcome_id"))
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private List<byte[]> carouselImages = new ArrayList<>();

    // getters & setters

    public Long getId() { return id; }

    public String getSiteTitle() { return siteTitle; }
    public void setSiteTitle(String siteTitle) { this.siteTitle = siteTitle; }

    public String getSiteSubtitle() { return siteSubtitle; }
    public void setSiteSubtitle(String siteSubtitle) { this.siteSubtitle = siteSubtitle; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGoogleMapsUrl() { return googleMapsUrl; }
    public void setGoogleMapsUrl(String googleMapsUrl) { this.googleMapsUrl = googleMapsUrl; }

    public String getScheduleText() { return scheduleText; }
    public void setScheduleText(String scheduleText) { this.scheduleText = scheduleText; }

    public List<byte[]> getCarouselImages() { return carouselImages; }
    public void setCarouselImages(List<byte[]> carouselImages) { this.carouselImages = carouselImages; }

    public String getSiteDomain() {
        return siteDomain;
    }

    public void setSiteDomain(String siteDomain) {
        this.siteDomain = siteDomain;
    }
}
