import styles from './Home.module.scss';
import Hero from '../../components/Hero/Hero';

const Home = () => {
  return (
    <div className={styles.home}>
      <Hero
        title={"Welcome to \nPokédex-Social 2.0"}
        description="Discover all Pokémon, explore abilities, types, and build your collection."
        ctaText="Explore the Pokedex"
        ctaLink="/pokedex"
        imageSrc="/images/hero/hero1.png"
        imageAlt="Pokémon illustration"
      />
    </div>
  );
};

export default Home;