let isFirst = true
let lineChartInstance;

function fetchDashboardData(urlParams) {
    fetch(`http://localhost:8080/api/stocks/dashboard/stats?${urlParams}`)
        .then(response => response.json())
        .then(data => {
            const stats = data.data;
            document.getElementById('minValue').textContent = stats.low;
            document.getElementById('maxValue').textContent = stats.high;
            document.getElementById('lastPrice').textContent = stats.closePrice;
            document.getElementById('average').textContent = stats.avgPrice;
            document.getElementById('quantity').textContent = stats.volume;
        })
        .catch(error => console.error('Error fetching stats:', error));
}

function fetchNews(urlParams) {
    let loading = document.getElementById("loadingNews");
    loading.style.display = "block"

    let ul = document.getElementById("newsList");
    ul.innerHTML = ""

    fetch(`http://localhost:8080/api/stocks/dashboard/news?${urlParams}`)
        .then(response => response.json())
        .then(data => {
            const newsList = document.getElementById('newsList');
            newsList.innerHTML = '';
            for (const [headline, sentiment] of Object.entries(data)) {
                const newsItem = document.createElement('li');
                newsItem.textContent = `${headline}\n(${sentiment})`;
                newsList.appendChild(newsItem);
            }
            let loading = document.getElementById("loadingNews");
            loading.style.display = "none"
        })
        .catch(error => {
            console.error('Error fetching news:', error)
            const newsItem = document.createElement('li');
            newsItem.textContent = `No news for this Company`;
            newsList.appendChild(newsItem);
        });
}

function fetchLineChart(urlParams) {
    fetch(`http://localhost:8080/api/stocks/dashboard/lineChart?${urlParams}`)
        .then(response => response.json())
        .then(data => {
            const reversedDates = data.dates.reverse();
            const reversedPrices = data.prices.reverse();
            const reversedSMA = data.sma.reverse();
            const reversedSignals = data.signals.reverse();

            if (lineChartInstance) {
                lineChartInstance.destroy();
            }

            const canvas = document.getElementById('lineChart');
            canvas.style.height = "86vh";
            canvas.style.width = "50vw";
            const ctx = canvas.getContext('2d');

            lineChartInstance = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: reversedDates,
                    datasets: [
                        {
                            label: 'Stock Prices',
                            data: reversedPrices,
                            borderColor: 'blue',
                            fill: false,
                            tension: 0.1
                        },
                        {
                            label: 'SMA (10)',
                            data: reversedSMA,
                            borderColor: 'orange',
                            fill: false,
                            tension: 0.1
                        }
                    ]
                },
                options: {
                    responsive: false,
                    plugins: {
                        annotation: {
                            annotations: reversedSignals.map((signal, index) => ({
                                type: 'point',
                                xValue: reversedDates[index],
                                yValue: reversedPrices[index],
                                backgroundColor: signal === 'Buy' ? 'green' : (signal === 'Sell' ? 'red' : 'yellow')
                            }))
                        }
                    }
                }
            });
        })
        .catch(error => console.error('Error fetching chart data:', error));
}

function fetchStatisticsData(urlParams) {
    fetch(`http://localhost:8080/api/stocks/dashboard/statistics?${urlParams}`)
        .then(response => response.json())
        .then(data => {
            const statisticsDiv = document.getElementById('statistics');

            // Extract data
            const sma = data.sma;
            const oscillators = data.oscillators;
            const signals = data.signals;

            // Generate HTML content
            const statsHtml = `
            <h3>Oscillators and Moving Averages</h3>
            <p>Technical indicators used to analyze historical prices and predict trends.</p>
            <h4>Top 5 Moving Averages</h4>
            <ul>
                <li>SMA (10): ${sma.sma10[sma.sma10.length - 1]}</li>
                <li>SMA (20): ${sma.sma20[sma.sma20.length - 1]}</li>
                <li>EMA (10): ${sma.ema10[sma.ema10.length - 1]}</li>
                <li>EMA (20): ${sma.ema20[sma.ema20.length - 1]}</li>
                <li>WMA: ${sma.wma[sma.wma.length - 1]}</li>
            </ul>
            <h4>Top 5 Oscillators</h4>
            <ul>
                <li>RSI (14): ${oscillators.rsi[oscillators.rsi.length - 1]}</li>
                <li>Stochastic: ${oscillators.stochastic[oscillators.stochastic.length - 1]}</li>
                <li>MACD: ${oscillators.macd[oscillators.macd.length - 1]}</li>
                <li>ADX: ${oscillators.adx[oscillators.adx.length - 1]}</li>
                <li>CCI: ${oscillators.cci[oscillators.cci.length - 1]}</li>
            </ul>
            <h4>Signals</h4>
            <p>Last Signal: ${signals[signals.length - 1]}</p>
        `;

            // Update the statistics div
            statisticsDiv.innerHTML = statsHtml;
        })
        .catch(error => {
            console.error('Error fetching technical indicators:', error);
            document.getElementById('statistics').textContent = 'Error loading statistics.';
        });
}

function loadPage() {
    if (isFirst) {
        isFirst = false
        fetch('http://localhost:8080/api/stocks/companies')
            .then(response => response.json())
            .then(data => {
                const companies = data;
                const companySelect = document.getElementById('company');
                companies.forEach(company => {
                    if (company !== "ALK") {
                        const option = document.createElement('option');
                        option.value = company;
                        option.textContent = company;
                        companySelect.appendChild(option);
                    }
                });
            })
            .catch(error => console.error('Error fetching companies:', error));

        fetchDashboardData()
        fetchLineChart()
        fetchStatisticsData()
        fetchNews()
    }
}

function reloadData() {
    const fromYear = document.getElementById('fromYear').value;
    const toYear = document.getElementById('toYear').value;
    const companySelected = document.getElementById('company').value;

    const urlParams = new URLSearchParams({ fromYear, toYear, companySelected });

    fetchDashboardData(urlParams);
    fetchLineChart(urlParams);
    fetchStatisticsData(urlParams);
    fetchNews(urlParams);
}

// document.addEventListener('DOMContentLoaded', () => {
//     fetchDashboardData();
//     fetchLineChart();
// });

loadPage()