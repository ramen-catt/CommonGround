import { useState, useEffect, useRef, useMemo } from 'react';
import { MessageCircle, ShoppingCart, X, Send, User } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { ScrollArea } from './ui/scroll-area';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router';
import { toast } from 'sonner';
import { getConversations, sendMessage } from '../lib/api';

interface Message {
  id: string;
  text: string;
  senderEmail: string;
  senderName: string;
  timestamp: string;
  readBy: string[]; // Array of user emails who have read this message
}

interface Conversation {
  id: string;
  otherId?: number;
  otherName?: string;
  otherEmail?: string;
  buyerEmail?: string;
  buyerName?: string;
  sellerEmail?: string;
  sellerName?: string;
  sellerId?: number;
  listingId?: number;
  listingTitle: string;
  messages: Message[];
  lastMessageTime: string;
}

export function ChatSidebar() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isOpen, setIsOpen] = useState(false);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<string | null>(null);
  const [newMessage, setNewMessage] = useState('');
  const [unreadCount, setUnreadCount] = useState(0);
  const previousMessageCount = useRef<number>(0);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const selectedConv = useMemo(() =>
    conversations.find(c => c.id === selectedConversation),
    [conversations, selectedConversation]
  );

  useEffect(() => {
    loadConversations();
  }, [user]);

  // Poll for new messages every 3 seconds
  useEffect(() => {
    if (!user) return;

    const interval = setInterval(() => {
      loadConversations();
    }, 3000);

    return () => clearInterval(interval);
  }, [user]);

  // Scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [selectedConv?.messages]);

  useEffect(() => {
    // Open chat if coming from message dialog
    if (location.state?.openChat && location.state?.conversationId) {
      setIsOpen(true);
      setSelectedConversation(location.state.conversationId);
      // Clear the state
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate]);

  useEffect(() => {
    // Calculate unread messages from other party
    const unread = conversations.reduce((count, conv) => {
      const unreadInConv = conv.messages.filter(
        m => m.senderEmail !== user?.email && !m.readBy.includes(user?.email || '')
      ).length;
      return count + unreadInConv;
    }, 0);

    // Show notification if new unread messages arrived
    if (unread > previousMessageCount.current && previousMessageCount.current > 0) {
      toast.info('You have a new message!', {
        duration: 3000,
      });
    }

    previousMessageCount.current = unread;
    setUnreadCount(unread);
  }, [conversations, user]);

  const loadConversations = async () => {
    if (!user) return;

    try {
      const loaded = await getConversations();
      setConversations(loaded as Conversation[]);
    } catch (err) {
      console.error('could not load conversations', err);
    }
  };

  const handleSendMessage = async () => {
    if (!newMessage.trim() || !selectedConversation || !user) return;

    try {
      if (!selectedConv?.otherId) return;
      await sendMessage({
        receiverId: selectedConv.otherId,
        listingId: selectedConv.listingId,
        text: newMessage,
      });
      setNewMessage('');
      loadConversations();
    } catch (err: any) {
      toast.error(err.message || 'Could not send message');
    }
  };

  return (
    <>
      {/* Floating Chat Button */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-8 left-8 bg-red-600 hover:bg-red-700 text-white rounded-full p-4 shadow-lg transition-all hover:shadow-xl z-50 flex items-center space-x-2"
      >
        <MessageCircle className="w-6 h-6" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-black text-white text-xs rounded-full w-6 h-6 flex items-center justify-center">
            {unreadCount}
          </span>
        )}
      </button>

      {/* Sidebar */}
      {isOpen && (
        <div className="fixed inset-y-0 right-0 w-96 bg-white shadow-2xl z-50 flex flex-col border-l border-gray-200">
            {/* Header */}
            <div className="bg-red-600 text-white p-4 flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <MessageCircle className="w-5 h-5" />
                <h2 className="text-lg font-semibold">Messages</h2>
              </div>
              <button 
                onClick={() => {
                  setIsOpen(false);
                  setSelectedConversation(null);
                }}
                className="hover:bg-red-700 p-1 rounded transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Conversations List or Chat View */}
            {!selectedConversation ? (
              <div className="flex-1 overflow-hidden">
                {conversations.length === 0 ? (
                  <div className="flex items-center justify-center h-full text-gray-500 px-6 text-center">
                    <div>
                      <MessageCircle className="w-12 h-12 mx-auto mb-3 text-gray-400" />
                      <p>No conversations yet</p>
                      <p className="text-sm mt-1">Message a seller to start chatting</p>
                    </div>
                  </div>
                ) : (
                  <ScrollArea className="h-full">
                    <div className="divide-y divide-gray-200">
                      {conversations.map((conv) => {
                        const otherPartyName = conv.otherName || (conv.buyerEmail === user?.email ? conv.sellerName : conv.buyerName);
                        const unreadInConv = conv.messages.filter(
                          m => m.senderEmail !== user?.email && !m.readBy.includes(user?.email || '')
                        ).length;

                        return (
                          <button
                            key={conv.id}
                            onClick={() => {
                              setSelectedConversation(conv.id);
                            }}
                            className="w-full p-4 hover:bg-gray-50 transition-colors text-left relative"
                          >
                            <div className="flex items-start space-x-3">
                              <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0">
                                <User className="w-5 h-5 text-red-600" />
                              </div>
                              <div className="flex-1 min-w-0">
                                <div className="flex items-center justify-between">
                                  <p className="font-semibold text-gray-900 truncate">
                                    {otherPartyName}
                                  </p>
                                  {unreadInConv > 0 && (
                                    <span className="bg-red-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center flex-shrink-0">
                                      {unreadInConv}
                                    </span>
                                  )}
                                </div>
                                <p className="text-sm text-gray-600 truncate">
                                  {conv.listingTitle}
                                </p>
                                <p className="text-xs text-gray-500 mt-1">
                                  {conv.messages.length} message{conv.messages.length !== 1 ? 's' : ''}
                                </p>
                              </div>
                            </div>
                          </button>
                        );
                      })}
                    </div>
                  </ScrollArea>
                )}
              </div>
            ) : (
              /* Chat View */
              <>
                {/* Chat Header */}
                <div className="p-4 border-b border-gray-200">
                  <button
                    onClick={() => setSelectedConversation(null)}
                    className="text-red-600 hover:text-red-700 text-sm mb-2"
                  >
                    Back to conversations
                  </button>
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center">
                      <User className="w-5 h-5 text-red-600" />
                    </div>
                    <div>
                      <p className="font-semibold text-gray-900">
                        {selectedConv?.otherName || (selectedConv?.buyerEmail === user?.email ? selectedConv?.sellerName : selectedConv?.buyerName)}
                      </p>
                      <p className="text-sm text-gray-600 truncate">
                        {selectedConv?.listingTitle}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Messages */}
                <ScrollArea className="flex-1 p-4">
                  <div className="space-y-3">
                    {selectedConv?.messages.map((message) => {
                      const isMyMessage = message.senderEmail === user?.email;
                      return (
                        <div
                          key={message.id}
                          className={`flex ${
                            isMyMessage ? 'justify-end' : 'justify-start'
                          }`}
                        >
                          <div
                            className={`max-w-[75%] rounded-lg px-4 py-2 ${
                              isMyMessage
                                ? 'bg-red-600 text-white'
                                : 'bg-gray-300 text-gray-900'
                            }`}
                          >
                            <p className="text-sm">{message.text}</p>
                            <p
                              className={`text-xs mt-1 ${
                                isMyMessage
                                  ? 'text-red-200'
                                  : 'text-gray-600'
                              }`}
                            >
                              {message.timestamp}
                            </p>
                          </div>
                        </div>
                      );
                    })}
                    <div ref={messagesEndRef} />
                  </div>
                </ScrollArea>

                {/* Transaction Button */}
                {selectedConv?.listingId && selectedConv?.sellerId && String(selectedConv.sellerId) !== user?.id && (
                  <div className="p-4 border-t border-gray-200">
                    <Button
                      onClick={() => navigate('/transaction', {
                        state: {
                          listingId: selectedConv.listingId,
                          sellerId: selectedConv.sellerId,
                          sellerName: selectedConv.sellerName || selectedConv.otherName,
                          sellerEmail: selectedConv.sellerEmail || selectedConv.otherEmail,
                          listingTitle: selectedConv.listingTitle,
                          conversationId: selectedConv.id,
                        },
                      })}
                      className="w-full bg-red-600 hover:bg-red-700"
                    >
                      <ShoppingCart className="w-4 h-4 mr-2" />
                      Confirm Purchase
                    </Button>
                  </div>
                )}

                {/* Message Input */}
                <div className="p-4 border-t border-gray-200">
                  <div className="flex space-x-2">
                    <Input
                      value={newMessage}
                      onChange={(e) => setNewMessage(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                      placeholder="Type a message..."
                      className="flex-1"
                    />
                    <Button
                      onClick={handleSendMessage}
                      className="bg-red-600 hover:bg-red-700"
                      disabled={!newMessage.trim()}
                    >
                      <Send className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </>
            )}
        </div>
      )}
    </>
  );
}
