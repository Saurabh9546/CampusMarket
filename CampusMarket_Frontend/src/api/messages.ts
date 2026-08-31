import { apiClient } from './client';
import type { ApiResponse } from '@/types';

/** Fields common to both the list view and detail view of a conversation. */
interface ConversationBase {
  id: number;
  productId: number;
  buyerId: number;
  sellerId: number;
  createdAt: string;
  otherPartyId: number;
  otherPartyName: string;
  productTitle: string;
}

/** Returned by GET /conversations (list). Includes a last-message preview
 * for the inbox — the detail endpoint does NOT return these two fields,
 * so they must not be assumed present on a ConversationDetailDto. */
export interface ConversationDto extends ConversationBase {
  lastMessagePreview: string | null;
  lastMessageAt: string | null;
}

export interface MessageDto {
  id: number;
  conversationId: number;
  senderId: number;
  message: string;
  sentAt: string;
}

/** Returned by GET /conversations/:id (detail/thread view). No lastMessage
 * fields — you have the full `messages` array instead. */
export interface ConversationDetailDto extends ConversationBase {
  messages: MessageDto[];
}

export const messagesApi = {
  listConversations: (): Promise<ApiResponse<ConversationDto[]>> =>
    apiClient.get('/conversations'),

  createConversation: (productId: number): Promise<ApiResponse<ConversationDto>> =>
    apiClient.post('/conversations', { productId }),

  getConversation: (conversationId: number): Promise<ApiResponse<ConversationDetailDto>> =>
    apiClient.get(`/conversations/${conversationId}`),

  sendMessage: (conversationId: number, message: string): Promise<ApiResponse<MessageDto>> =>
    apiClient.post('/messages', { conversationId, message }),
};