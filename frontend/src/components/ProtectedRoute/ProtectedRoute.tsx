import React, {type JSX, useContext} from 'react';
import { Navigate } from 'react-router-dom';
import {AuthContext} from "../../types/context/AuthContext.ts";

interface ProtectedRouteProps {
    children: JSX.Element;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const { isAuthenticated, loading } = useContext(AuthContext);

    if (loading) return <div style={{ textAlign: 'center', marginTop: '2rem' }}>Loading...</div>;

    if (!isAuthenticated) return <Navigate to="/login" replace />;

    return children;
};

export default ProtectedRoute;