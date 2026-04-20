import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { ArrowLeft, Clock, Package, DollarSign, MapPin, MessageCircle, Star } from 'lucide-react';
import { Button } from '../components/ui/button';
import { useAuth } from '../context/AuthContext';
import { SiteLogo } from '../components/SiteLogo';
import { MessageDialog } from '../components/MessageDialog';
import { getTransactions } from '../lib/api';

interface Transaction {
  id: string;
  userId: string;
  buyerId: number;
  buyerEmail: string;
  buyerName: string;
  sellerId: number;
  sellerEmail: string;
  sellerName: string;
  listingId: number;
  listingTitle: string;
  agreedPrice: number;
  paymentMethod: string;
  meetupLocation: {
    name: string;
    address: string;
  };
  status: string;
  buyerConfirmed: boolean;
  sellerConfirmed: boolean;
  timestamp: string;
}

export function PendingTransactionsPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [messageTarget, setMessageTarget] = useState<{ name: string; email: string; id: number; listingId: number; listingTitle: string } | null>(null);

  useEffect(() => {
    if (!user) { navigate('/'); return; }

    getTransactions()
      .then((all) => {
        // show everything except cancelled — both pending and completed so both parties can review
        const visible = all.filter((t: Transaction) => t.status !== 'Cancelled');
        setTransactions(visible as Transaction[]);
      })
      .catch(() => setTransactions([]));
  }, [user, navigate]);

  const getStatusBadge = (transaction: Transaction) => {
    const { status, buyerConfirmed, sellerConfirmed, sellerEmail } = transaction;
    const isSeller = sellerEmail === user?.email;

    if (status === 'Completed') {
      return (
        <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
          Completed
        </span>
      );
    }
    if (buyerConfirmed && sellerConfirmed) {
      return (
        <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
          Both Confirmed
        </span>
      );
    }
    if (isSeller) {
      return buyerConfirmed ? (
        <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-yellow-100 text-yellow-800">
          Awaiting Your Confirmation
        </span>
      ) : (
        <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-blue-100 text-blue-800">
          Waiting for Buyer
        </span>
      );
    }
    return buyerConfirmed ? (
      <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-blue-100 text-blue-800">
        Waiting for Seller
      </span>
    ) : (
      <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-yellow-100 text-yellow-800">
        Awaiting Your Confirmation
      </span>
    );
  };

  const pending = transactions.filter(t => t.status !== 'Completed');
  const completed = transactions.filter(t => t.status === 'Completed');

  return (
    <div className="min-h-screen bg-red-50">
      {/* Background Pattern */}
      <div className="fixed inset-0 pointer-events-none opacity-5 z-0">
        <div className="absolute top-40 left-1/4 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute top-96 right-1/3 w-18 h-18 bg-black rounded-full"></div>
        <div className="absolute top-[600px] left-1/2 w-30 h-30 bg-black rounded-full"></div>
        <div className="absolute bottom-80 right-1/4 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute bottom-40 left-1/3 w-18 h-18 bg-black rounded-full"></div>
      </div>

      {/* Header */}
      <header className="bg-red-600 shadow-md sticky top-0 z-10 relative">
        <div className="max-w-[1600px] mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/')}>
              <SiteLogo />
              <h1 className="text-2xl font-bold text-white">Common Ground</h1>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-4xl mx-auto px-6 py-8 relative z-1">
        <button
          onClick={() => navigate('/')}
          className="flex items-center space-x-2 text-gray-700 hover:text-red-600 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Back to Home</span>
        </button>

        <div className="bg-white rounded-lg shadow-md p-8">
          <div className="flex items-center gap-3 mb-6">
            <Clock className="w-8 h-8 text-red-600" />
            <div>
              <h1 className="text-3xl font-bold text-gray-900">My Transactions</h1>
              <p className="text-gray-600">
                {pending.length} pending · {completed.length} completed
              </p>
            </div>
          </div>

          {transactions.length === 0 ? (
            <div className="text-center py-12">
              <Clock className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-500 text-lg mb-2">No transactions yet</p>
              <p className="text-gray-400 text-sm">
                When you initiate or receive a purchase, it will appear here
              </p>
              <Button onClick={() => navigate('/')} className="mt-6 bg-red-600 hover:bg-red-700">
                Browse Listings
              </Button>
            </div>
          ) : (
            <div className="space-y-8">
              {/* Pending */}
              {pending.length > 0 && (
                <div>
                  <h2 className="text-lg font-semibold text-gray-700 mb-3">Active</h2>
                  <div className="space-y-4">
                    {pending.map((transaction) => {
                      const isSeller = transaction.sellerEmail === user?.email;
                      return (
                        <TransactionCard
                          key={transaction.id}
                          transaction={transaction}
                          isSeller={isSeller}
                          statusBadge={getStatusBadge(transaction)}
                          user={user}
                          onViewDetails={() =>
                            navigate('/transaction-confirm', {
                              state: {
                                transactionId: transaction.id,
                                sellerName: transaction.sellerName,
                                sellerEmail: transaction.sellerEmail,
                                buyerName: transaction.buyerName,
                                listingTitle: transaction.listingTitle,
                                agreedPrice: transaction.agreedPrice,
                                paymentMethod: transaction.paymentMethod,
                                meetupLocation: transaction.meetupLocation,
                              },
                            })
                          }
                          onMessageOther={isSeller ? () => setMessageTarget({
                            name: transaction.buyerName,
                            email: transaction.buyerEmail,
                            id: transaction.buyerId,
                            listingId: transaction.listingId,
                            listingTitle: transaction.listingTitle,
                          }) : undefined}
                        />
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Completed */}
              {completed.length > 0 && (
                <div>
                  <h2 className="text-lg font-semibold text-gray-700 mb-3">Completed — Leave a Review</h2>
                  <div className="space-y-4">
                    {completed.map((transaction) => {
                      const isSeller = transaction.sellerEmail === user?.email;
                      return (
                        <TransactionCard
                          key={transaction.id}
                          transaction={transaction}
                          isSeller={isSeller}
                          statusBadge={getStatusBadge(transaction)}
                          user={user}
                          onViewDetails={() =>
                            navigate('/transaction-confirm', {
                              state: {
                                transactionId: transaction.id,
                                sellerName: transaction.sellerName,
                                sellerEmail: transaction.sellerEmail,
                                buyerName: transaction.buyerName,
                                listingTitle: transaction.listingTitle,
                                agreedPrice: transaction.agreedPrice,
                                paymentMethod: transaction.paymentMethod,
                                meetupLocation: transaction.meetupLocation,
                              },
                            })
                          }
                          onMessageOther={isSeller ? () => setMessageTarget({
                            name: transaction.buyerName,
                            email: transaction.buyerEmail,
                            id: transaction.buyerId,
                            listingId: transaction.listingId,
                            listingTitle: transaction.listingTitle,
                          }) : undefined}
                          isCompleted
                        />
                      );
                    })}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Message Dialog (seller → buyer) */}
      {messageTarget && (
        <MessageDialog
          isOpen={!!messageTarget}
          onClose={() => setMessageTarget(null)}
          listerName={messageTarget.name}
          listerEmail={messageTarget.email}
          listingTitle={messageTarget.listingTitle}
          sellerId={messageTarget.id}
          listingId={messageTarget.listingId}
        />
      )}
    </div>
  );
}

function TransactionCard({
  transaction,
  isSeller,
  statusBadge,
  user,
  onViewDetails,
  onMessageOther,
  isCompleted = false,
}: {
  transaction: Transaction;
  isSeller: boolean;
  statusBadge: React.ReactNode;
  user: any;
  onViewDetails: () => void;
  onMessageOther?: () => void;
  isCompleted?: boolean;
}) {
  return (
    <div className="border border-gray-200 rounded-lg p-6 hover:border-red-300 transition-colors">
      <div className="flex items-start justify-between mb-4">
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900 text-lg mb-1">{transaction.listingTitle}</h3>
          <p className="text-sm text-gray-600">
            {isSeller ? `Buyer: ${transaction.buyerName}` : `Seller: ${transaction.sellerName}`}
          </p>
        </div>
        {statusBadge}
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="flex items-center gap-2 text-sm">
          <DollarSign className="w-4 h-4 text-red-600" />
          <div>
            <p className="text-gray-500">Agreed Price</p>
            <p className="font-semibold text-gray-900">${transaction.agreedPrice}</p>
          </div>
        </div>
        <div className="flex items-center gap-2 text-sm">
          <Package className="w-4 h-4 text-red-600" />
          <div>
            <p className="text-gray-500">Payment Method</p>
            <p className="font-semibold text-gray-900 capitalize">{transaction.paymentMethod}</p>
          </div>
        </div>
      </div>

      <div className="flex items-start gap-2 text-sm mb-4">
        <MapPin className="w-4 h-4 text-red-600 mt-1" />
        <div>
          <p className="text-gray-500">Meetup Location</p>
          <p className="font-semibold text-gray-900">{transaction.meetupLocation.name}</p>
          <p className="text-xs text-gray-600">{transaction.meetupLocation.address}</p>
        </div>
      </div>

      <div className="flex items-center justify-between pt-4 border-t border-gray-200 gap-3">
        <p className="text-xs text-gray-500">
          {new Date(transaction.timestamp.replace(' ', 'T') + 'Z').toLocaleDateString('en-US', {
            month: 'long', day: 'numeric', year: 'numeric',
            hour: 'numeric', minute: '2-digit',
          })}
        </p>
        <div className="flex gap-2">
          {onMessageOther && (
            <Button
              onClick={onMessageOther}
              variant="outline"
              className="border-red-600 text-red-600 hover:bg-red-50"
            >
              <MessageCircle className="w-4 h-4 mr-1" />
              Message Buyer
            </Button>
          )}
          <Button
            onClick={onViewDetails}
            className={isCompleted ? 'bg-yellow-500 hover:bg-yellow-600' : 'bg-red-600 hover:bg-red-700'}
          >
            {isCompleted ? (
              <>
                <Star className="w-4 h-4 mr-1" />
                Leave Review
              </>
            ) : (
              'View Details'
            )}
          </Button>
        </div>
      </div>
    </div>
  );
}
