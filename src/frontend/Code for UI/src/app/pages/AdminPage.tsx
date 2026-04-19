import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Users, Flag, Package, Trash2, Shield, Ban, CheckCircle, Clock } from 'lucide-react';
import { Button } from '../components/ui/button';
import { useAuth } from '../context/AuthContext';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
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
import {
  adminGetReports,
  adminGetTransactions,
  adminGetUsers,
  adminRemoveListing,
  adminRemoveReview,
  adminSuspendUser,
  adminUnsuspendUser,
  getListings,
} from '../lib/api';

interface AdminUser {
  id: number;
  name: string;
  email: string;
  isAdmin: boolean;
  isSuspended: boolean;
}

interface AdminReport {
  id: number;
  buyerId: number;
  sellerId: number;
  listingId: number;
  ratingDescription: string;
  reportDescription: string;
  createdAt: string;
}

interface AdminTransaction {
  id: number;
  buyerId: number;
  sellerId: number;
  listingId: number;
  status: string;
  createdAt: string;
}

function mapAdminUser(row: any[]): AdminUser {
  return {
    id: Number(row[0]),
    name: row[1] || `User #${row[0]}`,
    email: row[2] || '',
    isAdmin: row[3] === true || row[3] === 'true',
    isSuspended: row[4] === true || row[4] === 'true',
  };
}

function mapAdminReport(row: any[]): AdminReport {
  return {
    id: Number(row[0]),
    buyerId: Number(row[1]),
    sellerId: Number(row[2]),
    listingId: Number(row[3]),
    ratingDescription: row[4] || '',
    reportDescription: row[5] || '',
    createdAt: row[6] || '',
  };
}

function mapAdminTransaction(row: any[]): AdminTransaction {
  return {
    id: Number(row[0]),
    buyerId: Number(row[1]),
    sellerId: Number(row[2]),
    listingId: Number(row[3]),
    status: row[4] || '',
    createdAt: row[5] || '',
  };
}

export function AdminPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [reports, setReports] = useState<AdminReport[]>([]);
  const [transactions, setTransactions] = useState<AdminTransaction[]>([]);
  const [listings, setListings] = useState<Listing[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteItemType, setDeleteItemType] = useState<'listing' | 'report' | null>(null);
  const [deleteItemId, setDeleteItemId] = useState<number | null>(null);

  useEffect(() => {
    if (!user) return;
    if (!user.isAdmin) {
      navigate('/');
    }
  }, [user, navigate]);

  useEffect(() => {
    if (!user?.isAdmin) return;
    loadData();
  }, [user]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [userRows, reportRows, transactionRows, listingRows] = await Promise.all([
        adminGetUsers(),
        adminGetReports(),
        adminGetTransactions(),
        getListings(),
      ]);

      setUsers(userRows.map(mapAdminUser));
      setReports(reportRows.map(mapAdminReport));
      setTransactions(transactionRows.map(mapAdminTransaction));
      setListings(listingRows as Listing[]);
    } catch (err: any) {
      toast.error(err.message || 'Could not load admin data');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleSuspend = async (target: AdminUser) => {
    try {
      const result = target.isSuspended
        ? await adminUnsuspendUser(target.id)
        : await adminSuspendUser(target.id);

      if (result.success) {
        toast.success(result.message);
        loadData();
      } else {
        toast.error(result.message);
      }
    } catch (err: any) {
      toast.error(err.message || 'Could not update user');
    }
  };

  const handleDeleteListing = async (listingId: number) => {
    try {
      const result = await adminRemoveListing(listingId);
      if (result.success) {
        toast.success(result.message);
        loadData();
      } else {
        toast.error(result.message);
      }
    } catch (err: any) {
      toast.error(err.message || 'Could not remove listing');
    } finally {
      setDeleteItemType(null);
      setDeleteItemId(null);
    }
  };

  const handleDeleteReport = async (reportId: number) => {
    try {
      const result = await adminRemoveReview(reportId);
      if (result.success) {
        toast.success(result.message);
        loadData();
      } else {
        toast.error(result.message);
      }
    } catch (err: any) {
      toast.error(err.message || 'Could not remove report');
    } finally {
      setDeleteItemType(null);
      setDeleteItemId(null);
    }
  };

  if (!user?.isAdmin) {
    return null;
  }

  return (
    <div className="min-h-screen bg-red-50">
      <div className="fixed inset-0 pointer-events-none opacity-5 z-0">
        <div className="absolute top-40 left-1/4 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute top-96 right-1/3 w-18 h-18 bg-black rounded-full"></div>
        <div className="absolute top-[600px] left-1/2 w-30 h-30 bg-black rounded-full"></div>
        <div className="absolute bottom-80 right-1/4 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute bottom-40 left-1/3 w-18 h-18 bg-black rounded-full"></div>
      </div>

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

      <div className="max-w-[1400px] mx-auto px-6 py-8 relative z-1">
        <div className="bg-white rounded-lg shadow-md p-8">
          <div className="flex items-center justify-between gap-4 mb-6">
            <h2 className="text-3xl font-bold text-gray-900">Admin Dashboard</h2>
            <Button onClick={loadData} variant="outline" disabled={loading}>
              {loading ? 'Loading...' : 'Refresh'}
            </Button>
          </div>

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
                Listings ({listings.length})
              </TabsTrigger>
              <TabsTrigger value="transactions">
                <Clock className="w-4 h-4 mr-2" />
                Transactions ({transactions.length})
              </TabsTrigger>
            </TabsList>

            <TabsContent value="users" className="mt-6">
              <div className="space-y-4">
                {users.filter((account) => !account.isAdmin).map((account) => (
                  <div key={account.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="font-semibold text-gray-900">{account.name}</h3>
                          {account.isSuspended && (
                            <span className="bg-red-100 text-red-700 text-xs px-2 py-1 rounded">
                              Suspended
                            </span>
                          )}
                        </div>
                        <p className="text-sm text-gray-600">{account.email}</p>
                        <p className="text-sm text-gray-500 mt-1">Account ID: {account.id}</p>
                      </div>
                      <Button
                        size="sm"
                        variant={account.isSuspended ? 'default' : 'outline'}
                        onClick={() => handleToggleSuspend(account)}
                        className={account.isSuspended ? 'bg-green-600 hover:bg-green-700' : 'border-red-600 text-red-600 hover:bg-red-50'}
                      >
                        {account.isSuspended ? (
                          <>
                            <CheckCircle className="w-4 h-4 mr-1" />
                            Unsuspend
                          </>
                        ) : (
                          <>
                            <Ban className="w-4 h-4 mr-1" />
                            Suspend
                          </>
                        )}
                      </Button>
                    </div>
                  </div>
                ))}
                {!loading && users.filter((account) => !account.isAdmin).length === 0 && (
                  <p className="text-center text-gray-500 py-8">No non-admin users found</p>
                )}
              </div>
            </TabsContent>

            <TabsContent value="reports" className="mt-6">
              <div className="space-y-4">
                {reports.length === 0 ? (
                  <p className="text-center text-gray-500 py-8">No reports to review</p>
                ) : (
                  reports.map((report) => (
                    <div key={report.id} className="border border-red-200 bg-red-50 rounded-lg p-4">
                      <div className="flex items-start justify-between gap-4 mb-3">
                        <div className="flex-1">
                          <h3 className="font-semibold text-gray-900">
                            Report #{report.id} for listing #{report.listingId}
                          </h3>
                          <p className="text-sm text-gray-600">
                            Buyer #{report.buyerId} reported seller #{report.sellerId}
                          </p>
                          <p className="text-sm text-gray-500">{report.createdAt}</p>
                        </div>
                        <Button
                          onClick={() => {
                            setDeleteItemType('report');
                            setDeleteItemId(report.id);
                          }}
                          variant="outline"
                          size="sm"
                          className="border-red-600 text-red-600"
                        >
                          <Trash2 className="w-4 h-4 mr-1" />
                          Remove
                        </Button>
                      </div>
                      <div className="bg-white rounded p-3">
                        <p className="text-sm font-medium text-gray-700">{report.ratingDescription || 'No rating description'}</p>
                        <p className="text-sm text-gray-600 mt-1">{report.reportDescription || 'No report description'}</p>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </TabsContent>

            <TabsContent value="listings" className="mt-6">
              <div className="space-y-4">
                {listings.map((listing) => (
                  <div key={listing.id} className="border border-gray-200 rounded-lg p-4 flex gap-4">
                    <img
                      src={listing.image}
                      alt={listing.title}
                      className="w-24 h-24 object-cover rounded"
                    />
                    <div className="flex-1">
                      <h3 className="font-semibold text-gray-900">{listing.title}</h3>
                      <p className="text-sm text-gray-600">${listing.price}</p>
                      <p className="text-sm text-gray-500">Category: {listing.category}</p>
                      <p className="text-sm text-gray-500">
                        Posted by: {listing.listerName} ({listing.listerEmail})
                      </p>
                      <p className="text-sm text-gray-500">{listing.postedDate}</p>
                    </div>
                    <Button
                      onClick={() => {
                        setDeleteItemType('listing');
                        setDeleteItemId(listing.id);
                      }}
                      variant="outline"
                      size="sm"
                      className="border-red-600 text-red-600"
                    >
                      <Trash2 className="w-4 h-4 mr-1" />
                      Remove
                    </Button>
                  </div>
                ))}
                {!loading && listings.length === 0 && (
                  <p className="text-center text-gray-500 py-8">No available listings found</p>
                )}
              </div>
            </TabsContent>

            <TabsContent value="transactions" className="mt-6">
              <div className="space-y-4">
                {transactions.length === 0 ? (
                  <p className="text-center text-gray-500 py-8">No transactions found</p>
                ) : (
                  transactions.map((transaction) => (
                    <div key={transaction.id} className="border border-gray-200 rounded-lg p-4">
                      <h3 className="font-semibold text-gray-900">Transaction #{transaction.id}</h3>
                      <p className="text-sm text-gray-600">Listing #{transaction.listingId}</p>
                      <p className="text-sm text-gray-500">Buyer #{transaction.buyerId} and seller #{transaction.sellerId}</p>
                      <p className="text-sm text-gray-500">Status: {transaction.status}</p>
                      <p className="text-sm text-gray-500">{transaction.createdAt}</p>
                    </div>
                  ))
                )}
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </div>

      <AlertDialog open={!!deleteItemType} onOpenChange={() => { setDeleteItemType(null); setDeleteItemId(null); }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {deleteItemType === 'report' ? 'Remove Report' : 'Remove Listing'}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {deleteItemType === 'report'
                ? 'This removes the feedback/report record from the database.'
                : 'This marks the listing as Removed in the database.'}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (deleteItemType === 'listing' && deleteItemId) {
                  handleDeleteListing(deleteItemId);
                } else if (deleteItemType === 'report' && deleteItemId) {
                  handleDeleteReport(deleteItemId);
                }
              }}
              className="bg-red-600 hover:bg-red-700"
            >
              Remove
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
