import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ListingForm, type ListingFormValues } from '../components/ListingForm';
import { productsApi } from '@/api/products';
import { ROUTES } from '@/constants/routes';
import { Navbar } from '@/components/Navbar/Navbar';
import styles from './CreateListingPage.module.css';

export function CreateListingPage() {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | undefined>();

  const handleSubmit = async (values: ListingFormValues) => {
    setIsSubmitting(true);
    setSubmitError(undefined);
    const result = await productsApi.create(values);
    setIsSubmitting(false);

    if (result.success) {
      navigate(ROUTES.home);
    } else {
      setSubmitError(result.message);
    }
  };

  return (
    <>
      <Navbar />
      <div className={styles.layout}>
        <h1 className={styles.pageTitle}>Sell an item</h1>
        <p className={styles.subtle}>Listing will be visible only to students at your college.</p>

        <ListingForm
          submitLabel="Publish listing"
          isSubmitting={isSubmitting}
          submitError={submitError}
          onSubmit={(values) => void handleSubmit(values)}
        />
      </div>
    </>
  );
}