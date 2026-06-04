/**
 * Feature Model
 * Defines the structure for landing page feature cards
 */

export interface Feature {
  id: string;
  titleKey: string;        // i18n translation key
  descriptionKey: string;  // i18n translation key
  benefitsKeys: string[];  // Array of i18n keys for benefits
  color: string;           // Hex color for the feature
  iconName?: string;       // Icon identifier (for icon library)
  displayOrder: number;
}

/**
 * Predefined features for the landing page
 */
export const FEATURES: Feature[] = [
  {
    id: 'stock-summary',
    titleKey: 'landing.features.stockSummary.title',
    descriptionKey: 'landing.features.stockSummary.description',
    benefitsKeys: [
      'landing.features.stockSummary.benefit1',
      'landing.features.stockSummary.benefit2',
      'landing.features.stockSummary.benefit3'
    ],
    color: '#E8944A',
    iconName: 'package',
    displayOrder: 1
  },
  {
    id: 'profit-metrics',
    titleKey: 'landing.features.profitMetrics.title',
    descriptionKey: 'landing.features.profitMetrics.description',
    benefitsKeys: [
      'landing.features.profitMetrics.benefit1',
      'landing.features.profitMetrics.benefit2',
      'landing.features.profitMetrics.benefit3'
    ],
    color: '#8BC34A',
    iconName: 'trending-up',
    displayOrder: 2
  },
  {
    id: 'recipe-optimization',
    titleKey: 'landing.features.recipeOptimization.title',
    descriptionKey: 'landing.features.recipeOptimization.description',
    benefitsKeys: [
      'landing.features.recipeOptimization.benefit1',
      'landing.features.recipeOptimization.benefit2',
      'landing.features.recipeOptimization.benefit3'
    ],
    color: '#D4A574',
    iconName: 'chef-hat',
    displayOrder: 3
  },
  {
    id: 'trend-notifications',
    titleKey: 'landing.features.trendNotifications.title',
    descriptionKey: 'landing.features.trendNotifications.description',
    benefitsKeys: [
      'landing.features.trendNotifications.benefit1',
      'landing.features.trendNotifications.benefit2',
      'landing.features.trendNotifications.benefit3'
    ],
    color: '#AD8E4A',
    iconName: 'sparkles',
    displayOrder: 4
  },
  {
    id: 'margin-boost',
    titleKey: 'landing.features.marginBoost.title',
    descriptionKey: 'landing.features.marginBoost.description',
    benefitsKeys: [
      'landing.features.marginBoost.benefit1',
      'landing.features.marginBoost.benefit2',
      'landing.features.marginBoost.benefit3'
    ],
    color: '#C1692E',
    iconName: 'zap',
    displayOrder: 5
  },
  {
    id: 'bestseller-insights',
    titleKey: 'landing.features.bestsellerInsights.title',
    descriptionKey: 'landing.features.bestsellerInsights.description',
    benefitsKeys: [
      'landing.features.bestsellerInsights.benefit1',
      'landing.features.bestsellerInsights.benefit2',
      'landing.features.bestsellerInsights.benefit3'
    ],
    color: '#F4D78E',
    iconName: 'star',
    displayOrder: 6
  },
  {
    id: 'ai-chat-assistant',
    titleKey: 'landing.features.aiChat.title',
    descriptionKey: 'landing.features.aiChat.description',
    benefitsKeys: [
      'landing.features.aiChat.benefit1',
      'landing.features.aiChat.benefit2',
      'landing.features.aiChat.benefit3'
    ],
    color: '#8E7CC3',
    iconName: 'message-circle',
    displayOrder: 7
  },
  {
    id: 'advanced-analytics',
    titleKey: 'landing.features.advancedAnalytics.title',
    descriptionKey: 'landing.features.advancedAnalytics.description',
    benefitsKeys: [
      'landing.features.advancedAnalytics.benefit1',
      'landing.features.advancedAnalytics.benefit2',
      'landing.features.advancedAnalytics.benefit3'
    ],
    color: '#D64045',
    iconName: 'bar-chart-3',
    displayOrder: 8
  }
];
