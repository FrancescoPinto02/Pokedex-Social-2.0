import React, { useState, useEffect } from 'react';
import { AuthContext } from '../types/context/AuthContext';

interface AuthProviderProps {
    children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [token, setToken] = useState<string | null>(null);
    const [userId, setUserId] = useState<number | null>(null);
    const [username, setUsername] = useState<string | null>(null);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Carica dati da localStorage all'avvio
        const storedToken = localStorage.getItem('token');
        const storedUserId = localStorage.getItem('userId');
        const storedUsername = localStorage.getItem('username');

        if (storedToken && storedUserId && storedUsername) {
            setToken(storedToken);
            setUserId(Number(storedUserId));
            setUsername(storedUsername);
            setIsAuthenticated(true);
        }

        setLoading(false); // fine caricamento
    }, []);

    const login = (token: string, userId: number, username: string) => {
        localStorage.setItem('token', token);
        localStorage.setItem('userId', userId.toString());
        localStorage.setItem('username', username);

        setToken(token);
        setUserId(userId);
        setUsername(username);
        setIsAuthenticated(true);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');

        setToken(null);
        setUserId(null);
        setUsername(null);
        setIsAuthenticated(false);
    };

    return (
        <AuthContext.Provider
            value={{ token, userId, username, isAuthenticated, loading, login, logout }}
        >
            {children}
        </AuthContext.Provider>
    );
};

