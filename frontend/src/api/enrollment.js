import request from './request'

export function getByStudentId(studentId) {
  return request.get(`/enrollments/student/${studentId}`)
}

export function enroll(data) {
  return request.post('/enrollments', data)
}

export function unenroll(id) {
  return request.delete(`/enrollments/${id}`)
}
