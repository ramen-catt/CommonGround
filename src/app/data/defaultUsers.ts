// Default users for the listings
export const defaultUsers = [
  {
    id: '1',
    email: 'sarah.johnson@email.com',
    name: 'Sarah Johnson',
    joinDate: 'January 2024',
    rating: 4.8,
    totalReviews: 12,
    bio: 'Selling quality home office equipment. Always responsive and honest about item condition.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '2',
    email: 'michael.chen@email.com',
    name: 'Michael Chen',
    joinDate: 'March 2023',
    rating: 4.9,
    totalReviews: 28,
    bio: 'Furniture enthusiast. All items come from smoke-free, pet-free home.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '3',
    email: 'jessica.martinez@email.com',
    name: 'Jessica Martinez',
    joinDate: 'June 2024',
    rating: 5.0,
    totalReviews: 8,
    bio: 'Gaming setup seller. Everything is well-maintained and lightly used.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '4',
    email: 'david.wilson@email.com',
    name: 'David Wilson',
    joinDate: 'August 2024',
    rating: 4.7,
    totalReviews: 15,
    bio: 'Home decor and plants. Fast shipping and great communication.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '5',
    email: 'emily.rodriguez@email.com',
    name: 'Emily Rodriguez',
    joinDate: 'February 2024',
    rating: 4.6,
    totalReviews: 19,
    bio: 'Tech seller. All electronics are tested and working perfectly.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '6',
    email: 'amanda.lee@email.com',
    name: 'Amanda Lee',
    joinDate: 'April 2024',
    rating: 5.0,
    totalReviews: 22,
    bio: 'Minimalist furniture seller. Moving sale - everything must go!',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '7',
    email: 'robert.thompson@email.com',
    name: 'Robert Thompson',
    joinDate: 'November 2023',
    rating: 4.8,
    totalReviews: 31,
    bio: 'Vintage furniture collector. Authentic pieces with character.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '8',
    email: 'jennifer.kim@email.com',
    name: 'Jennifer Kim',
    joinDate: 'July 2024',
    rating: 4.9,
    totalReviews: 11,
    bio: 'Work from home setup seller. Quality items at great prices.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '9',
    email: 'thomas.anderson@email.com',
    name: 'Thomas Anderson',
    joinDate: 'September 2023',
    rating: 4.5,
    totalReviews: 24,
    bio: 'Retro electronics enthusiast. Great for collectors!',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '10',
    email: 'lisa.brown@email.com',
    name: 'Lisa Brown',
    joinDate: 'December 2024',
    rating: 5.0,
    totalReviews: 5,
    bio: 'New to Common Ground. Selling brand new items still in packaging.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '11',
    email: 'chris.davis@email.com',
    name: 'Chris Davis',
    joinDate: 'January 2024',
    rating: 4.7,
    totalReviews: 16,
    bio: 'Office equipment specialist. Ergonomic and functional items.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  },
  {
    id: '12',
    email: 'kevin.white@email.com',
    name: 'Kevin White',
    joinDate: 'May 2024',
    rating: 4.9,
    totalReviews: 13,
    bio: 'Premium office furniture. Gently used and well-maintained.',
    location: 'Houston, TX',
    phoneNumber: '',
    password: 'demo123'
  }
];

// Function to initialize default users if they don't exist
export function initializeDefaultUsers() {
  const existingUsers = localStorage.getItem('users');
  
  if (!existingUsers) {
    localStorage.setItem('users', JSON.stringify(defaultUsers));
  } else {
    // Merge with existing users
    const users = JSON.parse(existingUsers);
    const userEmails = users.map((u: any) => u.email);
    
    const newUsers = defaultUsers.filter(du => !userEmails.includes(du.email));
    
    if (newUsers.length > 0) {
      localStorage.setItem('users', JSON.stringify([...users, ...newUsers]));
    }
  }
}
