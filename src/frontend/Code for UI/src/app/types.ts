export interface Listing {
  id: number;
  image: string;
  title: string;
  price: number;
  location: string;
  condition: string;
  category: string;
  description: string;
  listerName: string;
  listerJoinDate: string;
  postedDate: string;
  images: string[];
  listerEmail?: string; // Added to link to user profile
}

export interface FilterState {
  categories: string[];
  priceRanges: string[];
  conditions: string[];
}

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  joinDate: string;
  rating: number; // 0-5 stars
  totalReviews: number;
  bio?: string;
  location?: string;
  phoneNumber?: string;
}

export interface Report {
  id: string;
  reporterEmail: string;
  reportedUserEmail: string;
  reason: string;
  description: string;
  timestamp: string;
}
