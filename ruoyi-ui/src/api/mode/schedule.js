import request from '@/utils/request'

// 查询排程列表
export function listSchedule(query) {
  return request({
    url: '/system/schedule/list',
    method: 'get',
    params: query
  })
}

// 查询排程详细
export function getSchedule(scheduleId) {
  return request({
    url: '/system/schedule/' + scheduleId,
    method: 'get'
  })
}

// 新增排程
export function addSchedule(data) {
  return request({
    url: '/system/schedule',
    method: 'post',
    data: data
  })
}

// 修改排程
export function updateSchedule(data) {
  return request({
    url: '/system/schedule',
    method: 'put',
    data: data
  })
}

// 删除排程
export function delSchedule(scheduleId) {
  return request({
    url: '/system/schedule/' + scheduleId,
    method: 'delete'
  })
}

// 切换排程状态
export function toggleScheduleStatus(scheduleId) {
  return request({
    url: '/system/schedule/toggleStatus/' + scheduleId,
    method: 'put'
  })
}

// 获取日历数据
export function getCalendarData(params) {
  return request({
    url: '/system/schedule/calendar',
    method: 'get',
    params: params
  })
}
