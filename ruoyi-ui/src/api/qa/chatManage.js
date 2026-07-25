import request from '@/utils/request'

export function listQaChat(query) {
  return request({
    url: '/qa/chat/manage/list',
    method: 'get',
    params: query
  })
}

export function getQaChat(id) {
  return request({
    url: '/qa/chat/manage/' + id,
    method: 'get'
  })
}

export function addQaChat(data) {
  return request({
    url: '/qa/chat/manage',
    method: 'post',
    data: data
  })
}

export function updateQaChat(data) {
  return request({
    url: '/qa/chat/manage',
    method: 'put',
    data: data
  })
}

export function delQaChat(id) {
  return request({
    url: '/qa/chat/manage/' + id,
    method: 'delete'
  })
}

export function listQaChatOptions() {
  return request({
    url: '/qa/chat/manage/options',
    method: 'get'
  })
}

export function listQaRobotChatRel(query) {
  return request({
    url: '/qa/chat/rel/list',
    method: 'get',
    params: query
  })
}

export function getQaRobotChatRel(robotId) {
  return request({
    url: '/qa/chat/rel/' + robotId,
    method: 'get'
  })
}

export function addQaRobotChatRel(data) {
  return request({
    url: '/qa/chat/rel',
    method: 'post',
    data: data
  })
}

export function updateQaRobotChatRel(data) {
  return request({
    url: '/qa/chat/rel',
    method: 'put',
    data: data
  })
}

export function delQaRobotChatRel(robotId) {
  return request({
    url: '/qa/chat/rel/' + robotId,
    method: 'delete'
  })
}
