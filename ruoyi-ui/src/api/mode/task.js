import request from '@/utils/request'

// 获取任务日历数据
export function getTaskCalendarData(params) {
  return request({
    url: '/system/task/calendar',
    method: 'get',
    params: params
  })
}
