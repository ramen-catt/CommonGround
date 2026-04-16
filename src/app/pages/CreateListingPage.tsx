import { useState } from 'react';
import { useNavigate } from 'react-router';
import { useAuth } from '../context/AuthContext';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { Textarea } from '../components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';
import { Listing } from '../types';

export function CreateListingPage() {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const [title, setTitle] = useState('');
  const [price, setPrice] = useState('');
  const [category, setCategory] = useState('');
  const [condition, setCondition] = useState('');
  const [location, setLocation] = useState('');
  const [description, setDescription] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Redirect if not authenticated
  if (!isAuthenticated) {
    navigate('/auth');
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title || !price || !category || !condition || !location || !description) {
      toast.error('Please fill in all required fields');
      return;
    }

    const priceNum = parseFloat(price);
    if (isNaN(priceNum) || priceNum <= 0) {
      toast.error('Please enter a valid price');
      return;
    }

    setIsSubmitting(true);

    // Create new listing
    const newListing: Listing = {
      id: Date.now(),
      title,
      price: priceNum,
      category,
      condition: condition as 'New' | 'Like New' | 'Good' | 'Fair',
      location,
      description,
      image: imageUrl || 'https://images.unsplash.com/photo-1612015900986-4c4d017d1648?w=400',
      images: [imageUrl || 'https://images.unsplash.com/photo-1612015900986-4c4d017d1648?w=800'],
      listerName: user!.name,
      listerEmail: user!.email,
      listerJoinDate: user!.joinDate,
      postedDate: new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' }),
    };

    // Get existing listings from localStorage
    const existingListingsJson = localStorage.getItem('userListings');
    const existingListings = existingListingsJson ? JSON.parse(existingListingsJson) : [];

    // Add new listing
    existingListings.push(newListing);
    localStorage.setItem('userListings', JSON.stringify(existingListings));

    setIsSubmitting(false);
    toast.success('Listing created successfully!');
    navigate('/');
  };

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

            <button 
              onClick={() => navigate('/')}
              className="flex items-center space-x-2 px-5 py-2.5 bg-black text-white hover:bg-gray-900 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
              <span>Back to Listings</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-2xl mx-auto px-6 py-12 relative z-1">
        <div className="bg-white rounded-lg shadow-lg p-8 border border-gray-200">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Create New Listing</h2>
          <p className="text-gray-600 mb-8">Fill in the details to post your item</p>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="title">Title *</Label>
              <Input
                id="title"
                type="text"
                placeholder="e.g., Modern Computer Monitor & Desk Setup"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="price">Price ($) *</Label>
                <Input
                  id="price"
                  type="number"
                  placeholder="250"
                  min="0"
                  step="0.01"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="condition">Condition *</Label>
                <Select value={condition} onValueChange={setCondition} required>
                  <SelectTrigger id="condition">
                    <SelectValue placeholder="Select condition" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="New">New</SelectItem>
                    <SelectItem value="Like New">Like New</SelectItem>
                    <SelectItem value="Good">Good</SelectItem>
                    <SelectItem value="Fair">Fair</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="category">Category *</Label>
                <Select value={category} onValueChange={setCategory} required>
                  <SelectTrigger id="category">
                    <SelectValue placeholder="Select category" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Furniture">Furniture</SelectItem>
                    <SelectItem value="Electronics">Electronics</SelectItem>
                    <SelectItem value="Home Decor">Home Decor</SelectItem>
                    <SelectItem value="Office Equipment">Office Equipment</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="location">Location *</Label>
                <Select value={location} onValueChange={setLocation} required>
                  <SelectTrigger id="location">
                    <SelectValue placeholder="Select location" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Downtown Houston, TX">Downtown Houston, TX</SelectItem>
                    <SelectItem value="The Woodlands, TX">The Woodlands, TX</SelectItem>
                    <SelectItem value="Sugar Land, TX">Sugar Land, TX</SelectItem>
                    <SelectItem value="Katy, TX">Katy, TX</SelectItem>
                    <SelectItem value="Pearland, TX">Pearland, TX</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="imageUrl">Image URL (optional)</Label>
              <Input
                id="imageUrl"
                type="url"
                placeholder="https://example.com/image.jpg"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
              />
              <p className="text-sm text-gray-500">Leave blank to use a default image</p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description *</Label>
              <Textarea
                id="description"
                placeholder="Describe your item in detail..."
                rows={6}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                required
              />
            </div>

            <div className="flex gap-4 pt-4">
              <Button 
                type="submit" 
                className="flex-1 bg-red-600 hover:bg-red-700 text-white"
                disabled={isSubmitting}
              >
                {isSubmitting ? 'Creating Listing...' : 'Create Listing'}
              </Button>
              <Button 
                type="button" 
                variant="outline"
                onClick={() => navigate('/')}
                className="flex-1"
              >
                Cancel
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}