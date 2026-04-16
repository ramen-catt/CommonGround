import { createBrowserRouter } from 'react-router';
import { HomePage } from './pages/HomePage';
import { ListingDetailPage } from './pages/ListingDetailPage';
import { AuthPage } from './pages/AuthPage';
import { CreateListingPage } from './pages/CreateListingPage';
import { ProfilePage } from './pages/ProfilePage';
import { TransactionPage } from './pages/TransactionPage';

export const router = createBrowserRouter([
  {
    path: '/',
    Component: HomePage,
  },
  {
    path: '/listing/:id',
    Component: ListingDetailPage,
  },
  {
    path: '/auth',
    Component: AuthPage,
  },
  {
    path: '/create-listing',
    Component: CreateListingPage,
  },
  {
    path: '/profile/:email',
    Component: ProfilePage,
  },
  {
    path: '/transaction',
    Component: TransactionPage,
  },
]);