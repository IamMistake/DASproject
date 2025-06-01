package mk.das.finki.designandarchitectureproject.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "issuers")
public class Issuer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer issuerId;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private LocalDateTime lastUpdated;
}