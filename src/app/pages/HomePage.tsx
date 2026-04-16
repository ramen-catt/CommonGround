import { useState, useMemo } from 'react';
import { User, Plus, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router';
import { ListingCard } from '../components/ListingCard';
import { FilterSection, CheckboxFilter } from '../components/FilterSection';
import { listings } from '../data/listings';
import { FilterState, Listing } from '../types';
import { useAuth } from '../context/AuthContext';
import { ChatSidebar } from '../components/ChatSidebar';

export function HomePage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, signOut } = useAuth();
  const [filters, setFilters] = useState<FilterState>({
    categories: [],
    priceRanges: [],
    conditions: [],
    locations: [],
  });

  const [sortBy, setSortBy] = useState('recommended');

  // Combine default listings with user-created listings
  const allListings = useMemo(() => {
    const userListingsJson = localStorage.getItem('userListings');
    const userListings: Listing[] = userListingsJson ? JSON.parse(userListingsJson) : [];
    return [...listings, ...userListings];
  }, []);

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

    // Apply location filters
    if (filters.locations.length > 0) {
      filtered = filtered.filter((listing) => 
        filters.locations.some(loc => listing.location.includes(loc))
      );
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
        <div className="max-w-[1600px] mx-auto px-6 py-4 bg-[#00000000]">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/')}>
              <div className="relative">
                <div className="w-10 h-10 bg-black rounded-full flex items-center justify-center bg-[#000000]">
                  <div className="w-2 h-2 bg-white rounded-full absolute top-2 left-2"></div>
                  <div className="w-2 h-2 bg-white rounded-full absolute top-2 right-2"></div>
                  <div className="w-2 h-2 bg-white rounded-full absolute bottom-2 left-3 p-[0px] mx-[5px] my-[0px]"></div>
                </div>
              </div>
              <h1 className="text-2xl font-bold text-white">Common Ground</h1>
            </div>

            {isAuthenticated ? (
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
      </header>

      {/* Main Content */}
      <div className="max-w-[1600px] mx-auto px-6 py-8 relative z-1">
        <div className="flex gap-6">
          {/* Sidebar Filters */}
          <aside className="w-64 flex-shrink-0">
            <div className="bg-amber-50 border border-amber-100 rounded-lg p-6 sticky top-24 shadow-sm">
              <h2 className="text-lg font-bold text-gray-900 mb-6">Filters</h2>

              <FilterSection title="Category">
                <CheckboxFilter 
                  label="Furniture" 
                  count={listings.filter(l => l.category === 'Furniture').length}
                  checked={filters.categories.includes('Furniture')}
                  onChange={(checked) => handleFilterChange('categories', 'Furniture', checked)}
                />
                <CheckboxFilter 
                  label="Electronics" 
                  count={listings.filter(l => l.category === 'Electronics').length}
                  checked={filters.categories.includes('Electronics')}
                  onChange={(checked) => handleFilterChange('categories', 'Electronics', checked)}
                />
                <CheckboxFilter 
                  label="Home Decor" 
                  count={listings.filter(l => l.category === 'Home Decor').length}
                  checked={filters.categories.includes('Home Decor')}
                  onChange={(checked) => handleFilterChange('categories', 'Home Decor', checked)}
                />
                <CheckboxFilter 
                  label="Office Equipment" 
                  count={listings.filter(l => l.category === 'Office Equipment').length}
                  checked={filters.categories.includes('Office Equipment')}
                  onChange={(checked) => handleFilterChange('categories', 'Office Equipment', checked)}
                />
              </FilterSection>

              <FilterSection title="Price Range">
                <CheckboxFilter 
                  label="Under $50" 
                  count={listings.filter(l => l.price < 50).length}
                  checked={filters.priceRanges.includes('Under $50')}
                  onChange={(checked) => handleFilterChange('priceRanges', 'Under $50', checked)}
                />
                <CheckboxFilter 
                  label="$50 - $200" 
                  count={listings.filter(l => l.price >= 50 && l.price <= 200).length}
                  checked={filters.priceRanges.includes('$50 - $200')}
                  onChange={(checked) => handleFilterChange('priceRanges', '$50 - $200', checked)}
                />
                <CheckboxFilter 
                  label="$200 - $500" 
                  count={listings.filter(l => l.price > 200 && l.price <= 500).length}
                  checked={filters.priceRanges.includes('$200 - $500')}
                  onChange={(checked) => handleFilterChange('priceRanges', '$200 - $500', checked)}
                />
                <CheckboxFilter 
                  label="$500 - $1000" 
                  count={listings.filter(l => l.price > 500 && l.price <= 1000).length}
                  checked={filters.priceRanges.includes('$500 - $1000')}
                  onChange={(checked) => handleFilterChange('priceRanges', '$500 - $1000', checked)}
                />
                <CheckboxFilter 
                  label="Over $1000" 
                  count={listings.filter(l => l.price > 1000).length}
                  checked={filters.priceRanges.includes('Over $1000')}
                  onChange={(checked) => handleFilterChange('priceRanges', 'Over $1000', checked)}
                />
              </FilterSection>

              <FilterSection title="Condition">
                <CheckboxFilter 
                  label="New" 
                  count={listings.filter(l => l.condition === 'New').length}
                  checked={filters.conditions.includes('New')}
                  onChange={(checked) => handleFilterChange('conditions', 'New', checked)}
                />
                <CheckboxFilter 
                  label="Like New" 
                  count={listings.filter(l => l.condition === 'Like New').length}
                  checked={filters.conditions.includes('Like New')}
                  onChange={(checked) => handleFilterChange('conditions', 'Like New', checked)}
                />
                <CheckboxFilter 
                  label="Good" 
                  count={listings.filter(l => l.condition === 'Good').length}
                  checked={filters.conditions.includes('Good')}
                  onChange={(checked) => handleFilterChange('conditions', 'Good', checked)}
                />
                <CheckboxFilter 
                  label="Fair" 
                  count={listings.filter(l => l.condition === 'Fair').length}
                  checked={filters.conditions.includes('Fair')}
                  onChange={(checked) => handleFilterChange('conditions', 'Fair', checked)}
                />
              </FilterSection>

              <FilterSection title="Location">
                <CheckboxFilter 
                  label="Downtown Houston" 
                  count={listings.filter(l => l.location.includes('Downtown Houston')).length}
                  checked={filters.locations.includes('Downtown Houston')}
                  onChange={(checked) => handleFilterChange('locations', 'Downtown Houston', checked)}
                />
                <CheckboxFilter 
                  label="The Woodlands" 
                  count={listings.filter(l => l.location.includes('The Woodlands')).length}
                  checked={filters.locations.includes('The Woodlands')}
                  onChange={(checked) => handleFilterChange('locations', 'The Woodlands', checked)}
                />
                <CheckboxFilter 
                  label="Sugar Land" 
                  count={listings.filter(l => l.location.includes('Sugar Land')).length}
                  checked={filters.locations.includes('Sugar Land')}
                  onChange={(checked) => handleFilterChange('locations', 'Sugar Land', checked)}
                />
                <CheckboxFilter 
                  label="Katy" 
                  count={listings.filter(l => l.location.includes('Katy')).length}
                  checked={filters.locations.includes('Katy')}
                  onChange={(checked) => handleFilterChange('locations', 'Katy', checked)}
                />
                <CheckboxFilter 
                  label="Pearland" 
                  count={listings.filter(l => l.location.includes('Pearland')).length}
                  checked={filters.locations.includes('Pearland')}
                  onChange={(checked) => handleFilterChange('locations', 'Pearland', checked)}
                />
              </FilterSection>

              {(filters.categories.length > 0 || filters.priceRanges.length > 0 || 
                filters.conditions.length > 0 || filters.locations.length > 0) && (
                <button 
                  onClick={() => setFilters({ categories: [], priceRanges: [], conditions: [], locations: [] })}
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
                    {...listing}
                    onClick={() => navigate(`/listing/${listing.id}`)}
                  />
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <p className="text-gray-500 text-lg">No listings match your filters</p>
                <button
                  onClick={() => setFilters({ categories: [], priceRanges: [], conditions: [], locations: [] })}
                  className="mt-4 px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                >
                  Clear All Filters
                </button>
              </div>
            )}
          </main>
        </div>
      </div>

      {/* Floating Post Listing Button (only for authenticated users) */}
      {isAuthenticated && (
        <>
          <button
            onClick={() => navigate('/create-listing')}
            className="fixed bottom-8 right-8 bg-red-600 hover:bg-red-700 text-white rounded-full p-4 shadow-lg transition-all hover:shadow-xl flex items-center space-x-2 z-50"
          >
            <Plus className="w-6 h-6" />
            <span className="font-semibold pr-2">Post Listing</span>
          </button>

          {/* Chat Sidebar */}
          <ChatSidebar />
        </>
      )}
    </div>
  );
}