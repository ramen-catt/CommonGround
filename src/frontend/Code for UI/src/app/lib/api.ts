// all fetch calls to the Java backend go through here
// VITE_API_BASE_URL is set in Vercel env vars to your Railway backend URL
const BASE = (import.meta.env.VITE_API_BASE_URL ?? '') + '/api';

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(BASE + url, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || data.message || 'Request failed');
  return data as T;
}

// --- listings ---

export function getListings() {
  return request<any[]>('/listings');
}

export function getListing(id: number) {
  return request<any>(`/listings?id=${id}`);
}

export function getListingsByCategory(category: string) {
  return request<any[]>(`/listings?category=${encodeURIComponent(category)}`);
}

export function searchListings(keyword: string) {
  return request<any[]>(`/listings?search=${encodeURIComponent(keyword)}`);
}

export function getMyListings(clientId: number) {
  return request<any[]>(`/listings?clientId=${clientId}`);
}

export function getListingOptions() {
  return request<{
    categories: string[];
    conditions: string[];
    paymentTypes: string[];
  }>('/listings/options');
}

export function createListing(data: {
  clientId: number;
  title: string;
  price: number;
  category: string;
  condition: string;
  description: string;
  paymentType?: string;
  imageUrl?: string;
}) {
  return request<{ success: boolean; message: string; id?: number }>('/listings', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function editListing(data: {
  id: number;
  clientId: number;
  title: string;
  price: number;
  category: string;
  condition: string;
  description: string;
  paymentType?: string;
  imageUrl?: string;
}) {
  return request<{ success: boolean; message: string }>('/listings', {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export function deleteListing(id: number, clientId: number) {
  return request<{ success: boolean; message: string }>(
    `/listings?id=${id}&clientId=${clientId}`,
    { method: 'DELETE' }
  );
}

// --- auth ---

export function login(email: string, password: string) {
  return request<{ success: boolean; id: number; name: string; email: string; isAdmin: boolean; address: string }>(
    '/auth/login',
    { method: 'POST', body: JSON.stringify({ email, password }) }
  );
}

export function register(data: {
  name: string;
  email: string;
  password: string;
  phoneNumber: string;
  address: string;
}) {
  return request<{ success: boolean; id: number; name: string; email: string; isAdmin: boolean; address: string }>(
    '/auth/register',
    { method: 'POST', body: JSON.stringify(data) }
  );
}

export function logout() {
  return request<{ success: boolean }>('/auth/logout');
}

export function getMe() {
  return request<{ id: number; name: string; email: string; isAdmin: boolean; address: string }>(
    '/auth/me'
  );
}

// --- account/profile ---

export function getProfile(email: string) {
  return request<any>(`/account?email=${encodeURIComponent(email)}`);
}

export function updateProfile(data: { name: string; phoneNumber?: string; address?: string }) {
  return request<any>('/account/profile', {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

// --- messages ---

export function getConversations() {
  return request<any[]>('/messages');
}

export function getConversation(otherId: number, listingId?: number) {
  const query = new URLSearchParams({ otherId: String(otherId) });
  if (listingId) query.set('listingId', String(listingId));
  return request<any[]>(`/messages/conversation?${query.toString()}`);
}

export function sendMessage(data: { receiverId?: number; receiverEmail?: string; listingId?: number; text: string }) {
  return request<{ success: boolean; message: string }>('/messages', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// --- transactions ---

export function getTransactions() {
  return request<any[]>('/transactions');
}

export function getTransaction(id: string | number) {
  return request<any>(`/transactions?id=${id}`);
}

export function createTransaction(data: { listingId: number; sellerId: number; meetupAddress: string }) {
  return request<{ success: boolean; id: number; transaction: any }>('/transactions', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function updateTransaction(id: string | number, action: 'confirm' | 'cancel') {
  return request<{ success: boolean; transaction: any }>('/transactions', {
    method: 'PUT',
    body: JSON.stringify({ id, action }),
  });
}

// --- locations ---

export function getLocations() {
  return request<any[]>('/map/locations');
}

export function getClosestMeetupLocation(data: {
  buyerLat: number;
  buyerLng: number;
  sellerLat: number;
  sellerLng: number;
}) {
  const query = new URLSearchParams({
    buyerLat: String(data.buyerLat),
    buyerLng: String(data.buyerLng),
    sellerLat: String(data.sellerLat),
    sellerLng: String(data.sellerLng),
  });
  return request<any>(`/map/closest?${query.toString()}`);
}

export function getSuggestedMeetupLocation(data: { buyerId: number; sellerId: number }) {
  const query = new URLSearchParams({
    buyerId: String(data.buyerId),
    sellerId: String(data.sellerId),
  });
  return request<any>(`/map/suggest?${query.toString()}`);
}

export function updateTransactionMeetupLocation(data: { transactionId: number; meetupAddress: string }) {
  return request<{ success: boolean; message: string }>('/map/transaction-location', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// --- feedback/reviews ---

export function getFeedback(params: { sellerId?: number; listingId?: number }) {
  const query = new URLSearchParams();
  if (params.sellerId) query.set('sellerId', String(params.sellerId));
  if (params.listingId) query.set('listingId', String(params.listingId));
  return request<any[]>(`/feedback?${query.toString()}`);
}

export function createFeedback(data: {
  sellerId: number;
  listingId: number;
  rating: number;
  ratingDesc: string;
  reportDesc?: string;
  isReport?: boolean;
}) {
  return request<{ success: boolean; message: string }>('/feedback', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// --- admin ---

export function adminGetUsers() {
  return request<any[]>(`/admin/users`);
}

export function adminGetReports() {
  return request<any[]>('/admin/reports');
}

export function adminGetTransactions() {
  return request<any[]>('/admin/transactions');
}

export function adminSuspendUser(targetId: number) {
  return request<{ success: boolean; message: string }>('/admin/suspend', {
    method: 'POST',
    body: JSON.stringify({ targetId }),
  });
}

export function adminUnsuspendUser(targetId: number) {
  return request<{ success: boolean; message: string }>('/admin/unsuspend', {
    method: 'POST',
    body: JSON.stringify({ targetId }),
  });
}

export function adminRemoveListing(listingId: number) {
  return request<{ success: boolean; message: string }>('/admin/remove-listing', {
    method: 'POST',
    body: JSON.stringify({ listingId }),
  });
}

export function adminRemoveReview(reviewId: number) {
  return request<{ success: boolean; message: string }>('/admin/remove-review', {
    method: 'POST',
    body: JSON.stringify({ reviewId }),
  });
}
