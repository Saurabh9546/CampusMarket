import { apiClient } from './client';
import type { ApiResponse } from '@/types';
import type { ProductDto } from './products';

export const wishlistApi = {
  list: (): Promise<ApiResponse<ProductDto[]>> =>
    apiClient.get('/wishlist'),

  add: (productId: number): Promise<ApiResponse<void>> =>
    apiClient.post(`/wishlist/${productId}`),

  remove: (productId: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/wishlist/${productId}`),
};