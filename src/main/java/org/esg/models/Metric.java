package org.esg.models;

import jakarta.persistence.*;

@Entity
@Table(name="metrics")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    @Column(unique = true, nullable = false)
    private String name;

    public Metric() {}

    public Metric(Long metricId, String name) {
        this.metricId = metricId;
        this.name = name;
    }
}
