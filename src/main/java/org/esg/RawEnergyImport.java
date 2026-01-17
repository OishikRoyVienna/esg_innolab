package org.esg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "raw_energy_import")
@Getter
@Setter
public class RawEnergyImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_label")
    private String yearLabel;

    private Short month;

    private Short year;

    @Column(name = "location")
    private String location;

    private String value;

    private String unit;
}
