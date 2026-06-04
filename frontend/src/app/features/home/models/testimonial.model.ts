/**
 * Testimonial Model
 * Defines the structure for restaurant owner/chef testimonials in the carousel
 */

export interface Testimonial {
  id: string;
  nameKey: string;               // i18n key for author name
  restaurantKey: string;         // i18n key for restaurant name
  roleKey: string;               // i18n key for role/title
  quoteKey: string;              // i18n key for the testimonial quote
  imageUrl: string;              // URL to chef/owner photo
  rating: number;                // Star rating (1-5)
  avatarColor: string;           // Color for avatar background
  initials: string;              // Author initials (e.g., "CM")
  restaurantLogoSvg?: string;    // SVG content or path for restaurant logo
}

export const TESTIMONIALS: Testimonial[] = [
  {
    id: 'testimonial-1',
    nameKey: 'landing.recommendations.chef1.name',
    restaurantKey: 'landing.recommendations.chef1.restaurant',
    roleKey: 'landing.recommendations.chef1.role',
    quoteKey: 'landing.recommendations.chef1.quote',
    imageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=400&fit=crop',
    rating: 5,
    avatarColor: '#E8944A',
    initials: 'MR',
    restaurantLogoSvg: 'golden-fork-logo'
  },
  {
    id: 'testimonial-2',
    nameKey: 'landing.recommendations.owner1.name',
    restaurantKey: 'landing.recommendations.owner1.restaurant',
    roleKey: 'landing.recommendations.owner1.role',
    quoteKey: 'landing.recommendations.owner1.quote',
    imageUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=400&fit=crop',
    rating: 5,
    avatarColor: '#8BC34A',
    initials: 'LC',
    restaurantLogoSvg: 'urban-eats-logo'
  },
  {
    id: 'testimonial-3',
    nameKey: 'landing.recommendations.chef2.name',
    restaurantKey: 'landing.recommendations.chef2.restaurant',
    roleKey: 'landing.recommendations.chef2.role',
    quoteKey: 'landing.recommendations.chef2.quote',
    imageUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&h=400&fit=crop',
    rating: 5,
    avatarColor: '#D4A574',
    initials: 'DT',
    restaurantLogoSvg: 'coastal-dining-logo'
  },
  {
    id: 'testimonial-4',
    nameKey: 'landing.recommendations.owner2.name',
    restaurantKey: 'landing.recommendations.owner2.restaurant',
    roleKey: 'landing.recommendations.owner2.role',
    quoteKey: 'landing.recommendations.owner2.quote',
    imageUrl: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400&h=400&fit=crop',
    rating: 5,
    avatarColor: '#8E7CC3',
    initials: 'PP',
    restaurantLogoSvg: 'spice-route-logo'
  },
  {
    id: 'testimonial-5',
    nameKey: 'landing.recommendations.chef3.name',
    restaurantKey: 'landing.recommendations.chef3.restaurant',
    roleKey: 'landing.recommendations.chef3.role',
    quoteKey: 'landing.recommendations.chef3.quote',
    imageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=400&fit=crop',
    rating: 5,
    avatarColor: '#C1692E',
    initials: 'TA',
    restaurantLogoSvg: 'farm-table-logo'
  }
];
