<<template>
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
        <!-- 关键改动 1：value 改为字符串，避免对象引用比较问题 -->
        <el-select
          v-if="sortMode === 'local'"
          v-model="resourceFilter.selectedResource"
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
              :value="'robot-' + r.id"
            />
          </el-option-group>
          <el-option-group label="机器人组">
            <el-option
              v-for="g in robotGroupOptions"
              :key="'group-' + g.id"
              :label="g.name"
              :value="'group-' + g.id"
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
              <span style="margin-left:8px;">{{ scope.row.completedSteps || 0 }}/{{ scope.row.totalSteps || 0 }}</span>
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
          <el-descriptions-item label="任务时长">{{ formatDuration(currentTask.duration) }}分钟</el-descriptions-item>
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
          <el-table-column label="参数详情" min-width="200">
            <template slot-scope="scope">
              <pre v-if="scope.row.operationJsonDisplay" style="margin:0; font-size:12px; background:#f5f7fa; padding:8px; border-radius:4px;">{{ scope.row.operationJsonDisplay }}</pre>
              <span v-else>-</span>
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
  updateGlobalOrder,
  listTemplate, getDynamicParams
} from '@/api/taskmgt/taskmgt'
import {listRobots} from '@/api/system/robots'
import { listGroups } from '@/api/system/robots'

export default {
  name: 'TaskSchedule',
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        robotId: undefined,
        status: undefined,
        displayOrder: 'status ASC, pending_order ASC, priority DESC, create_time DESC'
      },
      filter: {
        robotId: undefined,
        status: undefined
      },
      resourceFilter: {
        selectedResource: null   // 现在存储字符串，如 'robot-1' 或 'group-1'
      },
      sortMode: 'none',
      loading: false,
      taskList: [],
      total: 0,
      robotOptions: [],
      robotGroupOptions: [],
      templateOptions: [],
      tableKey: 0,
      sortableInstance: null,
      originalTaskList: [],
      detailVisible: false,
      detailLoading: false,
      currentTask: null,
      taskSteps: [],
      taskLogs: [],
      formFields: [],
      dynamicOptionsCache: {}
    }
  },
  computed: {
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
    showSortHandle() {
      return this.sortMode !== 'none'
    },
    isSortableEnabled() {
      return this.sortMode === 'global' || (this.sortMode === 'local' && this.resourceFilter.selectedResource)
    },
    // 关键改动 2：添加计算属性，将字符串解析为对象
    selectedResourceObj() {
      if (!this.resourceFilter.selectedResource) return null
      const val = this.resourceFilter.selectedResource
      if (val.startsWith('robot-')) {
        return { type: 'robot', id: Number(val.substring(6)) }
      } else if (val.startsWith('group-')) {
        return { type: 'group', id: Number(val.substring(6)) }
      }
      return null
    },
    displayedTasks() {
      if (this.sortMode === 'none') {
        return this.taskList
      } else if (this.sortMode === 'global') {
        return this.taskList
          .filter(t => t.status === 1)
          .sort((a, b) => {
            const globalOrderDiff = (a.globalPendingOrder || 0) - (b.globalPendingOrder || 0)
            if (globalOrderDiff !== 0) return globalOrderDiff
            return (a.pendingOrder || 0) - (b.pendingOrder || 0)
          })
      } else if (this.sortMode === 'local') {
        // 关键改动 3：使用 selectedResourceObj 替代 resourceFilter.selectedResource
        if (!this.selectedResourceObj) {
          return []
        }
        return this.taskList
          .filter(t => {
            if (t.status !== 1) return false
            if (this.selectedResourceObj.type === 'group') {
              return t.robotGroupId === this.selectedResourceObj.id && t.isGroupTask === 1
            } else {
              return t.robotId === this.selectedResourceObj.id && t.isGroupTask !== 1
            }
          })
          .sort((a, b) => (a.pendingOrder || 0) - (b.pendingOrder || 0))
      }
      return this.taskList
    }
  },
  watch: {
    taskList: {
      handler() {
        this.$nextTick(() => {
          this.initSortable()
        })
      },
      deep: true
    },
    sortMode(val) {
      if (val === 'none') {
        this.destroySortable()
        this.getList()
      } else if (val === 'global') {
        this.destroySortable()
        this.getAllPendingTasks()
      } else if (val === 'local') {
        this.destroySortable()
        this.resourceFilter.selectedResource = null
        this.taskList = []
      }
    },
    'resourceFilter.selectedResource'(val) {
      if (this.sortMode === 'local' && val) {
        this.getLocalPendingTasks()
      }
    }
  },
  created() {
    this.getRobotList()
    this.getRobotGroupList()
    this.getTemplates()
    this.getList()
  },
  beforeDestroy() {
    this.destroySortable()
  },
  methods: {
    async getRobotList() {
      try {
        const res = await listRobots({pageSize: 1000})
        this.robotOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人列表失败')
        console.error(error)
      }
    },
    async getRobotGroupList() {
      try {
        const res = await listGroups({pageSize: 1000})
        this.robotGroupOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人组列表失败')
        console.error(error)
      }
    },
    async getTemplates() {
      try {
        const res = await listTemplate({ pageSize: 100 })
        this.templateOptions = (res.rows || []).map(tpl => {
          let fields = []
          try {
            if (tpl.formContent) {
              const content = JSON.parse(tpl.formContent)
              fields = content.fields || []
            }
          } catch (e) {
            console.warn('解析模板内容失败', e)
          }
          return { ...tpl, fields }
        })
      } catch (error) {
        console.error('获取模板列表失败', error)
      }
    },
    async loadDynamicOptions(task) {
      if (!task || !task.robotId || !task.templateId) return
      const template = this.templateOptions.find(t => t.id === task.templateId)
      if (!template || !template.fields) return
      const dynamicFields = template.fields.filter(f => f.type === 'dynamicSelect')
      for (const field of dynamicFields) {
        if (!field.apiId) continue
        try {
          const res = await getDynamicParams(field.apiId, { robotId: task.robotId })
          const params = res.data || []
          const targetParam = params.find(p => p.paramKey === field.paramKey)
          if (targetParam && targetParam.options) {
            this.dynamicOptionsCache[field.paramKey] = targetParam.options
          }
        } catch (e) {
          console.warn('加载动态参数失败', e)
        }
      }
    },
    async getList() {
      this.loading = true
      try {
        const params = {...this.queryParams}
        const res = await listTask(params)
        this.taskList = res.rows || []
        this.total = res.total || 0
        this.backupOriginalData()
      } catch (error) {
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },
    async getAllPendingTasks() {
      this.loading = true
      try {
        const params = {
          pageNum: 1,
          pageSize: 9999,
          status: 1,
          displayOrder: 'global_pending_order ASC, pending_order ASC'
        }
        const res = await listTask(params)
        this.taskList = res.rows || []
        this.total = res.total || 0
        this.backupOriginalData()
      } catch (error) {
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },
    async getLocalPendingTasks() {
      // 关键改动 4：使用 selectedResourceObj
      if (!this.selectedResourceObj) return

      this.loading = true
      try {
        const params = {
          pageNum: 1,
          pageSize: 9999,
          status: 1,
          displayOrder: 'pending_order ASC'
        }

        if (this.selectedResourceObj.type === 'group') {
          params.robotGroupId = this.selectedResourceObj.id
          params.isGroupTask = 1
        } else {
          params.robotId = this.selectedResourceObj.id
          params.isGroupTask = 0
        }

        const res = await listTask(params)
        this.taskList = res.rows || []
        this.total = res.total || 0
        this.backupOriginalData()
      } catch (error) {
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },
    backupOriginalData() {
      this.originalTaskList = JSON.parse(JSON.stringify(this.taskList))
    },
    rollbackData() {
      this.taskList = JSON.parse(JSON.stringify(this.originalTaskList))
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
    onSortModeChange(val) {
      this.sortMode = val
      this.resourceFilter.selectedResource = null
      this.filter.status = undefined
      this.queryParams.status = undefined
    },
    onResourceFilterChange(selected) {
      // 已通过 watch 触发，此处无需额外逻辑
    },
    onStatusFilterChange(val) {
      this.filter.status = val
      this.queryParams.status = val !== undefined ? val : undefined
      this.queryParams.pageNum = 1
      this.getList()
    },
    getRowClassName({row}) {
      if (this.sortMode === 'global' && row.status === 1) {
        const resourceKey = row.isGroupTask === 1
          ? `group-${row.robotGroupId}`
          : `robot-${row.robotId}`
        const colors = ['row-resource-1', 'row-resource-2', 'row-resource-3', 'row-resource-4', 'row-resource-5']
        let hash = 0
        for (let i = 0; i < resourceKey.length; i++) {
          hash = resourceKey.charCodeAt(i) + ((hash << 5) - hash)
        }
        return colors[Math.abs(hash) % colors.length]
      }
      return ''
    },
    getResourceKey(task) {
      if (!task) return null
      if (task.isGroupTask === 1) {
        return `group-${task.robotGroupId}`
      } else {
        return `robot-${task.robotId}`
      }
    },
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
    handleSortMove(evt, originalEvent) {
      const {dragged, related} = evt
      const draggedIndex = Array.from(dragged.parentNode.children).indexOf(dragged)
      const relatedIndex = Array.from(related.parentNode.children).indexOf(related)

      const draggedTask = this.displayedTasks[draggedIndex]
      const relatedTask = this.displayedTasks[relatedIndex]

      if (!draggedTask || !relatedTask) return false

      if (this.sortMode === 'global') {
        const draggedResource = this.getResourceKey(draggedTask)
        const relatedResource = this.getResourceKey(relatedTask)
        if (draggedResource === relatedResource) {
          return false
        }
      }

      return true
    },
    async handleGlobalSortEnd(evt) {
      const {oldIndex, newIndex} = evt
      if (oldIndex === newIndex) return

      const movedTask = this.displayedTasks[oldIndex]
      const targetTask = this.displayedTasks[newIndex]

      if (!movedTask || movedTask.status !== 1) {
        this.rollbackData()
        return
      }

      const newOrderList = [...this.displayedTasks]
      const [removed] = newOrderList.splice(oldIndex, 1)
      newOrderList.splice(newIndex, 0, removed)

      const resourceGroups = {}
      newOrderList.forEach(task => {
        const key = this.getResourceKey(task)
        if (!resourceGroups[key]) {
          resourceGroups[key] = []
        }
        resourceGroups[key].push(task)
      })

      for (const key in resourceGroups) {
        const groupTasks = resourceGroups[key]
        const currentOrderIds = groupTasks.map(t => t.id)
        const sortedByPendingOrder = [...groupTasks]
          .sort((a, b) => (a.pendingOrder || 0) - (b.pendingOrder || 0))
          .map(t => t.id)

        if (JSON.stringify(currentOrderIds) !== JSON.stringify(sortedByPendingOrder)) {
          this.$message.error('全局排序不允许改变资源内的相对顺序，请使用"资源内排序"模式')
          this.rollbackData()
          return
        }
      }

      const taskIds = newOrderList.map(t => t.id)

      try {
        await updateGlobalOrder({}, taskIds)
        this.$message.success('全局排序已保存')
        this.backupOriginalData()
        await this.getAllPendingTasks()
      } catch (error) {
        this.$message.error(error.response?.data?.msg || '保存全局排序失败，任务已恢复原位置')
        this.rollbackData()
      }
    },
    async handleLocalSortEnd(evt) {
      const {oldIndex, newIndex} = evt
      if (oldIndex === newIndex) return

      const movedTask = this.displayedTasks[oldIndex]

      if (!movedTask || movedTask.status !== 1) {
        this.rollbackData()
        return
      }

      const newOrderList = [...this.displayedTasks]
      const [removed] = newOrderList.splice(oldIndex, 1)
      newOrderList.splice(newIndex, 0, removed)

      const taskIds = newOrderList.map(t => t.id)

      try {
        // 关键改动 5：使用 selectedResourceObj
        const query = {
          robotId: this.selectedResourceObj.id,
          isGroupId: this.selectedResourceObj.type === 'group'
        }
        await updatePendingOrder(query, taskIds)
        this.$message.success('资源内排序已保存')
        this.backupOriginalData()
        await this.getLocalPendingTasks()
      } catch (error) {
        this.$message.error(error.response?.data?.msg || '保存资源内排序失败，任务已恢复原位置')
        this.rollbackData()
      }
    },
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
          listLogByTask({taskId:id})
        ])

        this.currentTask = taskRes.data

        if (this.currentTask.formContent) {
          try {
            this.currentTask.formData = JSON.parse(this.currentTask.formContent)
          } catch (e) {
            this.currentTask.formData = {}
          }
        } else {
          this.currentTask.formData = {}
        }

        await this.loadDynamicOptions(this.currentTask)

        if (this.currentTask.templateId && this.templateOptions) {
          const template = this.templateOptions.find(t => t.id === this.currentTask.templateId)
          if (template && template.fields) {
            this.formFields = template.fields || []
            this.formFields.forEach(field => {
              if (['image','video','audio','file'].includes(field.type)) {
                const value = this.currentTask.formData[field.id]
                if (value && typeof value === 'string') {
                  this.currentTask.formData[field.id] = [{ name: '已上传文件', url: value }]
                } else if (!value || !Array.isArray(value)) {
                  this.currentTask.formData[field.id] = []
                }
              }
            })
          } else {
            this.formFields = []
          }
        } else {
          this.formFields = []
        }

        const stepsData = stepsRes.data || []
        for (let step of stepsData) {
          if (step.operationJson) {
            try {
              const json = JSON.parse(step.operationJson)
              for (let key in json) {
                if (this.dynamicOptionsCache[key]) {
                  const opt = this.dynamicOptionsCache[key].find(o => o.value === String(json[key]))
                  if (opt) json[key] = opt.label
                }
              }
              step.operationJsonDisplay = JSON.stringify(json, null, 2)
            } catch (e) {}
          }
        }
        this.taskSteps = stepsData.map(step => ({
          ...step,
          orderNum: step.orderNum || 0,
          stepName: step.stepName || step.name || '未命名步骤',
          description: step.description || '-',
          status: step.status || 0
        })).sort((a, b) => a.orderNum - b.orderNum)

        this.taskLogs = logsRes.rows || []
      } catch (error) {
        this.$message.error('获取任务详情失败')
        this.detailVisible = false
      } finally {
        this.detailLoading = false
      }
    },
    formatDuration(seconds) {
      if (seconds == null || isNaN(seconds) || seconds < 0) return '0秒';
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      const secs = seconds % 60;
      const parts = [];
      if (hours > 0) parts.push(`${hours}小时`);
      if (minutes > 0) parts.push(`${minutes}分钟`);
      if (secs > 0 || parts.length === 0) parts.push(`${secs}秒`);
      return parts.join('');
    },
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
      if (field.type === 'dynamicSelect' && this.dynamicOptionsCache[field.paramKey]) {
        const opt = this.dynamicOptionsCache[field.paramKey].find(o => String(o.value) === String(value))
        if (opt) return opt.label
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

.el-button [class*="fas"] {
  font-size: 14px;
}

.el-table .cell .el-button + .el-button {
  margin-left: 4px;
}

.sortable-drag .drag-handle {
  background-color: #1890ff;
  color: white;
}

.sortable-drag .queue-position {
  color: white;
}
</style>

<style>
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

.sortable-invalid {
  cursor: not-allowed !important;
  opacity: 0.5;
}
</style>
