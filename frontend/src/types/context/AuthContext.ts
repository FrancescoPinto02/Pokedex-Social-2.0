import { createContext } from 'react';

interface AuthContextType {
    token: string | null;
    userId: number | null;
    username: string | null;
    isAuthenticated: boolean;
    loading: boolean;
    login: (token: string, userId: number, username: string) => void;
    logout: () => void;
}

export const AuthContext = createContext<AuthContextType>({
    token: null,
    userId: null,
    username: null,
    isAuthenticated: false,
    loading: true,
    login: () => {},
    logout: () => {},
});
