package mk.das.finki.designandarchitectureproject.service.implementation;

import mk.das.finki.designandarchitectureproject.model.Issuer;
import mk.das.finki.designandarchitectureproject.model.StockData;
import mk.das.finki.designandarchitectureproject.repository.IssuerRepository;
import mk.das.finki.designandarchitectureproject.repository.StockDataRepository;
import mk.das.finki.designandarchitectureproject.service.DashboardService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final StockDataRepository stockDataRepository;
    private final IssuerRepository issuerRepository;

    public DashboardServiceImpl(StockDataRepository stockDataRepository,
                                IssuerRepository issuerRepository) {
        this.stockDataRepository = stockDataRepository;
        this.issuerRepository = issuerRepository;
    }

    @Override
    public Map<String, Object> getStats(String companyCode) {
        Issuer issuer = issuerRepository.findByCode(companyCode);
        if (issuer == null) {
            throw new RuntimeException("Company not found: " + companyCode);
        }

        List<StockData> stockData = stockDataRepository.findByIssuerOrderByDateAsc(issuer);
        if (stockData.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", convertToDto(stockData.get(0)));
        return response;
    }

    @Override
    public Map<String, Object> getCompanyData(String companyCode, Integer fromYear, Integer toYear) {
        Issuer issuer = issuerRepository.findByCode(companyCode);
        if (issuer == null) {
            throw new RuntimeException("Company not found: " + companyCode);
        }

        LocalDate startDate = LocalDate.of(fromYear, 1, 1);
        LocalDate endDate = LocalDate.of(toYear, 12, 31);

        List<StockData> stockData = stockDataRepository.findByIssuerAndDateBetweenOrderByDateAsc(issuer, startDate, endDate);
        List<Double> prices = stockData.stream()
                .map(sd -> sd.getLastPrice().doubleValue())
                .collect(Collectors.toList());

        List<Double> sma10 = calculateSMA(prices, 10);
        List<Double> rsi14 = calculateRSI(prices, 14);

        List<String> signals = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            double sma = i < sma10.size() ? sma10.get(i) : 0;
            double rsi = i < rsi14.size() ? rsi14.get(i) : 0;
            signals.add(generateSignal(rsi, sma, prices.get(i)));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("dates", stockData.stream().map(StockData::getDate).collect(Collectors.toList()));
        response.put("prices", prices);
        response.put("sma", sma10);
        response.put("rsi", rsi14);
        response.put("signals", signals);

        return response;
    }

    @Override
    public Map<String, Object> getStatsData(String companyCode, Integer fromYear, Integer toYear) {
        Issuer issuer = issuerRepository.findByCode(companyCode);
        if (issuer == null) {
            throw new RuntimeException("Company not found: " + companyCode);
        }

        LocalDate startDate = LocalDate.of(fromYear, 1, 1);
        LocalDate endDate = LocalDate.of(toYear, 12, 31);

        List<StockData> stockData = stockDataRepository.findByIssuerAndDateBetweenOrderByDateAsc(issuer, startDate, endDate);
        List<Double> prices = stockData.stream()
                .map(sd -> sd.getLastPrice().doubleValue())
                .collect(Collectors.toList());

        // Calculate moving averages
        List<Double> sma10 = calculateSMA(prices, 10);
        List<Double> sma20 = calculateSMA(prices, 20);
        List<Double> ema10 = calculateEMA(prices, 10);
        List<Double> ema20 = calculateEMA(prices, 20);
        List<Double> wma = calculateWMA(prices, 14);

        // Calculate oscillators
        List<Double> rsi = calculateRSI(prices, 14);
        List<Double> stochastic = calculateStochasticOscillator(prices);
        List<Double> macd = calculateMACD(prices);
        List<Double> adx = calculateADX(stockData);
        List<Double> cci = calculateCCI(stockData);

        // Generate signals
        List<String> signals = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            signals.add(generateSignal(
                    i < rsi.size() ? rsi.get(i) : 0,
                    i < sma10.size() ? sma10.get(i) : 0,
                    prices.get(i))
            );
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("dates", stockData.stream().map(StockData::getDate).collect(Collectors.toList()));
        response.put("prices", prices);
        response.put("sma", Map.of(
                "sma10", sma10,
                "sma20", sma20,
                "ema10", ema10,
                "ema20", ema20,
                "wma", wma
        ));
        response.put("oscillators", Map.of(
                "rsi", rsi,
                "stochastic", stochastic,
                "macd", macd,
                "adx", adx,
                "cci", cci
        ));
        response.put("signals", signals);

        return response;
    }

    @Override
    public Map<String, String> getNews(String companyCode) {
        Map<String, String> result = new HashMap<>();

        result.put("Latest news about " + companyCode, "Positive");
        return result;
    }

    // Technical Analysis Methods
    private List<Double> calculateSMA(List<Double> prices, int period) {
        List<Double> sma = new ArrayList<>();
        for (int i = period - 1; i < prices.size(); i++) {
            double sum = 0;
            for (int j = 0; j < period; j++) {
                sum += prices.get(i - j);
            }
            sma.add(sum / period);
        }
        return sma;
    }

    private List<Double> calculateEMA(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);

        // First EMA is the SMA
        if (prices.size() >= period) {
            double sum = 0;
            for (int i = 0; i < period; i++) {
                sum += prices.get(i);
            }
            ema.add(sum / period);

            // Calculate subsequent EMAs
            for (int i = period; i < prices.size(); i++) {
                double currentEma = (prices.get(i) - ema.get(ema.size() - 1)) * multiplier + ema.get(ema.size() - 1);
                ema.add(currentEma);
            }
        }
        return ema;
    }

    private List<Double> calculateWMA(List<Double> prices, int period) {
        List<Double> wma = new ArrayList<>();
        int denominator = period * (period + 1) / 2;

        for (int i = period - 1; i < prices.size(); i++) {
            double sum = 0;
            for (int j = 0; j < period; j++) {
                sum += prices.get(i - j) * (period - j);
            }
            wma.add(sum / denominator);
        }
        return wma;
    }

    private List<Double> calculateRSI(List<Double> prices, int period) {
        List<Double> rsi = new ArrayList<>();
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        // Calculate initial gains and losses
        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            gains.add(change > 0 ? change : 0);
            losses.add(change < 0 ? -change : 0);
        }

        // Calculate first RSI value
        double avgGain = gains.subList(0, period).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgLoss = losses.subList(0, period).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
        rsi.add(100 - (100 / (1 + rs)));

        // Calculate subsequent RSI values
        for (int i = period; i < gains.size(); i++) {
            avgGain = ((avgGain * (period - 1)) + gains.get(i)) / period;
            avgLoss = ((avgLoss * (period - 1)) + losses.get(i)) / period;
            rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
            rsi.add(100 - (100 / (1 + rs)));
        }

        return rsi;
    }

    private List<Double> calculateStochasticOscillator(List<Double> prices) {
        List<Double> stochastic = new ArrayList<>();
        int period = 14;

        for (int i = period - 1; i < prices.size(); i++) {
            double highestHigh = prices.subList(i - period + 1, i + 1).stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0);
            double lowestLow = prices.subList(i - period + 1, i + 1).stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0);

            if (highestHigh != lowestLow) {
                stochastic.add(100 * (prices.get(i) - lowestLow) / (highestHigh - lowestLow));
            } else {
                stochastic.add(50.0); // Neutral value when no range
            }
        }
        return stochastic;
    }

    private List<Double> calculateMACD(List<Double> prices) {
        List<Double> macd = new ArrayList<>();
        List<Double> ema12 = calculateEMA(prices, 12);
        List<Double> ema26 = calculateEMA(prices, 26);

        int minLength = Math.min(ema12.size(), ema26.size());
        for (int i = 0; i < minLength; i++) {
            macd.add(ema12.get(i) - ema26.get(i));
        }
        return macd;
    }

    private List<Double> calculateADX(List<StockData> stockData) {
        // Simplified ADX calculation - real implementation would be more complex
        List<Double> adx = new ArrayList<>();
        int period = 14;

        if (stockData.size() >= period) {
            for (int i = 0; i < stockData.size() - period + 1; i++) {
                double sum = 0;
                for (int j = 0; j < period; j++) {
                    sum += stockData.get(i + j).getMaxPrice().doubleValue() -
                            stockData.get(i + j).getMinPrice().doubleValue();
                }
                adx.add(sum / period);
            }
        }
        return adx;
    }

    private List<Double> calculateCCI(List<StockData> stockData) {
        List<Double> cci = new ArrayList<>();
        int period = 20;

        for (int i = period - 1; i < stockData.size(); i++) {
            double typicalPrice = (stockData.get(i).getMaxPrice().doubleValue() +
                    stockData.get(i).getMinPrice().doubleValue() +
                    stockData.get(i).getLastPrice().doubleValue()) / 3;

            double meanDeviation = stockData.subList(i - period + 1, i + 1).stream()
                    .mapToDouble(sd -> Math.abs(
                            ((sd.getMaxPrice().doubleValue() +
                                    sd.getMinPrice().doubleValue() +
                                    sd.getLastPrice().doubleValue()) / 3) - typicalPrice)
                    )
                    .average()
                    .orElse(0);

            if (meanDeviation != 0) {
                cci.add((typicalPrice - typicalPrice) / (0.015 * meanDeviation)); // Simplified
            } else {
                cci.add(0.0);
            }
        }
        return cci;
    }

    private String generateSignal(double rsi, double sma, double price) {
        if (rsi > 70 && price > sma) {
            return "SELL";
        } else if (rsi < 30 && price < sma) {
            return "BUY";
        } else {
            return "HOLD";
        }
    }

    private Map<String, Object> convertToDto(StockData stockData) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("date", stockData.getDate());
        dto.put("lastPrice", stockData.getLastPrice());
        dto.put("maxPrice", stockData.getMaxPrice());
        dto.put("minPrice", stockData.getMinPrice());
        dto.put("avgPrice", stockData.getAvgPrice());
        dto.put("percentChange", stockData.getPercentChange());
        dto.put("quantity", stockData.getQuantity());
        dto.put("bestTurnover", stockData.getBestTurnover());
        dto.put("totalTurnover", stockData.getTotalTurnover());
        return dto;
    }

    public static List<String> extractDropdownOptions(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element dropdown = doc.selectFirst("#Code");
            if (dropdown != null) {
                return dropdown.select("option").stream()
                        .map(Element::text)
                        .filter(text -> !text.matches(".*\\d.*")) // Exclude options with numbers
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}