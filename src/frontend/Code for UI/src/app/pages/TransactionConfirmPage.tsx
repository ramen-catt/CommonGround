import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router';
import { ArrowLeft, Check, MapPin, Package, DollarSign, CreditCard, Star, X } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Textarea } from '../components/ui/textarea';
import { Label } from '../components/ui/label';
import { useAuth } from '../context/AuthContext';
import { toast } from 'sonner';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '../components/ui/alert-dialog';
import { SiteLogo } from '../components/SiteLogo';
import { createFeedback, getTransaction, updateTransaction } from '../lib/api';

export function TransactionConfirmPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  
  const transactionState = location.state || {};
  const { transactionId } = transactionState;
  
  const [transaction, setTransaction] = useState<any>(null);
  const [hasConfirmed, setHasConfirmed] = useState(false);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [reviewComment, setReviewComment] = useState('');
  const [isSubmittingReview, setIsSubmittingReview] = useState(false);
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [showSecondCancelDialog, setShowSecondCancelDialog] = useState(false);
  const [isCancelled, setIsCancelled] = useState(false);

  useEffect(() => {
    // Redirect if not authenticated or no state
    if (!user || !transactionId) {
      navigate('/');
      return;
    }

    loadTransaction();
  }, [user, transactionId, navigate]);

  const loadTransaction = async () => {
    try {
      const found = await getTransaction(transactionId);
      setTransaction(found);
      const isSeller = found.sellerEmail === user?.email;
      setHasConfirmed(isSeller ? found.sellerConfirmed : found.buyerConfirmed);
    } catch {
      setTransaction(null);
    }
  };

  const handleConfirm = async () => {
    if (!transaction) return;

    const isSeller = transaction.sellerEmail === user?.email;
    try {
      const result = await updateTransaction(transactionId, 'confirm');
      setTransaction(result.transaction);
      setHasConfirmed(true);
      if (result.transaction.status === 'Completed') {
        toast.success('Both parties confirmed. You can leave a review.');
        setShowReviewForm(true);
      } else if (isSeller) {
        toast.success('Meetup confirmed! Waiting for buyer confirmation.');
      } else {
        toast.success('Meetup confirmed! Waiting for seller confirmation.');
      }
    } catch (err: any) {
      toast.error(err.message || 'Could not confirm transaction');
    }
  };

  const handleFirstCancelConfirm = () => {
    setShowCancelDialog(false);
    setShowSecondCancelDialog(true);
  };

  const handleFinalCancelConfirm = async () => {
    if (!transaction) return;

    try {
      const result = await updateTransaction(transactionId, 'cancel');
      setTransaction(result.transaction);
      setIsCancelled(true);
      setShowSecondCancelDialog(false);
      toast.success('Transaction cancelled. You can still leave a review.');
      setShowReviewForm(true);
    } catch (err: any) {
      toast.error(err.message || 'Could not cancel transaction');
    }
  };

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();

    if (rating === 0) {
      toast.error('Please select a rating');
      return;
    }

    if (!reviewComment.trim()) {
      toast.error('Please write a review comment');
      return;
    }

    setIsSubmittingReview(true);

    const isSeller = transaction?.sellerEmail === user?.email;
    const reviewedUserId = isSeller ? transaction?.buyerId : transaction?.sellerId;

    try {
      await createFeedback({
        sellerId: reviewedUserId,
        listingId: transaction.listingId,
        rating,
        ratingDesc: reviewComment,
      });
      setIsSubmittingReview(false);
      toast.success('Review submitted successfully!');
      setTimeout(() => {
        navigate('/');
      }, 1500);
    } catch (err: any) {
      setIsSubmittingReview(false);
      toast.error(err.message || 'Could not submit review');
    }
  };

  if (!transaction) {
    return (
      <div className="min-h-screen bg-red-50 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Transaction not found</h2>
          <Button onClick={() => navigate('/')} className="bg-red-600 hover:bg-red-700">
            Back to Home
          </Button>
        </div>
      </div>
    );
  }

  const bothConfirmed = transaction.buyerConfirmed && transaction.sellerConfirmed;
  const transactionCancelled = transaction.status === 'Cancelled';
  const isSeller = transaction.sellerEmail === user?.email;
  const hasReviewed = false;

  // If both confirmed and not yet reviewed, show review form
  if (bothConfirmed && !showReviewForm && !hasReviewed && !transactionCancelled) {
    setShowReviewForm(true);
  }

  // If transaction was cancelled, allow review
  if (transactionCancelled && !showReviewForm && !hasReviewed && isCancelled) {
    setShowReviewForm(true);
  }

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
      <div className="max-w-2xl mx-auto px-6 py-12 relative z-1">
        <button
          onClick={() => navigate('/')}
          className="flex items-center space-x-2 text-gray-700 hover:text-red-600 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Back to Home</span>
        </button>

        <div className="bg-white rounded-lg shadow-lg p-8">
          {!showReviewForm ? (
            <>
              <div className="text-center mb-6">
                <div className="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Package className="w-10 h-10 text-blue-600" />
                </div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">Transaction In Progress</h2>
                <p className="text-gray-600">
                  Meet at the agreed location and confirm the exchange
                </p>
              </div>

              {/* Transaction Details */}
              <div className="bg-gray-50 rounded-lg p-6 mb-6">
                <h3 className="font-semibold text-gray-900 mb-4">Transaction Details</h3>
                <div className="space-y-3">
                  <div className="flex items-start gap-3">
                    <Package className="w-5 h-5 text-red-600 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-500">Item</p>
                      <p className="font-medium text-gray-900">{transaction.listingTitle}</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <DollarSign className="w-5 h-5 text-red-600 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-500">Agreed Price</p>
                      <p className="font-medium text-gray-900">${transaction.agreedPrice}</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <CreditCard className="w-5 h-5 text-red-600 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-500">Payment Method</p>
                      <p className="font-medium text-gray-900 capitalize">{transaction.paymentMethod}</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <MapPin className="w-5 h-5 text-red-600 mt-0.5" />
                    <div className="flex-1">
                      <p className="text-sm text-gray-500">Common Ground Meetup Location</p>
                      <p className="font-medium text-gray-900">{transaction.meetupLocation?.name}</p>
                      <p className="text-sm text-gray-600">{transaction.meetupLocation?.address}</p>
                    </div>
                  </div>
                </div>

                {/* Map */}
                {transaction.meetupLocation && (
                  <div className="mt-4 rounded-lg overflow-hidden border border-gray-200">
                    <iframe
                      width="100%"
                      height="200"
                      style={{ border: 0 }}
                      loading="lazy"
                      allowFullScreen
                      src={
                        transaction.meetupLocation.lat && transaction.meetupLocation.lng
                          ? `https://www.openstreetmap.org/export/embed.html?bbox=${transaction.meetupLocation.lng - 0.01}%2C${transaction.meetupLocation.lat - 0.01}%2C${transaction.meetupLocation.lng + 0.01}%2C${transaction.meetupLocation.lat + 0.01}&layer=mapnik&marker=${transaction.meetupLocation.lat}%2C${transaction.meetupLocation.lng}`
                          : `https://www.openstreetmap.org/search?query=${encodeURIComponent(transaction.meetupLocation.address || transaction.meetupLocation.name)}`
                      }
                    ></iframe>
                  </div>
                )}
              </div>

              {/* Confirmation Status */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
                <h3 className="font-semibold text-gray-900 mb-4">Confirmation Status</h3>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-700">Your Confirmation:</span>
                    {hasConfirmed ? (
                      <span className="flex items-center gap-2 text-green-600 font-medium">
                        <Check className="w-5 h-5" />
                        Confirmed
                      </span>
                    ) : (
                      <span className="text-gray-500">Pending</span>
                    )}
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-700">
                      {transaction.sellerEmail === user?.email ? 'Buyer Confirmation:' : 'Seller Confirmation:'}
                    </span>
                    {transaction.sellerEmail === user?.email ? (
                      transaction.buyerConfirmed ? (
                        <span className="flex items-center gap-2 text-green-600 font-medium">
                          <Check className="w-5 h-5" />
                          Confirmed
                        </span>
                      ) : (
                        <span className="text-gray-500">Pending</span>
                      )
                    ) : (
                      transaction.sellerConfirmed ? (
                        <span className="flex items-center gap-2 text-green-600 font-medium">
                          <Check className="w-5 h-5" />
                          Confirmed
                        </span>
                      ) : (
                        <span className="text-gray-500">Pending</span>
                      )
                    )}
                  </div>
                </div>
              </div>

              {!hasConfirmed && (
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
                  <p className="text-sm text-yellow-900">
                    <strong>Important:</strong> Only confirm after you've met the seller at the agreed location and successfully exchanged the item and payment.
                  </p>
                </div>
              )}

              {bothConfirmed && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
                  <p className="text-sm text-green-900">
                    <strong>Success!</strong> Both parties have confirmed the transaction. You can now leave a review for the seller.
                  </p>
                </div>
              )}

              <div className="space-y-3">
                {!hasConfirmed && !transactionCancelled && (
                  <Button
                    onClick={handleConfirm}
                    className="w-full bg-red-600 hover:bg-red-700"
                  >
                    <Check className="w-5 h-5 mr-2" />
                    Confirm Meetup & Exchange
                  </Button>
                )}

                {!transactionCancelled && (
                  <Button
                    onClick={() => setShowCancelDialog(true)}
                    variant="outline"
                    className="w-full border-red-600 text-red-600 hover:bg-red-50"
                  >
                    <X className="w-5 h-5 mr-2" />
                    Cancel Transaction
                  </Button>
                )}

                {transactionCancelled && (
                  <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-center">
                    <p className="text-red-900 font-semibold">Transaction Cancelled</p>
                    <p className="text-sm text-red-700 mt-1">You can still leave a review below.</p>
                  </div>
                )}

                {(hasConfirmed || transactionCancelled) && (
                  <Button
                    onClick={() => navigate('/')}
                    variant="outline"
                    className="w-full"
                  >
                    Back to Home
                  </Button>
                )}
              </div>
            </>
          ) : (
            /* Review Form */
            <>
              <div className="text-center mb-6">
                <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Check className="w-10 h-10 text-green-600" />
                </div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">Transaction Complete!</h2>
                <p className="text-gray-600">
                  How was your experience with <strong>{transaction?.sellerEmail === user?.email ? transaction.buyerName : transaction.sellerName}</strong>?
                </p>
              </div>

              <form onSubmit={handleSubmitReview} className="space-y-6">
                {/* Star Rating */}
                <div className="space-y-2">
                  <Label>{transaction?.sellerEmail === user?.email ? 'Rate the Buyer' : 'Rate the Seller'}</Label>
                  <div className="flex gap-2 justify-center">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <button
                        key={star}
                        type="button"
                        onClick={() => setRating(star)}
                        onMouseEnter={() => setHoverRating(star)}
                        onMouseLeave={() => setHoverRating(0)}
                        className="transition-transform hover:scale-110"
                      >
                        <Star
                          className={`w-10 h-10 ${
                            star <= (hoverRating || rating)
                              ? 'fill-yellow-400 text-yellow-400'
                              : 'text-gray-300'
                          }`}
                        />
                      </button>
                    ))}
                  </div>
                  {rating > 0 && (
                    <p className="text-center text-sm text-gray-600">
                      {rating === 1 && 'Poor'}
                      {rating === 2 && 'Fair'}
                      {rating === 3 && 'Good'}
                      {rating === 4 && 'Very Good'}
                      {rating === 5 && 'Excellent'}
                    </p>
                  )}
                </div>

                {/* Review Comment */}
                <div className="space-y-2">
                  <Label htmlFor="reviewComment">Your Review</Label>
                  <Textarea
                    id="reviewComment"
                    placeholder="Share your experience with this seller..."
                    rows={5}
                    value={reviewComment}
                    onChange={(e) => setReviewComment(e.target.value)}
                    required
                  />
                </div>

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <p className="text-sm text-blue-900">
                    Your review helps build trust in the Common Ground community. Please be honest and respectful.
                  </p>
                </div>

                <Button
                  type="submit"
                  className="w-full bg-red-600 hover:bg-red-700"
                  disabled={isSubmittingReview}
                >
                  {isSubmittingReview ? 'Submitting Review...' : 'Submit Review'}
                </Button>
              </form>
            </>
          )}
        </div>
      </div>

      {/* First Cancel Confirmation Dialog */}
      <AlertDialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancel Transaction?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to cancel this transaction? This will notify the seller that the deal is off.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>No, Keep Transaction</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleFirstCancelConfirm}
              className="bg-red-600 hover:bg-red-700"
            >
              Yes, Continue Cancellation
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Second Cancel Confirmation Dialog */}
      <AlertDialog open={showSecondCancelDialog} onOpenChange={setShowSecondCancelDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. The transaction will be permanently cancelled for both you and the seller. However, you'll still be able to leave a review about your experience with this seller.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Go Back</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleFinalCancelConfirm}
              className="bg-red-600 hover:bg-red-700"
            >
              Yes, Cancel Transaction
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
