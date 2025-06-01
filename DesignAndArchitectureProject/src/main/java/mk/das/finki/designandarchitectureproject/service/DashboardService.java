package mk.das.finki.designandarchitectureproject.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> getStats(String companySelected);
    Map<String, Object> getCompanyData(String companySelected, Integer fromYear, Integer toYear);
    Map<String, Object> getStatsData(String companySelected, Integer fromYear, Integer toYear);
    Map<String, String> getNews(String companySelected);
}