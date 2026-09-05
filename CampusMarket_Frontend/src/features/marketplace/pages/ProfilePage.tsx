import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { productsApi } from '@/api/products';
import { wishlistApi } from '@/api/wishlist';
import { Avatar } from '@/components/Avatar/Avatar';
import { Navbar } from '@/components/Navbar/Navbar';
import { Input } from '@/components/Input/Input';
import { Button } from '@/components/Button/Button';
import { FormError } from '@/components/FormError/FormError';
import { ROUTES } from '@/constants/routes';
import styles from './ProfilePage.module.css';

function formatJoinedDate(isoString: string): string {
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });
}

export function ProfilePage() {
  const { currentUser, logout, updateProfile } = useAuth();
  const navigate = useNavigate();

  const [listingCount, setListingCount] = useState<number | null>(null);
  const [wishlistCount, setWishlistCount] = useState<number | null>(null);

  const [isEditing, setIsEditing] = useState(false);
  const [nameDraft, setNameDraft] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [editError, setEditError] = useState<string | undefined>();

  const loadCounts = useCallback(async () => {
    const [listingsResult, wishlistResult] = await Promise.all([
      productsApi.getMine(),
      wishlistApi.list(),
    ]);
    if (listingsResult.success) {
      setListingCount(listingsResult.data.totalElements);
    }
    if (wishlistResult.success) {
      setWishlistCount(wishlistResult.data.length);
    }
  }, []);

  useEffect(() => {
    void loadCounts();
  }, [loadCounts]);

  const handleLogout = async () => {
    await logout();
    navigate(ROUTES.login);
  };

  const startEditing = () => {
    setNameDraft(currentUser?.name ?? '');
    setEditError(undefined);
    setIsEditing(true);
  };

  const cancelEditing = () => {
    setIsEditing(false);
    setEditError(undefined);
  };

  const saveEditing = async () => {
    const trimmed = nameDraft.trim();
    if (!trimmed) {
      setEditError('Name is required');
      return;
    }
    setIsSaving(true);
    const result = await updateProfile(trimmed);
    setIsSaving(false);

    if (result.success) {
      setIsEditing(false);
    } else {
      setEditError(result.message);
    }
  };

  if (!currentUser) return null;

  return (
    <>
      <Navbar />
      <div className={styles.page}>
        <div className={styles.profileCard}>
          <Avatar name={currentUser.name} size="lg" />
          <div className={styles.profileInfo}>
            {isEditing ? (
              <div className={styles.editForm}>
                <Input
                  id="profile-name"
                  label="Full name"
                  value={nameDraft}
                  onChange={(e) => setNameDraft(e.target.value)}
                />
                <FormError message={editError} />
                <div className={styles.editActions}>
                  <Button onClick={() => void saveEditing()} isLoading={isSaving}>
                    Save
                  </Button>
                  <Button onClick={cancelEditing} disabled={isSaving}>
                    Cancel
                  </Button>
                </div>
              </div>
            ) : (
              <>
                <div className={styles.name}>{currentUser.name}</div>
                <div className={styles.meta}>
                  {currentUser.joinedDate && `Joined ${formatJoinedDate(currentUser.joinedDate)}`}
                </div>
                {currentUser.verified && (
                  <span className={styles.verifiedBadge}>✓ verified student</span>
                )}
              </>
            )}
          </div>
          {!isEditing && (
            <button className={styles.editButton} onClick={startEditing}>
              Edit profile
            </button>
          )}
        </div>

        <div className={styles.statsRow}>
          <button className={styles.statCard} onClick={() => navigate(ROUTES.myListings)}>
            <div className={styles.statLabel}>My listings</div>
            <div className={styles.statValue}>{listingCount ?? '–'}</div>
          </button>
          <button className={styles.statCard} onClick={() => navigate(ROUTES.wishlist)}>
            <div className={styles.statLabel}>Wishlist</div>
            <div className={styles.statValue}>{wishlistCount ?? '–'}</div>
          </button>
        </div>

        <button className={styles.logoutButton} onClick={() => void handleLogout()}>
          Log out
        </button>
      </div>
    </>
  );
}