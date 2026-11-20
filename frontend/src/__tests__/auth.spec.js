import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('../services/api', () => ({ api: { post: vi.fn() } }))

import { api } from '../services/api'
import { useAuth } from '../stores/auth'

describe('store de autenticacao', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('guarda token e usuario no login', async () => {
    api.post.mockResolvedValue({
      data: { token: 'abc', nome: 'Administrador', email: 'admin@krill.com', perfil: 'ADMIN' }
    })
    const auth = useAuth()

    await auth.login('admin@krill.com', 'admin123')

    expect(auth.logado).toBe(true)
    expect(auth.isAdmin).toBe(true)
    expect(localStorage.getItem('token')).toBe('abc')
  })

  it('usuario comum nao e administrador', async () => {
    api.post.mockResolvedValue({
      data: { token: 'xyz', nome: 'Comum', email: 'usuario@krill.com', perfil: 'COMUM' }
    })
    const auth = useAuth()

    await auth.login('usuario@krill.com', 'usuario123')

    expect(auth.logado).toBe(true)
    expect(auth.isAdmin).toBe(false)
  })

  it('limpa a sessao no logout', async () => {
    api.post.mockResolvedValue({ data: { token: 'abc', nome: 'A', email: 'a@a.com', perfil: 'ADMIN' } })
    const auth = useAuth()
    await auth.login('a@a.com', '1')

    auth.logout()

    expect(auth.logado).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })
})
