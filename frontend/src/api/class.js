import request from './request'

export function getPage(params) {
  return request.get('/classes/page', { params })
}

export function search(params) {
  return request.get('/classes/search', { params })
}

export function getById(id) {
  return request.get(`/classes/${id}`)
}

export function getStudents(id) {
  return request.get(`/classes/${id}/students`)
}

export function create(data) {
  return request.post('/classes', data)
}

export function update(data) {
  return request.put('/classes', data)
}

export function remove(id) {
  return request.delete(`/classes/${id}`)
}
