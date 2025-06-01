package mk.das.finki.designandarchitectureproject.repository;

import mk.das.finki.designandarchitectureproject.model.Issuer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssuerRepository extends JpaRepository<Issuer, Long> {
    List<Issuer> findAllByOrderByNameAsc();
    Issuer findByCode(String code);
}