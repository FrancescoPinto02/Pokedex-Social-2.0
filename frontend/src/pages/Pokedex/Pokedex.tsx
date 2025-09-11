import React from 'react';
import styles from './Pokedex.module.scss';

const Pokedex = () => {
  return (
    <div className={styles.pokedex}>
      <h1>Il tuo Pokedex</h1>
      <p>Qui in futuro vedrai tutti i Pokémon!</p>
    </div>
  );
};

export default Pokedex;
