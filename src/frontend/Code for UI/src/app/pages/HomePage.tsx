import { useState, useMemo, useEffect } from 'react';
import { User, Plus, LogOut, HelpCircle, Clock, Shield } from 'lucide-react';
import { useNavigate } from 'react-router';
import { ListingCard } from '../components/ListingCard';
import { FilterSection, CheckboxFilter } from '../components/FilterSection';
import { FilterState, Listing } from '../types';
import { useAuth } from '../context/AuthContext';
import { ChatSidebar } from '../components/ChatSidebar';
import { SiteLogo } from '../components/SiteLogo';
import { getListingOptions, getListings, getTransactions } from '../lib/api';

export function HomePage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, signOut } = useAuth();
  const [filters, setFilters] = useState<FilterState>({
    categories: [],
    priceRanges: [],
    conditions: [],
  });

  const [sortBy, setSortBy] = useState('recommended');
  const [pendingTransactionsCount, setPendingTransactionsCount] = useState(0);
  const [allListings, setAllListings] = useState<Listing[]>([]);
  const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
  const [conditionOptions, setConditionOptions] = useState<string[]>([]);
  const [loadingListings, setLoadingListings] = useState(true);

  // pull real listings from the database
  useEffect(() => {
    getListings()
      .then((listings) => {
        const loadedListings = listings as Listing[];
        setAllListings(loadedListings);
      })
      .catch((err) => console.error('could not load listings', err))
      .finally(() => setLoadingListings(false));
  }, []);

  useEffect(() => {
    getListingOptions()
      .then((options) => {
        setCategoryOptions(options.categories);
        setConditionOptions(options.conditions);
      })
      .catch((err) => console.error('could not load listing options from backend', err));
  }, []);

  useEffect(() => {
    if (user) {
      getTransactions()
        .then((transactions) => {
          const pending = transactions.filter((t: any) =>
            t.status !== 'Completed' && t.status !== 'Cancelled'
          );
          setPendingTransactionsCount(pending.length);
        })
        .catch(() => setPendingTransactionsCount(0));
    }
  }, [user]);

  const handleFilterChange = (filterType: keyof FilterState, value: string, checked: boolean) => {
    setFilters((prev) => ({
      ...prev,
      [filterType]: checked
        ? [...prev[filterType], value]
        : prev[filterType].filter((item) => item !== value),
    }));
  };

  const filteredAndSortedListings = useMemo(() => {
    let filtered = [...allListings];

    // Apply category filters
    if (filters.categories.length > 0) {
      filtered = filtered.filter((listing) => filters.categories.includes(listing.category));
    }

    // Apply price range filters
    if (filters.priceRanges.length > 0) {
      filtered = filtered.filter((listing) => {
        return filters.priceRanges.some((range) => {
          if (range === 'Under $50') return listing.price < 50;
          if (range === '$50 - $200') return listing.price >= 50 && listing.price <= 200;
          if (range === '$200 - $500') return listing.price > 200 && listing.price <= 500;
          if (range === '$500 - $1000') return listing.price > 500 && listing.price <= 1000;
          if (range === 'Over $1000') return listing.price > 1000;
          return false;
        });
      });
    }

    // Apply condition filters
    if (filters.conditions.length > 0) {
      filtered = filtered.filter((listing) => filters.conditions.includes(listing.condition));
    }

    // Apply sorting
    switch (sortBy) {
      case 'price-low':
        filtered.sort((a, b) => a.price - b.price);
        break;
      case 'price-high':
        filtered.sort((a, b) => b.price - a.price);
        break;
      case 'newest':
        filtered.sort((a, b) => new Date(b.postedDate).getTime() - new Date(a.postedDate).getTime());
        break;
      default:
        // Keep original order for 'recommended'
        break;
    }

    return filtered;
  }, [allListings, filters, sortBy]);

  return (
    <div className="min-h-screen bg-red-50 relative overflow-x-hidden">
      {/* Background Pattern */}
      <div className="fixed inset-0 pointer-events-none opacity-5 z-0">
        <div className="absolute top-40 left-1/4 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute top-80 right-1/3 w-18 h-18 bg-black rounded-full"></div>
        <div className="absolute top-[500px] left-1/2 w-30 h-30 bg-black rounded-full"></div>
        <div className="absolute top-[700px] right-1/4 w-24 h-24 bg-black rounded-full"></div>
        <div className="absolute bottom-60 left-1/3 w-18 h-18 bg-black rounded-full"></div>
        <div className="absolute bottom-40 right-1/2 w-24 h-24 bg-black rounded-full"></div>
      </div>

      {/* Header */}
      <header className="bg-red-600 shadow-md sticky top-0 z-10 relative">
        <div className="max-w-[1600px] mx-auto px-6 py-4 bg-[#00000000]">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/')}>
              <SiteLogo />
              <h1 className="text-2xl font-bold text-white">Common Ground</h1>
            </div>

            <div className="flex items-center space-x-4">
              {!isAuthenticated && (
                <button
                  onClick={() => navigate('/help')}
                  className="flex items-center space-x-2 px-5 py-2.5 bg-white text-red-600 hover:bg-red-50 rounded-lg transition-colors border border-red-600"
                >
                  <HelpCircle className="w-5 h-5" />
                  <span>Help</span>
                </button>
              )}
              {isAuthenticated ? (
                <>
                  {user?.isAdmin && (
                    <button
                      onClick={() => navigate('/admin')}
                      className="flex items-center space-x-2 px-5 py-2.5 bg-yellow-500 text-black hover:bg-yellow-400 rounded-lg transition-colors border border-yellow-600 font-semibold"
                    >
                      <Shield className="w-5 h-5" />
                      <span>Admin</span>
                    </button>
                  )}
                  <button
                    onClick={() => navigate(`/profile/${user!.email}`)}
                    className="flex items-center space-x-2 px-5 py-2.5 bg-white text-red-600 hover:bg-red-50 rounded-lg transition-colors border border-white"
                  >
                    <User className="w-5 h-5" />
                    <span>{user!.name}</span>
                  </button>
                  <button
                    onClick={() => {
                      signOut();
                      navigate('/');
                    }}
                    className="flex items-center space-x-2 px-5 py-2.5 bg-black text-white hover:bg-gray-900 rounded-lg transition-colors bg-[#2a2a2a]"
                  >
                    <LogOut className="w-5 h-5" />
                    <span>Log Out</span>
                  </button>
                </>
              ) : (
                <button
                  onClick={() => navigate('/auth')}
                  className="flex items-center space-x-2 px-5 py-2.5 bg-black text-white hover:bg-gray-900 rounded-lg transition-colors"
                >
                  <User className="w-5 h-5" />
                  <span>Sign In</span>
                </button>
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-[1600px] mx-auto px-6 py-8 relative z-1">
        <div className="flex gap-6">
          {/* Sidebar Filters */}
          <aside className="w-64 flex-shrink-0">
            <div className="bg-amber-50 border border-amber-100 rounded-lg p-6 sticky top-24 shadow-sm">
              <h2 className="text-lg font-bold text-gray-900 mb-6">Filters</h2>

              <FilterSection title="Category">
                {categoryOptions.map((category) => (
                  <CheckboxFilter
                    key={category}
                    label={category}
                    count={allListings.filter(l => l.category === category).length}
                    checked={filters.categories.includes(category)}
                    onChange={(checked) => handleFilterChange('categories', category, checked)}
                  />
                ))}
              </FilterSection>

              <FilterSection title="Price Range">
                <CheckboxFilter 
                  label="Under $50" 
                  count={allListings.filter(l => l.price < 50).length}
                  checked={filters.priceRanges.includes('Under $50')}
                  onChange={(checked) => handleFilterChange('priceRanges', 'Under $50', checked)}
                />
                <CheckboxFilter 
                  label="$50 - $200" 
                  count={allListings.filter(l => l.price >= 50 && l.price <= 200).length}
                  checked={filters.priceRanges.includes('$50 - $200')}
                  onChange={(checked) => handleFilterChange('priceRanges', '$50 - $200', checked)}
                />
                <CheckboxFilter 
                  label="$200 - $500" 
                  count={allListings.filter(l => l.price > 200 && l.price <= 500).length}
                  checked={filters.priceRanges.includes('$200 - $500')}
                  onChange={(checked) => handleFilterChange('priceRanges', '$200 - $500', checked)}
                />
                <CheckboxFilter 
                  label="$500 - $1000" 
                  count={allListings.filter(l => l.price > 500 && l.price <= 1000).length}
                  checked={filters.priceRanges.includes('$500 - $1000')}
                  onChange={(checked) => handleFilterChange('priceRanges', '$500 - $1000', checked)}
                />
                <CheckboxFilter 
                  label="Over $1000" 
                  count={allListings.filter(l => l.price > 1000).length}
                  checked={filters.priceRanges.includes('Over $1000')}
                  onChange={(checked) => handleFilterChange('priceRanges', 'Over $1000', checked)}
                />
              </FilterSection>

              <FilterSection title="Condition">
                {conditionOptions.map((condition) => (
                  <CheckboxFilter
                    key={condition}
                    label={condition}
                    count={allListings.filter(l => l.condition === condition).length}
                    checked={filters.conditions.includes(condition)}
                    onChange={(checked) => handleFilterChange('conditions', condition, checked)}
                  />
                ))}
              </FilterSection>

              {(filters.categories.length > 0 || filters.priceRanges.length > 0 ||
                filters.conditions.length > 0) && (
                <button
                  onClick={() => setFilters({ categories: [], priceRanges: [], conditions: [] })}
                  className="w-full mt-6 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
                >
                  Clear Filters
                </button>
              )}
            </div>
          </aside>

          {/* Listings Grid */}
          <main className="flex-1">
            <div className="mb-6 flex items-center justify-between">
              <p className="text-gray-700">
                Showing <span className="font-semibold text-red-700">{filteredAndSortedListings.length}</span> results
              </p>
              <select 
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="px-4 py-2 bg-white border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600"
              >
                <option value="recommended">Sort by: Recommended</option>
                <option value="price-low">Price: Low to High</option>
                <option value="price-high">Price: High to Low</option>
                <option value="newest">Newest First</option>
              </select>
            </div>

            {filteredAndSortedListings.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {filteredAndSortedListings.map((listing) => (
                  <ListingCard
                    key={listing.id}
                    image={listing.image}
                    title={listing.title}
                    price={listing.price}
                    postedDate={listing.postedDate}
                    condition={listing.condition}
                    onClick={() => navigate(`/listing/${listing.id}`)}
                  />
                ))}
              </div>
            ) : loadingListings ? (
              <div className="text-center py-12">
                <p className="text-gray-500 text-lg">Loading listings...</p>
              </div>
            ) : (
              <div className="text-center py-12">
                <p className="text-gray-500 text-lg">No listings match your filters</p>
                <button
                  onClick={() => setFilters({ categories: [], priceRanges: [], conditions: [] })}
                  className="mt-4 px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                >
                  Clear All Filters
                </button>
              </div>
            )}
          </main>
        </div>
      </div>

      {/* Floating Buttons (only for authenticated users) */}
      {isAuthenticated && (
        <>
          {/* Post Listing - Hidden for admins and banned users */}
          {!user?.isAdmin && !user?.bannedFromListing && (
            <button
              onClick={() => navigate('/create-listing')}
              className="fixed bottom-8 right-8 bg-red-600 hover:bg-red-700 text-white rounded-full p-4 shadow-lg transition-all hover:shadow-xl flex items-center space-x-2 z-50"
            >
              <Plus className="w-6 h-6" />
              <span className="font-semibold pr-2">Post Listing</span>
            </button>
          )}

          {/* Pending Transactions Button */}
          {pendingTransactionsCount > 0 && !user?.isAdmin && (
            <button
              onClick={() => navigate('/pending-transactions')}
              className="fixed bottom-24 right-8 bg-blue-600 hover:bg-blue-700 text-white rounded-full p-4 shadow-lg transition-all hover:shadow-xl flex items-center space-x-2 z-50"
            >
              <Clock className="w-6 h-6" />
              <span className="font-semibold pr-2">Pending ({pendingTransactionsCount})</span>
            </button>
          )}

          {/* Chat Sidebar */}
          <ChatSidebar />
        </>
      )}
    </div>
  );
}
