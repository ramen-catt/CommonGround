import { useEffect } from 'react';
import { RouterProvider } from 'react-router';
import { Toaster } from './components/ui/sonner';
import { router } from './routes';
import { AuthProvider } from './context/AuthContext';
import { initializeDefaultUsers } from './data/defaultUsers';

export default function App() {
  useEffect(() => {
    // Initialize default users on app load
    initializeDefaultUsers();
  }, []);

  return (
    <AuthProvider>
      <RouterProvider router={router} />
      <Toaster />
    </AuthProvider>
  );
}