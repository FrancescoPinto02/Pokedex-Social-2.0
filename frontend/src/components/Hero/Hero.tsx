import { Link } from "react-router-dom";
import styles from "./Hero.module.scss";

type HeroProps = {
  title: string;
  description: string;
  ctaText: string;
  ctaLink: string;
  imageSrc: string;
  imageAlt?: string;
  gradient?: string; // opzionale: per cambiare colore background
};

const Hero: React.FC<HeroProps> = ({
  title,
  description,
  ctaText,
  ctaLink,
  imageSrc,
  imageAlt = "",
  gradient = "linear-gradient(135deg, #3b4cca, #ffcb05)",
}) => {
  return (
    <section className={styles.hero} style={{ background: gradient }}>
      <div className={styles.content}>
        <h1>{title}</h1>
        <p>{description}</p>
        <Link to={ctaLink} className={styles.cta}>
          {ctaText}
        </Link>
      </div>

      <div className={styles.image}>
        <img src={imageSrc} alt={imageAlt} />
      </div>
    </section>
  );
};

export default Hero;