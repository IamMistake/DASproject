package mk.das.finki.designandarchitectureproject.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "stock_data")
public class StockData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dataId;

    @ManyToOne
    @JoinColumn(name = "issuer_id", nullable = false)
    private Issuer issuer;

    @Column(nullable = false)
    private LocalDate date;

    @Column(precision = 12, scale = 2)
    private BigDecimal lastPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal avgPrice;

    @Column(precision = 6, scale = 2)
    private BigDecimal percentChange;

    private Integer quantity;

    @Column(name = "best_turnover", precision = 15, scale = 2)
    private BigDecimal bestTurnover;

    @Column(name = "total_turnover", precision = 15, scale = 2)
    private BigDecimal totalTurnover;
}