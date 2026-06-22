import request from './request'

export function getPage(params) {
  return request.get('/teachers/page', { params })
}

export function search(params) {
  return request.get('/teachers/search', { params })
}

export function getById(id) {
  return request.get(`/teachers/${id}`)
}

export function create(data) {
  return request.post('/teachers', data)
}

export function update(data) {
  return request.put('/teachers', data)
}

export function remove(id) {
  return request.delete(`/teachers/${id}`)
}
