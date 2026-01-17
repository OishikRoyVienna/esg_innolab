package org.esg.models;

import jakarta.persistence.*;

@Entity
@Table(name="locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    @Column(unique = true, nullable = false)
    private String name;

    public Location() {}

    public Location(Long locationId, String name) {
        this.locationId = locationId;
        this.name = name;
    }
}
