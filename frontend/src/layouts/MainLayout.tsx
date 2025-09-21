import React from 'react';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';

interface Props {
    children: React.ReactNode;
}

const MainLayout: React.FC<Props> = ({ children }) => {
    return (
        <div className="app-container">
            <Navbar />
            <div className="main-container">
                <main>{children}</main>
            </div>
            <Footer />
        </div>
    );
};

export default MainLayout;