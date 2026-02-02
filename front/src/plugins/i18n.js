import { createI18n } from 'vue-i18n'
import en from '../locales/en.json'
import zh from '../locales/zh.json'

const i18n = createI18n({
  legacy: true, // Enable legacy mode for Options API ($t)
  locale: localStorage.getItem('lang') || 'zh',
  fallbackLocale: 'en',
  messages: {
    en,
    zh
  }
})

export default i18n
