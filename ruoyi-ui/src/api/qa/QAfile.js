import request from '@/utils/request'

// 查询QA文件管理列表
export function listQAfile(query) {
  return request({
    url: '/qa/QAfile/list',
    method: 'get',
    params: query
  })
}

// 查询QA文件管理详细
export function getQAfile(id) {
  return request({
    url: '/qa/QAfile/' + id,
    method: 'get'
  })
}

// 新增QA文件管理
export function addQAfile(data) {
  return request({
    url: '/qa/QAfile',
    method: 'post',
    data: data
  })
}

// 修改QA文件管理
export function updateQAfile(data) {
  return request({
    url: '/qa/QAfile',
    method: 'put',
    data: data
  })
}

// 删除QA文件管理
export function delQAfile(id) {
  return request({
    url: '/qa/QAfile/' + id,
    method: 'delete'
  })
}
