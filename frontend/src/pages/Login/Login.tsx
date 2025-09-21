import {useContext, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {AuthContext} from "../../types/context/AuthContext.ts";
import styles from './Login.module.scss';
const API_URL = import.meta.env.VITE_API_URL;

const Login: React.FC = () => {
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        try {
            const response = await fetch(`${API_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) {
                throw new Error('Credenziali non valide');
            }

            const data = await response.json();
            login(data.token, data.userId, data.username);
            navigate('/'); // reindirizza alla home
        } catch (err: any) {
            setError(err.message);
        }
    };

    return (
        <div className={styles.loginPage}>
            <div className={styles.overlay}>
                <form className={styles.loginForm} onSubmit={handleSubmit}>
                    <img
                        src="/images/logo/pokemon_logo.png"
                        alt="Pokemon Logo"
                        className={styles.logo}
                    />
                    {error && <div className={styles.error}>{error}</div>}
                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <button type="submit">Login</button>
                    <p className={styles.registerLink}>
                        Non sei registrato? <Link to="/register">Crea un account</Link>
                    </p>
                </form>
            </div>
        </div>
    );
};

export default Login;