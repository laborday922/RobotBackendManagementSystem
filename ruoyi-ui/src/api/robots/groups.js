import request from '@/utils/request'

// 查询机器人分组列表
export function listGroups(query) {
  return request({
    url: '/robots/groups/list',
    method: 'get',
    params: query
  })
}

// 查询机器人分组详细
export function getGroups(id) {
  return request({
    url: '/robots/groups/' + id,
    method: 'get'
  })
}

// 新增机器人分组
export function addGroups(data) {
  return request({
    url: '/robots/groups',
    method: 'post',
    data: data
  })
}

// 修改机器人分组
export function updateGroups(data) {
  return request({
    url: '/robots/groups',
    method: 'put',
    data: data
  })
}

// 删除机器人分组
export function delGroups(id) {
  return request({
    url: '/robots/groups/' + id,
    method: 'delete'
  })
}
