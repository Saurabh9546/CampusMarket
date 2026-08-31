import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import {
  messagesApi,
  type ConversationDto,
  type ConversationDetailDto,
  type MessageDto,
} from '@/api/messages';
import { ROUTES } from '@/constants/routes';
import { Avatar } from '@/components/Avatar/Avatar';
import { Navbar } from '@/components/Navbar/Navbar';
import styles from './MessagesPage.module.css';

export function MessagesPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { currentUser } = useAuth();

  const [conversations, setConversations] = useState<ConversationDto[]>([]);
  const [isLoadingList, setIsLoadingList] = useState(true);
  const [listError, setListError] = useState<string | undefined>();

  const [activeConversation, setActiveConversation] = useState<ConversationDetailDto | null>(null);
  const [isLoadingThread, setIsLoadingThread] = useState(false);
  const [threadError, setThreadError] = useState<string | undefined>();

  const [draft, setDraft] = useState('');
  const [isSending, setIsSending] = useState(false);
  const [sendError, setSendError] = useState<string | undefined>();

  const loadConversations = useCallback(async () => {
    setIsLoadingList(true);
    setListError(undefined);
    const result = await messagesApi.listConversations();
    if (result.success) {
      setConversations(result.data);
    } else {
      setListError(result.message);
    }
    setIsLoadingList(false);
  }, []);

  useEffect(() => {
    void loadConversations();
  }, [loadConversations]);

  const loadThread = useCallback(async (conversationId: number) => {
    setIsLoadingThread(true);
    setThreadError(undefined);
    const result = await messagesApi.getConversation(conversationId);
    if (result.success) {
      setActiveConversation(result.data);
    } else {
      setThreadError(result.message);
      setActiveConversation(null);
    }
    setIsLoadingThread(false);
  }, []);

  useEffect(() => {
    const conversationId = Number(id);
    if (id && !Number.isNaN(conversationId)) {
      void loadThread(conversationId);
    } else {
      setActiveConversation(null);
    }
  }, [id, loadThread]);

  const handleSend = async () => {
    const text = draft.trim();
    if (!text || !activeConversation || !currentUser) return;

    setIsSending(true);
    setSendError(undefined);

    const tempId = -Date.now();
    const optimisticMessage: MessageDto = {
      id: tempId,
      conversationId: activeConversation.id,
      senderId: Number(currentUser.id),
      message: text,
      sentAt: new Date().toISOString(),
    };
    setActiveConversation((prev) =>
      prev ? { ...prev, messages: [...prev.messages, optimisticMessage] } : prev,
    );
    setDraft('');

    const result = await messagesApi.sendMessage(activeConversation.id, text);
    setIsSending(false);

    if (result.success) {
      setActiveConversation((prev) =>
        prev
          ? {
              ...prev,
              messages: prev.messages.map((m) => (m.id === tempId ? result.data : m)),
            }
          : prev,
      );
      setConversations((prev) =>
        prev.map((c) =>
          c.id === activeConversation.id
            ? {
                ...c,
                lastMessagePreview: text.length > 60 ? `${text.slice(0, 60)}…` : text,
                lastMessageAt: result.data.sentAt,
              }
            : c,
        ),
      );
    } else {
      setActiveConversation((prev) =>
        prev ? { ...prev, messages: prev.messages.filter((m) => m.id !== tempId) } : prev,
      );
      setDraft(text);
      setSendError(result.message);
    }
  };

  const activeId = id ? Number(id) : undefined;
  // On mobile, only one pane shows at a time: the list when nothing is
  // selected, the thread once a conversation is open. Desktop CSS shows
  // both panes regardless of these classes — see MessagesPage.module.css.
  const showThreadOnMobile = !!id;

  return (
    <>
      <Navbar />
      <div className={styles.layout}>
        <aside
          className={`${styles.sidebar} ${showThreadOnMobile ? styles.sidebarHiddenOnMobile : ''}`}
        >
          <h2 className={styles.sidebarTitle}>Messages</h2>

          {isLoadingList && <p className={styles.subtle}>Loading conversations...</p>}
          {listError && <p className={styles.errorText}>{listError}</p>}

          {!isLoadingList && !listError && conversations.length === 0 && (
            <p className={styles.subtle}>No conversations yet.</p>
          )}

          <div className={styles.conversationList}>
            {conversations.map((c) => (
              <button
                key={c.id}
                className={`${styles.conversationItem} ${
                  activeId === c.id ? styles.conversationItemActive : ''
                }`}
                onClick={() => navigate(ROUTES.conversationPath(c.id))}
              >
                <div className={styles.conversationThumb}>Photo</div>
                <div className={styles.conversationText}>
                  <div className={styles.conversationTop}>
                    <Avatar name={c.otherPartyName} size="sm" />
                    <span className={styles.otherPartyName}>{c.otherPartyName}</span>
                  </div>
                  <div className={styles.productTitle}>{c.productTitle}</div>
                  <div className={styles.lastMessage}>
                    {c.lastMessagePreview ?? 'No messages yet'}
                  </div>
                </div>
              </button>
            ))}
          </div>
        </aside>

        <main
          className={`${styles.thread} ${!showThreadOnMobile ? styles.threadHiddenOnMobile : ''}`}
        >
          {!id && (
            <div className={styles.emptyThread}>
              <p className={styles.subtle}>Select a conversation to view messages.</p>
            </div>
          )}

          {id && isLoadingThread && <p className={styles.subtle}>Loading conversation...</p>}
          {id && threadError && <p className={styles.errorText}>{threadError}</p>}

          {id && !isLoadingThread && !threadError && activeConversation && (
            <>
              <div className={styles.threadHeader}>
                <button
                  className={styles.backButton}
                  onClick={() => navigate(ROUTES.messages)}
                  aria-label="Back to conversations"
                >
                  ← Back
                </button>
                <div className={styles.threadHeaderTop}>
                  <Avatar name={activeConversation.otherPartyName} size="md" />
                  <div>
                    <div className={styles.threadOtherParty}>
                      {activeConversation.otherPartyName}
                    </div>
                    <div className={styles.threadProduct}>{activeConversation.productTitle}</div>
                  </div>
                </div>
              </div>

              <div className={styles.messageList}>
                {activeConversation.messages.length === 0 && (
                  <p className={styles.subtle}>No messages yet — say hello.</p>
                )}
                {activeConversation.messages.map((m) => {
                  const isMine = !!currentUser && Number(currentUser.id) === m.senderId;
                  return (
                    <div
                      key={m.id}
                      className={`${styles.bubble} ${isMine ? styles.bubbleMine : styles.bubbleTheirs}`}
                    >
                      {m.message}
                    </div>
                  );
                })}
              </div>

              <div className={styles.composer}>
                <input
                  className={styles.composerInput}
                  placeholder="Type a message..."
                  value={draft}
                  onChange={(e) => setDraft(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !isSending) void handleSend();
                  }}
                />
                <button
                  className={styles.sendButton}
                  onClick={() => void handleSend()}
                  disabled={isSending || !draft.trim()}
                >
                  Send
                </button>
              </div>
              {sendError && <p className={styles.errorText}>{sendError}</p>}
            </>
          )}
        </main>
      </div>
    </>
  );
}