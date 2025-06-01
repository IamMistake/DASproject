package mk.das.finki.designandarchitectureproject.repository;

import mk.das.finki.designandarchitectureproject.model.Issuer;
import mk.das.finki.designandarchitectureproject.model.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    List<StockData> findByIssuerAndDateBetween(Issuer issuer, LocalDate startDate, LocalDate endDate);
    List<StockData> findByIssuerAndDateBetweenOrderByDateAsc(Issuer issuer, LocalDate startDate, LocalDate endDate);
    List<StockData> findByIssuerOrderByDateAsc(Issuer issuer);

    @Query("SELECT MIN(s.date) FROM StockData s WHERE s.issuer = :issuer")
    LocalDate findMinDateByIssuer(@Param("issuer") Issuer issuer);

    @Query("SELECT MAX(s.date) FROM StockData s WHERE s.issuer = :issuer")
    LocalDate findMaxDateByIssuer(@Param("issuer") Issuer issuer);

    @Query("SELECT AVG(s.lastPrice) FROM StockData s WHERE s.issuer = :issuer AND s.date BETWEEN :startDate AND :endDate")
    Double findAveragePriceByIssuerAndDateBetween(@Param("issuer") Issuer issuer,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}