import { useState, useMemo } from 'react';
import { useParams, useNavigate, Link } from 'react-router';
import { ArrowLeft, Heart, MapPin, Calendar, User as UserIcon, MessageCircle, Star } from 'lucide-react';
import { listings } from '../data/listings';
import { MessageDialog } from '../components/MessageDialog';
import { Button } from '../components/ui/button';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';
import { Listing } from '../types';
import { useAuth } from '../context/AuthContext';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '../components/ui/alert-dialog';

export function ListingDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);
  const [isMessageDialogOpen, setIsMessageDialogOpen] = useState(false);
  const [showLoginAlert, setShowLoginAlert] = useState(false);

  // Combine default listings with user-created listings
  const allListings = useMemo(() => {
    const userListingsJson = localStorage.getItem('userListings');
    const userListings: Listing[] = userListingsJson ? JSON.parse(userListingsJson) : [];
    return [...listings, ...userListings];
  }, []);

  const listing = allListings.find((l) => l.id === Number(id));

  // Get lister profile info
  const listerProfile = useMemo(() => {
    if (!listing?.listerEmail) return null;
    const usersJson = localStorage.getItem('users');
    const users = usersJson ? JSON.parse(usersJson) : [];
    return users.find((u: any) => u.email === listing.listerEmail);
  }, [listing]);

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
    } else {
      setIsMessageDialogOpen(true);
    }
  };

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
                  src={listing.images[selectedImageIndex]}
                  alt={listing.title}
                  className="w-full h-full object-cover"
                />
              </div>
              <div className="grid grid-cols-4 gap-2">
                {listing.images.map((image, index) => (
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
                  <MapPin className="w-4 h-4" />
                  <span>{listing.location}</span>
                </div>
                <div className="flex items-center space-x-1">
                  <Calendar className="w-4 h-4" />
                  <span>Posted {listing.postedDate}</span>
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
                      {listerProfile && listerProfile.rating && renderStars(listerProfile.rating)}
                    </div>
                    <p className="text-sm text-gray-500">Member since {listing.listerJoinDate}</p>
                  </div>
                </div>
                
                {listing.listerEmail && (
                  <Link 
                    to={`/profile/${listing.listerEmail}`}
                    className="block mb-3"
                  >
                    <Button
                      variant="outline"
                      className="w-full border-red-600 text-red-600 hover:bg-red-50"
                    >
                      View Profile
                    </Button>
                  </Link>
                )}
                
                <Button
                  onClick={handleMessageClick}
                  className="w-full bg-red-600 hover:bg-red-700"
                  size="lg"
                >
                  <MessageCircle className="w-5 h-5 mr-2" />
                  Message Seller
                </Button>
              </div>
            </div>
          </div>

          {/* Location Map Section */}
          <div className="border-t p-8">
            <h3 className="font-semibold text-gray-900 mb-4">Location</h3>
            <div className="aspect-video bg-gray-100 rounded-lg overflow-hidden">
              <iframe
                width="100%"
                height="100%"
                style={{ border: 0 }}
                loading="lazy"
                allowFullScreen
                src={`https://www.google.com/maps/embed/v1/place?key=AIzaSyBFw0Qbyq9zTFTd-tUY6dZWTgaQzuU17R8&q=${encodeURIComponent(listing.location + ', Houston, TX')}&zoom=13`}
              ></iframe>
            </div>
            <p className="text-sm text-gray-500 mt-2">
              <MapPin className="w-4 h-4 inline mr-1" />
              {listing.location}
            </p>
          </div>
        </div>
      </div>

      <MessageDialog
        isOpen={isMessageDialogOpen}
        onClose={() => setIsMessageDialogOpen(false)}
        listerName={listing.listerName}
        listingTitle={listing.title}
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
    </div>
  );
}