import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { RegisterPage } from '@/features/auth/pages/RegisterPage';
import { VerifyEmailPage } from '@/features/auth/pages/VerifyEmailPage';
import { ForgotPasswordPage } from '@/features/auth/pages/ForgotPasswordPage';
import { ResetPasswordPage } from '@/features/auth/pages/ResetPasswordPage';
import { ResendVerificationPage } from '@/features/auth/pages/ResendVerificationPage';
import { HomePage } from '@/features/marketplace/pages/HomePage';
import { CreateListingPage } from '@/features/marketplace/pages/CreateListingPage';
import { EditListingPage } from '@/features/marketplace/pages/EditListingPage';
import { MyListingsPage } from '@/features/marketplace/pages/MyListingsPage';
import { WishlistPage } from '@/features/marketplace/pages/WishlistPage';
import { ProductDetailPage } from '@/features/marketplace/pages/ProductDetailPage';
import { MessagesPage } from '@/features/marketplace/pages/MessagesPage';
import { ProfilePage } from '@/features/marketplace/pages/ProfilePage';
import { ProtectedRoute } from './ProtectedRoute';
import { ROUTES } from '@/constants/routes';

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to={ROUTES.login} replace />} />
        <Route path={ROUTES.login} element={<LoginPage />} />
        <Route path={ROUTES.register} element={<RegisterPage />} />
        <Route path={ROUTES.verifyEmail} element={<VerifyEmailPage />} />
        <Route path={ROUTES.forgotPassword} element={<ForgotPasswordPage />} />
        <Route path={ROUTES.resetPassword} element={<ResetPasswordPage />} />
        <Route path={ROUTES.resendVerification} element={<ResendVerificationPage />} />
        <Route
          path={ROUTES.home}
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.sell}
          element={
            <ProtectedRoute>
              <CreateListingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.myListings}
          element={
            <ProtectedRoute>
              <MyListingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.editListing}
          element={
            <ProtectedRoute>
              <EditListingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.wishlist}
          element={
            <ProtectedRoute>
              <WishlistPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.productDetail}
          element={
            <ProtectedRoute>
              <ProductDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.messages}
          element={
            <ProtectedRoute>
              <MessagesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.conversation}
          element={
            <ProtectedRoute>
              <MessagesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={ROUTES.profile}
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to={ROUTES.login} replace />} />
      </Routes>
    </BrowserRouter>
  );
}