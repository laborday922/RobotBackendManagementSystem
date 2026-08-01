import request from '@/utils/request'

export function listQaKnowledgeBase(query) {
  return request({
    url: '/qa/knowledgeBase/list',
    method: 'get',
    params: query
  })
}

export function getQaKnowledgeBase(id) {
  return request({
    url: '/qa/knowledgeBase/' + id,
    method: 'get'
  })
}

export function addQaKnowledgeBase(data) {
  return request({
    url: '/qa/knowledgeBase',
    method: 'post',
    data: data
  })
}

export function updateQaKnowledgeBase(data) {
  return request({
    url: '/qa/knowledgeBase',
    method: 'put',
    data: data
  })
}

export function delQaKnowledgeBase(id) {
  return request({
    url: '/qa/knowledgeBase/' + id,
    method: 'delete'
  })
}

export function listQaKnowledgeBaseOptions() {
  return request({
    url: '/qa/knowledgeBase/options',
    method: 'get'
  })
}
