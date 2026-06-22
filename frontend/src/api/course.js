import request from './request'

export function getPage(params) {
  return request.get('/courses/page', { params })
}

export function search(params) {
  return request.get('/courses/search', { params })
}

export function getById(id) {
  return request.get(`/courses/${id}`)
}

export function getByTeacherId(teacherId) {
  return request.get(`/courses/teacher/${teacherId}`)
}

export function create(data) {
  return request.post('/courses', data)
}

export function update(data) {
  return request.put('/courses', data)
}

export function remove(id) {
  return request.delete(`/courses/${id}`)
}
