package org.esg.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name="measurements",
        uniqueConstraints=@UniqueConstraint(columnNames={
                "location_id","metric_id","period_id","year","month"}))
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long measurementId;

    @ManyToOne @JoinColumn(name="location_id")
    @Getter
    @Setter
    private Location location;

    @ManyToOne @JoinColumn(name="metric_id")
    @Getter @Setter
    private Metric metric;

    @ManyToOne @JoinColumn(name="period_id")
    @Getter @Setter
    private Period period;

    @Getter @Setter
    private short year;

    @Getter @Setter
    private short month;

    @Getter @Setter
    private BigDecimal value;

    @Getter @Setter
    private String unit;

    public Measurement() {}   // REQUIRED
}
