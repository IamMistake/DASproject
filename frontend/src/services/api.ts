import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/stocks';

export const fetchCompanies = async () => {
    const response = await axios.get(`${API_BASE_URL}/companies`);
    return response.data;
};

export const fetchDashboardData = async (params: { companySelected: string; fromYear: string; toYear: string }) => {
    const response = await axios.get(`${API_BASE_URL}/dashboard/stats`, { params });
    return response.data;
};

export const fetchLineChartData = async (params: { companySelected: string; fromYear: string; toYear: string }) => {
    const response = await axios.get(`${API_BASE_URL}/dashboard/lineChart`, { params });
    return response.data;
};

export const fetchStatistics = async (params: { companySelected: string; fromYear: string; toYear: string }) => {
    const response = await axios.get(`${API_BASE_URL}/dashboard/statistics`, { params });
    return response.data;
};

export const fetchNews = async (params: { companySelected: string }) => {
    const response = await axios.get(`${API_BASE_URL}/dashboard/news`, { params });
    return response.data;
};
