import { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { productsApi, type ProductDto, type BrowseParams } from '@/api/products';
import { wishlistApi } from '@/api/wishlist';
import { ProductCard } from '../components/ProductCard';
import { Navbar } from '@/components/Navbar/Navbar';
import { CATEGORIES } from '@/types';
import styles from './HomePage.module.css';

export function HomePage() {
  const { currentUser } = useAuth();
  const [searchParams] = useSearchParams();
  const searchFromUrl = searchParams.get('search') ?? undefined;

  const [products, setProducts] = useState<ProductDto[]>([]);
  const [wishlistIds, setWishlistIds] = useState<Set<number>>(new Set());
  const [category, setCategory] = useState<string | undefined>(undefined);
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();
  // Controls the price-range popover near the header (previously controlled
  // the mobile sidebar toggle — repurposed since the sidebar no longer exists).
  const [filtersOpen, setFiltersOpen] = useState(false);

  const loadProducts = useCallback(async (params: BrowseParams) => {
    setIsLoading(true);
    setError(undefined);
    const result = await productsApi.browse(params);
    if (result.success) {
      setProducts(result.data.content);
    } else {
      setError(result.message);
    }
    setIsLoading(false);
  }, []);

  const loadWishlist = useCallback(async () => {
    const result = await wishlistApi.list();
    if (result.success) {
      setWishlistIds(new Set(result.data.map((p) => p.id)));
    }
  }, []);

  useEffect(() => {
    void loadProducts({ category, search: searchFromUrl });
    void loadWishlist();
  }, [category, searchFromUrl, loadProducts, loadWishlist]);

  const applyPriceFilter = () => {
    void loadProducts({
      category,
      search: searchFromUrl,
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
    });
  };

  const toggleWishlist = async (productId: number) => {
    const isWishlisted = wishlistIds.has(productId);
    setWishlistIds((prev) => {
      const next = new Set(prev);
      if (isWishlisted) next.delete(productId);
      else next.add(productId);
      return next;
    });

    const result = isWishlisted
      ? await wishlistApi.remove(productId)
      : await wishlistApi.add(productId);

    if (!result.success) {
      setWishlistIds((prev) => {
        const next = new Set(prev);
        if (isWishlisted) next.add(productId);
        else next.delete(productId);
        return next;
      });
    }
  };

  return (
    <>
      <Navbar />
      <div className={styles.layout}>
        <div className={styles.categoryBar}>
          <button
            className={category === undefined ? styles.pillActive : styles.pill}
            onClick={() => setCategory(undefined)}
          >
            All categories
          </button>
          {CATEGORIES.map((c) => (
            <button
              key={c}
              className={category === c ? styles.pillActive : styles.pill}
              onClick={() => setCategory(c)}
            >
              {c}
            </button>
          ))}
        </div>

        <main className={styles.main}>
          <div className={styles.headerRow}>
            <div>
              <h1 className={styles.pageTitle}>
                {searchFromUrl ? `Results for "${searchFromUrl}"` : 'All listings'}
              </h1>
              <div className={styles.subtle}>
                {currentUser ? 'Your college · ' : ''}
                {products.length} items found
              </div>
            </div>

            <div className={styles.filterPopoverWrapper}>
              <button
                className={styles.btnSecondary}
                onClick={() => setFiltersOpen((v) => !v)}
              >
                Price {filtersOpen ? '▲' : '▼'}
              </button>
              {filtersOpen && (
                <div className={styles.filterPopover}>
                  <div className={styles.subtleSmall}>Price range</div>
                  <div style={{ display: 'flex', gap: 6 }}>
                    <input
                      className={styles.forminput}
                      placeholder="Min"
                      value={minPrice}
                      onChange={(e) => setMinPrice(e.target.value)}
                    />
                    <input
                      className={styles.forminput}
                      placeholder="Max"
                      value={maxPrice}
                      onChange={(e) => setMaxPrice(e.target.value)}
                    />
                  </div>
                  <button
                    className={`${styles.btnSecondary} ${styles.fullWidth}`}
                    onClick={() => {
                      applyPriceFilter();
                      setFiltersOpen(false);
                    }}
                  >
                    Apply
                  </button>
                </div>
              )}
            </div>
          </div>

          {isLoading && <p className={styles.subtle}>Loading listings...</p>}
          {error && <p className={styles.errorText}>{error}</p>}

          {!isLoading && !error && products.length === 0 && (
            <div className={styles.emptyState}>
              <h3>No listings yet</h3>
              <p className={styles.subtle}>Be the first to post one.</p>
            </div>
          )}

          <div className={styles.grid}>
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                isWishlisted={wishlistIds.has(product.id)}
                onWishlistToggle={(id) => void toggleWishlist(id)}
                isOwnListing={!!currentUser && Number(currentUser.id) === product.sellerId}
              />
            ))}
          </div>
        </main>
      </div>
    </>
  );
}