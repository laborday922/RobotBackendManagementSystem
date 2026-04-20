<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="90px">
        <el-form-item label="任务名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入任务名称"
            clearable
            style="width: 180px"
            @input="debouncedQuery"
            @clear="debouncedQuery"
          />
        </el-form-item>
        <el-form-item label="任务状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="handleQuery"
            @clear="handleQuery"
          >
            <el-option label="未开始" :value="3" />
            <el-option label="准备中" :value="1" />
            <el-option label="执行中" :value="0" />
            <el-option label="已暂停" :value="2" />
            <el-option label="已完成" :value="6" />
            <el-option label="已禁用" :value="4" />
            <el-option label="已终止" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select
            v-model="queryParams.taskType"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="handleQuery"
            @clear="handleQuery"
          >
            <el-option label="定时任务" :value="1" />
            <el-option label="电量任务" :value="2" />
            <el-option label="闲时任务" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="机器人" prop="robotId">
          <el-select
            v-model="queryParams.robotId"
            placeholder="请选择机器人"
            clearable
            style="width: 150px"
            @change="handleQuery"
            @clear="handleQuery"
          >
            <el-option
              v-for="item in robotOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="机器人组" prop="robotGroupId">
          <el-select
            v-model="queryParams.robotGroupId"
            placeholder="请选择组"
            clearable
            style="width: 150px"
            @change="handleQuery"
            @clear="handleQuery"
          >
            <el-option
              v-for="item in robotGroupOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">创建任务</el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="taskList" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="任务名称" prop="name" min-width="150" show-overflow-tooltip />
        <el-table-column label="模板" prop="templateName" width="140" show-overflow-tooltip />
        <el-table-column label="触发方式" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getTriggerTypeTag(scope.row.taskType)">
              {{ getTriggerTypeText(scope.row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否是组任务" width="100" align="center">
          <template slot-scope="scope">
            {{ scope.row.isGroupTask === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="机器人组名称" prop="robotGroupName" min-width="120" show-overflow-tooltip/>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <span :class="'status-tag status-' + getStatusClass(scope.row.status)">{{ getStatusText(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="机器人" prop="robotName" width="120" show-overflow-tooltip />
        <el-table-column label="优先级" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.priority === 1" type="danger">高</el-tag>
            <el-tag v-else-if="scope.row.priority === 2" type="warning">中</el-tag>
            <el-tag v-else type="info">低</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <!-- 操作列 -->
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template slot-scope="scope">
            <!-- 详情 -->
            <el-button size="small" circle title="详情" @click="handleView(scope.row)">
              <i class="fas fa-eye"></i>
            </el-button>

            <!-- 编辑 -->
            <el-button
              v-if="canEdit(scope.row)"
              size="small"
              type="primary"
              circle
              title="编辑"
              @click="handleEdit(scope.row)"
            >
              <i class="fas fa-edit"></i>
            </el-button>

            <!-- 删除 -->
            <el-button
              v-if="canDelete(scope.row)"
              size="small"
              type="danger"
              circle
              title="删除"
              @click="handleDelete(scope.row)"
            >
              <i class="fas fa-trash-alt"></i>
            </el-button>

            <!-- 禁用 -->
            <el-button
              v-if="scope.row.status === 3"
              size="small"
              type="warning"
              circle
              title="禁用"
              @click="handleBan(scope.row)"
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
              @click="handleResume(scope.row)"
            >
              <i class="fas fa-undo"></i>
            </el-button>

            <!-- 暂停 -->
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="warning"
              circle
              title="暂停"
              @click="handlePause(scope.row)"
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
              @click="handleContinue(scope.row)"
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
              @click="handleCancel(scope.row)"
            >
              <i class="fas fa-times"></i>
            </el-button>

            <!-- 终止 -->
            <el-button
              v-if="[0,2].includes(scope.row.status)"
              size="small"
              type="danger"
              circle
              title="终止"
              @click="handleTerminate(scope.row)"
            >
              <i class="fas fa-power-off"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 任务创建/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      :visible.sync="dialog.visible"
      width="850px"
      append-to-body
      @close="cancelTaskDialog"
    >
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="130px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>

        <el-form-item label="选择模板" prop="templateId" required>
          <el-select
            v-model="taskForm.templateId"
            placeholder="请选择模板"
            style="width:100%"
            @change="onTemplateChange"
            :disabled="dialog.mode === 'edit'"
          >
            <el-option
              v-for="item in templateOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 动态表单字段（根据模板渲染） -->
        <template v-if="taskForm.templateId && currentTemplate">
          <el-divider content-position="left">填写表单字段</el-divider>
          <div class="dynamic-form-container">
            <el-form-item
              v-for="field in currentTemplate.fields"
              :key="field.id"
              :label="field.label"
              :required="field.required"
            >
              <!-- 普通文本/数字 -->
              <el-input
                v-if="field.type === 'text' || field.type === 'number'"
                v-model="taskForm.formData[field.id]"
                :type="field.type === 'number' ? 'number' : 'text'"
                :placeholder="`请输入${field.label}`"
              />
              <!-- 日期 -->
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="taskForm.formData[field.id]"
                type="date"
                placeholder="选择日期"
                style="width:100%"
              />
              <!-- 时间 -->
              <el-time-picker
                v-else-if="field.type === 'time'"
                v-model="taskForm.formData[field.id]"
                placeholder="选择时间"
                style="width:100%"
              />
              <!-- 下拉选择 -->
              <el-select
                v-else-if="field.type === 'select'"
                v-model="taskForm.formData[field.id]"
                placeholder="请选择"
                style="width:100%"
              >
                <el-option
                  v-for="opt in field.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
              <!-- 动态下拉（如位置） -->
              <el-select
                v-else-if="field.type === 'dynamicSelect'"
                v-model="taskForm.formData[field.id]"
                :placeholder="`请选择${field.label}`"
                style="width:100%"
                :loading="field.loading"
                @focus="() => loadFieldOptions(field)"
                clearable
              >
                <el-option
                  v-for="opt in field.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
              <!-- 文件上传（图片/视频等） -->
              <el-upload
                v-else-if="['image','video','audio','file'].includes(field.type)"
                :action="uploadAction"
                :headers="uploadHeaders"
                :file-list="taskForm.formData[field.id] || []"
                :on-success="(res, file) => handleUploadSuccess(field.id, res, file)"
                :before-upload="(file) => beforeUpload(file, field)"
                :list-type="field.type === 'image' ? 'picture-card' : 'text'"
                :multiple="field.maxCount > 1"
                :limit="field.maxCount"
                :accept="getAcceptByType(field.type)"
              >
                <i class="el-icon-plus"></i>
              </el-upload>
              <!-- 位置类型（地图选点，简化处理） -->
              <el-input
                v-else-if="field.type === 'location'"
                v-model="taskForm.formData[field.id]"
                placeholder="经纬度或位置描述"
              />
            </el-form-item>
          </div>
        </template>

        <!-- 步骤预览 -->
        <step-preview
          :steps="generatedSteps"
          :is-group-task="taskForm.isGroupTask"
          :available-robots="availableRobotsForGroup"
          @robot-change="onStepRobotChange"
          v-if="generatedSteps.length > 0"
        />

        <el-divider content-position="left">任务设置</el-divider>
        <el-form-item label="任务优先级" prop="priority">
          <el-radio-group v-model="taskForm.priority">
            <el-radio :label="1">高</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">低</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="是否为组任务">
          <el-switch v-model="taskForm.isGroupTask" @change="onGroupTaskChange"/>
          <span class="el-form-item-msg">组任务可由多个机器人协同完成</span>
        </el-form-item>

        <el-form-item label="任务时长(分钟)" prop="duration">
          <el-input-number v-model="taskForm.duration" :min="1" :max="480" style="width:100%" />
        </el-form-item>

        <!-- 触发类型 -->
        <el-form-item label="任务触发类型" prop="taskType" required>
          <el-radio-group v-model="taskForm.taskType">
            <el-radio :label="1">定时任务</el-radio>
            <el-radio :label="2">电量任务</el-radio>
            <el-radio :label="3">闲时任务</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 定时任务设置 -->
        <template v-if="taskForm.taskType === 1">
          <el-form-item label="Cron表达式" prop="cronExpression">
            <el-input v-model="taskForm.cronExpression" placeholder="如：0 0 9 * * ?" />
            <div class="el-form-item-msg">留空表示立即执行</div>
          </el-form-item>
          <el-form-item label="定时开始时间" prop="scheduledTime">
            <el-date-picker
              v-model="taskForm.scheduledTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width:100%"
            />
          </el-form-item>
        </template>

        <!-- 电量任务设置 -->
        <template v-if="taskForm.taskType === 2">
          <el-form-item label="触发电量(%)" prop="batteryThreshold" required>
            <el-slider v-model="taskForm.batteryThreshold" :max="100" show-input />
          </el-form-item>
        </template>

        <!-- 闲时任务设置 -->
        <template v-if="taskForm.taskType === 3">
          <el-form-item label="闲时等待(分钟)" prop="idleTime" required>
            <el-input-number v-model="taskForm.idleTime" :min="1" :max="120" style="width:100%" />
          </el-form-item>
        </template>

        <!-- 机器人/机器人组选择 -->
        <el-form-item
          :label="taskForm.isGroupTask ? '选择机器人组' : '选择机器人'"
          required
        >
          <template v-if="taskForm.isGroupTask">
            <el-select v-model="taskForm.robotGroupId" placeholder="请选择机器人组" style="width:100%" @change="onRobotGroupChange">
              <el-option
                v-for="item in robotGroupOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </template>
          <template v-else>
            <el-select v-model="taskForm.robotId" placeholder="请选择机器人" style="width:100%" @change="onRobotChange">
              <el-option
                v-for="item in availableRobots"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <div style="display: flex; justify-content: space-between;">
                  <span>{{ item.name }}</span>
                  <span style="color: #999; font-size: 12px;">
                    电量{{ item.battery }}% |
                    {{ formatRobotStatus(item) }}
                  </span>
                </div>
              </el-option>
            </el-select>
          </template>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitTask" :loading="dialog.loading">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog
      title="任务详情"
      :visible.sync="detailDialog.visible"
      width="800px"
      append-to-body
    >
      <task-detail
        :task="currentTask"
        :loading="detailLoading"
        :form-fields="formFields"
        :task-steps="taskSteps"
        :task-logs="taskLogs"
        :operation-list="operationList"
        :dynamic-options-cache="dynamicOptionsCache"
        @download="downloadFile"
      />
    </el-dialog>

    <!-- 视频预览弹窗 -->
    <el-dialog :visible.sync="videoPreview.visible" width="60%" title="视频预览" append-to-body>
      <video
        v-if="videoPreview.url"
        :src="videoPreview.url"
        controls
        style="width: 100%; max-height: 600px;"
      ></video>
    </el-dialog>
  </div>
</template>

<script>
import {
  addTask,
  updateTask,
  delTask,
  getTask,
  listTask,
  listTemplate,
  pauseTask,
  resumeTask,
  banTask,
  cancelTask,
  continueTask,
  terminateTask,
  getTaskSteps,
  listLogByTask,
  updateTaskSteps, getDynamicParams
} from '@/api/taskmgt/taskmgt'
import { listRobots, listGroups } from '@/api/system/robots'
import { getToken } from '@/utils/auth'
import debounce from 'lodash/debounce'
import StepPreview from './components/StepPreview'
import TaskDetail from './components/TaskDetail'

export default {
  name: 'TaskList',
  components: {
    StepPreview,
    TaskDetail
  },
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        status: undefined,
        taskType: undefined,
        robotId: undefined,
        robotGroupId: undefined,
        displayOrder: 'status ASC, pending_order ASC, priority DESC, create_time DESC'
      },
      loading: false,
      taskList: [],
      total: 0,
      robotOptions: [],
      robotGroupOptions: [],
      templateOptions: [],
      uploadAction: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + getToken()
      },
      dialog: {
        visible: false,
        title: '',
        mode: 'create',
        loading: false
      },
      taskForm: this.getInitialTaskForm(),
      taskRules: {
        name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
        templateId: [{ required: true, message: '请选择模板', trigger: 'change' }],
        batteryThreshold: [{ required: true, message: '请输入触发电量', trigger: 'blur' }],
        idleTime: [{ required: true, message: '请输入闲时等待时间', trigger: 'blur' }],
      },
      detailDialog: {
        visible: false
      },
      detailLoading: false,
      currentTask: null,
      taskSteps: [],
      taskLogs: [],
      formFields: [],
      operationList: [],
      generatedSteps: [],
      videoPreview: {
        visible: false,
        url: ''
      },
      // 动态选项缓存 { paramKey: { options: [], loading: false } }
      dynamicOptionsCache: {},
      debouncedQuery: null
    }
  },
  computed: {
    availableRobotsForGroup() {
      if (!this.currentTemplate || !this.taskForm.isGroupTask) return []
      const groupId = this.taskForm.robotGroupId
      return this.robotOptions.filter(robot =>
        robot.groupId === groupId &&
        robot.status === 1 &&
        robot.hardwareStatus === 0 &&
        robot.battery > 20
      )
    },
    availableRobots() {
      if (!this.currentTemplate || !this.currentTemplate.robotGroupIds || this.currentTemplate.robotGroupIds.length === 0) {
        return []
      }
      const allowedGroupIds = this.currentTemplate.robotGroupIds.map(id => Number(id))
      return this.robotOptions.filter(robot => {
        const isHealthy = robot.status === 1 && robot.hardwareStatus === 0
        const inAllowedGroup = allowedGroupIds.includes(Number(robot.groupId))
        return isHealthy && inAllowedGroup
      }).sort((a, b) => {
        const aHealthy = (a.status === 1 && a.hardwareStatus === 0) ? 1 : 0
        const bHealthy = (b.status === 1 && b.hardwareStatus === 0) ? 1 : 0
        return bHealthy - aHealthy
      })
    },
    currentTemplate() {
      if (!this.taskForm.templateId) return null
      return this.templateOptions.find(t => t.id === this.taskForm.templateId)
    }
  },
  watch: {
    'taskForm.robotGroupId': {
      handler(newVal, oldVal) {
        if (this.taskForm.isGroupTask && newVal !== oldVal && this.generatedSteps.length) {
          this.generatedSteps.forEach(step => {
            step.assignedRobotId = null
          })
          if (oldVal !== undefined) {
            this.$message.info('机器人组已变更，已清空步骤机器人分配，请重新分配')
          }
        }
      }
    }
  },
  created() {
    this.debouncedQuery = debounce(this.handleQuery, 500)
    this.getRobotData()
    this.getRobotGroups()
    this.getTemplates()
    this.getList()
  },
  beforeDestroy() {
    this.debouncedQuery.cancel()
  },
  methods: {
    getInitialTaskForm() {
      return {
        id: undefined,
        name: '',
        templateId: undefined,
        formData: {},
        priority: 2,
        isGroupTask: false,
        duration: 30,
        taskType: 1,
        cronExpression: '',
        scheduledTime: undefined,
        batteryThreshold: 80,
        idleTime: 30,
        robotId: undefined,
        robotGroupId: undefined,
      }
    },
    formatRobotStatus(robot) {
      if (robot.status === 1 && robot.hardwareStatus === 0) return '在线正常'
      if (robot.status === 1 && robot.hardwareStatus === 1) return '硬件警告'
      if (robot.status === 1 && robot.hardwareStatus === 2) return '硬件故障'
      if (robot.status === 0) return '离线'
      if (robot.status === 2) return '待激活'
      return '未知'
    },
    async getRobotData() {
      try {
        const res = await listRobots({ pageSize: 1000 })
        this.robotOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人列表失败')
      }
    },
    async getRobotGroups() {
      try {
        const res = await listGroups()
        this.robotGroupOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人组失败')
      }
    },
    async getTemplates() {
      try {
        const res = await listTemplate({ pageSize: 100 })
        this.templateOptions = (res.rows || []).map(tpl => {
          let fields = [], steps = []
          try {
            if (tpl.formContent) {
              const content = JSON.parse(tpl.formContent)
              fields = (content.fields || []).map(f => {
                // 为动态下拉字段初始化 options 和 loading
                if (f.type === 'dynamicSelect') {
                  f.options = []
                  f.loading = false
                }
                return f
              })
            }
            if (tpl.workflow) {
              const wf = JSON.parse(tpl.workflow)
              steps = wf.steps || []
            }
          } catch (e) {
            console.warn('解析模板内容失败', e)
          }
          let robotGroupIds = []
          if (tpl.robotGroupIds) {
            if (Array.isArray(tpl.robotGroupIds)) {
              robotGroupIds = tpl.robotGroupIds
            } else if (typeof tpl.robotGroupIds === 'string') {
              robotGroupIds = tpl.robotGroupIds.split(',').map(s => Number(s.trim()))
            }
          }
          return { ...tpl, fields, steps, robotGroupIds }
        })
      } catch (error) {
        this.$message.error('获取模板列表失败')
      }
    },
    async getList() {
      this.loading = true
      try {
        const res = await listTask(this.queryParams)
        this.taskList = res.rows || []
        this.total = res.total || 0
      } catch (error) {
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    getStatusText(status) {
      const map = { 3: '未开始', 1: '准备中', 0: '执行中', 2: '已暂停', 6: '已完成', 4: '已禁用', 5: '已终止' }
      return map[status] || status
    },
    getStatusClass(status) {
      const map = { 3: 'pending', 1: 'ready', 0: 'executing', 2: 'paused', 6: 'completed', 4: 'disabled', 5: 'aborted' }
      return map[status] || 'pending'
    },
    getTriggerTypeText(type) {
      const map = { 1: '定时任务', 2: '电量任务', 3: '闲时任务' }
      return map[type] || type
    },
    getTriggerTypeTag(type) {
      const map = { 1: 'primary', 2: 'success', 3: 'warning' }
      return map[type] || 'info'
    },
    canEdit(row) {
      return  row.status===3 || row.status===4 ||row.status===5|| row.status===6
    },
    canDelete(row) {
      return  row.status===3 || row.status===4 ||row.status===5|| row.status===6
    },
    handlePause(row) {
      this.$confirm('确认暂停该任务吗？', '提示', { type: 'info' })
        .then(() => pauseTask(row.id))
        .then(() => { this.$message.success('已暂停'); this.getList() })
        .catch(() => {})
    },
    handleContinue(row) {
      this.$confirm('确认继续该任务吗？', '提示', { type: 'info' })
        .then(() => continueTask(row.id))
        .then(() => { this.$message.success('已继续'); this.getList() })
        .catch(() => {})
    },
    handleCancel(row) {
      this.$confirm('确认取消该任务吗？', '警告', { type: 'warning' })
        .then(() => cancelTask(row.id))
        .then(() => { this.$message.success('已取消'); this.getList() })
        .catch(() => {})
    },
    async handleTerminate(row) {
      try {
        const { value: reason } = await this.$prompt('请输入终止原因', '终止任务', { inputPlaceholder: '终止原因' })
        await terminateTask(row.id, reason)
        this.$message.success('已终止')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败')
      }
    },
    handleBan(row) {
      this.$confirm('确认禁用该任务吗？', '警告', { type: 'warning' })
        .then(() => banTask(row.id))
        .then(() => { this.$message.success('已禁用'); this.getList() })
        .catch(() => {})
    },
    handleResume(row) {
      this.$confirm('确认恢复该任务吗？', '提示', { type: 'info' })
        .then(() => resumeTask(row.id))
        .then(() => { this.$message.success('已恢复'); this.getList() })
        .catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确认删除该任务吗？', '警告', { type: 'warning' })
        .then(() => delTask(row.id))
        .then(() => { this.$message.success('删除成功'); this.getList() })
        .catch(() => {})
    },
    downloadFile(url, fileName) {
      const fullUrl = url.startsWith('http') ? url : process.env.VUE_APP_BASE_API + url
      const link = document.createElement('a')
      link.href = fullUrl
      link.download = fileName || 'download'
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },
    // 新增/编辑对话框
    handleAdd() {
      this.dialog.mode = 'create'
      this.dialog.title = '创建任务'
      this.taskForm = this.getInitialTaskForm()
      this.generatedSteps = []
      this.dynamicOptionsCache = {}
      this.dialog.visible = true
    },
    async handleEdit(row) {
      this.dialog.mode = 'edit'
      this.dialog.title = '修改任务'
      this.taskForm = {
        id: row.id,
        name: row.name,
        templateId: row.templateId,
        priority: row.priority,
        isGroupTask: row.isGroupTask === 1,
        duration: row.duration,
        taskType: row.taskType,
        cronExpression: row.cronExpression || '',
        scheduledTime: row.scheduledTime,
        batteryThreshold: row.batteryThreshold,
        idleTime: row.idleTime,
        robotId: row.robotId ? Number(row.robotId) : undefined,
        robotGroupId: row.robotGroupId ? Number(row.robotGroupId) : undefined,
        formData: {}
      }
      if (row.formContent) {
        try {
          const parsed = JSON.parse(row.formContent) || {}
          if (this.currentTemplate && this.currentTemplate.fields) {
            this.currentTemplate.fields.forEach(field => {
              if (['image','video','audio','file'].includes(field.type)) {
                if (parsed[field.id] && typeof parsed[field.id] === 'string') {
                  parsed[field.id] = [{ name: '已上传文件', url: parsed[field.id] }]
                } else if (!parsed[field.id]) {
                  parsed[field.id] = []
                }
              }
            })
          }
          this.taskForm.formData = parsed
        } catch (e) {
          this.taskForm.formData = {}
        }
      }
      this.$nextTick(() => {
        this.updateStepPreview()
        this.validateStepRobotsBelongToGroup()
        // 编辑时若已选机器人，预加载动态字段选项
        if (this.taskForm.robotId && this.currentTemplate) {
          this.onRobotChange(this.taskForm.robotId)
        }
      })
      this.dialog.visible = true
    },
    cancelTaskDialog() {
      this.dialog.visible = false
      this.$refs.taskFormRef?.resetFields()
      this.generatedSteps = []
    },
    onTemplateChange(val) {
      const template = this.templateOptions.find(t => t.id === val)
      if (template) {
        const formData = {}
        template.fields.forEach(field => {
          if (['image','video','audio','file'].includes(field.type)) {
            formData[field.id] = []
          } else {
            formData[field.id] = ''
          }
        })
        this.taskForm.formData = formData
        this.taskForm.robotId = undefined
        this.taskForm.robotGroupId = undefined
        // 预加载动态参数元数据（不加载选项，等待选机器人）
        this.prepareDynamicFields(template)
        this.updateStepPreview()
      }
    },
    // 为模板中的动态字段预配置
    prepareDynamicFields(template) {
      if (!template || !template.fields) return
      template.fields.forEach(field => {
        if (field.type === 'dynamicSelect') {
          field.options = field.options || []
          field.loading = false
          // 从模板配置中可获取关联的 apiId/paramKey
          if (!field.dynamicConfig) {
            // 若模板未配置，可从步骤中推断，简化处理：默认从模板第一个步骤的apiId获取
            if (template.steps && template.steps.length) {
              const firstStep = template.steps[0]
              if (firstStep.apiId) {
                field.apiId = firstStep.apiId
                field.paramKey = field.id // 假设字段ID与参数key一致
              }
            }
          } else {
            try {
              const cfg = JSON.parse(field.dynamicConfig)
              field.apiId = cfg.apiId
              field.paramKey = cfg.paramKey
            } catch (e) {}
          }
        }
      })
    },
    // 加载单个动态字段的选项
    async loadFieldOptions(field) {
      if (!field.apiId || !this.taskForm.robotId) {
        if (!this.taskForm.robotId) this.$message.warning('请先选择机器人')
        return
      }
      if (field.options && field.options.length > 0) return // 已加载
      field.loading = true
      try {
        const res = await getDynamicParams(field.apiId, { robotId: this.taskForm.robotId })
        const params = res.data || []
        const targetParam = params.find(p => p.paramKey === field.paramKey)
        if (targetParam && targetParam.options) {
          field.options = targetParam.options.map(opt => ({ value: opt.value, label: opt.label }))
          // 存入缓存供详情和预览使用
          this.dynamicOptionsCache[field.paramKey] = targetParam.options
        }
      } catch (e) {
        this.$message.error('加载选项失败')
      } finally {
        field.loading = false
      }
    },
    // 选择机器人时加载所有动态字段选项
    onRobotChange(robotId) {
      if (!robotId || !this.currentTemplate) return
      const dynamicFields = this.currentTemplate.fields.filter(f => f.type === 'dynamicSelect')
      dynamicFields.forEach(field => this.loadFieldOptions(field))
    },
    onRobotGroupChange() {
      // 组任务暂不支持动态参数加载，可扩展
    },
    updateStepPreview() {
      if (this.currentTemplate && this.currentTemplate.steps) {
        this.generatedSteps = this.generateStepsFromTemplate(this.currentTemplate, this.taskForm.formData)
      } else {
        this.generatedSteps = []
      }
    },
    generateStepsFromTemplate(template, formData) {
      if (!template || !template.steps) return []
      return template.steps.map((step, index) => {
        let description = step.description || ''
        if (formData) {
          Object.entries(formData).forEach(([key, val]) => {
            const placeholder = new RegExp(`\\{${key}\\}`, 'g')
            let displayValue = val
            // 尝试将ID转为名称显示（用于预览）
            if (this.dynamicOptionsCache[key]) {
              const opt = this.dynamicOptionsCache[key].find(o => o.value === val)
              displayValue = opt ? opt.label : val
            }
            if (Array.isArray(val)) {
              displayValue = val.length > 0 ? `[${val.length}个文件]` : ''
            }
            description = description.replace(placeholder, displayValue || `{${key}}`)
          })
        }
        return {
          stepName: step.name,
          description: description,
          orderNum: index + 1,
          status: 0,
          assignedRobotId: null
        }
      })
    },
    onStepRobotChange({ step, robotId }) {
      step.assignedRobotId = robotId
    },
    onGroupTaskChange(val) {
      if (!val) {
        this.taskForm.robotGroupId = undefined
        this.generatedSteps.forEach(step => { step.assignedRobotId = null })
      } else {
        this.generatedSteps.forEach(step => { step.assignedRobotId = null })
      }
    },
    validateStepRobotsBelongToGroup() {
      if (!this.taskForm.isGroupTask || !this.taskForm.robotGroupId) {
        if (this.generatedSteps.some(s => s.assignedRobotId)) {
          this.generatedSteps.forEach(step => { step.assignedRobotId = null })
          if (this.taskForm.isGroupTask && !this.taskForm.robotGroupId) {
            this.$message.warning('请先选择机器人组，再分配步骤机器人')
          }
        }
        return
      }
      const validRobotIds = this.availableRobotsForGroup.map(r => r.id)
      let hasInvalid = false
      this.generatedSteps.forEach(step => {
        if (step.assignedRobotId && !validRobotIds.includes(step.assignedRobotId)) {
          step.assignedRobotId = null
          hasInvalid = true
        }
      })
      if (hasInvalid) this.$message.warning('检测到部分步骤分配的机器人不在当前机器人组内，已自动清空')
    },
    // 上传相关
    handleUploadSuccess(fieldId, res, file) {
      const fileList = this.taskForm.formData[fieldId] || []
      fileList.push({ name: file.name, url: res.data.url })
      this.taskForm.formData[fieldId] = fileList
      this.updateStepPreview()
    },
    beforeUpload(file, field) {
      const isLtSize = file.size / 1024 / 1024 < (field.maxSize || 10)
      if (!isLtSize) {
        this.$message.error(`文件大小不能超过 ${field.maxSize || 10}MB!`)
        return false
      }
      return true
    },
    getAcceptByType(type) {
      const map = {
        image: 'image/*',
        video: 'video/*',
        audio: 'audio/*',
        file: '.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip,.rar'
      }
      return map[type] || '*'
    },
    async submitTask() {
      this.$refs.taskFormRef.validate(async (valid) => {
        if (!valid) return
        if (this.taskForm.isGroupTask) {
          if (!this.taskForm.robotGroupId) return this.$message.error('请选择机器人组')
          const validRobotIds = this.availableRobotsForGroup.map(r => r.id)
          const invalidSteps = this.generatedSteps.filter(
            step => step.assignedRobotId && !validRobotIds.includes(step.assignedRobotId)
          )
          if (invalidSteps.length) return this.$message.error('存在步骤分配的机器人不在所选机器人组内')
        } else {
          if (!this.taskForm.robotId) return this.$message.error('请选择机器人')
        }
        this.dialog.loading = true
        try {
          const dto = {
            name: this.taskForm.name,
            templateId: this.taskForm.templateId,
            priority: this.taskForm.priority,
            isGroupTask: this.taskForm.isGroupTask ? 1 : 0,
            duration: this.taskForm.duration,
            taskType: this.taskForm.taskType,
            cronExpression: this.taskForm.cronExpression,
            scheduledTime: this.taskForm.scheduledTime,
            batteryThreshold: this.taskForm.batteryThreshold,
            idleTime: this.taskForm.idleTime,
            robotId: this.taskForm.isGroupTask ? null : this.taskForm.robotId,
            robotGroupId: this.taskForm.isGroupTask ? this.taskForm.robotGroupId : null,
            formContent: JSON.stringify(this.taskForm.formData)
          }
          let taskId
          if (this.dialog.mode === 'create') {
            const res = await addTask(dto)
            taskId = res.data.id
            this.$message.success('创建成功')
          } else {
            await updateTask(this.taskForm.id, dto)
            taskId = this.taskForm.id
            this.$message.success('修改成功')
          }
          if (this.taskForm.isGroupTask && this.generatedSteps.some(s => s.assignedRobotId)) {
            const updates = this.generatedSteps
              .filter(step => step.assignedRobotId)
              .map(step => ({ orderNum: step.orderNum, assignedRobotId: step.assignedRobotId }))
            if (updates.length) {
              await updateTaskSteps(taskId, updates)
              this.$message.success('步骤机器人分配已保存')
            }
          }
          this.dialog.visible = false
          this.getList()
        } catch (error) {
          console.error(error)
          this.$message.error(error.message || '操作失败')
        } finally {
          this.dialog.loading = false
        }
      })
    },
    async handleView(row) {
      this.detailLoading = true
      this.detailDialog.visible = true
      try {
        const [taskRes, stepsRes, logsRes] = await Promise.all([
          getTask(row.id),
          getTaskSteps(row.id),
          listLogByTask({taskId:row.id})
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
        if (this.currentTask.templateId) {
          const template = this.templateOptions.find(t => t.id === this.currentTask.templateId)
          if (template) {
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
        // 处理步骤数据，将 operationJson 中的ID转为名称
        const stepsData = stepsRes.data || []
        for (let step of stepsData) {
          if (step.operationJson) {
            try {
              const json = JSON.parse(step.operationJson)
              // 对每个动态字段尝试转换
              for (let key in json) {
                if (this.dynamicOptionsCache[key]) {
                  const opt = this.dynamicOptionsCache[key].find(o => o.value === String(json[key]))
                  if (opt) json[key] = opt.label // 替换为名称显示
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
          status: step.status || 0,
          operationName: this.getOperationName(step.operationId)
        })).sort((a, b) => a.orderNum - b.orderNum)
        this.taskLogs = logsRes.rows || []
      } catch (error) {
        console.error('查看详情失败:', error)
        this.$message.error('获取详情失败: ' + (error.message || '未知错误'))
        this.detailDialog.visible = false
      } finally {
        this.detailLoading = false
      }
    },
    getOperationName(operationId) {
      if (!operationId) return '-'
      const op = this.operationList.find(o => o.id === operationId)
      return op ? op.name : `操作${operationId}`
    }
  }
}
</script>

<style scoped>
.app-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.status-tag { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: 500; }
.status-pending { background-color: #fff7e6; color: #fa8c16; }
.status-ready { background-color: #e6f7ff; color: #1890ff; }
.status-executing { background-color: #f6ffed; color: #52c41a; }
.status-paused { background-color: #fffbe6; color: #faad14; }
.status-completed { background-color: #f6ffed; color: #52c41a; }
.status-disabled { background-color: #f5f5f5; color: #999; }
.status-aborted { background-color: #fff1f0; color: #ff4d4f; }
.empty-tip { text-align: center; color: #999; padding: 20px; }
.el-form-item-msg { font-size: 12px; color: #999; line-height: 1; margin-top: 4px; }
.el-button [class*="fas"] { font-size: 14px; }
.el-table .cell .el-button + .el-button { margin-left: 4px; }
.dynamic-form-container { max-height: 400px; overflow-y: auto; padding-right: 10px; }
</style>
