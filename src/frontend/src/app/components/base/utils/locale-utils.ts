export class LocaleUtils {
  static getCurrencyForLocale(locale: string | undefined): string {
    const localeMap: Record<string, string> = {
      pt: 'BRL',
      'pt-BR': 'BRL',
      en: 'USD',
      'en-US': 'USD',
      es: 'EUR',
      'es-ES': 'EUR',
      fr: 'EUR',
      de: 'EUR',
    };
    if (!locale) return 'USD';
    return localeMap[locale] || 'USD';
  }
}
