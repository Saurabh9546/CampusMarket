import { useNavigate } from 'react-router-dom';
import type { ProductDto } from '@/api/products';
import { ROUTES } from '@/constants/routes';
import styles from './ProductCard.module.css';

interface ProductCardProps {
  product: ProductDto;
  isWishlisted: boolean;
  onWishlistToggle: (productId: number) => void;
  isOwnListing?: boolean;
}

export function ProductCard({
  product,
  isWishlisted,
  onWishlistToggle,
  isOwnListing = false,
}: ProductCardProps) {
  const navigate = useNavigate();

  const goToDetail = () => {
    navigate(ROUTES.productDetailPath(product.id));
  };

  const handleHeartClick = (e: React.MouseEvent) => {
    // Stop this click from bubbling up to the card's own onClick —
    // otherwise tapping the heart would also navigate to the detail page.
    e.stopPropagation();
    onWishlistToggle(product.id);
  };

  return (
    <div
      className={styles.listingCard}
      onClick={goToDetail}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') goToDetail();
      }}
    >
      <div className={styles.listingImg}>
        Photo
        <button
          className={styles.heart}
          onClick={handleHeartClick}
          aria-label="Toggle wishlist"
        >
          {isWishlisted ? '♥' : '♡'}
        </button>
      </div>
      <div className={styles.listingBody}>
        <div className={styles.listingTitle}>{product.title}</div>
        <div className={styles.listingPrice}>₹{product.price}</div>
        <div className={styles.listingMeta}>
          {product.category}
          {isOwnListing && <span className={styles.ownBadge}> · Your listing</span>}
        </div>
        <div className={styles.listingMetaRow}>
          <span
            className={
              product.status === 'AVAILABLE' ? styles.statusAvailable : styles.statusSold
            }
          >
            {product.status === 'AVAILABLE' ? 'Available' : 'Sold'}
          </span>
        </div>
      </div>
    </div>
  );
}