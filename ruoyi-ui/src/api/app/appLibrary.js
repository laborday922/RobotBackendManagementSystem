import request from '@/utils/request'

// 查询应用库列表
export function listAppLibrary(query) {
  return request({
    url: '/app/appLibrary/list',
    method: 'get',
    params: query
  })
}

// 查询应用库详细
export function getAppLibrary(id) {
  return request({
    url: '/app/appLibrary/' + id,
    method: 'get'
  })
}

// 新增应用库
export function addAppLibrary(data) {
  return request({
    url: '/app/appLibrary',
    method: 'post',
    data: data
  })
}

// 修改应用库
export function updateAppLibrary(data) {
  return request({
    url: '/app/appLibrary',
    method: 'put',
    data: data
  })
}

// 删除应用库
export function delAppLibrary(id) {
  return request({
    url: '/app/appLibrary/' + id,
    method: 'delete'
  })
}

  // 开启/关闭应用
export function changeStatus(id,enabled) {
  return request({
    url: '/app/appLibrary/' + id+'/'+'enabled/'+enabled,
    method: 'put'
  })
}
