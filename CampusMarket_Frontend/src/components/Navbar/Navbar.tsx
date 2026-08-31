import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { ROUTES } from '@/constants/routes';
import { Avatar } from '@/components/Avatar/Avatar';
import { SearchBar } from '@/components/SearchBar/SearchBar';
import styles from './Navbar.module.css';

/** Shared across every authenticated page. Previously each page either
 * duplicated its own nav buttons (HomePage) or had no navigation at all
 * (Wishlist, My Listings, Messages, Product Detail, Create/Edit Listing).
 * This also wires up logout, which existed in AuthContext but had no
 * button anywhere in the UI calling it. */
export function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { currentUser, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);

  if (!currentUser) return null;

  const closeMenus = () => {
    setMobileMenuOpen(false);
    setProfileMenuOpen(false);
  };

  const handleLogout = async () => {
    closeMenus();
    await logout();
    navigate(ROUTES.login);
  };

  const goTo = (path: string) => {
    navigate(path);
    closeMenus();
  };

  // matchPrefix: Messages should stay highlighted while inside any
  // conversation thread (/messages/123), not just on the bare /messages list.
  const isActive = (path: string, matchPrefix = false) =>
    matchPrefix ? location.pathname.startsWith(path) : location.pathname === path;

  const navLinks = (
    <>
      <button
        className={`${styles.navLink} ${isActive(ROUTES.myListings) ? styles.navLinkActive : ''}`}
        onClick={() => goTo(ROUTES.myListings)}
      >
        📦 My Listings
      </button>
      <button
        className={`${styles.navLink} ${isActive(ROUTES.messages, true) ? styles.navLinkActive : ''}`}
        onClick={() => goTo(ROUTES.messages)}
      >
        💬 Messages
      </button>
      <button
        className={`${styles.navLink} ${isActive(ROUTES.wishlist) ? styles.navLinkActive : ''}`}
        onClick={() => goTo(ROUTES.wishlist)}
      >
        ♡ Wishlist
      </button>
      <button
        className={`${styles.btnPrimary} ${isActive(ROUTES.sell) ? styles.btnPrimaryActive : ''}`}
        onClick={() => goTo(ROUTES.sell)}
      >
        + Sell an item
      </button>
    </>
  );

  return (
    <header className={styles.navbar}>
      <div className={styles.navbarInner}>
        <button className={styles.logo} onClick={() => goTo(ROUTES.home)}>
          Campus<span className={styles.logoAccent}>Market</span>
        </button>

        <div className={styles.desktopSearchSlot}>
          <SearchBar onNavigate={closeMenus} />
        </div>

        <nav className={styles.desktopNav}>{navLinks}</nav>

        <button
          className={styles.hamburger}
          onClick={() => setMobileMenuOpen((v) => !v)}
          aria-label="Toggle menu"
        >
          ☰
        </button>

        <div className={styles.profileWrapper}>
          <button className={styles.profileChip} onClick={() => setProfileMenuOpen((v) => !v)}>
            <Avatar name={currentUser.name} size="sm" />
            <span className={styles.profileName}>{currentUser.name}</span>
          </button>
          {profileMenuOpen && (
            <div className={styles.profileDropdown}>
              <button className={styles.profileMenuItem} onClick={() => goTo(ROUTES.profile)}>
                View Profile
              </button>
              <button className={styles.logoutBtn} onClick={() => void handleLogout()}>
                Log out
              </button>
            </div>
          )}
        </div>
      </div>

      {mobileMenuOpen && (
        <div className={styles.mobileMenu}>
          <SearchBar placeholder="Search for items..." onNavigate={closeMenus} />
          {navLinks}
        </div>
      )}
    </header>
  );
}