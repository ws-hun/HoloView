import axios from 'axios'
import { logger } from '@/utils/logger'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 8000,
  headers: { Accept: 'application/json' },
})

http.interceptors.request.use((config) => {
  logger.debug('API request', { method: config.method?.toUpperCase(), url: config.url })
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    const detail = axios.isAxiosError(error)
      ? { url: error.config?.url, status: error.response?.status, message: error.message }
      : { message: String(error) }
    logger.error('API request failed', detail)
    return Promise.reject(error)
  },
)
