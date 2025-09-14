import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import styles from './NavButton.module.scss';

type NavButtonProps = {
  to: string;
  label: string;
  icon: React.ReactNode;
  color: string;
  onClick?: () => void;
};

const NavButton: React.FC<NavButtonProps> = ({ to, label, icon, color, onClick }) => {
  const location = useLocation();
  const isActive = location.pathname === to;

  return (
    <li className={styles.navItem}
        style={{ '--btn-color': color } as React.CSSProperties}
        onClick={onClick}
    >
      <Link
        to={to}
        className={`${styles.navButton} ${isActive ? styles.active : ''}`}
      >
        <div className={styles.icon}>{icon}</div>
        <span className={styles.label}>{label}</span>
      </Link>
    </li>
  );
};

export default NavButton;
