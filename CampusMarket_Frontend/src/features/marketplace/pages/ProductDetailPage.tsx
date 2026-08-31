import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { productsApi, type ProductDto } from '@/api/products';
import { messagesApi } from '@/api/messages';
import { ROUTES } from '@/constants/routes';
import { Avatar } from '@/components/Avatar/Avatar';
import { Navbar } from '@/components/Navbar/Navbar';
import styles from './ProductDetailPage.module.css';

export function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { currentUser } = useAuth();

  const [product, setProduct] = useState<ProductDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();
  const [isStartingConversation, setIsStartingConversation] = useState(false);
  const [messageError, setMessageError] = useState<string | undefined>();

  const loadProduct = useCallback(async (productId: number) => {
    setIsLoading(true);
    setError(undefined);
    const result = await productsApi.getById(productId);
    if (result.success) {
      setProduct(result.data);
    } else {
      setError(result.message);
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    const productId = Number(id);
    if (!id || Number.isNaN(productId)) {
      setError('Invalid listing.');
      setIsLoading(false);
      return;
    }
    void loadProduct(productId);
  }, [id, loadProduct]);

  const handleMessageSeller = async () => {
    if (!product) return;
    setIsStartingConversation(true);
    setMessageError(undefined);

    const result = await messagesApi.createConversation(product.id);
    setIsStartingConversation(false);

    if (result.success) {
      navigate(ROUTES.conversationPath(result.data.id));
    } else {
      setMessageError(result.message);
    }
  };

  if (isLoading) {
    return (
      <>
        <Navbar />
        <div className={styles.page}>
          <p className={styles.subtle}>Loading listing...</p>
        </div>
      </>
    );
  }

  if (error || !product) {
    return (
      <>
        <Navbar />
        <div className={styles.page}>
          <p className={styles.errorText}>{error ?? 'Listing not found.'}</p>
          <button className={styles.btnSecondary} onClick={() => navigate(ROUTES.home)}>
            Back to Browse
          </button>
        </div>
      </>
    );
  }

  const isOwnListing = !!currentUser && Number(currentUser.id) === product.sellerId;

  return (
    <>
      <Navbar />
      <div className={styles.page}>
        <button className={styles.backLink} onClick={() => navigate(ROUTES.home)}>
          ← Back to Browse
        </button>

        <div className={styles.layout}>
          <div className={styles.imageBlock}>Photo</div>

          <div className={styles.infoBlock}>
            <div className={styles.headerRow}>
              <h1 className={styles.title}>{product.title}</h1>
              <span
                className={
                  product.status === 'AVAILABLE' ? styles.statusAvailable : styles.statusSold
                }
              >
                {product.status === 'AVAILABLE' ? 'Available' : 'Sold'}
              </span>
            </div>

            <div className={styles.price}>₹{product.price}</div>
            <div className={styles.category}>{product.category}</div>

            <p className={styles.description}>{product.description}</p>

            <div className={styles.sellerCard}>
              <Avatar name={product.sellerName ?? '?'} size="md" />
              <div>
                <div className={styles.subtleSmall}>Seller</div>
                <div className={styles.sellerName}>{product.sellerName ?? 'Unknown user'}</div>
              </div>
            </div>

            {isOwnListing ? (
              <div className={styles.ownNotice}>This is your listing.</div>
            ) : (
              <>
                <button
                  className={styles.btnPrimary}
                  onClick={() => void handleMessageSeller()}
                  disabled={isStartingConversation}
                >
                  {isStartingConversation ? 'Starting conversation...' : 'Message Seller'}
                </button>
                {messageError && <p className={styles.errorText}>{messageError}</p>}
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
}