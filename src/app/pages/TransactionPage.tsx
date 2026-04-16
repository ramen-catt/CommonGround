import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router';
import { ArrowLeft, Check, CreditCard, MapPin, User as UserIcon, Package, ShieldCheck } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { useAuth } from '../context/AuthContext';
import { toast } from 'sonner';

export function TransactionPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  
  const { sellerName, listingTitle, conversationId } = location.state || {};
  
  const [step, setStep] = useState<'payment' | 'confirmation'>('payment');
  const [cardNumber, setCardNumber] = useState('');
  const [expiryDate, setExpiryDate] = useState('');
  const [cvv, setCvv] = useState('');
  const [nameOnCard, setNameOnCard] = useState('');
  const [billingAddress, setBillingAddress] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    // Redirect if not authenticated or no state
    if (!user || !sellerName) {
      navigate('/');
    }
  }, [user, sellerName, navigate]);

  const handlePayment = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!cardNumber || !expiryDate || !cvv || !nameOnCard || !billingAddress) {
      toast.error('Please fill in all payment details');
      return;
    }

    setIsProcessing(true);

    // Simulate payment processing
    setTimeout(() => {
      // Save transaction to localStorage
      const transaction = {
        id: Date.now().toString(),
        userId: user!.id,
        sellerName,
        listingTitle,
        amount: 0, // You could pass this from the listing
        timestamp: new Date().toISOString(),
        status: 'completed',
      };

      const transactionsJson = localStorage.getItem('transactions');
      const transactions = transactionsJson ? JSON.parse(transactionsJson) : [];
      transactions.push(transaction);
      localStorage.setItem('transactions', JSON.stringify(transactions));

      setIsProcessing(false);
      setStep('confirmation');
    }, 2000);
  };

  if (step === 'confirmation') {
    return (
      <div className="min-h-screen bg-red-50">
        {/* Background Pattern */}
        <div className="fixed inset-0 pointer-events-none opacity-5 z-0">
          <div className="absolute top-20 left-1/4 w-24 h-24 bg-black rounded-full"></div>
          <div className="absolute top-40 right-1/3 w-18 h-18 bg-black rounded-full"></div>
          <div className="absolute top-60 left-1/2 w-30 h-30 bg-black rounded-full"></div>
          <div className="absolute top-96 right-1/4 w-24 h-24 bg-black rounded-full"></div>
          <div className="absolute bottom-60 left-1/3 w-18 h-18 bg-black rounded-full"></div>
          <div className="absolute bottom-40 right-1/2 w-24 h-24 bg-black rounded-full"></div>
          <div className="absolute top-1/3 left-2/3 w-18 h-18 bg-black rounded-full"></div>
          <div className="absolute bottom-1/3 right-2/3 w-30 h-30 bg-black rounded-full"></div>
        </div>

        {/* Header */}
        <header className="bg-red-600 shadow-md sticky top-0 z-10 relative">
          <div className="max-w-[1600px] mx-auto px-6 py-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/')}>
                <div className="relative">
                  <div className="w-10 h-10 bg-black rounded-full flex items-center justify-center">
                    <div className="w-2 h-2 bg-white rounded-full absolute top-2 left-2"></div>
                    <div className="w-2 h-2 bg-white rounded-full absolute top-2 right-2"></div>
                    <div className="w-2 h-2 bg-white rounded-full absolute bottom-2 left-3"></div>
                  </div>
                </div>
                <h1 className="text-2xl font-bold text-white">Common Ground</h1>
              </div>
            </div>
          </div>
        </header>

        {/* Confirmation Content */}
        <div className="max-w-2xl mx-auto px-6 py-12 relative z-1">
          <div className="bg-white rounded-lg shadow-lg p-8 text-center">
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <Check className="w-10 h-10 text-green-600" />
            </div>
            
            <h2 className="text-3xl font-bold text-gray-900 mb-4">Purchase Confirmed!</h2>
            <p className="text-gray-600 mb-8">
              Your transaction has been completed successfully.
            </p>

            <div className="bg-gray-50 rounded-lg p-6 mb-8 text-left">
              <h3 className="font-semibold text-gray-900 mb-4">Transaction Details</h3>
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <Package className="w-5 h-5 text-red-600 mt-0.5" />
                  <div>
                    <p className="text-sm text-gray-500">Item</p>
                    <p className="font-medium text-gray-900">{listingTitle}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <UserIcon className="w-5 h-5 text-red-600 mt-0.5" />
                  <div>
                    <p className="text-sm text-gray-500">Seller</p>
                    <p className="font-medium text-gray-900">{sellerName}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <ShieldCheck className="w-5 h-5 text-red-600 mt-0.5" />
                  <div>
                    <p className="text-sm text-gray-500">Status</p>
                    <p className="font-medium text-green-600">Completed</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
              <p className="text-sm text-blue-900">
                <strong>What's next?</strong> The seller will contact you to arrange pickup or delivery.
                Check your messages for updates.
              </p>
            </div>

            <div className="flex gap-4">
              <Button
                onClick={() => navigate('/')}
                variant="outline"
                className="flex-1"
              >
                Browse More Listings
              </Button>
              <Button
                onClick={() => navigate('/')}
                className="flex-1 bg-red-600 hover:bg-red-700"
              >
                Go to Home
              </Button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-red-50">
      {/* Background Pattern */}
      <div className="fixed inset-0 pointer-events-none opacity-5 z-0">
        <div className="absolute top-20 left-1/4 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute top-40 right-1/3 w-18 h-18 bg-black rounded-full"></div>
        <div className="absolute top-60 left-1/2 w-30 h-30 bg-black rounded-full"></div>
        <div className="absolute top-96 right-1/4 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute bottom-60 left-1/3 w-18 h-18 bg-black rounded-full"></div>
        <div className="absolute bottom-40 right-1/2 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute top-1/3 left-2/3 w-18 h-18 bg-black rounded-full"></div>
        <div className="absolute bottom-1/3 right-2/3 w-30 h-30 bg-black rounded-full"></div>
      </div>

      {/* Header */}
      <header className="bg-red-600 shadow-md sticky top-0 z-10 relative">
        <div className="max-w-[1600px] mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/')}>
              <div className="relative">
                <div className="w-10 h-10 bg-black rounded-full flex items-center justify-center">
                  <div className="w-2 h-2 bg-white rounded-full absolute top-2 left-2"></div>
                  <div className="w-2 h-2 bg-white rounded-full absolute top-2 right-2"></div>
                  <div className="w-2 h-2 bg-white rounded-full absolute bottom-2 left-3"></div>
                </div>
              </div>
              <h1 className="text-2xl font-bold text-white">Common Ground</h1>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-2xl mx-auto px-6 py-12 relative z-1">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center space-x-2 text-gray-700 hover:text-red-600 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Back</span>
        </button>

        <div className="bg-white rounded-lg shadow-lg p-8">
          <div className="mb-6">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Complete Your Purchase</h2>
            <p className="text-gray-600">
              You're purchasing <strong>{listingTitle}</strong> from <strong>{sellerName}</strong>
            </p>
          </div>

          {/* Transaction Summary */}
          <div className="bg-gray-50 rounded-lg p-6 mb-8">
            <h3 className="font-semibold text-gray-900 mb-4">Transaction Summary</h3>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Item:</span>
                <span className="font-medium text-gray-900">{listingTitle}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Seller:</span>
                <span className="font-medium text-gray-900">{sellerName}</span>
              </div>
            </div>
          </div>

          {/* Payment Form */}
          <form onSubmit={handlePayment} className="space-y-6">
            <div className="space-y-4">
              <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                <CreditCard className="w-5 h-5 text-red-600" />
                Payment Information
              </h3>

              <div className="space-y-2">
                <Label htmlFor="cardNumber">Card Number</Label>
                <Input
                  id="cardNumber"
                  type="text"
                  placeholder="1234 5678 9012 3456"
                  value={cardNumber}
                  onChange={(e) => setCardNumber(e.target.value)}
                  maxLength={19}
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="expiryDate">Expiry Date</Label>
                  <Input
                    id="expiryDate"
                    type="text"
                    placeholder="MM/YY"
                    value={expiryDate}
                    onChange={(e) => setExpiryDate(e.target.value)}
                    maxLength={5}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cvv">CVV</Label>
                  <Input
                    id="cvv"
                    type="text"
                    placeholder="123"
                    value={cvv}
                    onChange={(e) => setCvv(e.target.value)}
                    maxLength={3}
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="nameOnCard">Name on Card</Label>
                <Input
                  id="nameOnCard"
                  type="text"
                  placeholder="John Doe"
                  value={nameOnCard}
                  onChange={(e) => setNameOnCard(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                <MapPin className="w-5 h-5 text-red-600" />
                Billing Address
              </h3>

              <div className="space-y-2">
                <Label htmlFor="billingAddress">Address</Label>
                <Input
                  id="billingAddress"
                  type="text"
                  placeholder="123 Main St, Houston, TX 77001"
                  value={billingAddress}
                  onChange={(e) => setBillingAddress(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <p className="text-sm text-blue-900">
                <strong>Note:</strong> This is a demonstration. No actual payment will be processed.
                Your information is not stored or transmitted.
              </p>
            </div>

            <div className="flex gap-4 pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate(-1)}
                className="flex-1"
                disabled={isProcessing}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                className="flex-1 bg-red-600 hover:bg-red-700"
                disabled={isProcessing}
              >
                {isProcessing ? 'Processing...' : 'Confirm Purchase'}
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
