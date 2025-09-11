import React from 'react';
import styles from './Navbar.module.scss';

const Navbar = () => {
  return (
    <nav className={styles.navbar}>
      <div className={styles.logo}>PokeDex 2.0</div>
      <ul className={styles.menu}>
        <li><a href="/">Home</a></li>
        <li><a href="/pokedex">Pokedex</a></li>
        <li><a href="/teams">Teams</a></li>
        <li><a href="/login">Login</a></li>
      </ul>
    </nav>
  );
};

export default Navbar;
