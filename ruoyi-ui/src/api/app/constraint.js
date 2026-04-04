import request from '@/utils/request'

// 查询应用级约束规则列表
export function listConstraint(query) {
  return request({
    url: '/app/constraint/list',
    method: 'get',
    params: query
  })
}

// 查询应用级约束规则详细
export function getConstraint(id) {
  return request({
    url: '/app/constraint/' + id,
    method: 'get'
  })
}

// 新增应用级约束规则
export function addConstraint(data) {
  return request({
    url: '/app/constraint',
    method: 'post',
    data: data
  })
}

// 修改应用级约束规则
export function updateConstraint(data) {
  return request({
    url: '/app/constraint',
    method: 'put',
    data: data
  })
}

// 删除应用级约束规则
export function delConstraint(id) {
  return request({
    url: '/app/constraint/' + id,
    method: 'delete'
  })
}
