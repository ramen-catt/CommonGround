import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router';
import { useAuth } from '../context/AuthContext';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { Textarea } from '../components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';
import { SiteLogo } from '../components/SiteLogo';
import { getListing, editListing, getListingOptions } from '../lib/api';

export function EditListingPage() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { user, isAuthenticated, isAuthLoading } = useAuth();

  const [title, setTitle] = useState('');
  const [price, setPrice] = useState('');
  const [category, setCategory] = useState('');
  const [condition, setCondition] = useState('');
  const [description, setDescription] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [imageDataUrl, setImageDataUrl] = useState('');
  const [currentImageUrl, setCurrentImageUrl] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [listingId, setListingId] = useState<number | null>(null);
  const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
  const [conditionOptions, setConditionOptions] = useState<string[]>([]);
  const [optionsError, setOptionsError] = useState('');

  useEffect(() => {
    if (isAuthLoading) {
      return;
    }

    if (!isAuthenticated) {
      navigate('/auth');
      return;
    }

    if (user?.isAdmin) {
      toast.error('Admin accounts cannot edit listings');
      navigate('/');
      return;
    }

    if (user?.bannedFromListing) {
      toast.error('Your account is restricted from editing listings');
      navigate('/');
      return;
    }

    if (!id) return;

    Promise.all([getListing(Number(id)), getListingOptions()])
      .then(([data, options]) => {
        setCategoryOptions(options.categories);
        setConditionOptions(options.conditions);
        setOptionsError('');

        if (data.category && !options.categories.includes(data.category)) {
          setCategoryOptions([...options.categories, data.category]);
        }
        if (data.condition && !options.conditions.includes(data.condition)) {
          setConditionOptions([...options.conditions, data.condition]);
        }

        // make sure this user owns it
        if (String(data.clientId) !== user?.id) {
          toast.error('You can only edit your own listings');
          navigate('/');
          return;
        }

        setListingId(data.id);
        setTitle(data.title || '');
        setPrice(String(data.price || ''));
        setCategory(data.category || '');
        setCondition(data.condition || '');
        setDescription(data.description || '');
        setCurrentImageUrl(data.image || '');
        setImageUrl('');
      })
      .catch((err) => {
        setOptionsError(err.message || 'Could not load listing options from the backend.');
        toast.error('Could not load this listing');
      })
      .finally(() => setLoading(false));
  }, [id, isAuthenticated, isAuthLoading, user, navigate]);

  const handleImageFileChange = (file?: File) => {
    if (!file) {
      setImageDataUrl('');
      return;
    }
    if (!file.type.startsWith('image/')) {
      toast.error('Please choose an image file');
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      toast.error('Please choose an image smaller than 2 MB');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => setImageDataUrl(String(reader.result || ''));
    reader.onerror = () => toast.error('Could not read that image file');
    reader.readAsDataURL(file);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title || !price || !category || !condition || !description) {
      toast.error('Please fill in all required fields');
      return;
    }

    const priceNum = parseFloat(price);
    if (isNaN(priceNum) || priceNum <= 0) {
      toast.error('Please enter a valid price');
      return;
    }

    setIsSubmitting(true);

    try {
      const result = await editListing({
        id: listingId!,
        clientId: parseInt(user!.id),
        title,
        price: priceNum,
        category,
        condition,
        description,
        imageUrl: imageDataUrl || imageUrl,
      });

      if (result.success) {
        toast.success('Listing updated!');
        navigate(`/listing/${id}`);
      } else {
        toast.error(result.message);
      }
    } catch (err: any) {
      toast.error(err.message || 'Something went wrong');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isAuthLoading || loading) {
    return (
      <div className="min-h-screen bg-red-50 flex items-center justify-center">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-red-50 relative overflow-x-hidden">
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

            <button
              onClick={() => navigate(`/listing/${id}`)}
              className="flex items-center space-x-2 px-5 py-2.5 bg-black text-white hover:bg-gray-900 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
              <span>Cancel</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-2xl mx-auto px-6 py-12 relative z-1">
        <div className="bg-white rounded-lg shadow-lg p-8 border border-gray-200">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Edit Listing</h2>
          <p className="text-gray-600 mb-8">Update your listing details</p>

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
                  className="[&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="condition">Condition *</Label>
                <Select value={condition} onValueChange={setCondition} required>
                  <SelectTrigger id="condition">
                    <SelectValue placeholder="Select condition" />
                  </SelectTrigger>
                  <SelectContent>
                    {conditionOptions.map((option) => (
                      <SelectItem key={option} value={option}>{option}</SelectItem>
                    ))}
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
                    {categoryOptions.map((option) => (
                      <SelectItem key={option} value={option}>{option}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {optionsError && (
              <p className="text-sm text-red-600">
                Listing options did not load from the Java backend: {optionsError}
              </p>
            )}

            <div className="space-y-2">
              <Label htmlFor="imageFile">Replace Image (optional)</Label>
              <Input
                id="imageFile"
                type="file"
                accept="image/*"
                onChange={(e) => handleImageFileChange(e.target.files?.[0])}
              />
              {(imageDataUrl || imageUrl || currentImageUrl) && (
                <img
                  src={imageDataUrl || imageUrl || currentImageUrl}
                  alt="Selected listing preview"
                  className="h-32 w-32 rounded-lg object-cover border border-gray-200"
                />
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="imageUrl">Or Image URL</Label>
              <Input
                id="imageUrl"
                type="url"
                placeholder="https://example.com/image.jpg"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                disabled={!!imageDataUrl}
              />
              <p className="text-sm text-gray-500">
                Leave this blank to keep the current image.
              </p>
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
                disabled={isSubmitting || categoryOptions.length === 0 || conditionOptions.length === 0}
              >
                {isSubmitting ? 'Updating Listing...' : 'Update Listing'}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate(`/listing/${id}`)}
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
