export const ROUTES = {
  login: '/login',
  register: '/register',
  verifyEmail: '/verify-email',
  forgotPassword: '/forgot-password',
  resetPassword: '/reset-password',
  home: '/home',
  sell: '/sell',
  wishlist: '/wishlist',
  resendVerification: '/resend-verification',

  productDetail: '/products/:id',
  productDetailPath: (id: number | string) => `/products/${id}`,

  myListings: '/my-listings',
  editListing: '/products/:id/edit',
  editListingPath: (id: number | string) => `/products/${id}/edit`,

  profile: '/profile',

  messages: '/messages',
  conversation: '/messages/:id',
  conversationPath: (id: number | string) => `/messages/${id}`,
} as const;