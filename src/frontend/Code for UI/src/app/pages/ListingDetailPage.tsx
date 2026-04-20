import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router';
import { ArrowLeft, Calendar, User as UserIcon, MessageCircle, Star, Edit2, Trash2, ExternalLink } from 'lucide-react';
import { MessageDialog } from '../components/MessageDialog';
import { Button } from '../components/ui/button';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';
import { useAuth } from '../context/AuthContext';
import { SiteLogo } from '../components/SiteLogo';
import { toast } from 'sonner';
import { getListing, deleteListing } from '../lib/api';
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

function formatDate(raw: string): string {
  if (!raw) return '';
  const d = new Date(raw.replace(' ', 'T') + 'Z');
  return d.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
}

export function ListingDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);
  const [isMessageDialogOpen, setIsMessageDialogOpen] = useState(false);
  const [showLoginAlert, setShowLoginAlert] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [listing, setListing] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    getListing(Number(id))
      .then(setListing)
      .catch(() => setListing(null))
      .finally(() => setLoading(false));
  }, [id]);

  const renderStars = (rating: number) => {
    return (
      <div className="flex items-center gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`w-4 h-4 ${
              star <= Math.floor(rating)
                ? 'fill-yellow-400 text-yellow-400'
                : 'text-gray-300'
            }`}
          />
        ))}
        <span className="ml-1 text-sm font-medium text-gray-700">{rating.toFixed(1)}</span>
      </div>
    );
  };

  if (loading) {
    return <div className="min-h-screen bg-red-50 flex items-center justify-center"><p>Loading...</p></div>;
  }

  if (!listing) {
    return (
      <div className="min-h-screen bg-red-50 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Listing not found</h2>
          <Button onClick={() => navigate('/')} className="bg-red-600 hover:bg-red-700">
            Back to Listings
          </Button>
        </div>
      </div>
    );
  }

  const handleMessageClick = () => {
    if (!user) {
      setShowLoginAlert(true);
    } else if (user.isAdmin) {
      toast.error('Admin accounts cannot message sellers');
    } else if (user.bannedFromPurchasing) {
      toast.error('Your account is restricted from purchasing');
    } else {
      setIsMessageDialogOpen(true);
    }
  };

  const handleDeleteListing = async () => {
    setShowDeleteDialog(false);
    try {
      await deleteListing(listing.id, parseInt(user!.id));
      toast.success('Listing deleted');
      navigate('/');
    } catch (err: any) {
      toast.error(err.message || 'Could not delete listing');
    }
  };

  const isOwnListing = user && listing && String(listing.clientId) === user.id;

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

            {user ? (
              <button 
                onClick={() => navigate(`/profile/${user.email}`)}
                className="flex items-center space-x-2 px-5 py-2.5 bg-black text-white hover:bg-gray-900 rounded-lg transition-colors"
              >
                <UserIcon className="w-5 h-5" />
                <span>{user.name}</span>
              </button>
            ) : (
              <button 
                onClick={() => navigate('/auth')}
                className="flex items-center space-x-2 px-5 py-2.5 bg-black text-white hover:bg-gray-900 rounded-lg transition-colors"
              >
                <UserIcon className="w-5 h-5" />
                <span>Sign In</span>
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-[1200px] mx-auto px-6 py-8 relative z-1">
        <button
          onClick={() => navigate('/')}
          className="flex items-center space-x-2 text-gray-700 hover:text-red-600 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Back to listings</span>
        </button>

        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          <div className="grid md:grid-cols-2 gap-8 p-8">
            {/* Image Gallery */}
            <div className="space-y-4">
              <div className="aspect-square bg-gray-100 rounded-lg overflow-hidden">
                <ImageWithFallback
                  src={(listing.images?.[selectedImageIndex]) || listing.image || ''}
                  alt={listing.title}
                  className="w-full h-full object-cover"
                />
              </div>
              <div className="grid grid-cols-4 gap-2">
                {(listing.images || []).map((image: string, index: number) => (
                  <button
                    key={index}
                    onClick={() => setSelectedImageIndex(index)}
                    className={`aspect-square bg-gray-100 rounded-lg overflow-hidden border-2 transition-all ${
                      selectedImageIndex === index
                        ? 'border-red-600'
                        : 'border-transparent hover:border-gray-300'
                    }`}
                  >
                    <ImageWithFallback
                      src={image}
                      alt={`${listing.title} ${index + 1}`}
                      className="w-full h-full object-cover"
                    />
                  </button>
                ))}
              </div>
            </div>

            {/* Listing Details */}
            <div className="space-y-6">
              <div>
                <div className="flex items-start justify-between mb-2">
                  <h1 className="text-3xl font-bold text-gray-900">{listing.title}</h1>
                  
                </div>
                <p className="text-4xl font-bold text-red-600">${listing.price}</p>
              </div>

              <div className="flex items-center space-x-4 text-sm text-gray-600">
                <div className="flex items-center space-x-1">
                  <Calendar className="w-4 h-4" />
                  <span>Posted {formatDate(listing.postedDate)}</span>
                </div>
              </div>

              <div className="inline-block bg-red-100 text-red-700 px-3 py-1 rounded-full text-sm font-medium">
                {listing.condition}
              </div>

              <div className="space-y-2">
                <h3 className="font-semibold text-gray-900">Category</h3>
                <p className="text-gray-600">{listing.category}</p>
              </div>

              <div className="space-y-2">
                <h3 className="font-semibold text-gray-900">Description</h3>
                <p className="text-gray-600 leading-relaxed">{listing.description}</p>
              </div>

              <div className="border-t pt-6">
                <h3 className="font-semibold text-gray-900 mb-3">Seller Information</h3>
                <div className="flex items-center space-x-3 mb-4">
                  <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                    <UserIcon className="w-6 h-6 text-red-600" />
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <p className="font-medium text-gray-900">{listing.listerName}</p>
                    </div>
                    <p className="text-sm text-gray-500">Member since {formatDate(listing.listerJoinDate) || 'N/A'}</p>
                  </div>
                </div>
                

                {isOwnListing ? (
                  <div className="space-y-3">
                    <Button
                      onClick={() => navigate(`/edit-listing/${listing.id}`)}
                      className="w-full bg-blue-600 hover:bg-blue-700"
                      size="lg"
                    >
                      <Edit2 className="w-5 h-5 mr-2" />
                      Edit Listing
                    </Button>
                    <Button
                      onClick={() => setShowDeleteDialog(true)}
                      variant="outline"
                      className="w-full border-red-600 text-red-600 hover:bg-red-50"
                      size="lg"
                    >
                      <Trash2 className="w-5 h-5 mr-2" />
                      Delete Listing
                    </Button>
                  </div>
                ) : user?.isAdmin ? (
                  <div className="bg-gray-100 border border-gray-300 rounded-lg p-4 text-center">
                    <p className="text-gray-600 text-sm">Admin accounts cannot purchase items</p>
                  </div>
                ) : user?.bannedFromPurchasing ? (
                  <div className="bg-red-100 border border-red-300 rounded-lg p-4 text-center">
                    <p className="text-red-700 text-sm font-semibold">Your account is restricted from purchasing</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    <Button
                      onClick={() => navigate(`/profile/${listing.listerEmail}`)}
                      variant="outline"
                      className="w-full border-red-600 text-red-600 hover:bg-red-50"
                      size="lg"
                    >
                      <ExternalLink className="w-5 h-5 mr-2" />
                      View Profile
                    </Button>
                    <Button
                      onClick={handleMessageClick}
                      className="w-full bg-red-600 hover:bg-red-700"
                      size="lg"
                    >
                      <MessageCircle className="w-5 h-5 mr-2" />
                      Message Seller
                    </Button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      <MessageDialog
        isOpen={isMessageDialogOpen}
        onClose={() => setIsMessageDialogOpen(false)}
        listerName={listing.listerName || 'Seller'}
        listerEmail={listing.listerEmail || ''}
        listingTitle={listing.title}
        sellerId={listing.clientId}
        listingId={listing.id}
      />

      <AlertDialog open={showLoginAlert} onOpenChange={setShowLoginAlert}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Sign In Required</AlertDialogTitle>
            <AlertDialogDescription>
              You need to sign in to message the seller.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogAction
              onClick={() => {
                setShowLoginAlert(false);
                navigate('/auth');
              }}
            >
              Sign In
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Listing</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this listing? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteListing}
              className="bg-red-600 hover:bg-red-700"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
