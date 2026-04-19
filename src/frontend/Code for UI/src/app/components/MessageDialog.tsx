import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from './ui/dialog';
import { Button } from './ui/button';
import { Textarea } from './ui/textarea';
import { Send } from 'lucide-react';
import { toast } from 'sonner';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router';
import { sendMessage } from '../lib/api';

interface MessageDialogProps {
  isOpen: boolean;
  onClose: () => void;
  listerName: string;
  listerEmail: string;
  listingTitle: string;
  sellerId?: number;
  listingId?: number;
}

export function MessageDialog({ isOpen, onClose, listerName, listerEmail, listingTitle, sellerId, listingId }: MessageDialogProps) {
  const [message, setMessage] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  const handleSend = async () => {
    if (message.trim() && user) {
      try {
        await sendMessage({
          receiverId: sellerId,
          receiverEmail: sellerId ? undefined : listerEmail,
          listingId,
          text: message,
        });
        setMessage('');
        onClose();
        navigate('/', {
          state: {
            openChat: true,
            conversationId: sellerId && listingId ? `${sellerId}:${listingId}` : sellerId ? `${sellerId}:general` : undefined,
          },
        });
      } catch (err: any) {
        toast.error(err.message || 'Could not send message');
      }
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Message {listerName}</DialogTitle>
          <DialogDescription>
            Send a message about "{listingTitle}"
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4 mt-4">
          <Textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder={`Hi ${listerName}, I'm interested in your listing...`}
            className="min-h-[150px]"
          />
          <div className="flex justify-end space-x-2">
            <Button variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              onClick={handleSend}
              disabled={!message.trim()}
              className="bg-red-600 hover:bg-red-700"
            >
              <Send className="w-4 h-4 mr-2" />
              Send Message
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
