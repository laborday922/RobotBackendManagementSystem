<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon blue"><i class="fas fa-list"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ totalTasks }}</div>
            <div class="stat-label">总任务数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon green"><i class="fas fa-play-circle"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ executingCount }}</div>
            <div class="stat-label">执行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon orange"><i class="fas fa-clock"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ readyCount }}</div>
            <div class="stat-label">准备中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon red"><i class="fas fa-pause-circle"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ pausedCount }}</div>
            <div class="stat-label">已暂停</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主卡片 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>所有任务</span>
          <el-button type="text" @click="refreshTasks">
            <i class="fas fa-sync-alt"></i> 刷新
          </el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select
          v-model="filter.robotId"
          placeholder="选择机器人"
          clearable
          style="width:180px"
          @change="onRobotFilterChange"
        >
          <el-option
            v-for="r in robotOptions"
            :key="r.id"
            :label="r.name"
            :value="r.id"
          />
        </el-select>
        <el-select
          v-model="filter.status"
          placeholder="状态"
          clearable
          style="width:150px; margin-left:10px;"
          @change="onStatusFilterChange"
        >
          <el-option label="未开始" :value="0" />
          <el-option label="准备中" :value="1" />
          <el-option label="执行中" :value="2" />
          <el-option label="已暂停" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已禁用" :value="5" />
          <el-option label="已终止" :value="6" />
        </el-select>
        <el-switch
          v-if="filter.robotId"
          v-model="sortableEnabled"
          active-text="启用拖拽排序"
          style="margin-left:20px;"
          @change="onSortableChange"
        />
      </div>

      <!-- 表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="sortedTasks"
        border
        style="width:100%"
        row-key="id"
        :key="tableKey"
        @row-click="handleRowClick"
      >
        <!-- 拖拽手柄列（仅准备中且启用排序时显示） -->
        <el-table-column v-if="showSortHandle" width="80" align="center">
          <template #header><span>排序</span></template>
          <template #default="scope">
            <div v-if="scope.row.status === 1"
            class="drag-handle"
            :class="{ 'drag-enabled': sortableEnabled }"
            >
            <i class="fas fa-grip-vertical"></i>
            <span class="queue-position">{{ getPendingOrderIndex(scope.row) + 1 }}</span>
  </div>
  <div v-else class="status-icon">
    <i v-if="scope.row.status === 2" class="fas fa-play" style="color:#52c41a;"></i>
    <i v-else-if="scope.row.status === 3" class="fas fa-pause" style="color:#faad14;"></i>
    <i v-else class="fas fa-clock" style="color:#999;"></i>
  </div>
</template>
</el-table-column>

<el-table-column prop="id" label="ID" width="80" align="center" />
<el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip />
<el-table-column prop="templateName" label="模板" width="140" show-overflow-tooltip />
<el-table-column label="状态" width="100" align="center">
<template #default="scope">
            <span :class="'status-tag status-' + getStatusClass(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </span>
</template>
</el-table-column>
<el-table-column prop="robotName" label="执行机器人" width="140" show-overflow-tooltip />
<el-table-column label="优先级" width="80" align="center">
<template #default="scope">
  <el-tag v-if="scope.row.priority === 1" type="danger">高</el-tag>
  <el-tag v-else-if="scope.row.priority === 2" type="warning">中</el-tag>
  <el-tag v-else type="info">低</el-tag>
</template>
</el-table-column>
<el-table-column label="进度" width="180">
<template #default="scope">
  <div class="step-progress">
    <div
      v-for="i in scope.row.totalSteps || 5"
      :key="i"
      :class="['step-dot', {
                  completed: i <= (scope.row.completedSteps || 0),
                  executing: i === (scope.row.completedSteps || 0) + 1 && scope.row.status === 2
                }]"
    ></div>
    <span style="margin-left:8px;">{{ scope.row.completedSteps || 0 }}/{{ scope.row.totalSteps || 5 }}</span>
  </div>
</template>
</el-table-column>
<el-table-column label="操作" width="250" fixed="right" align="center">
<template #default="scope">
  <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
  <el-button
    v-if="scope.row.status === 2"
    link
    type="warning"
    icon="Pause"
    @click="pauseTask(scope.row)"
  >暂停</el-button>
  <el-button
    v-if="scope.row.status === 3"
    link
    type="success"
    icon="Play"
    @click="resumeTask(scope.row)"
  >继续</el-button>
  <el-button
    v-if="scope.row.status === 1"
    link
    type="danger"
    plain
    icon="Ban"
    @click="cancelTask(scope.row)"
  >取消</el-button>
  <el-button
    v-if="scope.row.status !== 5"
    link
    type="info"
    icon="CircleClose"
    @click="disableTask(scope.row)"
  >禁用</el-button>
  <el-button
    v-if="scope.row.status === 5"
    link
    type="primary"
    icon="CircleCheck"
    @click="enableTask(scope.row)"
  >恢复</el-button>
  <el-button
    v-if="scope.row.status === 2 || scope.row.status === 3"
    link
    type="danger"
    icon="Stop"
    @click="stopTask(scope.row)"
  >停止</el-button>
</template>
</el-table-column>
</el-table>

<!-- 分页 -->
<pagination
  v-show="total > 0"
  :total="total"
  v-model:page="queryParams.pageNum"
  v-model:limit="queryParams.pageSize"
  @pagination="getList"
/>
</el-card>

<!-- 任务详情对话框（可复用任务列表的组件，此处简化） -->
<el-dialog title="任务详情" v-model="detailVisible" width="700px" append-to-body>
<div v-if="currentTask">
  <el-descriptions :column="1" border>
    <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
    <el-descriptions-item label="状态">{{ getStatusText(currentTask.status) }}</el-descriptions-item>
    <el-descriptions-item label="机器人">{{ currentTask.robotName }}</el-descriptions-item>
    <!-- 更多详情... -->
  </el-descriptions>
</div>
</el-dialog>
</div>
</template>

<script setup>
import {computed, nextTick, onMounted, reactive, ref, watch} from 'vue'
import Sortable from 'sortablejs'
import {
  banTask,
  cancelTask as cancelTaskApi,
  continueTask,
  listTask,
  pauseTask as pauseTaskApi,
  terminateTask,
  updateTask
} from '@/api/taskmgt/taskmgt'

// ========== 查询参数 ==========
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  robotId: undefined,
  status: undefined,
  displayOrder: 'status ASC, pending_order ASC, priority DESC, create_time DESC'
})

const filter = reactive({
  robotId: undefined,
  status: undefined
})

// ========== 数据 ==========
const loading = ref(false)
const taskList = ref([])
const total = ref(0)

// 统计
const totalTasks = computed(() => taskList.value.length)
const executingCount = computed(() => taskList.value.filter(t => t.status === 2).length)
const readyCount = computed(() => taskList.value.filter(t => t.status === 1).length)
const pausedCount = computed(() => taskList.value.filter(t => t.status === 3).length)

// 机器人选项（模拟，实际应从接口获取）
const robotOptions = ref([
  { id: 1, name: '机器人A' },
  { id: 2, name: '机器人B' },
  { id: 3, name: '机器人C' }
])

// ========== 拖拽排序相关 ==========
const sortableEnabled = ref(false)
const tableRef = ref(null)
const tableKey = ref(0)
let sortableInstance = null

// 是否显示拖拽手柄列
const showSortHandle = computed(() => sortableEnabled.value && filter.robotId)

// 当前筛选条件下的所有任务
const sortedTasks = computed(() => {
  // 已经通过 queryParams 传给后端排序，此处直接使用 taskList
  return taskList.value
})

// 获取准备中任务在当前列表中的索引（用于显示序号）
const getPendingOrderIndex = (task) => {
  if (task.status !== 1) return -1
  const readyTasks = taskList.value.filter(t => t.status === 1 && t.robotId === filter.robotId)
  const index = readyTasks.findIndex(t => t.id === task.id)
  return index
}

// 初始化或销毁 Sortable
const initSortable = () => {
  if (!showSortHandle.value) {
    destroySortable()
    return
  }

  // 等待 DOM 更新
  nextTick(() => {
    destroySortable()
    const tableEl = tableRef.value?.$el
    if (!tableEl) return
    const tbody = tableEl.querySelector('.el-table__body tbody')
    if (!tbody) return

    // 获取所有准备中任务的行（通过类名或数据属性，此处简单处理：所有行都可拖拽，但通过filter控制）
    sortableInstance = new Sortable(tbody, {
      animation: 150,
      ghostClass: 'sortable-ghost',
      dragClass: 'sortable-drag',
      handle: '.drag-handle', // 只有点击手柄才能拖拽
      filter: (evt, target) => {
        // 只允许拖拽准备中任务的行
        const row = target.closest('.el-table__row')
        if (!row) return true
        const index = row.__vueParentComponent?.props?.rowIndex
        if (index === undefined) return true
        const task = taskList.value[index]
        return task?.status !== 1
      },
      onMove: (evt) => {
        const relatedRow = evt.related
        const index = relatedRow?.__vueParentComponent?.props?.rowIndex
        if (index === undefined) return false
        const task = taskList.value[index]
        return task?.status === 1
      },
      onEnd: async (evt) => {
        const { oldIndex, newIndex } = evt
        if (oldIndex === newIndex) return

        // 获取移动的任务和目标任务
        const movedTask = taskList.value[oldIndex]
        const targetTask = taskList.value[newIndex]
        if (!movedTask || movedTask.status !== 1 || !targetTask || targetTask.status !== 1) {
          // 强制刷新表格以恢复顺序
          refreshTasks()
          return
        }

        // 获取当前机器人下的所有准备中任务（按当前显示顺序）
        const readyTasks = taskList.value
          .filter(t => t.status === 1 && t.robotId === filter.robotId)
          .map(t => ({ ...t })) // 浅拷贝

        // 找到移动的任务在 readyTasks 中的索引
        const movedIndex = readyTasks.findIndex(t => t.id === movedTask.id)
        const targetIndex = readyTasks.findIndex(t => t.id === targetTask.id)
        if (movedIndex === -1 || targetIndex === -1) return

        // 在数组中移动
        const [removed] = readyTasks.splice(movedIndex, 1)
        readyTasks.splice(targetIndex, 0, removed)

        // 重新计算 pending_order（从0开始或从1开始，取决于后端约定，此处假设从0开始）
        // 调用 updateTask 更新每个任务的 pending_order
        try {
          const updates = readyTasks.map((task, idx) => {
            if (task.pendingOrder !== idx) {
              return updateTask(task.id, { pendingOrder: idx })
            }
            return Promise.resolve()
          })
          await Promise.all(updates)
          ElMessage.success('排序已保存')
          // 刷新列表以获取最新数据
          await getList()
        } catch (error) {
          ElMessage.error('保存排序失败')
          console.error(error)
          refreshTasks()
        }
      }
    })
  })
}

const destroySortable = () => {
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
}

// 监听筛选变化，重新初始化拖拽
const onRobotFilterChange = (val) => {
  filter.robotId = val
  queryParams.robotId = val || undefined
  queryParams.pageNum = 1
  sortableEnabled.value = false // 切换机器人时自动关闭拖拽
  getList()
}

const onStatusFilterChange = (val) => {
  filter.status = val
  queryParams.status = val !== undefined ? val : undefined
  queryParams.pageNum = 1
  getList()
}

const onSortableChange = (val) => {
  if (val) {
    initSortable()
  } else {
    destroySortable()
    // 可选的：恢复默认排序，重新获取列表
    // getList()
  }
}

// ========== 获取数据 ==========
const getList = async () => {
  loading.value = true
  try {
    const res = await listTask(queryParams)
    taskList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

const refreshTasks = () => {
  getList()
}

// ========== 状态操作 ==========
const pauseTask = async (row) => {
  try {
    await pauseTaskApi(row.id)
    ElMessage.success('已暂停')
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resumeTask = async (row) => {
  try {
    await continueTask(row.id)
    ElMessage.success('已继续')
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const cancelTask = async (row) => {
  try {
    await cancelTaskApi(row.id)
    ElMessage.success('已取消')
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const disableTask = async (row) => {
  try {
    await banTask(row.id)
    ElMessage.success('已禁用')
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const enableTask = async (row) => {
  try {
    await resumeTask(row.id)
    ElMessage.success('已恢复')
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const stopTask = async (row) => {
  try {
    // 停止任务：可以调用 terminate 或自定义，这里简单用 terminate
    await terminateTask(row.id, '管理员停止')
    ElMessage.success('已停止')
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 查看详情（简化）
const detailVisible = ref(false)
const currentTask = ref(null)
const handleView = (row) => {
  currentTask.value = row
  detailVisible.value = true
}

// ========== 辅助函数 ==========
const statusMap = {
  0: 'pending', 1: 'ready', 2: 'executing', 3: 'paused', 4: 'completed', 5: 'disabled', 6: 'aborted'
}
const statusTextMap = {
  0: '未开始', 1: '准备中', 2: '执行中', 3: '已暂停', 4: '已完成', 5: '已禁用', 6: '已终止'
}
const getStatusText = (status) => statusTextMap[status] || status
const getStatusClass = (status) => statusMap[status] || 'pending'

// ========== 生命周期 ==========
onMounted(() => {
  getList()
})

// 监听表格数据变化，重新初始化拖拽（当启用排序时）
watch(
  [() => taskList.value, sortableEnabled, filter.robotId],
  () => {
    if (sortableEnabled.value && filter.robotId) {
      initSortable()
    } else {
      destroySortable()
    }
  },
  { deep: true, immediate: true }
)

// 组件卸载时销毁拖拽
onUnmounted(() => {
  destroySortable()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.stats-cards {
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}
.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.red { background-color: #fff1f0; color: #ff4d4f; }
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}
.table-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.drag-handle {
  cursor: move;
  color: #999;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  user-select: none;
}
.drag-handle:hover {
  color: #1890ff;
  background-color: #f0f0f0;
}
.drag-enabled {
  background-color: #e6f7ff;
  color: #1890ff;
}
.queue-position {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #1890ff;
  color: white;
  font-size: 12px;
}
.status-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}
.step-progress {
  display: flex;
  align-items: center;
  gap: 4px;
}
.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #d9d9d9;
}
.step-dot.completed {
  background-color: #52c41a;
}
.step-dot.executing {
  background-color: #1890ff;
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}
.status-pending { background-color: #fff7e6; color: #fa8c16; }
.status-ready { background-color: #e6f7ff; color: #1890ff; }
.status-executing { background-color: #f6ffed; color: #52c41a; }
.status-paused { background-color: #fffbe6; color: #faad14; }
.status-completed { background-color: #f6ffed; color: #52c41a; }
.status-disabled { background-color: #f5f5f5; color: #999; }
.status-aborted { background-color: #fff1f0; color: #ff4d4f; }
</style>

<style>
/* 全局 Sortable 样式 */
.sortable-ghost {
  opacity: 0.4;
  background-color: #e6f7ff !important;
  border: 2px dashed #1890ff !important;
}
.sortable-drag {
  opacity: 0.8;
}
</style>
