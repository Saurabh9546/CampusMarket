import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { wishlistApi } from '@/api/wishlist';
import type { ProductDto } from '@/api/products';
import { ProductCard } from '../components/ProductCard';
import { Navbar } from '@/components/Navbar/Navbar';
import { ROUTES } from '@/constants/routes';
import styles from './HomePage.module.css'; // reuse layout/typography classes, no new CSS file needed

export function WishlistPage() {
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const [items, setItems] = useState<ProductDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();

  const loadWishlist = useCallback(async () => {
    setIsLoading(true);
    setError(undefined);
    const result = await wishlistApi.list();
    if (result.success) {
      setItems(result.data);
    } else {
      setError(result.message);
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    void loadWishlist();
  }, [loadWishlist]);

  // Instant remove, optimistic, rollback on failure — same pattern as Browse heart-toggle
  const removeFromWishlist = async (productId: number) => {
    const prevItems = items;
    setItems((current) => current.filter((p) => p.id !== productId));

    const result = await wishlistApi.remove(productId);
    if (!result.success) {
      setItems(prevItems);
    }
  };

  return (
    <>
      <Navbar />
      <div className={styles.layout}>
        <main className={styles.main} style={{ width: '100%' }}>
          <div className={styles.headerRow}>
            <div>
              <h1 className={styles.pageTitle}>My Wishlist</h1>
              <div className={styles.subtle}>{items.length} items saved</div>
            </div>
          </div>

          {isLoading && <p className={styles.subtle}>Loading wishlist...</p>}
          {error && <p className={styles.errorText}>{error}</p>}

          {!isLoading && !error && items.length === 0 && (
            <div className={styles.emptyState}>
              <h3>No items in your wishlist yet</h3>
              <p className={styles.subtle}>
                Browse listings and tap the heart icon to save items here.
              </p>
              <button className={styles.btnSecondary} onClick={() => navigate(ROUTES.home)}>
                Go to Browse
              </button>
            </div>
          )}

          <div className={styles.grid}>
            {items.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                isWishlisted={true}
                onWishlistToggle={(id) => void removeFromWishlist(id)}
                isOwnListing={!!currentUser && Number(currentUser.id) === product.sellerId}
              />
            ))}
          </div>
        </main>
      </div>
    </>
  );
}