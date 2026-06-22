import request from './request'

export function getPage(params) {
  return request.get('/scores/page', { params })
}

export function getById(id) {
  return request.get(`/scores/${id}`)
}

export function getByStudentId(studentId) {
  return request.get(`/scores/student/${studentId}`)
}

export function create(data) {
  return request.post('/scores', data)
}

export function update(data) {
  return request.put('/scores', data)
}

export function remove(id) {
  return request.delete(`/scores/${id}`)
}
