import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router';
import { ArrowLeft, Star, MapPin, Calendar, Mail, User as UserIcon, Package } from 'lucide-react';
import { Button } from '../components/ui/button';
import { useAuth } from '../context/AuthContext';
import { SiteLogo } from '../components/SiteLogo';
import { toast } from 'sonner';
import { getMyListings, getProfile, getFeedback, deleteAccount } from '../lib/api';

interface UserProfile {
  id: string;
  name: string;
  email: string;
  joinDate: string;
  rating: number;
  totalReviews: number;
  location?: string;
  address?: string;
}

// Treat DB datetime strings as UTC so local-timezone display is correct
function formatDate(raw: string): string {
  if (!raw) return '';
  const d = new Date(raw.replace(' ', 'T') + 'Z');
  return d.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
}

export function ProfilePage() {
  const { email } = useParams<{ email: string }>();
  const navigate = useNavigate();
  const { user: currentUser, signOut, isAuthLoading } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [userListings, setUserListings] = useState<any[]>([]);
  const [reviews, setReviews] = useState<any[]>([]);
  const [showDeleteAccountDialog, setShowDeleteAccountDialog] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isAuthLoading) return;
    if (!currentUser) { setLoading(false); return; }

    setLoading(true);
    getProfile(email || currentUser.email)
      .then((data) => {
        setProfile({
          id: String(data.id),
          name: data.name,
          email: data.email,
          joinDate: data.joinDate || 'N/A',
          rating: data.rating ?? 5.0,
          totalReviews: data.totalReviews ?? 0,
          location: data.location || 'Houston, TX',
          address: data.address,
        });
        // Fetch independently so one failure doesn't blank the other
        getMyListings(Number(data.id))
          .then((listings) => setUserListings(listings.filter((l: any) => l.status === 'Available')))
          .catch(() => setUserListings([]));
        getFeedback({ sellerId: Number(data.id) })
          .then((feedbackData) => setReviews((feedbackData as any[]).filter((f: any) => !f.isReport)))
          .catch(() => setReviews([]));
      })
      .catch(() => {
        setProfile(null);
        setUserListings([]);
        setReviews([]);
      })
      .finally(() => setLoading(false));
  }, [email, currentUser, isAuthLoading]);

  const handleDeleteAccount = async () => {
    try {
      await deleteAccount();
      toast.success('Account deleted');
      setShowDeleteAccountDialog(false);
      signOut();
      navigate('/');
    } catch (err: any) {
      toast.error(err.message || 'Could not delete account');
      return;
    }
  };

  const renderStars = (rating: number, size: 'sm' | 'md' = 'md') => {
    const cls = size === 'sm' ? 'w-4 h-4' : 'w-5 h-5';
    return (
      <div className="flex items-center gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`${cls} ${
              star <= Math.floor(rating)
                ? 'fill-yellow-400 text-yellow-400'
                : 'text-gray-300'
            }`}
          />
        ))}
      </div>
    );
  };

  if (!currentUser) {
    return (
      <div className="min-h-screen bg-red-50 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Please sign in to view your profile</h2>
          <Button onClick={() => navigate('/auth')} className="bg-red-600 hover:bg-red-700">
            Sign In
          </Button>
        </div>
      </div>
    );
  }

  if (isAuthLoading || loading) {
    return (
      <div className="min-h-screen bg-red-50 flex items-center justify-center">
        <p className="text-gray-500">Loading profile...</p>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-red-50 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">User not found</h2>
          <Button onClick={() => navigate('/')} className="bg-red-600 hover:bg-red-700">
            Back to Listings
          </Button>
        </div>
      </div>
    );
  }

  const isOwnProfile = currentUser?.email === profile.email;

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
      <div className="max-w-[1000px] mx-auto px-6 py-8 relative z-1">
        <button
          onClick={() => navigate('/')}
          className="flex items-center space-x-2 text-gray-700 hover:text-red-600 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Back to Listings</span>
        </button>

        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          {/* Profile Header */}
          <div className="bg-gradient-to-r from-red-600 to-red-700 h-32"></div>

          <div className="px-8 pb-8">
            {/* Avatar and Basic Info */}
            <div className="flex items-start gap-6 -mt-16 mb-6">
              <div className="w-32 h-32 bg-white rounded-full flex items-center justify-center border-4 border-white shadow-lg">
                <UserIcon className="w-16 h-16 text-red-600" />
              </div>
              <div className="mt-16 flex-1">
                <div className="flex items-start justify-between">
                  <div>
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">{profile.name}</h1>
                    <div className="flex items-center gap-2">
                      {renderStars(profile.rating || 5.0)}
                      <span className="text-lg font-semibold text-gray-900">{(profile.rating || 5.0).toFixed(1)}</span>
                      <span className="text-sm text-gray-500">
                        ({profile.totalReviews} {profile.totalReviews === 1 ? 'review' : 'reviews'})
                      </span>
                    </div>
                  </div>
                  {isOwnProfile && (
                    <Button
                      onClick={() => setShowDeleteAccountDialog(true)}
                      variant="outline"
                      className="border-red-600 text-red-600 hover:bg-red-50"
                    >
                      Delete Account
                    </Button>
                  )}
                </div>
              </div>
            </div>

            {/* Profile Information */}
            <div className="grid md:grid-cols-2 gap-6 mb-6">
              <div className="space-y-4">
                {profile.joinDate && profile.joinDate !== 'N/A' && (
                  <div className="flex items-center gap-3 text-gray-600">
                    <Calendar className="w-5 h-5 text-red-600" />
                    <div>
                      <p className="text-sm text-gray-500">Member Since</p>
                      <p className="font-medium">{profile.joinDate}</p>
                    </div>
                  </div>
                )}
                {profile.location && (
                  <div className="flex items-center gap-3 text-gray-600">
                    <MapPin className="w-5 h-5 text-red-600" />
                    <div>
                      <p className="text-sm text-gray-500">Location</p>
                      <p className="font-medium">{profile.location}</p>
                    </div>
                  </div>
                )}
              </div>
              <div className="space-y-4">
                <div className="flex items-center gap-3 text-gray-600">
                  <Mail className="w-5 h-5 text-red-600" />
                  <div>
                    <p className="text-sm text-gray-500">Email</p>
                    <p className="font-medium">{profile.email}</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Reviews — shown first so they're visible without scrolling */}
            <div className="border-t pt-6 mb-6">
              <h3 className="font-semibold text-gray-900 mb-4">
                Reviews ({profile.totalReviews})
              </h3>
              {reviews.length === 0 ? (
                <p className="text-gray-500 text-sm">
                  {profile.totalReviews > 0 ? 'Loading reviews...' : 'No reviews yet.'}
                </p>
              ) : (
                <div className="space-y-4">
                  {reviews.map((review) => (
                    <div key={review.id} className="bg-gray-50 rounded-lg p-4">
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          {renderStars(review.rating, 'sm')}
                          <span className="font-semibold text-gray-900 text-sm">{review.rating}/5</span>
                        </div>
                        <span className="text-xs text-gray-400">{formatDate(review.date)}</span>
                      </div>
                      {review.comment && (
                        <p className="text-gray-700 text-sm">{review.comment}</p>
                      )}
                      <p className="text-xs text-gray-400 mt-1">— {review.reviewerName}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Active Listings */}
            {isOwnProfile && userListings.length > 0 && (
              <div className="border-t pt-6 mb-6">
                <h3 className="font-semibold text-gray-900 mb-4">My Active Listings</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {userListings.map((listing) => (
                    <div
                      key={listing.id}
                      className="bg-gray-50 rounded-lg p-4 flex items-center gap-4 hover:bg-gray-100 transition-colors cursor-pointer"
                      onClick={() => navigate(`/listing/${listing.id}`)}
                    >
                      <div className="w-16 h-16 bg-gray-200 rounded-lg flex items-center justify-center flex-shrink-0">
                        <Package className="w-8 h-8 text-gray-400" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h4 className="font-medium text-gray-900 truncate hover:text-red-600">
                          {listing.title}
                        </h4>
                        <p className="text-lg font-bold text-red-600">${listing.price}</p>
                        <p className="text-sm text-gray-500">{listing.condition}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {isOwnProfile && userListings.length === 0 && (
              <div className="border-t pt-6 text-center text-gray-500">
                <Package className="w-12 h-12 mx-auto mb-2 text-gray-300" />
                <p>No active listings.</p>
                <Button
                  onClick={() => navigate('/create-listing')}
                  className="mt-4 bg-red-600 hover:bg-red-700"
                >
                  Create a Listing
                </Button>
              </div>
            )}
            {!isOwnProfile && (
              <div className="border-t pt-6">
                {/* seller's active listings visible to buyers */}
                {userListings.length > 0 && (
                  <>
                    <h3 className="font-semibold text-gray-900 mb-4">Active Listings</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {userListings.map((listing) => (
                        <div
                          key={listing.id}
                          className="bg-gray-50 rounded-lg p-4 flex items-center gap-4 hover:bg-gray-100 transition-colors cursor-pointer"
                          onClick={() => navigate(`/listing/${listing.id}`)}
                        >
                          <div className="w-16 h-16 bg-gray-200 rounded-lg flex items-center justify-center flex-shrink-0">
                            <Package className="w-8 h-8 text-gray-400" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <h4 className="font-medium text-gray-900 truncate hover:text-red-600">
                              {listing.title}
                            </h4>
                            <p className="text-lg font-bold text-red-600">${listing.price}</p>
                            <p className="text-sm text-gray-500">{listing.condition}</p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Delete Account confirmation */}
      {showDeleteAccountDialog && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4 shadow-xl">
            <h2 className="text-xl font-bold text-gray-900 mb-2">Delete Account?</h2>
            <p className="text-gray-600 mb-6">
              Are you sure? This will permanently delete your account, listings, messages, and reviews. You will not be able to log back in with this account.
            </p>
            <div className="flex gap-3 justify-end">
              <Button variant="outline" onClick={() => setShowDeleteAccountDialog(false)}>
                Cancel
              </Button>
              <Button onClick={handleDeleteAccount} className="bg-red-600 hover:bg-red-700">
                Delete Account
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
