import {useContext, useEffect, useState} from "react";
import type {UserData} from "../../types/Profile/UserData.ts";
import {AuthContext} from "../../types/context/AuthContext.ts";
import styles from './Profile.module.scss';
import {useNavigate} from "react-router-dom";

const Profile: React.FC = () => {
    const { token, userId, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    const [user, setUser] = useState<UserData | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const response = await fetch(`http://127.0.0.1:8080/user/${userId}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                if (!response.ok) throw new Error('Impossibile ottenere i dati utente');

                const data: UserData = await response.json();
                setUser(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        if (userId && token) fetchUserData();
    }, [userId, token]);

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    if (loading) return <div className={styles.loading}>Caricamento...</div>;
    if (error) return <div className={styles.error}>{error}</div>;

    return (
        <div className={styles.profilePage}>
            <div className={styles.card}>
                <img
                    src="/images/profile/profile.png"
                    alt="Avatar"
                    className={styles.avatar}
                />
                <h2>{user?.username}</h2>
                <p><strong>Nome:</strong> {user?.firstName} {user?.lastName}</p>
                <p><strong>Data di nascita:</strong> {user?.birthDate}</p>
                <p className={styles.pokecoins}>
                    <img src="/images/profile/pokecoin.png" alt="Pokecoin" className={styles.coin} /> {user?.pokecoins} Pokecoins
                </p>

                {/* Pulsante logout */}
                <button className={styles.logoutButton} onClick={handleLogout}>
                    Logout
                </button>
            </div>
        </div>
    );
};

export default Profile;