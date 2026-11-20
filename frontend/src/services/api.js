import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api'
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url || ''
    if (error.response?.status === 401 && !url.includes('/auth/login')) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/** Extrai a mensagem de erro devolvida pela API. */
export const msgErro = (e) => {
  const data = e?.response?.data
  if (data?.erros?.length) return data.erros.join(' ')
  return data?.message || 'Não foi possível concluir a operação. Tente novamente.'
}
