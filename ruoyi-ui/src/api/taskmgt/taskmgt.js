import request from '@/utils/request'

// 模板管理
export function listTemplate(query) {
  return request({
    url: '/taskmgt/templates',
    method: 'get',
    params: query
  })
}

export function getTemplate(id) {
  return request({
    url: '/taskmgt/templates/' + id,
    method: 'get'
  })
}

export function addTemplate(data) {
  return request({
    url: '/taskmgt/templates',
    method: 'post',
    data: data
  })
}

export function updateTemplate(data) {
  return request({
    url: '/taskmgt/templates/' + data.id,
    method: 'put',
    data: data
  })
}

export function delTemplate(id) {
  return request({
    url: '/taskmgt/templates/' + id,
    method: 'delete'
  })
}

// 任务管理
export function listTask(query) {
  return request({
    url: '/taskmgt/tasks',
    method: 'get',
    params: query
  })
}

export function getTask(id) {
  return request({
    url: '/taskmgt/tasks/' + id,
    method: 'get'
  })
}

export function addTask(data) {
  return request({
    url: '/taskmgt/task',
    method: 'post',
    data: data
  })
}

export function updateTask(id, data) {
  return request({
    url: '/taskmgt/tasks/' + id,
    method: 'put',
    data: data
  })
}

export function delTask(id) {
  return request({
    url: '/taskmgt/tasks/' + id,
    method: 'delete'
  })
}

export function listAbnormalTask(query) {
  return request({
    url: '/taskmgt/tasks/abnormal',
    method: 'get',
    params: query
  })
}

// 任务状态操作
export function banTask(id) {
  return request({
    url: '/taskmgt/tasks/' + id + '/ban',
    method: 'put'
  })
}

export function resumeTask(id) {
  return request({
    url: '/taskmgt/tasks/' + id + '/resume',
    method: 'put'
  })
}

export function pauseTask(id) {
  return request({
    url: '/taskmgt/tasks/' + id + '/pause',
    method: 'put'
  })
}

export function continueTask(id) {
  return request({
    url: '/taskmgt/tasks/' + id + '/continue',
    method: 'put'
  })
}

export function terminateTask(id, reason) {
  return request({
    url: '/taskmgt/tasks/' + id + '/terminate',
    method: 'put',
    data: { terminateReason: reason }
  })
}

export function cancelTask(id) {
  return request({
    url: '/taskmgt/tasks/' + id + '/cancel',
    method: 'put'
  })
}

export function resolveTaskRisk(id) {
  return request({
    url: '/taskmgt/tasks/' + id + '/resolve',
    method: 'put'
  })
}
export function addTaskSteps(id, data) {
  return request({
    url: '/taskmgt/tasks/' +  id + 'steps',
    method: 'post',
    data: data
  })
}

export function getTaskSteps(id) {
  return request({
    url: '/taskmgt/tasks/' + id + 'steps',
    method: 'get'
  })
}


export function updateTaskSteps(id, data) {
  return request({
    url: '/taskmgt/tasks/' + id + 'steps',
    method: 'put',
    data: data
  })
}

export function completeTaskSteps(id){
  return request({
    url: 'taskmgt/steps/' + id,
    method: 'put'
  })
}
