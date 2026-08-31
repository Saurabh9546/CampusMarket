import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { ListingForm, type ListingFormValues } from '../components/ListingForm';
import { productsApi, type ProductDto } from '@/api/products';
import { ROUTES } from '@/constants/routes';
import { Navbar } from '@/components/Navbar/Navbar';
import styles from './CreateListingPage.module.css';

export function EditListingPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { currentUser } = useAuth();

  const [product, setProduct] = useState<ProductDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | undefined>();

  const loadProduct = useCallback(async (productId: number) => {
    setIsLoading(true);
    setLoadError(undefined);
    const result = await productsApi.getById(productId);
    if (result.success) {
      setProduct(result.data);
    } else {
      setLoadError(result.message);
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    const productId = Number(id);
    if (!id || Number.isNaN(productId)) {
      setLoadError('Invalid listing.');
      setIsLoading(false);
      return;
    }
    void loadProduct(productId);
  }, [id, loadProduct]);

  const handleSubmit = async (values: ListingFormValues) => {
    if (!product) return;
    setIsSubmitting(true);
    setSubmitError(undefined);
    const result = await productsApi.update(product.id, values);
    setIsSubmitting(false);

    if (result.success) {
      navigate(ROUTES.myListings);
    } else {
      setSubmitError(result.message);
    }
  };

  if (isLoading) {
    return (
      <>
        <Navbar />
        <div className={styles.layout}>
          <p className={styles.subtle}>Loading listing...</p>
        </div>
      </>
    );
  }

  if (loadError || !product) {
    return (
      <>
        <Navbar />
        <div className={styles.layout}>
          <p className={styles.subtle}>{loadError ?? 'Listing not found.'}</p>
        </div>
      </>
    );
  }

  const isOwner = !!currentUser && Number(currentUser.id) === product.sellerId;
  if (!isOwner) {
    return (
      <>
        <Navbar />
        <div className={styles.layout}>
          <p className={styles.subtle}>You can only edit your own listings.</p>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className={styles.layout}>
        <h1 className={styles.pageTitle}>Edit listing</h1>

        <ListingForm
          initialValues={{
            title: product.title,
            description: product.description,
            category: product.category,
            price: product.price,
          }}
          submitLabel="Save changes"
          isSubmitting={isSubmitting}
          submitError={submitError}
          onSubmit={(values) => void handleSubmit(values)}
        />
      </div>
    </>
  );
}