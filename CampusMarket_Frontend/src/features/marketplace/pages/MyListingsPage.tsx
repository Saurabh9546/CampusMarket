import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { productsApi, type ProductDto } from '@/api/products';
import { Navbar } from '@/components/Navbar/Navbar';
import { ROUTES } from '@/constants/routes';
import styles from './MyListingsPage.module.css';

export function MyListingsPage() {
  const navigate = useNavigate();
  const [listings, setListings] = useState<ProductDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();
  const [actionError, setActionError] = useState<string | undefined>();

  const loadListings = useCallback(async () => {
    setIsLoading(true);
    setError(undefined);
    const result = await productsApi.getMine();
    if (result.success) {
      setListings(result.data.content);
    } else {
      setError(result.message);
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    void loadListings();
  }, [loadListings]);

  const handleMarkSold = async (productId: number) => {
    setActionError(undefined);
    const result = await productsApi.markSold(productId);
    if (result.success) {
      setListings((prev) => prev.map((p) => (p.id === productId ? result.data : p)));
    } else {
      setActionError(result.message);
    }
  };

  const handleDelete = async (productId: number) => {
    if (!window.confirm('Delete this listing? This cannot be undone.')) return;
    setActionError(undefined);
    const result = await productsApi.delete(productId);
    if (result.success) {
      setListings((prev) => prev.filter((p) => p.id !== productId));
    } else {
      setActionError(result.message);
    }
  };

  return (
    <>
      <Navbar />
      <div className={styles.page}>
        <div className={styles.headerRow}>
          <h1 className={styles.pageTitle}>My Listings</h1>
        </div>

        {isLoading && <p className={styles.subtle}>Loading your listings...</p>}
        {error && <p className={styles.errorText}>{error}</p>}
        {actionError && <p className={styles.errorText}>{actionError}</p>}

        {!isLoading && !error && listings.length === 0 && (
          <div className={styles.emptyState}>
            <h3>You haven&apos;t listed anything yet</h3>
            <p className={styles.subtle}>Start selling to see your listings here.</p>
            <button className={styles.btnPrimary} onClick={() => navigate(ROUTES.sell)}>
              + Sell an item
            </button>
          </div>
        )}

        <div className={styles.list}>
          {listings.map((product) => (
            <div key={product.id} className={styles.row}>
              <div className={styles.thumb}>Photo</div>
              <div className={styles.info}>
                <div className={styles.title}>{product.title}</div>
                <div className={styles.meta}>
                  ₹{product.price} · {product.category}
                </div>
                <span
                  className={
                    product.status === 'AVAILABLE' ? styles.statusAvailable : styles.statusSold
                  }
                >
                  {product.status === 'AVAILABLE' ? 'Available' : 'Sold'}
                </span>
              </div>
              <div className={styles.actions}>
                <button
                  className={styles.actionBtn}
                  onClick={() => navigate(ROUTES.editListingPath(product.id))}
                >
                  Edit
                </button>
                {product.status === 'AVAILABLE' && (
                  <button className={styles.actionBtn} onClick={() => void handleMarkSold(product.id)}>
                    Mark Sold
                  </button>
                )}
                <button
                  className={`${styles.actionBtn} ${styles.deleteBtn}`}
                  onClick={() => void handleDelete(product.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}