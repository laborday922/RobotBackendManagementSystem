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
      <div slot="header" class="card-header">
        <span>所有任务</span>
        <el-button type="text" @click="refreshTasks">
          <i class="fas fa-sync-alt"></i> 刷新
        </el-button>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <!-- 排序模式选择 -->
        <el-select
          v-model="sortMode"
          placeholder="选择排序模式"
          style="width:150px"
          @change="onSortModeChange"
        >
          <el-option label="普通模式" value="none" />
          <el-option label="全局排序" value="global" />
          <el-option label="资源内排序" value="local" />
        </el-select>

        <!-- 资源选择（仅在资源内排序时显示） -->
        <el-select
          v-if="sortMode === 'local'"
          v-model="resourceFilter.resourceId"
          placeholder="选择机器人/组"
          clearable
          style="width:180px; margin-left:10px;"
          @change="onResourceFilterChange"
        >
          <el-option-group label="机器人">
            <el-option
              v-for="r in robotOptions"
              :key="'robot-' + r.id"
              :label="r.name"
              :value="r.id"
            />
          </el-option-group>
          <el-option-group label="机器人组">
            <el-option
              v-for="g in robotGroupOptions"
              :key="'group-' + g.id"
              :label="g.name"
              :value="g.id"
            />
          </el-option-group>
        </el-select>

        <!-- 状态筛选 -->
        <el-select
          v-if="sortMode === 'none'"
          v-model="filter.status"
          placeholder="状态"
          clearable
          style="width:150px; margin-left:10px;"
          @change="onStatusFilterChange"
          @clear="onStatusFilterChange"
        >
          <el-option label="未开始" :value="3" />
          <el-option label="准备中" :value="1" />
          <el-option label="执行中" :value="0" />
          <el-option label="已暂停" :value="2" />
          <el-option label="已完成" :value="6" />
          <el-option label="已禁用" :value="4" />
          <el-option label="已终止" :value="5" />
        </el-select>

        <!-- 提示信息 -->
        <el-tag v-if="sortMode === 'global'" type="warning" style="margin-left:10px;">
          <i class="fas fa-info-circle"></i> 全局排序：可跨资源拖拽调整全局顺序，但保持资源内相对顺序不变
        </el-tag>
        <el-tag v-if="sortMode === 'local'" type="success" style="margin-left:10px;">
          <i class="fas fa-info-circle"></i> 资源内排序：调整选定资源内的任务顺序
        </el-tag>
      </div>

      <!-- 表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="displayedTasks"
        border
        style="width:100%"
        row-key="id"
        :key="tableKey"
        @row-click="handleRowClick"
        :row-class-name="getRowClassName"
      >
        <!-- 拖拽手柄列 -->
        <el-table-column v-if="showSortHandle" width="100" align="center">
          <template slot="header">
            <span>排序</span>
            <el-tooltip v-if="sortMode === 'global'" content="可跨资源拖拽，但资源内相对顺序保持不变" placement="top">
              <i class="fas fa-question-circle" style="color: #909399; margin-left: 4px;"></i>
            </el-tooltip>
          </template>
          <template slot-scope="scope">
            <div class="drag-handle" :class="{ 'drag-enabled': isSortableEnabled }">
              <i class="fas fa-grip-vertical"></i>
              <span v-if="sortMode === 'local'" class="queue-position">{{ scope.row.pendingOrder + 1 }}</span>
              <span v-else-if="sortMode === 'global'" class="queue-position">{{ scope.row.globalPendingOrder + 1 }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="templateName" label="模板" width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <span :class="'status-tag status-' + getStatusClass(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="是否是组任务" width="100" align="center">
          <template slot-scope="scope">
            {{ scope.row.isGroupTask === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="执行机器人组" prop="robotGroupName" min-width="120" show-overflow-tooltip />
        <el-table-column prop="robotName" label="执行机器人" width="140" show-overflow-tooltip />
        <el-table-column label="优先级" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.priority === 1" type="danger">高</el-tag>
            <el-tag v-else-if="scope.row.priority === 2" type="warning">中</el-tag>
            <el-tag v-else type="info">低</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="180">
          <template slot-scope="scope">
            <div class="step-progress">
              <div
                v-for="i in scope.row.totalSteps || 0"
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
        <!-- 操作列 -->
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template slot-scope="scope">
            <!-- 详情 -->
            <el-button size="small" circle title="详情" @click="handleView(scope.row)">
              <i class="fas fa-eye"></i>
            </el-button>

            <!-- 暂停 -->
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="warning"
              circle
              title="暂停"
              @click="pauseTask(scope.row)"
            >
              <i class="fas fa-pause"></i>
            </el-button>

            <!-- 继续 -->
            <el-button
              v-if="scope.row.status === 2"
              size="small"
              type="success"
              circle
              title="继续"
              @click="continueTask(scope.row)"
            >
              <i class="fas fa-play"></i>
            </el-button>

            <!-- 取消 -->
            <el-button
              v-if="scope.row.status === 1"
              size="small"
              type="info"
              circle
              title="取消"
              @click="cancelTask(scope.row)"
            >
              <i class="fas fa-times"></i>
            </el-button>

            <!-- 禁用 -->
            <el-button
              v-if="scope.row.status === 3"
              size="small"
              type="warning"
              circle
              title="禁用"
              @click="disableTask(scope.row)"
            >
              <i class="fas fa-ban"></i>
            </el-button>

            <!-- 恢复 -->
            <el-button
              v-if="scope.row.status === 4"
              size="small"
              type="success"
              circle
              title="恢复"
              @click="enableTask(scope.row)"
            >
              <i class="fas fa-undo"></i>
            </el-button>

            <!-- 停止 -->
            <el-button
              v-if="scope.row.status === 2 || scope.row.status === 0"
              size="small"
              type="danger"
              circle
              title="停止"
              @click="stopTask(scope.row)"
            >
              <i class="fas fa-power-off"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页（仅在非排序模式下显示） -->
      <pagination
        v-show="total > 0 && sortMode === 'none'"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />

      <!-- 排序模式下的提示 -->
      <div v-if="sortMode !== 'none'" style="text-align: center; padding: 10px; color: #909399;">
        <i class="fas fa-info-circle"></i> 排序模式下显示所有准备中任务，不分页
      </div>
    </el-card>

    <!-- 任务详情对话框 -->
    <el-dialog
      title="任务详情"
      :visible.sync="detailVisible"
      width="800px"
      append-to-body
    >
      <div v-loading="detailLoading" v-if="currentTask">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="模板">{{ currentTask.templateName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            {{ currentTask.templateId ? '流程任务' : '简单任务' }}
          </el-descriptions-item>
          <el-descriptions-item label="触发方式">
            {{ getTriggerTypeText(currentTask.taskType) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <span :class="'status-tag status-' + getStatusClass(currentTask.status)">{{
                getStatusText(currentTask.status)
              }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag v-if="currentTask.priority === 1" type="danger">高</el-tag>
            <el-tag v-else-if="currentTask.priority === 2" type="warning">中</el-tag>
            <el-tag v-else type="info">低</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="机器人">{{ currentTask.robotName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="机器人组">{{ currentTask.robotGroupName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentTask.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="任务时长">{{ currentTask.duration }}分钟</el-descriptions-item>
          <el-descriptions-item label="终止原因" v-if="currentTask.terminateReason">{{
              currentTask.terminateReason
            }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 表单内容 -->
        <el-divider content-position="left">表单内容</el-divider>
        <div v-if="formFields.length > 0">
          <el-descriptions :column="1" border>
            <el-descriptions-item v-for="field in formFields" :key="field.id" :label="field.label">
              {{ formatFormValue(field, currentTask.formData ? currentTask.formData[field.id] : '') }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else class="empty-tip">无表单内容</div>

        <!-- 作业步骤 -->
        <el-divider content-position="left">作业步骤</el-divider>
        <el-table :data="taskSteps" border size="small" style="width:100%">
          <el-table-column label="步骤序号" width="80" align="center">
            <template slot-scope="scope">{{ scope.row.orderNum }}</template>
          </el-table-column>
          <el-table-column prop="stepName" label="步骤名称" width="150"/>
          <el-table-column prop="description" label="描述" min-width="200"/>
          <el-table-column label="状态" width="100" align="center">
            <template slot-scope="scope">
              <span :class="'status-tag status-' + getStepStatusClass(scope.row.status)">{{
                  getStepStatusText(scope.row.status)
                }}</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 任务日志 -->
        <el-divider content-position="left">任务日志</el-divider>
        <el-timeline>
          <el-timeline-item
            v-for="log in taskLogs"
            :key="log.id"
            :type="log.eventType === 'ERROR' ? 'danger' : 'info'"
            :timestamp="log.createTime"
          >
            {{ log.content }}
          </el-timeline-item>
        </el-timeline>
        <div v-if="taskLogs.length === 0" class="empty-tip">暂无日志</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Sortable from 'sortablejs'
import {
  banTask,
  cancelTask as cancelTaskApi,
  continueTask,
  listTask,
  pauseTask as pauseTaskApi,
  terminateTask,
  getTask,
  getTaskSteps,
  listLogByTask,
  resumeTask,
  updatePendingOrder,
  updateGlobalOrder
} from '@/api/taskmgt/taskmgt'
import {listRobots} from '@/api/system/robots'
import { listGroups } from '@/api/system/robots'

export default {
  name: 'TaskSchedule',
  data() {
    return {
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        robotId: undefined,
        status: undefined,
        displayOrder: 'status ASC, pending_order ASC, priority DESC, create_time DESC'
      },
      // 前端筛选
      filter: {
        robotId: undefined,
        status: undefined
      },
      // 资源筛选（用于资源内排序）
      resourceFilter: {
        resourceId: undefined,
        isGroupTask: false
      },
      // 排序模式：none-普通模式, global-全局排序, local-资源内排序
      sortMode: 'none',
      loading: false,
      taskList: [],
      total: 0,
      robotOptions: [],
      robotGroupOptions: [],
      templateOptions: [],
      // 拖拽排序相关
      tableKey: 0,
      sortableInstance: null,
      // 用于回滚的原始数据备份
      originalTaskList: [],
      // 详情
      detailVisible: false,
      detailLoading: false,
      currentTask: null,
      taskSteps: [],
      taskLogs: [],
      formFields: []
    }
  },
  computed: {
    // 统计
    totalTasks() {
      return this.taskList.length
    },
    executingCount() {
      return this.taskList.filter(t => t.status === 0).length
    },
    readyCount() {
      return this.taskList.filter(t => t.status === 1).length
    },
    pausedCount() {
      return this.taskList.filter(t => t.status === 2).length
    },
    // 是否显示拖拽手柄列
    showSortHandle() {
      return this.sortMode !== 'none'
    },
    // 是否启用拖拽
    isSortableEnabled() {
      return this.sortMode === 'global' || (this.sortMode === 'local' && this.resourceFilter.resourceId)
    },
    // 显示的任务列表
    displayedTasks() {
      if (this.sortMode === 'none') {
        // 普通模式：返回过滤后的任务
        return this.taskList
      } else if (this.sortMode === 'global') {
        // 全局排序模式：只显示准备中状态的任务，按globalPendingOrder排序，同时确保资源内按pendingOrder排序
        return this.taskList
          .filter(t => t.status === 1)
          .sort((a, b) => {
            // 首先按globalPendingOrder排序
            const globalOrderDiff = (a.globalPendingOrder || 0) - (b.globalPendingOrder || 0)
            if (globalOrderDiff !== 0) return globalOrderDiff

            // 如果全局顺序相同，按资源内顺序排序
            return (a.pendingOrder || 0) - (b.pendingOrder || 0)
          })
      } else if (this.sortMode === 'local') {
        // 资源内排序模式：只显示选定资源的准备中任务
        if (!this.resourceFilter.resourceId) {
          return []
        }
        return this.taskList
          .filter(t => {
            if (t.status !== 1) return false
            if (this.resourceFilter.isGroupTask) {
              return t.robotGroupId === this.resourceFilter.resourceId
            } else {
              return t.robotId === this.resourceFilter.resourceId && t.isGroupTask !== 1
            }
          })
          .sort((a, b) => (a.pendingOrder || 0) - (b.pendingOrder || 0))
      }
      return this.taskList
    }
  },
  watch: {
    // 监听表格数据变化，重新初始化拖拽
    taskList: {
      handler() {
        this.$nextTick(() => {
          this.initSortable()
        })
      },
      deep: true
    },
    // 监听排序模式变化
    sortMode(val) {
      if (val === 'none') {
        this.destroySortable()
        this.getList()
      } else if (val === 'global') {
        this.destroySortable()
        this.getAllPendingTasks()
      } else if (val === 'local') {
        this.destroySortable()
        this.resourceFilter.resourceId = undefined
        this.taskList = []
      }
    },
    // 监听资源选择变化
    'resourceFilter.resourceId'(val) {
      if (this.sortMode === 'local' && val) {
        this.getLocalPendingTasks()
      }
    }
  },
  created() {
    this.getRobotList()
    this.getRobotGroupList()
    this.getList()
  },
  beforeDestroy() {
    this.destroySortable()
  },
  methods: {
    // 获取机器人列表
    async getRobotList() {
      try {
        const res = await listRobots({pageSize: 1000})
        this.robotOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人列表失败')
        console.error(error)
      }
    },
    // 获取机器人组列表
    async getRobotGroupList() {
      try {
        const res = await listGroups({pageSize: 1000})
        this.robotGroupOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人组列表失败')
        console.error(error)
      }
    },
    // 获取任务列表（普通模式）
    async getList() {
      this.loading = true
      try {
        const params = {...this.queryParams}
        const res = await listTask(params)
        this.taskList = res.rows || []
        this.total = res.total || 0
        // 备份原始数据
        this.backupOriginalData()
      } catch (error) {
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },
    // 获取所有准备中任务（全局排序模式）
    async getAllPendingTasks() {
      this.loading = true
      try {
        const params = {
          pageNum: 1,
          pageSize: 9999,
          status: 1, // 准备中
          displayOrder: 'global_pending_order ASC, pending_order ASC'
        }
        const res = await listTask(params)
        this.taskList = res.rows || []
        this.total = res.total || 0
        // 备份原始数据
        this.backupOriginalData()
      } catch (error) {
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },
    // 获取资源内准备中任务（资源内排序模式）
    async getLocalPendingTasks() {
      if (!this.resourceFilter.resourceId) return

      this.loading = true
      try {
        const params = {
          pageNum: 1,
          pageSize: 9999,
          status: 1, // 准备中
          displayOrder: 'pending_order ASC'
        }

        // 根据选择的资源类型设置查询参数
        if (this.resourceFilter.isGroupTask) {
          params.robotGroupId = this.resourceFilter.resourceId
          params.isGroupTask = 1
        } else {
          params.robotId = this.resourceFilter.resourceId
          params.isGroupTask = 0
        }

        const res = await listTask(params)
        this.taskList = res.rows || []
        this.total = res.total || 0
        // 备份原始数据
        this.backupOriginalData()
      } catch (error) {
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },
    // 备份原始数据用于回滚
    backupOriginalData() {
      this.originalTaskList = JSON.parse(JSON.stringify(this.taskList))
    },
    // 回滚到原始数据
    rollbackData() {
      this.taskList = JSON.parse(JSON.stringify(this.originalTaskList))
      // 强制刷新表格
      this.tableKey += 1
      this.$nextTick(() => {
        this.initSortable()
      })
    },
    refreshTasks() {
      if (this.sortMode === 'none') {
        this.getList()
      } else if (this.sortMode === 'global') {
        this.getAllPendingTasks()
      } else if (this.sortMode === 'local') {
        this.getLocalPendingTasks()
      }
    },
    // 排序模式切换
    onSortModeChange(val) {
      this.sortMode = val
      this.resourceFilter.resourceId = undefined
      this.filter.status = undefined
      this.queryParams.status = undefined
    },
    // 资源筛选变化
    onResourceFilterChange(val) {
      if (!val) {
        this.resourceFilter.isGroupTask = false
        this.taskList = []
        return
      }
      // 判断选择的是机器人还是机器人组
      const isRobot = this.robotOptions.some(r => r.id === val)
      this.resourceFilter.isGroupTask = !isRobot
      this.getLocalPendingTasks()
    },
    // 状态筛选变化
    onStatusFilterChange(val) {
      this.filter.status = val
      this.queryParams.status = val !== undefined ? val : undefined
      this.queryParams.pageNum = 1
      this.getList()
    },
    // 获取行类名（用于样式控制）
    getRowClassName({row}) {
      if (this.sortMode === 'global' && row.status === 1) {
        // 全局排序模式下，为不同资源的任务添加不同背景色
        const resourceKey = row.isGroupTask === 1
          ? `group-${row.robotGroupId}`
          : `robot-${row.robotId}`
        const colors = ['row-resource-1', 'row-resource-2', 'row-resource-3', 'row-resource-4', 'row-resource-5']
        // 使用简单的hash来选择颜色
        let hash = 0
        for (let i = 0; i < resourceKey.length; i++) {
          hash = resourceKey.charCodeAt(i) + ((hash << 5) - hash)
        }
        return colors[Math.abs(hash) % colors.length]
      }
      return ''
    },
    // 获取任务的资源标识（用于判断是否同一资源）
    getResourceKey(task) {
      if (!task) return null
      if (task.isGroupTask === 1) {
        return `group-${task.robotGroupId}`
      } else {
        return `robot-${task.robotId}`
      }
    },
    // 拖拽排序初始化
    initSortable() {
      if (!this.isSortableEnabled) {
        this.destroySortable()
        return
      }

      this.$nextTick(() => {
        this.destroySortable()
        const tableEl = this.$refs.tableRef?.$el
        if (!tableEl) return
        const tbody = tableEl.querySelector('.el-table__body tbody')
        if (!tbody) return

        // 根据排序模式配置不同的拖拽行为
        const config = {
          animation: 150,
          ghostClass: 'sortable-ghost',
          dragClass: 'sortable-drag',
          handle: '.drag-handle',
          onMove: this.handleSortMove,
          onEnd: this.sortMode === 'global' ? this.handleGlobalSortEnd : this.handleLocalSortEnd
        }

        this.sortableInstance = new Sortable(tbody, config)
      })
    },
    destroySortable() {
      if (this.sortableInstance) {
        this.sortableInstance.destroy()
        this.sortableInstance = null
      }
    },
    // 拖拽移动时的校验（前端校验）
    handleSortMove(evt, originalEvent) {
      const {dragged, related} = evt

      // 获取拖拽的元素索引
      const draggedIndex = Array.from(dragged.parentNode.children).indexOf(dragged)
      const relatedIndex = Array.from(related.parentNode.children).indexOf(related)

      const draggedTask = this.displayedTasks[draggedIndex]
      const relatedTask = this.displayedTasks[relatedIndex]

      if (!draggedTask || !relatedTask) return false

      // 全局排序模式：允许跨资源拖拽，但禁止同一资源内改变相对顺序
      if (this.sortMode === 'global') {
        const draggedResource = this.getResourceKey(draggedTask)
        const relatedResource = this.getResourceKey(relatedTask)

        // 如果同一资源内拖拽，禁止（应该去资源内排序模式）
        if (draggedResource === relatedResource) {
          return false // 返回false会显示禁止放置的样式
        }
      }

      return true // 允许放置
    },
    // 处理全局排序结束
    // 处理全局排序结束
    async handleGlobalSortEnd(evt) {
      const {oldIndex, newIndex} = evt

      // 如果位置没变，直接返回
      if (oldIndex === newIndex) return

      const movedTask = this.displayedTasks[oldIndex]
      const targetTask = this.displayedTasks[newIndex]

      if (!movedTask || movedTask.status !== 1) {
        this.rollbackData()
        return
      }

      // 【关键修复】获取所有准备中任务的新顺序（拖拽后的临时顺序）
      const newOrderList = [...this.displayedTasks]
      const [removed] = newOrderList.splice(oldIndex, 1)
      newOrderList.splice(newIndex, 0, removed)

      // 【关键修复】按资源分组，校验每个资源内的相对顺序是否与 pendingOrder 一致
      const resourceGroups = {}
      newOrderList.forEach(task => {
        const key = this.getResourceKey(task)
        if (!resourceGroups[key]) {
          resourceGroups[key] = []
        }
        resourceGroups[key].push(task)
      })

      // 校验每个资源组内的顺序是否按 pendingOrder 递增
      for (const key in resourceGroups) {
        const groupTasks = resourceGroups[key]
        // 按当前列表中的顺序获取ID
        const currentOrderIds = groupTasks.map(t => t.id)
        // 按 pendingOrder 排序应该得到的顺序
        const sortedByPendingOrder = [...groupTasks]
          .sort((a, b) => (a.pendingOrder || 0) - (b.pendingOrder || 0))
          .map(t => t.id)

        // 如果顺序不一致，说明改变了资源内相对顺序，不允许
        if (JSON.stringify(currentOrderIds) !== JSON.stringify(sortedByPendingOrder)) {
          this.$message.error('全局排序不允许改变资源内的相对顺序，请使用"资源内排序"模式')
          this.rollbackData()
          return
        }
      }

      // 校验通过，提取任务ID列表
      const taskIds = newOrderList.map(t => t.id)

      try {
        await updateGlobalOrder({}, taskIds)
        this.$message.success('全局排序已保存')
        // 更新备份数据
        this.backupOriginalData()
        // 刷新列表获取最新顺序
        await this.getAllPendingTasks()
      } catch (error) {
        // 后端校验失败或网络错误，回滚并提示
        this.$message.error(error.response?.data?.msg || '保存全局排序失败，任务已恢复原位置')
        this.rollbackData()
      }
    },
    // 处理资源内排序结束
    async handleLocalSortEnd(evt) {
      const {oldIndex, newIndex} = evt

      // 如果位置没变，直接返回
      if (oldIndex === newIndex) return

      const movedTask = this.displayedTasks[oldIndex]

      if (!movedTask || movedTask.status !== 1) {
        this.rollbackData()
        return
      }

      // 获取当前资源内任务的新顺序
      const newOrderList = [...this.displayedTasks]
      const [removed] = newOrderList.splice(oldIndex, 1)
      newOrderList.splice(newIndex, 0, removed)

      // 提取任务ID列表
      const taskIds = newOrderList.map(t => t.id)

      try {
        const query = {
          robotId: this.resourceFilter.resourceId,
          isGroupId: this.resourceFilter.isGroupTask
        }
        await updatePendingOrder(query, taskIds)
        this.$message.success('资源内排序已保存')
        // 更新备份数据
        this.backupOriginalData()
        // 刷新列表获取最新顺序
        await this.getLocalPendingTasks()
      } catch (error) {
        // 后端校验失败或网络错误，回滚并提示
        this.$message.error(error.response?.data?.msg || '保存资源内排序失败，任务已恢复原位置')
        this.rollbackData()
      }
    },
    // 状态操作
    async pauseTask(row) {
      try {
        await pauseTaskApi(Number(row.id))
        this.$message.success('已暂停')
        this.refreshTasks()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async continueTask(row) {
      try {
        await continueTask(Number(row.id))
        this.$message.success('已继续')
        this.refreshTasks()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async cancelTask(row) {
      try {
        await cancelTaskApi(Number(row.id))
        this.$message.success('已取消')
        this.refreshTasks()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async disableTask(row) {
      try {
        await banTask(Number(row.id))
        this.$message.success('已禁用')
        this.refreshTasks()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async enableTask(row) {
      try {
        await resumeTask(Number(row.id))
        this.$message.success('已恢复')
        this.refreshTasks()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async stopTask(row) {
      try {
        const {value: reason} = await this.$prompt('请输入停止原因', '停止任务', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '停止原因'
        })
        await terminateTask(Number(row.id), reason)
        this.$message.success('已停止')
        this.refreshTasks()
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败')
      }
    },
    // 查看详情
    async handleView(row) {
      const id = Number(row.id)
      if (isNaN(id)) {
        this.$message.error('无效的任务ID')
        return
      }

      this.detailLoading = true
      this.detailVisible = true
      try {
        const [taskRes, stepsRes, logsRes] = await Promise.all([
          getTask(id),
          getTaskSteps(id),
          listLogByTask(id)
        ])

        this.currentTask = taskRes.data

        // 解析表单字段定义
        if (this.currentTask.templateId && this.templateOptions) {
          const template = this.templateOptions.find(t => t.id === this.currentTask.templateId)
          if (template && template.formContent) {
            try {
              const content = JSON.parse(template.formContent)
              this.formFields = content.fields || []
            } catch (e) {
              this.formFields = []
            }
          } else {
            this.formFields = []
          }
        } else {
          this.formFields = []
        }

        this.taskSteps = stepsRes.data || []
        this.taskLogs = logsRes.rows || []
      } catch (error) {
        this.$message.error('获取任务详情失败')
        this.detailVisible = false
      } finally {
        this.detailLoading = false
      }
    },
    // 辅助函数
    getStatusText(status) {
      const map = {3: '未开始', 1: '准备中', 0: '执行中', 2: '已暂停', 6: '已完成', 4: '已禁用', 5: '已终止'}
      return map[status] || status
    },
    getStatusClass(status) {
      const map = {3: 'pending', 1: 'ready', 0: 'executing', 2: 'paused', 6: 'completed', 4: 'disabled', 5: 'aborted'}
      return map[status] || 'pending'
    },
    getTriggerTypeText(type) {
      const map = {1: '定时任务', 2: '电量任务', 3: '闲时任务'}
      return map[type] || type
    },
    getStepStatusText(status) {
      const map = {0: '未开始', 1: '执行中', 2: '已完成', 3: '已暂停', 4: '已终止'}
      return map[status] || status
    },
    getStepStatusClass(status) {
      const map = {0: 'pending', 1: 'executing', 2: 'completed', 3: 'paused', 4: 'aborted'}
      return map[status] || 'pending'
    },
    formatFormValue(field, value) {
      if (value === null || value === undefined) return '-'
      if (Array.isArray(value)) {
        return value.map(v => v.name || v).join(', ')
      }
      return String(value)
    },
    handleRowClick(row) {
      // 可添加点击行逻辑
    }
  }
}
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
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

.stat-icon.blue {
  background-color: #e6f7ff;
  color: #1890ff;
}

.stat-icon.green {
  background-color: #f6ffed;
  color: #52c41a;
}

.stat-icon.orange {
  background-color: #fff7e6;
  color: #fa8c16;
}

.stat-icon.red {
  background-color: #fff1f0;
  color: #ff4d4f;
}

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

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-ready {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-executing {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-paused {
  background-color: #fffbe6;
  color: #faad14;
}

.status-completed {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-disabled {
  background-color: #f5f5f5;
  color: #999;
}

.status-aborted {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}

/* 拖拽手柄样式 */
.drag-handle {
  cursor: move;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.drag-handle.drag-enabled {
  color: #1890ff;
  background-color: #e6f7ff;
}

.drag-handle.drag-enabled:hover {
  background-color: #bae7ff;
}

.drag-handle i {
  font-size: 16px;
}

.queue-position {
  font-size: 12px;
  font-weight: bold;
  color: #666;
  min-width: 20px;
  text-align: center;
}

/* 步骤进度样式 */
.step-progress {
  display: flex;
  align-items: center;
  gap: 4px;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #e0e0e0;
}

.step-dot.completed {
  background-color: #52c41a;
}

.step-dot.executing {
  background-color: #1890ff;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 资源分组背景色（全局排序模式下） */
.row-resource-1 {
  background-color: rgba(24, 144, 255, 0.05);
}

.row-resource-2 {
  background-color: rgba(82, 196, 26, 0.05);
}

.row-resource-3 {
  background-color: rgba(250, 140, 22, 0.05);
}

.row-resource-4 {
  background-color: rgba(255, 77, 79, 0.05);
}

.row-resource-5 {
  background-color: rgba(114, 46, 209, 0.05);
}

/* 操作按钮样式 */
.el-button [class*="fas"] {
  font-size: 14px;
}

.el-table .cell .el-button + .el-button {
  margin-left: 4px;
}

/* 拖拽时的视觉反馈 */
.sortable-drag .drag-handle {
  background-color: #1890ff;
  color: white;
}

.sortable-drag .queue-position {
  color: white;
}
</style>

<style>
/* 全局 Sortable 样式 */
.sortable-ghost {
  opacity: 0.4;
  background-color: #e6f7ff !important;
  border: 2px dashed #1890ff !important;
}

.sortable-drag {
  opacity: 0.9;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 禁止放置时的样式 */
.sortable-invalid {
  cursor: not-allowed !important;
  opacity: 0.5;
}
</style>
