import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router';
import { ArrowLeft, Users, Flag, Package, Trash2, Shield, Star, Ban, CheckCircle } from 'lucide-react';
import { Button } from '../components/ui/button';
import { useAuth } from '../context/AuthContext';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import { listings as defaultListings } from '../data/listings';
import { Listing } from '../types';
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
import { toast } from 'sonner';
import { SiteLogo } from '../components/SiteLogo';

export function AdminPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [users, setUsers] = useState<any[]>([]);
  const [reports, setReports] = useState<any[]>([]);
  const [reviews, setReviews] = useState<any[]>([]);
  const [userListings, setUserListings] = useState<Listing[]>([]);
  const [deleteItemType, setDeleteItemType] = useState<'listing' | 'review' | 'report' | null>(null);
  const [deleteItemId, setDeleteItemId] = useState<string | null>(null);

  // Check if user is admin
  useEffect(() => {
    if (!user || user.email !== 'admin@email.com') {
      navigate('/');
    }
  }, [user, navigate]);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = () => {
    // Load users
    const usersJson = localStorage.getItem('users');
    if (usersJson) {
      setUsers(JSON.parse(usersJson));
    }

    // Load reports
    const reportsJson = localStorage.getItem('reports');
    if (reportsJson) {
      setReports(JSON.parse(reportsJson));
    }

    // Load reviews
    const reviewsJson = localStorage.getItem('reviews');
    if (reviewsJson) {
      setReviews(JSON.parse(reviewsJson));
    }

    // Load user listings
    const listingsJson = localStorage.getItem('userListings');
    if (listingsJson) {
      setUserListings(JSON.parse(listingsJson));
    }
  };

  const allListings = useMemo(() => {
    return [...defaultListings, ...userListings];
  }, [userListings]);

  const handleDeleteListing = (listingId: number) => {
    const updatedListings = userListings.filter(l => l.id !== listingId);
    localStorage.setItem('userListings', JSON.stringify(updatedListings));
    setUserListings(updatedListings);
    toast.success('Listing deleted');
    setDeleteItemType(null);
    setDeleteItemId(null);
  };

  const handleDeleteReview = (reviewId: string) => {
    const updatedReviews = reviews.filter(r => r.id !== reviewId);
    localStorage.setItem('reviews', JSON.stringify(updatedReviews));
    setReviews(updatedReviews);

    // Update user ratings
    const deletedReview = reviews.find(r => r.id === reviewId);
    if (deletedReview) {
      const usersJson = localStorage.getItem('users');
      const users = usersJson ? JSON.parse(usersJson) : [];

      const remainingReviews = updatedReviews.filter(
        (r: any) => r.reviewedUserEmail === deletedReview.reviewedUserEmail
      );

      const totalReviews = remainingReviews.length;
      const newRating = totalReviews > 0
        ? remainingReviews.reduce((sum: number, r: any) => sum + r.rating, 0) / totalReviews
        : 5.0;

      const updatedUsers = users.map((u: any) => {
        if (u.email === deletedReview.reviewedUserEmail) {
          return { ...u, rating: newRating, totalReviews };
        }
        return u;
      });

      localStorage.setItem('users', JSON.stringify(updatedUsers));
    }

    toast.success('Review deleted');
    setDeleteItemType(null);
    setDeleteItemId(null);
  };

  const handleDismissReport = (reportId: string) => {
    const updatedReports = reports.filter(r => r.id !== reportId);
    localStorage.setItem('reports', JSON.stringify(updatedReports));
    setReports(updatedReports);
    toast.success('Report dismissed');
    setDeleteItemType(null);
    setDeleteItemId(null);
  };

  const handleToggleBanListing = (userEmail: string) => {
    const updatedUsers = users.map(u => {
      if (u.email === userEmail) {
        return { ...u, bannedFromListing: !u.bannedFromListing };
      }
      return u;
    });
    localStorage.setItem('users', JSON.stringify(updatedUsers));
    setUsers(updatedUsers);

    const user = updatedUsers.find(u => u.email === userEmail);
    toast.success(user?.bannedFromListing ? 'User banned from listing' : 'User unbanned from listing');
  };

  const handleToggleBanPurchasing = (userEmail: string) => {
    const updatedUsers = users.map(u => {
      if (u.email === userEmail) {
        return { ...u, bannedFromPurchasing: !u.bannedFromPurchasing };
      }
      return u;
    });
    localStorage.setItem('users', JSON.stringify(updatedUsers));
    setUsers(updatedUsers);

    const user = updatedUsers.find(u => u.email === userEmail);
    toast.success(user?.bannedFromPurchasing ? 'User banned from purchasing' : 'User unbanned from purchasing');
  };

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
              <h1 className="text-2xl font-bold text-white">Common Ground - Admin</h1>
            </div>
            <button
              onClick={() => navigate('/')}
              className="flex items-center space-x-2 px-5 py-2.5 bg-white text-red-600 hover:bg-red-50 rounded-lg transition-colors"
            >
              <Shield className="w-5 h-5" />
              <span>Exit Admin</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-[1400px] mx-auto px-6 py-8 relative z-1">
        <div className="bg-white rounded-lg shadow-md p-8">
          <h2 className="text-3xl font-bold text-gray-900 mb-6">Admin Dashboard</h2>

          <Tabs defaultValue="users" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="users">
                <Users className="w-4 h-4 mr-2" />
                Users ({users.length})
              </TabsTrigger>
              <TabsTrigger value="reports">
                <Flag className="w-4 h-4 mr-2" />
                Reports ({reports.length})
              </TabsTrigger>
              <TabsTrigger value="listings">
                <Package className="w-4 h-4 mr-2" />
                Listings ({allListings.length})
              </TabsTrigger>
              <TabsTrigger value="reviews">
                <Star className="w-4 h-4 mr-2" />
                Reviews ({reviews.length})
              </TabsTrigger>
            </TabsList>

            {/* Users Tab */}
            <TabsContent value="users" className="mt-6">
              <div className="space-y-4">
                {users.filter(u => u.email !== 'admin@email.com').map((user) => (
                  <div key={user.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="font-semibold text-gray-900">{user.name}</h3>
                          {user.bannedFromListing && (
                            <span className="bg-red-100 text-red-700 text-xs px-2 py-1 rounded">
                              Banned from Listing
                            </span>
                          )}
                          {user.bannedFromPurchasing && (
                            <span className="bg-orange-100 text-orange-700 text-xs px-2 py-1 rounded">
                              Banned from Purchasing
                            </span>
                          )}
                        </div>
                        <p className="text-sm text-gray-600">{user.email}</p>
                        <p className="text-sm text-gray-500 mt-1">
                          Member since: {user.joinDate}
                        </p>
                        <p className="text-sm text-gray-500">
                          Rating: {user.rating?.toFixed(1) || '5.0'} ({user.totalReviews || 0} reviews)
                        </p>
                        {user.phoneNumber && (
                          <p className="text-sm text-gray-500">Phone: {user.phoneNumber}</p>
                        )}
                        {user.address && (
                          <p className="text-sm text-gray-500">Address: {user.address}</p>
                        )}
                        <p className="text-sm text-gray-500 mt-2">
                          {reports.filter(r => r.reportedUserEmail === user.email).length} reports
                        </p>
                      </div>
                      <div className="flex flex-col gap-2">
                        <Button
                          size="sm"
                          variant={user.bannedFromListing ? "default" : "outline"}
                          onClick={() => handleToggleBanListing(user.email)}
                          className={user.bannedFromListing ? "bg-green-600 hover:bg-green-700" : "border-red-600 text-red-600 hover:bg-red-50"}
                        >
                          {user.bannedFromListing ? (
                            <>
                              <CheckCircle className="w-4 h-4 mr-1" />
                              Unban Listing
                            </>
                          ) : (
                            <>
                              <Ban className="w-4 h-4 mr-1" />
                              Ban Listing
                            </>
                          )}
                        </Button>
                        <Button
                          size="sm"
                          variant={user.bannedFromPurchasing ? "default" : "outline"}
                          onClick={() => handleToggleBanPurchasing(user.email)}
                          className={user.bannedFromPurchasing ? "bg-green-600 hover:bg-green-700" : "border-orange-600 text-orange-600 hover:bg-orange-50"}
                        >
                          {user.bannedFromPurchasing ? (
                            <>
                              <CheckCircle className="w-4 h-4 mr-1" />
                              Unban Purchase
                            </>
                          ) : (
                            <>
                              <Ban className="w-4 h-4 mr-1" />
                              Ban Purchase
                            </>
                          )}
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </TabsContent>

            {/* Reports Tab */}
            <TabsContent value="reports" className="mt-6">
              <div className="space-y-4">
                {reports.length === 0 ? (
                  <p className="text-center text-gray-500 py-8">No reports to review</p>
                ) : (
                  reports.map((report) => (
                    <div key={report.id} className="border border-red-200 bg-red-50 rounded-lg p-4">
                      <div className="flex items-start justify-between mb-3">
                        <div className="flex-1">
                          <h3 className="font-semibold text-gray-900">
                            Report against: {report.reportedUserEmail}
                          </h3>
                          <p className="text-sm text-gray-600">
                            Reported by: {report.reporterEmail}
                          </p>
                          <p className="text-sm text-gray-500">
                            {new Date(report.timestamp).toLocaleString()}
                          </p>
                        </div>
                      </div>
                      <div className="bg-white rounded p-3 mb-3">
                        <p className="text-sm font-medium text-gray-700">Reason: {report.reason}</p>
                        <p className="text-sm text-gray-600 mt-1">{report.description}</p>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          onClick={() => {
                            setDeleteItemType('report');
                            setDeleteItemId(report.id);
                          }}
                          variant="outline"
                          size="sm"
                        >
                          Dismiss Report
                        </Button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </TabsContent>

            {/* Listings Tab */}
            <TabsContent value="listings" className="mt-6">
              <div className="space-y-4">
                {allListings.map((listing) => (
                  <div key={listing.id} className="border border-gray-200 rounded-lg p-4 flex gap-4">
                    <img
                      src={listing.image}
                      alt={listing.title}
                      className="w-24 h-24 object-cover rounded"
                    />
                    <div className="flex-1">
                      <h3 className="font-semibold text-gray-900">{listing.title}</h3>
                      <p className="text-sm text-gray-600">${listing.price}</p>
                      <p className="text-sm text-gray-500">
                        Posted by: {listing.listerName} ({listing.listerEmail})
                      </p>
                      <p className="text-sm text-gray-500">{listing.postedDate}</p>
                    </div>
                    {userListings.some(l => l.id === listing.id) && (
                      <Button
                        onClick={() => {
                          setDeleteItemType('listing');
                          setDeleteItemId(listing.id.toString());
                        }}
                        variant="outline"
                        size="sm"
                        className="border-red-600 text-red-600"
                      >
                        <Trash2 className="w-4 h-4 mr-1" />
                        Delete
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            </TabsContent>

            {/* Reviews Tab */}
            <TabsContent value="reviews" className="mt-6">
              <div className="space-y-4">
                {reviews.map((review) => (
                  <div key={review.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900">
                          Review for: {review.reviewedUserEmail}
                        </h3>
                        <p className="text-sm text-gray-600">By: {review.reviewerName}</p>
                        <div className="flex items-center gap-1 mt-1">
                          {[1, 2, 3, 4, 5].map((star) => (
                            <Star
                              key={star}
                              className={`w-4 h-4 ${
                                star <= review.rating
                                  ? 'fill-yellow-400 text-yellow-400'
                                  : 'text-gray-300'
                              }`}
                            />
                          ))}
                        </div>
                      </div>
                      <Button
                        onClick={() => {
                          setDeleteItemType('review');
                          setDeleteItemId(review.id);
                        }}
                        variant="outline"
                        size="sm"
                        className="border-red-600 text-red-600"
                      >
                        <Trash2 className="w-4 h-4 mr-1" />
                        Delete
                      </Button>
                    </div>
                    <p className="text-sm text-gray-600">{review.comment}</p>
                    <p className="text-xs text-gray-500 mt-2">{review.date}</p>
                    {review.listingTitle && (
                      <p className="text-xs text-gray-500">Listing: {review.listingTitle}</p>
                    )}
                  </div>
                ))}
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </div>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={!!deleteItemType} onOpenChange={() => { setDeleteItemType(null); setDeleteItemId(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {deleteItemType === 'report' ? 'Dismiss Report' : `Delete ${deleteItemType}`}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {deleteItemType === 'report'
                ? 'Are you sure you want to dismiss this report? This action cannot be undone.'
                : `Are you sure you want to delete this ${deleteItemType}? This action cannot be undone.`}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (deleteItemType === 'listing' && deleteItemId) {
                  handleDeleteListing(Number(deleteItemId));
                } else if (deleteItemType === 'review' && deleteItemId) {
                  handleDeleteReview(deleteItemId);
                } else if (deleteItemType === 'report' && deleteItemId) {
                  handleDismissReport(deleteItemId);
                }
              }}
              className="bg-red-600 hover:bg-red-700"
            >
              {deleteItemType === 'report' ? 'Dismiss' : 'Delete'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
