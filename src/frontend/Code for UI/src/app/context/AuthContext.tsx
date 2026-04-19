import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { login, register, logout, getMe } from '../lib/api';

export interface User {
  id: string;
  email: string;
  name: string;
  joinDate: string;
  rating?: number;
  totalReviews?: number;
  bio?: string;
  location?: string;
  phoneNumber?: string;
  address?: string;
  isAdmin?: boolean;
  bannedFromListing?: boolean;
  bannedFromPurchasing?: boolean;
}

interface AuthContextType {
  user: User | null;
  signIn: (email: string, password: string) => Promise<boolean>;
  signUp: (name: string, email: string, password: string, phoneNumber: string, address: string) => Promise<boolean>;
  signOut: () => void;
  updateUser: (updatedUser: User) => void;
  isAuthenticated: boolean;
  isAuthLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthLoading, setIsAuthLoading] = useState(true);

  useEffect(() => {
    // check if the user already has a session when the page loads
    getMe()
      .then((data) => {
        setUser({
          id:                   String(data.id),
          name:                 data.name,
          email:                data.email,
          isAdmin:              data.isAdmin,
          address:              data.address,
          bannedFromListing:    (data as any).bannedFromListing ?? false,
          bannedFromPurchasing: (data as any).bannedFromPurchasing ?? false,
          joinDate:             '',
          rating:               5.0,
          totalReviews:         0,
          location:             'Houston, TX',
        });
      })
      .catch(() => { /* not logged in, thats fine */ })
      .finally(() => setIsAuthLoading(false));
  }, []);

  const signIn = async (email: string, password: string): Promise<boolean> => {
    try {
      const data = await login(email, password);
      setUser({
        id:                   String(data.id),
        name:                 data.name,
        email:                data.email,
        isAdmin:              data.isAdmin,
        address:              data.address,
        bannedFromListing:    (data as any).bannedFromListing ?? false,
        bannedFromPurchasing: (data as any).bannedFromPurchasing ?? false,
        joinDate:             '',
        rating:               5.0,
        totalReviews:         0,
        location:             'Houston, TX',
      });
      return true;
    } catch {
      return false;
    }
  };

  const signUp = async (
    name: string,
    email: string,
    password: string,
    phoneNumber: string,
    address: string
  ): Promise<boolean> => {
    const data = await register({ name, email, password, phoneNumber, address });
    setUser({
      id:                   String(data.id),
      name:                 data.name,
      email:                data.email,
      isAdmin:              data.isAdmin,
      address:              data.address,
      bannedFromListing:    (data as any).bannedFromListing ?? false,
      bannedFromPurchasing: (data as any).bannedFromPurchasing ?? false,
      joinDate:             new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' }),
      rating:               5.0,
      totalReviews:         0,
      location:             'Houston, TX',
    });
    return true;
  };

  const signOut = async () => {
    try { await logout(); } catch { /* ignore */ }
    setUser(null);
  };

  const updateUser = (updatedUser: User) => {
    setUser(updatedUser);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        signIn,
        signUp,
        signOut,
        updateUser,
        isAuthenticated: !!user,
        isAuthLoading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
