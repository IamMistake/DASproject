import React, { useState, useEffect } from 'react';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import { fetchDashboardData, fetchLineChartData, fetchStatistics, fetchNews, fetchCompanies } from '../services/api';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

interface DashboardStats {
    low: string;
    high: string;
    closePrice: string;
    avgPrice: string;
    volume: string;
}

interface NewsItem {
    [key: string]: string;
}

interface StatisticsData {
    sma: {
        sma10: number[];
        sma20: number[];
        ema10: number[];
        ema20: number[];
        wma: number[];
    };
    oscillators: {
        rsi: number[];
        stochastic: number[];
        macd: number[];
        adx: number[];
        cci: number[];
    };
    signals: string[];
}

const Dashboard: React.FC = () => {
    const [fromYear, setFromYear] = useState<string>('2014');
    const [toYear, setToYear] = useState<string>('2024');
    const [companySelected, setCompanySelected] = useState<string>('ALK');
    const [companies, setCompanies] = useState<string[]>([]);
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [news, setNews] = useState<NewsItem | null>(null);
    const [statistics, setStatistics] = useState<StatisticsData | null>(null);
    const [chartData, setChartData] = useState<object>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const loadInitialData = async () => {
            try {
                // Fetch companies list
                const companiesData = await fetchCompanies();
                setCompanies(companiesData);

                // Fetch all dashboard data
                await reloadData();
            } catch (error) {
                console.error('Error loading initial data:', error);
            } finally {
                setLoading(false);
            }
        };

        loadInitialData();
    }, []);

    const reloadData = async () => {
        setLoading(true);
        try {
            const params = { companySelected, fromYear, toYear };

            // Fetch all data in parallel
            const [statsData, lineChartData, statisticsData, newsData] = await Promise.all([
                fetchDashboardData(params),
                fetchLineChartData(params),
                fetchStatistics(params),
                fetchNews(params)
            ]);

            setStats(statsData.data);
            setNews(newsData);
            setStatistics(statisticsData);

            // Prepare chart data
            if (lineChartData) {
                const reversedDates = [...lineChartData.dates].reverse();
                const reversedPrices = [...lineChartData.prices].reverse();
                const reversedSMA = [...lineChartData.sma].reverse();

                setChartData({
                    labels: reversedDates,
                    datasets: [
                        {
                            label: 'Stock Prices',
                            data: reversedPrices,
                            borderColor: 'rgb(59, 130, 246)',
                            backgroundColor: 'rgba(59, 130, 246, 0.5)',
                            tension: 0.1
                        },
                        {
                            label: 'SMA (10)',
                            data: reversedSMA,
                            borderColor: 'rgb(234, 88, 12)',
                            backgroundColor: 'rgba(234, 88, 12, 0.5)',
                            tension: 0.1
                        }
                    ]
                });
            }
        } catch (error) {
            console.error('Error reloading data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        reloadData();
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-screen bg-gray-900 text-white">
                Loading dashboard data...
            </div>
        );
    }

    return (
        <div className="flex h-screen bg-gray-900 text-white">
            {/* Sidebar */}
            <div className="w-1/5 bg-amber-300 p-5 flex flex-col gap-5 overflow-y-auto">
                <h2 className="text-center text-black text-2xl font-bold">UpTrendy</h2>

                <form onSubmit={handleSubmit} className="flex flex-col gap-3">
                    <div>
                        <label htmlFor="fromYear" className="block text-black font-medium mb-1">From Year</label>
                        <select
                            id="fromYear"
                            value={fromYear}
                            onChange={(e) => setFromYear(e.target.value)}
                            className="w-full p-2 rounded border-none bg-white text-black"
                        >
                            {Array.from({length: 11}, (_, i) => 2014 + i).map(year => (
                                <option key={year} value={year.toString()}>{year}</option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label htmlFor="toYear" className="block text-black font-medium mb-1">To Year</label>
                        <select
                            id="toYear"
                            value={toYear}
                            onChange={(e) => setToYear(e.target.value)}
                            className="w-full p-2 rounded border-none bg-white text-black"
                        >
                            {Array.from({length: 11}, (_, i) => 2014 + i).map(year => (
                                <option key={year} value={year.toString()}>{year}</option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label htmlFor="company" className="block text-black font-medium mb-1">Company</label>
                        <select
                            id="company"
                            value={companySelected}
                            onChange={(e) => setCompanySelected(e.target.value)}
                            className="w-full p-2 rounded border-none bg-white text-black"
                        >
                            {companies.map(company => (
                                <option key={company} value={company}>{company}</option>
                            ))}
                        </select>
                    </div>

                    <button
                        type="submit"
                        className="w-full p-2 bg-amber-700 text-white rounded cursor-pointer hover:bg-amber-800 transition-colors"
                    >
                        Apply Filters
                    </button>
                </form>

                <div className="news-section">
                    <h3 className="text-black font-bold text-lg mb-2">Latest News</h3>
                    {news ? (
                        <ul className="space-y-2">
                            {Object.entries(news).map(([headline, sentiment]) => (
                                <li key={headline} className="text-black text-sm">
                                    {headline} <span className="font-semibold">({sentiment})</span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="text-black">No news available</p>
                    )}
                </div>
            </div>

            {/* Main Content */}
            <div className="w-4/5 p-5 flex flex-col gap-5 overflow-y-auto">
                {/* Stats Header */}
                <div className="grid grid-cols-5 gap-3">
                    <div className="bg-amber-300 p-5 rounded text-center">
                        Min<br />
                        <span className="font-bold text-black">{stats?.low || 'N/A'}</span>
                    </div>
                    <div className="bg-amber-300 p-5 rounded text-center">
                        Max<br />
                        <span className="font-bold text-black">{stats?.high || 'N/A'}</span>
                    </div>
                    <div className="bg-amber-300 p-5 rounded text-center">
                        Last Price<br />
                        <span className="font-bold text-black">{stats?.closePrice || 'N/A'}</span>
                    </div>
                    <div className="bg-amber-300 p-5 rounded text-center">
                        Average<br />
                        <span className="font-bold text-black">{stats?.avgPrice || 'N/A'}</span>
                    </div>
                    <div className="bg-amber-300 p-5 rounded text-center">
                        Quantity<br />
                        <span className="font-bold text-black">{stats?.volume || 'N/A'}</span>
                    </div>
                </div>

                {/* Charts Section */}
                <div className="flex flex-1 gap-5">
                    {/* Line Chart */}
                    <div className="flex-1 bg-gray-800 rounded p-5">
                        {chartData ? (
                            <Line
                                data={chartData}
                                options={{
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    plugins: {
                                        legend: {
                                            position: 'top' as const,
                                            labels: {
                                                color: 'white'
                                            }
                                        },
                                    },
                                    scales: {
                                        x: {
                                            ticks: {
                                                color: 'white'
                                            },
                                            grid: {
                                                color: 'rgba(255, 255, 255, 0.1)'
                                            }
                                        },
                                        y: {
                                            ticks: {
                                                color: 'white'
                                            },
                                            grid: {
                                                color: 'rgba(255, 255, 255, 0.1)'
                                            }
                                        }
                                    }
                                }}
                            />
                        ) : (
                            <p>Loading chart data...</p>
                        )}
                    </div>

                    {/* Statistics */}
                    <div className="w-1/3 bg-gray-800 rounded p-5 overflow-y-auto">
                        {statistics ? (
                            <div className="statistics text-sm">
                                <h3 className="text-amber-300 font-bold text-lg mb-3">Oscillators and Moving Averages</h3>
                                <p className="mb-4">Technical indicators used to analyze historical prices and predict trends.</p>

                                <h4 className="text-amber-200 font-bold mb-2">Top 5 Moving Averages</h4>
                                <ul className="mb-4 space-y-1">
                                    <li>SMA (10): {statistics.sma.sma10[statistics.sma.sma10.length - 1]?.toFixed(2)}</li>
                                    <li>SMA (20): {statistics.sma.sma20[statistics.sma.sma20.length - 1]?.toFixed(2)}</li>
                                    <li>EMA (10): {statistics.sma.ema10[statistics.sma.ema10.length - 1]?.toFixed(2)}</li>
                                    <li>EMA (20): {statistics.sma.ema20[statistics.sma.ema20.length - 1]?.toFixed(2)}</li>
                                    <li>WMA: {statistics.sma.wma[statistics.sma.wma.length - 1]?.toFixed(2)}</li>
                                </ul>

                                <h4 className="text-amber-200 font-bold mb-2">Top 5 Oscillators</h4>
                                <ul className="mb-4 space-y-1">
                                    <li>RSI (14): {statistics.oscillators.rsi[statistics.oscillators.rsi.length - 1]?.toFixed(2)}</li>
                                    <li>Stochastic: {statistics.oscillators.stochastic[statistics.oscillators.stochastic.length - 1]?.toFixed(2)}</li>
                                    <li>MACD: {statistics.oscillators.macd[statistics.oscillators.macd.length - 1]?.toFixed(2)}</li>
                                    <li>ADX: {statistics.oscillators.adx[statistics.oscillators.adx.length - 1]?.toFixed(2)}</li>
                                    <li>CCI: {statistics.oscillators.cci[statistics.oscillators.cci.length - 1]?.toFixed(2)}</li>
                                </ul>

                                <h4 className="text-amber-200 font-bold mb-2">Signals</h4>
                                <p>Last Signal: {statistics.signals[statistics.signals.length - 1]}</p>
                            </div>
                        ) : (
                            <p>Loading statistics...</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;