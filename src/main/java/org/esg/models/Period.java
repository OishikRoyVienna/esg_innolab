package org.esg.models;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name="periods")
public class Period {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long periodId;

    @Column(unique = true, nullable = false)
    private String yearLabel;

    public Period() {}   // REQUIRED by JPA

    public Period(Long periodId, String yearLabel) {
        this.periodId = periodId;
        this.yearLabel = yearLabel;
    }

    public void setYearLabel(String yearLabel) {
        this.yearLabel = yearLabel;
    }
}
