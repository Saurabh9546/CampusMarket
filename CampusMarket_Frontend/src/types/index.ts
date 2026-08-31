/**
 * Shared domain types. Student role only — no Admin/Reports.
 * Product.status is AVAILABLE | SOLD only (no Condition, Hostel, or
 * Reserved fields).
 */

export interface College {
  id: string;
  name: string;
  emailDomain: string;
}

export interface User {
  id: string;
  name: string;
  email: string;
  verified: boolean;
  /** No image upload system exists yet — marked optional so the type
   * doesn't imply a guarantee that isn't real. */
  profilePhotoUrl?: string;
  joinedDate: string;
  /** Not currently returned by the backend — optional until it is. */
  collegeId?: string;
}

export type ProductStatus = 'AVAILABLE' | 'SOLD';

export const CATEGORIES = [
  'Books',
  'Calculators',
  'Laptops',
  'Mobile Phones',
  'Hostel Furniture',
  'Bicycles',
  'Motorcycles',
  'Notes',
  'Lab Equipment',
  'Electronics',
  'Clothing',
  'Miscellaneous',
] as const;

export type Category = typeof CATEGORIES[number];

/** Standard response envelope: { success, data } / { success, message }. */
export interface ApiSuccess<T> {
  success: true;
  data: T;
}

export interface ApiFailure {
  success: false;
  message: string;
  errorCode?: string;
  /** True when the request never reached the server (network/connection failure),
   * as opposed to the server responding with an error. Lets callers show
   * "can't reach server" instead of treating this as an auth rejection or
   * a genuine empty-result state. */
  networkError?: boolean;
}

export type ApiResponse<T> = ApiSuccess<T> | ApiFailure;