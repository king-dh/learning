import request from './request'

export function getPage(params) {
  return request.get('/students/page', { params })
}

export function search(params) {
  return request.get('/students/search', { params })
}

export function getById(id) {
  return request.get(`/students/${id}`)
}

export function create(data) {
  return request.post('/students', data)
}

export function update(data) {
  return request.put('/students', data)
}

export function remove(id) {
  return request.delete(`/students/${id}`)
}
