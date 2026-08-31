import { apiClient } from './client';
import type { ApiResponse } from '@/types';

export interface ProductDto {
  id: number;
  title: string;
  description: string;
  price: number;
  category: string;
  status: 'AVAILABLE' | 'SOLD';
  sellerId: number;
  createdAt: string;
  /** Nullable — only populated by getById (product detail), not by browse. */
  sellerName?: string | null;
}

export interface ProductCreateRequest {
  title: string;
  description: string;
  price: number;
  category: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page (0-indexed)
  size: number;
}

export interface BrowseParams {
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  page?: number;
}

function buildQuery(params: BrowseParams): string {
  const query = new URLSearchParams();
  if (params.category) query.set('category', params.category);
  if (params.minPrice != null) query.set('minPrice', String(params.minPrice));
  if (params.maxPrice != null) query.set('maxPrice', String(params.maxPrice));
  if (params.search) query.set('search', params.search);
  if (params.page != null) query.set('page', String(params.page));
  const qs = query.toString();
  return qs ? `?${qs}` : '';
}

export const productsApi = {
  browse: (params: BrowseParams = {}): Promise<ApiResponse<PagedResponse<ProductDto>>> =>
    apiClient.get(`/products${buildQuery(params)}`),

  getMine: (page = 0): Promise<ApiResponse<PagedResponse<ProductDto>>> =>
    apiClient.get(`/products/mine?page=${page}`),

  getById: (productId: number): Promise<ApiResponse<ProductDto>> =>
    apiClient.get(`/products/${productId}`),

  create: (request: ProductCreateRequest): Promise<ApiResponse<ProductDto>> =>
    apiClient.post('/products', request),

  update: (productId: number, request: ProductCreateRequest): Promise<ApiResponse<ProductDto>> =>
    apiClient.put(`/products/${productId}`, request),

  markSold: (productId: number): Promise<ApiResponse<ProductDto>> =>
    apiClient.patch(`/products/${productId}/sold`),

  delete: (productId: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/products/${productId}`),
};