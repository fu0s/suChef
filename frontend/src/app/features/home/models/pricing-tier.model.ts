/**
 * Pricing Tier Model
 * Defines the structure for the three pricing plans on the landing page
 */

export interface PricingTier {
  id: string;
  nameKey: string;               // i18n key for tier name
  descriptionKey: string;        // i18n key for description
  monthlyPrice: number;          // Monthly price in USD
  features: string[];            // Array of i18n keys for features
  ctaKey: string;                // i18n key for CTA button text
  highlighted: boolean;          // Whether this is the featured/pro tier
  iconName?: string;             // Icon identifier
}

export const PRICING_TIERS: PricingTier[] = [
  {
    id: 'free',
    nameKey: 'landing.pricing.free.name',
    descriptionKey: 'landing.pricing.free.description',
    monthlyPrice: 0,
    features: [
      'landing.pricing.features.inventoryBasic',
      'landing.pricing.features.dashboard',
      'landing.pricing.features.locations'
    ],
    ctaKey: 'landing.pricing.free.cta',
    highlighted: false,
    iconName: 'star'
  },
  {
    id: 'pro',
    nameKey: 'landing.pricing.pro.name',
    descriptionKey: 'landing.pricing.pro.description',
    monthlyPrice: 99,
    features: [
      'landing.pricing.features.inventoryBasic',
      'landing.pricing.features.dashboard',
      'landing.pricing.features.reports',
      'landing.pricing.features.aiChat',
      'landing.pricing.features.trendAnalysis',
      'landing.pricing.features.locations',
      'landing.pricing.features.support24'
    ],
    ctaKey: 'landing.pricing.pro.cta',
    highlighted: true,
    iconName: 'gold-spoon'
  },
  {
    id: 'enterprise',
    nameKey: 'landing.pricing.enterprise.name',
    descriptionKey: 'landing.pricing.enterprise.description',
    monthlyPrice: 299,
    features: [
      'landing.pricing.features.inventoryBasic',
      'landing.pricing.features.dashboard',
      'landing.pricing.features.reports',
      'landing.pricing.features.aiChat',
      'landing.pricing.features.trendAnalysis',
      'landing.pricing.features.locations',
      'landing.pricing.features.support24',
      'landing.pricing.features.customIntegrations'
    ],
    ctaKey: 'landing.pricing.enterprise.cta',
    highlighted: false,
    iconName: 'crown'
  }
];

/**
 * Helper function to calculate annual savings
 */
export function calculateAnnualSavings(monthlyPrice: number): number {
  return monthlyPrice * 12 - (monthlyPrice * 10);
}

/**
 * Helper function to format price for display
 */
export function formatPrice(price: number, currency: string = 'USD'): string {
  if (price === 0) return 'Free';
  return `$${price.toLocaleString('en-US')}/${currency === 'USD' ? 'month' : 'year'}`;
}
