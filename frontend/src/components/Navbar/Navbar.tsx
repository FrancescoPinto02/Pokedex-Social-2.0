import { useState } from "react";
import { FaHome, FaBook, FaStar, FaUsers, FaBars, FaTimes } from "react-icons/fa";
import styles from "./Navbar.module.scss";
import NavButton from "./NavButton";

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);

  const navItems = [
    { path: "/", label: "Home", icon: <FaHome />, color: "#ee1515" },
    { path: "/pokedex", label: "Pokedex", icon: <FaBook />, color: "#3b4cca" },
    { path: "/link1", label: "Link1", icon: <FaStar />, color: "#ffcb05" },
    { path: "/profile", label: "Profile", icon: <FaUsers />, color: "#2a9d8f" },
  ];

  return (
    <nav className={styles.navbar}>
      {/* Hamburger */}
      <button
        className={styles.hamburger}
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Menu"
      >
        {isOpen ? <FaTimes /> : <FaBars />}
      </button>

      {/* Menu */}
      <ul className={`${styles.menu} ${isOpen ? styles.open : ""}`}>
        {navItems.map((item) => (
          <NavButton
            key={item.path}
            to={item.path}
            label={item.label}
            icon={item.icon}
            color={item.color}
            onClick={() => setIsOpen(false)}
          />
        ))}
      </ul>
    </nav>
  );
};

export default Navbar;
