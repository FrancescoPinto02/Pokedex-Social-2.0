import React from 'react';
import styles from './Home.module.scss';

const Home = () => {
  return (
    <div className={styles.home}>
      <section className={styles.hero}>
        <h1>Benvenuto su PokeDex 2.0</h1>
        <p>Esplora tutti i Pokémon e costruisci il tuo team!</p>
        <button>Scopri il Pokedex</button>
      </section>

      <section className={styles.features}>
        <div className={styles.feature}>
          <img src="/images/pokemon1.png" alt="Pokémon" />
          <h3>Lorem Ipsum</h3>
          <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>
        </div>
        <div className={styles.feature}>
          <img src="/images/pokemon2.png" alt="Pokémon" />
          <h3>Lorem Ipsum</h3>
          <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>
        </div>
      </section>
    </div>
  );
};

export default Home;