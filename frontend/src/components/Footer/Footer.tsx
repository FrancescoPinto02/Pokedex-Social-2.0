import { FaFacebookF, FaTwitter, FaInstagram, FaGithub, FaLinkedin } from "react-icons/fa";
import { Link } from "react-router-dom";
import styles from "./Footer.module.scss";

const Footer = () => {
  return (
    <footer className={styles.footer}>
      {/* Brand / Logo */}
      <div className={styles.brand}>
        <h2>Pokédex-Social 2.0</h2>
        <p>© 2025 All rights reserved</p>
      </div>

      {/* Quick Links */}
      <ul className={styles.links}>
        <li><Link to="/">Home</Link></li>
        <li><Link to="/pokedex">Pokedex</Link></li>
        <li><Link to="/link1">Link1</Link></li>
        <li><Link to="/link2">Link2</Link></li>
      </ul>

      {/* Social Icons */}
      <div className={styles.social}>
        <a href="#" aria-label="Facebook"><FaFacebookF /></a>
        <a href="https://www.linkedin.com/in/francescoalessandropinto/" aria-label="Linkedin"><FaLinkedin /></a>
        <a href="#" aria-label="Instagram"><FaInstagram /></a>
        <a href="https://github.com/FrancescoPinto02" aria-label="GitHub"><FaGithub /></a>
      </div>

      {/* Disclaimer */}
      <div className={styles.disclaimer}>
        Pokémon™ and all related characters are trademarks of Nintendo, Game Freak, and The Pokémon Company. This site is for educational and non-commercial purposes only.
      </div>
    </footer>
  );
};

export default Footer;