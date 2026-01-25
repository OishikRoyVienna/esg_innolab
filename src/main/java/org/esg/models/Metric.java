package org.esg.models;

import jakarta.persistence.*;

@Entity
@Table(name = "metrics")
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

    // Getter
    public Long getMetricId() {
        return metricId;
    }

    public String getName() {
        return name;
    }

    // Setter
    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    public void setName(String name) {
        this.name = name;
    }
}