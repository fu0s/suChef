/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}"
  ],
  theme: {
    extend: {
      colors: {
        // Landing Page - Warm Gastronomy Palette
        gastro: {
          cream: '#FDF9F3',
          'gold-light': '#F5D76E',
          'gold': '#D4AF37',
          'gold-dark': '#AA8C2C',
          'brown': '#8B4513',
          'sepia': '#704214',
          'clay': '#C17F4B',
          'sage': '#6B8E71',
          'emerald': '#2D5016',
          'soft-red': '#D64045',
          'burnt-orange': '#D2691E',
          'warm-gray': '#F5F1ED',
          'taupe': '#B8860B',
          'charcoal': '#2A2420',
        },
        // Feature-specific colors
        feature: {
          stock: '#E8944A',
          profit: '#8BC34A',
          recipe: '#D4A574',
          trend: '#AD8E4A',
          margin: '#C1692E',
          bestseller: '#F4D78E',
          ai: '#8E7CC3',
        },
        // Existing app colors (maintain compatibility)
        primary: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#1652b0',
          600: '#0b2545',
          700: '#0369a1',
          800: '#075985',
          900: '#0c4a6e',
          950: '#082f49',
        },
        secondary: {
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#6b7280',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
          950: '#020617',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto'],
        heading: ['Poppins', 'Inter', 'ui-sans-serif', 'system-ui'],
        accent: ['Outfit', 'Poppins', 'ui-sans-serif', 'system-ui']
      },
      borderRadius: {
        'sm': '0.25rem',
        'md': '0.375rem',
        'lg': '0.5rem',
        'xl': '0.75rem',
        '2xl': '1rem',
        'card': '16px',
      },
      boxShadow: {
        'sm': '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        'DEFAULT': '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1)',
        'md': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
        'lg': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1)',
        'xl': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
        'gold-glow': '0 0 24px rgba(212, 175, 55, 0.3)',
        'gold-glow-lg': '0 0 48px rgba(212, 175, 55, 0.4)',
      },
      animation: {
        'scroll-reveal': 'scrollReveal 0.75s cubic-bezier(0.34, 1.56, 0.64, 1) forwards',
        'card-lift': 'cardLift 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) forwards',
      },
      keyframes: {
        scrollReveal: {
          'from': { opacity: '0', transform: 'translateY(50px)' },
          'to': { opacity: '1', transform: 'translateY(0)' },
        },
        cardLift: {
          'from': { opacity: '0', transform: 'translateY(30px) scale(0.95)' },
          'to': { opacity: '1', transform: 'translateY(0) scale(1)' },
        },
      },
    },
  },
  plugins: [],
}
