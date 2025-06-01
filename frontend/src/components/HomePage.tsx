import React from 'react';
import { useNavigate } from 'react-router-dom';

const HomePage: React.FC = () => {
    const navigate = useNavigate();
    const companies = ['Alkaloid', 'Komercijalna', 'Stopanska'];

    const handleCompanyClick = (company: string) => {
        console.log('Selected company:', company);
    };

    const navigateToDashboard = () => {
        navigate('/dashboard');
    };

    return (
        <div className="flex h-screen">
            {/* Sidebar */}
            <div className="w-1/5 h-full bg-amber-300 p-5 flex flex-col items-center gap-5">
                <h1 className="text-center text-white font-bold text-4xl">UpTrendy</h1>
                <h2 className="text-center text-white text-2xl">TOP 10 Companies</h2>

                <div className="w-full">
                    {companies.map((company) => (
                        <button
                            key={company}
                            className="w-full bg-yellow-200 text-black p-3 mb-3 text-center border-none cursor-pointer text-lg hover:bg-amber-400 transition-colors"
                            onClick={() => handleCompanyClick(company)}
                        >
                            {company}
                        </button>
                    ))}
                </div>
            </div>

            {/* Main Content */}
            <div className="w-4/5 h-full bg-black flex justify-around items-center">
                <button
                    onClick={navigateToDashboard}
                    className="w-48 h-24 bg-amber-300 text-black flex justify-center items-center text-xl border-none cursor-pointer relative hover:opacity-90"
                >
                    DASHBOARD
                    <span className="absolute bottom-2 left-1/2 transform -translate-x-1/2 w-1/2 h-1 bg-amber-300"></span>
                </button>
            </div>
        </div>
    );
};

export default HomePage;