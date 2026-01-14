package org.esg;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "water_data", uniqueConstraints = @UniqueConstraint(columnNames = {"dept", "start_date", "end_date"}))
public class WaterRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String dept;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal consumption;

    // Getter & Setter (oder Lombok)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getConsumption() { return consumption; }
    public void setConsumption(BigDecimal consumption) { this.consumption = consumption; }
}