import { apiClient } from './client';
import type { ApiResponse, User } from '@/types';

export interface UpdateProfilePayload {
  name: string;
}

/** PATCH /users/me — currently supports updating only the display name. */
export function updateProfile(payload: UpdateProfilePayload): Promise<ApiResponse<User>> {
  return apiClient.patch('/users/me', payload);
}