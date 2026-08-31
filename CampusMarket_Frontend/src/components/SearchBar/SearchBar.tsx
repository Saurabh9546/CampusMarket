import { useState, useEffect, useRef, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { productsApi, type ProductDto } from '@/api/products';
import { ROUTES } from '@/constants/routes';
import styles from './SearchBar.module.css';

interface SearchBarProps {
  placeholder?: string;
  /** Called after any navigation away from this component — lets the
   * parent Navbar close its mobile menu when a result is picked. */
  onNavigate?: () => void;
}

const DEBOUNCE_MS = 350;
const MAX_SUGGESTIONS = 6;

export function SearchBar({
  placeholder = 'Search for items (e.g. calculator, laptop...)',
  onNavigate,
}: SearchBarProps) {
  const navigate = useNavigate();
  const [value, setValue] = useState('');
  const [results, setResults] = useState<ProductDto[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);

    const trimmed = value.trim();
    if (!trimmed) {
      setResults([]);
      setIsSearching(false);
      return;
    }

    setIsSearching(true);
    debounceRef.current = setTimeout(() => {
      void (async () => {
        const result = await productsApi.browse({ search: trimmed, page: 0 });
        if (result.success) {
          setResults(result.data.content.slice(0, MAX_SUGGESTIONS));
        } else {
          setResults([]);
        }
        setIsSearching(false);
      })();
    }, DEBOUNCE_MS);

    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [value]);

  const submitSearch = () => {
    const trimmed = value.trim();
    navigate(trimmed ? `${ROUTES.home}?search=${encodeURIComponent(trimmed)}` : ROUTES.home);
    setIsOpen(false);
    onNavigate?.();
  };

  const handleFormSubmit = (e: FormEvent) => {
    e.preventDefault();
    submitSearch();
  };

  const goToProduct = (productId: number) => {
    navigate(ROUTES.productDetailPath(productId));
    setIsOpen(false);
    setValue('');
    onNavigate?.();
  };

  return (
    <div className={styles.wrapper}>
      <form onSubmit={handleFormSubmit}>
        <input
          className={styles.input}
          placeholder={placeholder}
          value={value}
          onChange={(e) => {
            setValue(e.target.value);
            setIsOpen(true);
          }}
          onFocus={() => setIsOpen(true)}
          // Delay closing so a click/mousedown on a dropdown item can
          // register before the input's blur hides the dropdown.
          onBlur={() => setTimeout(() => setIsOpen(false), 150)}
        />
      </form>

      {isOpen && value.trim() && (
        <div className={styles.dropdown}>
          {isSearching && <div className={styles.dropdownMessage}>Searching...</div>}

          {!isSearching && results.length === 0 && (
            <div className={styles.dropdownMessage}>No matching listings</div>
          )}

          {!isSearching &&
            results.map((product) => (
              <button
                key={product.id}
                className={styles.resultItem}
                onMouseDown={() => goToProduct(product.id)}
              >
                <span className={styles.resultTitle}>{product.title}</span>
                <span className={styles.resultPrice}>₹{product.price}</span>
              </button>
            ))}

          {!isSearching && results.length > 0 && (
            <button className={styles.viewAll} onMouseDown={submitSearch}>
              View all results for &quot;{value.trim()}&quot;
            </button>
          )}
        </div>
      )}
    </div>
  );
}