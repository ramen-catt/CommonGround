import { Heart } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';

interface ListingCardProps {
  image: string;
  title: string;
  price: number;
  location: string;
  condition: string;
  onClick?: () => void;
}

export function ListingCard({ image, title, price, location, condition, onClick }: ListingCardProps) {
  return (
    <div 
      onClick={onClick}
      className="bg-white rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow cursor-pointer group"
    >
      <div className="relative aspect-square overflow-hidden bg-gray-100">
        <ImageWithFallback
          src={image}
          alt={title}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
        <div className="absolute top-3 left-3 bg-red-600 text-white text-xs px-2 py-1 rounded-full">
          {condition}
        </div>
      </div>
      <div className="p-4">
        <h3 className="text-lg font-semibold text-gray-900 mb-1 line-clamp-1">{title}</h3>
        <p className="text-2xl font-bold text-red-600 mb-2">${price}</p>
        <p className="text-sm text-gray-500">{location}</p>
      </div>
    </div>
  );
}