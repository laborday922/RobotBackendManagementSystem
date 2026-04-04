import request from '@/utils/request'

// 查询机器人基础信息列表
export function listRobots(query) {
  return request({
    url: '/robots/robots/list',
    method: 'get',
    params: query
  })
}

// 查询机器人基础信息详细
export function getRobots(id) {
  return request({
    url: '/robots/robots/' + id,
    method: 'get'
  })
}

// 新增机器人基础信息
export function addRobots(data) {
  return request({
    url: '/robots/robots',
    method: 'post',
    data: data
  })
}

// 修改机器人基础信息
export function updateRobots(data) {
  return request({
    url: '/robots/robots',
    method: 'put',
    data: data
  })
}

// 删除机器人基础信息
export function delRobots(id) {
  return request({
    url: '/robots/robots/' + id,
    method: 'delete'
  })
}
//查询分组列表
export function listGroups(query) {
  return request({
    url: '/robots/groups/list',
    method: 'get',
    params: query
  })
}