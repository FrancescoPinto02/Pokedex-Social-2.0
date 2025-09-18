import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import styles from './Register.module.scss';
import {AuthContext} from "../../types/context/AuthContext.ts";
const API_URL = import.meta.env.VITE_API_URL;

interface FormData {
    email: string;
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    birthDate: string;
}

const Register: React.FC = () => {
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const [formData, setFormData] = useState<FormData>({
        email: '',
        username: '',
        password: '',
        firstName: '',
        lastName: '',
        birthDate: '',
    });

    const [errors, setErrors] = useState<Partial<FormData>>({});
    const [loading, setLoading] = useState(false);
    const [serverError, setServerError] = useState('');

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
        setErrors({ ...errors, [e.target.name]: '' }); // reset errore su quel campo
        setServerError('');
    };

    const validate = (): boolean => {
        const newErrors: Partial<FormData> = {};

        // Email
        if (!formData.email) newErrors.email = 'Email is required';
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email))
            newErrors.email = 'Invalid email format';

        // Username
        if (!formData.username) newErrors.username = 'Username is required';
        else if (formData.username.length < 5 || formData.username.length > 50)
            newErrors.username = 'Username must be between 5 and 50 characters';
        else if (!/^[a-zA-Z0-9._-]{5,50}$/.test(formData.username))
            newErrors.username = 'Username can only contain letters, numbers, dots, underscores, and hyphens';

        // Password
        if (!formData.password) newErrors.password = 'Password is required';
        else if (formData.password.length < 8 || formData.password.length > 64)
            newErrors.password = 'Password must be between 8 and 64 characters';
        else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,64}$/.test(formData.password))
            newErrors.password = 'Password must contain uppercase, lowercase, number and special character';

        // First Name
        if (!formData.firstName) newErrors.firstName = 'First name is required';
        else if (formData.firstName.length > 50) newErrors.firstName = 'First name cannot exceed 50 characters';

        // Last Name
        if (!formData.lastName) newErrors.lastName = 'Last name is required';
        else if (formData.lastName.length > 50) newErrors.lastName = 'Last name cannot exceed 50 characters';

        // Birth Date
        if (!formData.birthDate) newErrors.birthDate = 'Birth date is required';
        else if (new Date(formData.birthDate) >= new Date())
            newErrors.birthDate = 'Birth date must be in the past';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setServerError('');

        if (!validate()) return;

        setLoading(true);

        try {
            const response = await fetch(`${API_URL}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData),
            });

            if (response.status === 409) {
                const errData = await response.json();
                if (errData.detail.includes('Email')) {
                    setErrors((prev) => ({ ...prev, email: errData.detail }));
                } else if (errData.detail.includes('Username')) {
                    setErrors((prev) => ({ ...prev, username: errData.detail }));
                } else {
                    setServerError(errData.detail || 'Conflict error');
                }
                return;
            }

            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.message || 'Errore nella registrazione');
            }

            const data = await response.json();
            login(data.token, data.userId, data.username);
            navigate('/profile');
        } catch (err: any) {
            setServerError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.registerPage}>
            <div className={styles.overlay}>
                <div className={styles.formContainer}>
                    <img src="/images/logo/pokemon_logo.png" alt="Logo Pokémon" className={styles.logo} />

                    <h2>Crea il tuo account</h2>

                    {serverError && <div className={styles.error}>{serverError}</div>}

                    <form onSubmit={handleSubmit} className={styles.form}>
                        <input type="email" name="email" placeholder="Email" value={formData.email} onChange={handleChange} />
                        {errors.email && <span className={styles.fieldError}>{errors.email}</span>}

                        <input type="text" name="username" placeholder="Username" value={formData.username} onChange={handleChange} />
                        {errors.username && <span className={styles.fieldError}>{errors.username}</span>}

                        <input type="password" name="password" placeholder="Password" value={formData.password} onChange={handleChange} />
                        {errors.password && <span className={styles.fieldError}>{errors.password}</span>}

                        <input type="text" name="firstName" placeholder="Nome" value={formData.firstName} onChange={handleChange} />
                        {errors.firstName && <span className={styles.fieldError}>{errors.firstName}</span>}

                        <input type="text" name="lastName" placeholder="Cognome" value={formData.lastName} onChange={handleChange} />
                        {errors.lastName && <span className={styles.fieldError}>{errors.lastName}</span>}

                        <input type="date" name="birthDate" placeholder="Data di nascita" value={formData.birthDate} onChange={handleChange} />
                        {errors.birthDate && <span className={styles.fieldError}>{errors.birthDate}</span>}

                        <button type="submit" disabled={loading}>
                            {loading ? 'Registrazione...' : 'Registrati'}
                        </button>
                    </form>

                    <p className={styles.switch}>
                        Hai già un account? <Link to="/login">Accedi</Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Register;