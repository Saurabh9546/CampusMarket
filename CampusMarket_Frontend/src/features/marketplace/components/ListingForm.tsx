import { useState, type FormEvent } from 'react';
import { Input } from '@/components/Input/Input';
import { Button } from '@/components/Button/Button';
import { FormError } from '@/components/FormError/FormError';
import { CATEGORIES } from '@/types';
import styles from './ListingForm.module.css';

export interface ListingFormValues {
  title: string;
  description: string;
  category: string;
  price: number;
}

interface ListingFormProps {
  initialValues?: Partial<ListingFormValues>;
  submitLabel: string;
  isSubmitting: boolean;
  submitError?: string;
  onSubmit: (values: ListingFormValues) => void;
}

/** Shared by CreateListingPage and EditListingPage — same fields, same
 * validation, only the submit action and prefill values differ between
 * the two pages. Keeping this in one place means a validation rule change
 * only has to happen once. */
export function ListingForm({
  initialValues,
  submitLabel,
  isSubmitting,
  submitError,
  onSubmit,
}: ListingFormProps) {
  const [title, setTitle] = useState(initialValues?.title ?? '');
  const [description, setDescription] = useState(initialValues?.description ?? '');
  const [category, setCategory] = useState<string>(initialValues?.category ?? CATEGORIES[0]);
  const [price, setPrice] = useState(
    initialValues?.price != null ? String(initialValues.price) : '',
  );
  const [validationError, setValidationError] = useState<string | undefined>();

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setValidationError(undefined);

    if (!title.trim() || !description.trim() || !price) {
      setValidationError('All fields required.');
      return;
    }
    const priceNum = Number(price);
    if (Number.isNaN(priceNum) || priceNum <= 0) {
      setValidationError('Price must be a positive number.');
      return;
    }

    onSubmit({ title: title.trim(), description: description.trim(), category, price: priceNum });
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Input
        id="listing-title"
        label="Title"
        placeholder="e.g. Java Programming Book, 11th edition"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
      />

      <label className={styles.formLabel} htmlFor="listing-description">
        Description
      </label>
      <textarea
        id="listing-description"
        className={styles.textarea}
        rows={3}
        placeholder="Condition details, why you're selling, anything a buyer should know"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <div className={styles.formRow}>
        <div>
          <label className={styles.formLabel} htmlFor="listing-category">
            Category
          </label>
          <select
            id="listing-category"
            className={styles.select}
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>
        <div>
          <Input
            id="listing-price"
            label="Price (₹)"
            placeholder="450"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
          />
        </div>
      </div>

      <FormError message={validationError ?? submitError} />

      <Button type="submit" fullWidth isLoading={isSubmitting} style={{ marginTop: 16 }}>
        {submitLabel}
      </Button>
    </form>
  );
}