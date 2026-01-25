package org.esg.models;

import jakarta.persistence.*;

@Entity
@Table(name = "raw_energy_import")
public class RawEnergyImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Primary key for save()

    @Column(name = "year_label")
    private String yearLabel;

    private Short month;
    private Short year;
    private String location;
    private String value;
    private String unit;

    // Getters and setters
    public Long getId() { return id; }

    public String getYearLabel() { return yearLabel; }
    public void setYearLabel(String yearLabel) { this.yearLabel = yearLabel; }

    public Short getMonth() { return month; }
    public void setMonth(Short month) { this.month = month; }

    public Short getYear() { return year; }
    public void setYear(Short year) { this.year = year; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
