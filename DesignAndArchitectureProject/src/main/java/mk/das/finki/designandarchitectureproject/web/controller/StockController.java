package mk.das.finki.designandarchitectureproject.web.controller;

import mk.das.finki.designandarchitectureproject.model.Utils;
import mk.das.finki.designandarchitectureproject.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {

    private final DashboardService dashboardService;

    public StockController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/stats")
    public Map<String, Object> getStats(@RequestParam(defaultValue = "ALK") String companySelected) {
        return dashboardService.getStats(companySelected);
    }

    @GetMapping("/dashboard/lineChart")
    public Map<String, Object> getCompanyData(@RequestParam(defaultValue = "ALK") String companySelected,
                                              @RequestParam(defaultValue = "2014") Integer fromYear,
                                              @RequestParam(defaultValue = "2024") Integer toYear) {
        return dashboardService.getCompanyData(companySelected, fromYear, toYear);
    }

    @GetMapping("/dashboard/statistics")
    public Map<String, Object> getStatsData(@RequestParam(defaultValue = "ALK") String companySelected,
                                            @RequestParam(defaultValue = "2014") Integer fromYear,
                                            @RequestParam(defaultValue = "2024") Integer toYear) {
        return dashboardService.getStatsData(companySelected, fromYear, toYear);
    }

    @GetMapping("/companies")
    public List<String> getCompanies() {
//        String url = "https://www.mse.mk/mk/stats/symbolhistory/kmb";
//        return Utils.extractDropdownOptions(url);
        return List.of("KMB", "ALK", "SBT");
    }

    @GetMapping("/dashboard/news")
    public Map<String, String> getNews(@RequestParam(defaultValue = "ALK") String companySelected) {
        return dashboardService.getNews(companySelected);
    }
}