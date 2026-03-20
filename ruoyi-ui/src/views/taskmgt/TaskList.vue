<template>
  <div class="app-container">
    <!-- 搜索栏 - 实时筛选，无重置按钮 -->
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
        <!-- 操作列改为图标按钮 -->
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

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 任务创建/编辑对话框 - 增加步骤预览功能 -->
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
          <el-form-item
            v-for="field in currentTemplate.fields"
            :key="field.id"
            :label="field.label"
            :required="field.required"
          >
            <!-- 文本 -->
            <el-input
              v-if="field.type === 'text'"
              v-model="taskForm.formData[field.id]"
              :placeholder="'请输入' + field.label"
              @blur="updateStepPreview"
            />
            <!-- 数字 -->
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="taskForm.formData[field.id]"
              style="width:100%"
              :placeholder="'请输入' + field.label"
              @change="updateStepPreview"
            />
            <!-- 下拉选择 -->
            <el-select
              v-else-if="field.type === 'select'"
              v-model="taskForm.formData[field.id]"
              placeholder="请选择"
              style="width:100%"
              @change="updateStepPreview"
            >
              <el-option label="选项1" value="option1" />
              <el-option label="选项2" value="option2" />
            </el-select>
            <!-- 日期 -->
            <el-date-picker
              v-else-if="field.type === 'date'"
              v-model="taskForm.formData[field.id]"
              type="date"
              placeholder="选择日期"
              style="width:100%"
              @change="updateStepPreview"
            />
            <!-- 时间 -->
            <el-time-picker
              v-else-if="field.type === 'time'"
              v-model="taskForm.formData[field.id]"
              placeholder="选择时间"
              style="width:100%"
              @change="updateStepPreview"
            />
            <!-- 位置（简单输入） -->
            <el-input
              v-else-if="field.type === 'location'"
              v-model="taskForm.formData[field.id]"
              placeholder="请输入位置"
              @blur="updateStepPreview"
            />
            <!-- 文件类型 -->
            <el-input
              v-else-if="['image','audio','video','file'].includes(field.type)"
              v-model="taskForm.formData[field.id]"
              placeholder="文件上传暂未实现，请填写文件路径"
              @blur="updateStepPreview"
            />
            <div v-else>未知字段类型</div>
            <div v-if="field.type === 'text' || field.type === 'location'" class="field-tip">
              此字段可在步骤描述中使用 <code>{{ '{' + field.id + '}' }}</code> 作为占位符
            </div>
          </el-form-item>
        </template>

        <!-- 步骤预览区域 - 新增 -->
        <template v-if="currentTemplate && currentTemplate.steps && currentTemplate.steps.length > 0">
          <el-divider content-position="left">作业步骤预览（自动填充）</el-divider>
          <div class="step-preview-container">
            <div v-for="(step, index) in generatedSteps" :key="index" class="step-preview-item">
              <div class="step-preview-header">
                <span class="step-number">{{ index + 1 }}</span>
                <span class="step-name">{{ step.stepName }}</span>
              </div>
              <div class="step-preview-desc">{{ step.description || '暂无描述' }}</div>
            </div>
          </div>
        </template>

        <el-divider content-position="left">任务设置</el-divider>
        <el-form-item label="任务优先级" prop="priority">
          <el-radio-group v-model="taskForm.priority">
            <el-radio :label="1">高</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">低</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="是否为组任务">
          <el-switch v-model="taskForm.isGroupTask" />
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
            <el-select v-model="taskForm.robotGroupId" placeholder="请选择机器人组" style="width:100%">
              <el-option
                v-for="item in robotGroupOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </template>
          <template v-else>
            <el-select v-model="taskForm.robotId" placeholder="请选择机器人" style="width:100%">
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
      <div v-loading="detailLoading" v-if="currentTask">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="模板">{{ currentTask.templateName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="触发方式">
            {{ getTriggerTypeText(currentTask.taskType) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <span :class="'status-tag status-' + getStatusClass(currentTask.status)">{{ getStatusText(currentTask.status) }}</span>
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
          <el-descriptions-item label="终止原因" v-if="currentTask.terminateReason">{{ currentTask.terminateReason }}</el-descriptions-item>
        </el-descriptions>

        <!-- 表单内容（解析formContent） -->
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
          <el-table-column prop="stepName" label="步骤名称" width="150" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column label="状态" width="100" align="center">
            <template slot-scope="scope">
              <span :class="'status-tag status-' + getStepStatusClass(scope.row.status)">{{ getStepStatusText(scope.row.status) }}</span>
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
import {
  addTask,
  addTaskSteps,
  banTask,
  cancelTask,
  continueTask,
  delTask,
  getTask,
  getTaskSteps,
  listLogByTask,
  listTask,
  listTemplate,
  pauseTask,
  resumeTask,
  terminateTask,
  updateTask,
  updateTaskSteps
} from '@/api/taskmgt/taskmgt'
import { listRobots, listGroups } from '@/api/system/robots'
import debounce from 'lodash/debounce'

export default {
  name: 'TaskList',
  data() {
    return {
      // 查询参数
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
      // 对话框
      dialog: {
        visible: false,
        title: '',
        mode: 'create',
        loading: false
      },
      taskForm: {
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
      },
      taskRules: {
        name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
        templateId: [{ required: true, message: '请选择模板', trigger: 'change' }],
        batteryThreshold: [{ required: true, message: '请输入触发电量', trigger: 'blur' }],
        idleTime: [{ required: true, message: '请输入闲时等待时间', trigger: 'blur' }],
      },
      // 详情对话框
      detailDialog: {
        visible: false
      },
      detailLoading: false,
      currentTask: null,
      taskSteps: [],
      taskLogs: [],
      formFields: [],
      // 生成的步骤预览
      generatedSteps: []
    }
  },
  computed: {
    // 可用机器人：根据当前模板过滤（仅模板允许的组内且状态正常的机器人）
    availableRobots() {
      if (!this.currentTemplate || !this.currentTemplate.robotGroupIds || this.currentTemplate.robotGroupIds.length === 0) {
        return []
      }
      const allowedGroupIds = this.currentTemplate.robotGroupIds.map(id => Number(id))
      return this.robotOptions.filter(robot => {
        const isHealthy = robot.status === '1' && robot.hardwareStatus === '0'
        const inAllowedGroup = allowedGroupIds.includes(Number(robot.groupId))
        return isHealthy && inAllowedGroup
      }).sort((a, b) => {
        const aHealthy = (a.status === '1' && a.hardwareStatus === '0') ? 1 : 0
        const bHealthy = (b.status === '1' && b.hardwareStatus === '0') ? 1 : 0
        return bHealthy - aHealthy
      })
    },
    // 当前选中的模板
    currentTemplate() {
      if (!this.taskForm.templateId) return null
      return this.templateOptions.find(t => t.id === this.taskForm.templateId)
    }
  },
  created() {
    this.getRobotData()
    this.getRobotGroups()
    this.getTemplates()
    this.getList()
    this.debouncedQuery = debounce(this.handleQuery, 500)
  },
  beforeDestroy() {
    this.debouncedQuery.cancel()
  },
  methods: {
    // 确保数组
    ensureArray(ids) {
      if (!ids) return []
      if (Array.isArray(ids)) return ids
      if (typeof ids === 'string') return ids.split(',').map(s => Number(s.trim()))
      return [ids]
    },
    // 格式化机器人状态显示
    formatRobotStatus(robot) {
      if (robot.status === '1' && robot.hardwareStatus === '0') return '在线正常'
      if (robot.status === '1' && robot.hardwareStatus === '1') return '硬件警告'
      if (robot.status === '1' && robot.hardwareStatus === '2') return '硬件故障'
      if (robot.status === '0') return '离线'
      if (robot.status === '2') return '待激活'
      return '未知'
    },
    // 获取机器人列表
    async getRobotData() {
      try {
        const res = await listRobots({ pageSize: 1000 })
        this.robotOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人列表失败')
        console.error(error)
      }
    },
    // 获取机器人组
    async getRobotGroups() {
      try {
        const res = await listGroups()
        this.robotGroupOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人组失败')
        console.error(error)
      }
    },
    // 获取模板列表（并解析字段、步骤，保留 robotGroupIds）
    async getTemplates() {
      try {
        const res = await listTemplate({ pageSize: 100 })
        this.templateOptions = (res.rows || []).map(tpl => {
          let fields = [], steps = []
          try {
            if (tpl.formContent) {
              const content = JSON.parse(tpl.formContent)
              fields = content.fields || []
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
    // 获取任务列表
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
    // 状态映射
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
    // 编辑权限：只有已禁用的任务可编辑
    canEdit(row) {
      return row.status === 4
    },
    // 删除权限：已禁用可删除
    canDelete(row) {
      return row.status === 4
    },
    // 以下为各状态操作的处理方法
    handlePause(row) {
      this.$confirm('确认暂停该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        await pauseTask(row.id)
        this.$message.success('已暂停')
        this.getList()
      }).catch(() => {})
    },
    handleContinue(row) {
      this.$confirm('确认继续该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        await continueTask(row.id)
        this.$message.success('已继续')
        this.getList()
      }).catch(() => {})
    },
    handleCancel(row) {
      this.$confirm('确认取消该任务吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await cancelTask(row.id)
        this.$message.success('已取消')
        this.getList()
      }).catch(() => {})
    },
    async handleTerminate(row) {
      try {
        const { value: reason } = await this.$prompt('请输入终止原因', '终止任务', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '终止原因'
        })
        await terminateTask(row.id, reason)
        this.$message.success('已终止')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },
    handleBan(row) {
      this.$confirm('确认禁用该任务吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await banTask(row.id)
        this.$message.success('已禁用')
        this.getList()
      }).catch(() => {})
    },
    handleResume(row) {
      this.$confirm('确认恢复该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        await resumeTask(row.id)
        this.$message.success('已恢复')
        this.getList()
      }).catch(() => {})
    },
    // 删除任务
    handleDelete(row) {
      this.$confirm('确认删除该任务吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await delTask(row.id)
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {})
    },
    // 打开新增对话框
    handleAdd() {
      this.dialog.mode = 'create'
      this.dialog.title = '创建任务'
      this.taskForm = {
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
      this.generatedSteps = []
      this.dialog.visible = true
    },
    // 打开编辑对话框
    handleEdit(row) {
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
      // 解析formData
      if (row.formContent) {
        try {
          this.taskForm.formData = JSON.parse(row.formContent) || {}
        } catch (e) {
          this.taskForm.formData = {}
        }
      }
      // 生成步骤预览
      this.$nextTick(() => {
        this.updateStepPreview()
      })
      this.dialog.visible = true
    },
    // 取消对话框
    cancelTaskDialog() {
      this.dialog.visible = false
      this.$refs.taskFormRef?.resetFields()
      this.generatedSteps = []
    },
    // 模板变更时初始化formData
    onTemplateChange(val) {
      const template = this.templateOptions.find(t => t.id === val)
      if (template) {
        const formData = {}
        template.fields.forEach(field => {
          formData[field.id] = ''
        })
        this.taskForm.formData = formData
        // 清空已选的机器人（因为模板变了，机器人可能不再允许）
        this.taskForm.robotId = undefined
        // 更新步骤预览
        this.updateStepPreview()
      }
    },
    // 更新步骤预览 - 新增方法
    updateStepPreview() {
      if (this.currentTemplate && this.currentTemplate.steps) {
        this.generatedSteps = this.generateStepsFromTemplate(this.currentTemplate, this.taskForm.formData)
      } else {
        this.generatedSteps = []
      }
    },
    // 根据模板和表单数据生成步骤列表
    generateStepsFromTemplate(template, formData) {
      if (!template || !template.steps) return []
      return template.steps.map((step, index) => {
        let description = step.description || ''
        if (formData) {
          Object.entries(formData).forEach(([key, value]) => {
            // 使用正则替换所有 {key} 的实例
            const placeholder = new RegExp(`\\{${key}\\}`, 'g')
            description = description.replace(placeholder, value || `{${key}}`)
          })
        }
        return {
          stepName: step.name,
          description: description,
          orderNum: index + 1
        }
      })
    },
    // 提交任务
    async submitTask() {
      this.$refs.taskFormRef.validate(async (valid) => {
        if (!valid) return

        // 手动检查机器人/机器人组选择
        if (this.taskForm.isGroupTask) {
          if (!this.taskForm.robotGroupId) {
            this.$message.error('请选择机器人组');
            return;
          }
        } else {
          if (!this.taskForm.robotId) {
            this.$message.error('请选择机器人');
            return;
          }
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
            // 新增任务时根据模板创建步骤
            if (this.currentTemplate) {
              const steps = this.generateStepsFromTemplate(this.currentTemplate, this.taskForm.formData)
              if (steps.length > 0) {
                await addTaskSteps(taskId, steps)
              }
            }
          } else {
            // 编辑任务：更新基本信息
            await updateTask(this.taskForm.id, dto)
            taskId = this.taskForm.id
            this.$message.success('修改成功')
            // 编辑任务时，根据当前模板和表单数据重新生成步骤并更新，确保orderNum传入
            if (this.currentTemplate) {
              const steps = this.generateStepsFromTemplate(this.currentTemplate, this.taskForm.formData)
              if (steps.length > 0) {
                // 确保每个步骤都有orderNum
                const stepsWithOrder = steps.map((step, index) => ({
                  ...step,
                  orderNum: step.orderNum || (index + 1)
                }))
                await updateTaskSteps(taskId, stepsWithOrder)
              }
            }
          }

          this.dialog.visible = false
          this.getList()
        } catch (error) {
          console.error(error)
          this.$message.error('操作失败')
        } finally {
          this.dialog.loading = false
        }
      })
    },
    // 查看详情
    async handleView(row) {
      this.detailLoading = true
      this.detailDialog.visible = true
      try {
        const taskRes = await getTask(row.id)
        this.currentTask = taskRes.data

        // 解析表单字段定义（从模板）
        if (this.currentTask.templateId) {
          const template = this.templateOptions.find(t => t.id === this.currentTask.templateId)
          if (template) {
            this.formFields = template.fields || []
          } else {
            this.formFields = []
          }
        } else {
          this.formFields = []
        }

        const stepsRes = await getTaskSteps(row.id)
        this.taskSteps = stepsRes.data || []

        const logsRes = await listLogByTask(row.id)
        this.taskLogs = logsRes.rows || []
      } catch (error) {
        this.$message.error('获取详情失败')
        this.detailDialog.visible = false
      } finally {
        this.detailLoading = false
      }
    },
    // 格式化表单值展示
    formatFormValue(field, value) {
      if (value === null || value === undefined) return '-'
      if (Array.isArray(value)) {
        return value.map(v => v.name || v).join(', ')
      }
      return String(value)
    },
    // 步骤状态
    getStepStatusText(status) {
      const map = { 0: '未开始', 1: '执行中', 2: '已完成', 3: '已暂停', 4: '已终止' }
      return map[status] || status
    },
    getStepStatusClass(status) {
      const map = { 0: 'pending', 1: 'executing', 2: 'completed', 3: 'paused', 4: 'aborted' }
      return map[status] || 'pending'
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.table-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}
.el-form-item-msg {
  font-size: 12px;
  color: #999;
  line-height: 1;
  margin-top: 4px;
}
/* 操作按钮样式优化 */
.el-button [class*="fas"] {
  font-size: 14px;
}
.el-table .cell .el-button + .el-button {
  margin-left: 4px;
}

/* 步骤预览样式 - 新增 */
.step-preview-container {
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
  margin-bottom: 20px;
  max-height: 300px;
  overflow-y: auto;
}
.step-preview-item {
  background: white;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 10px;
  border: 1px solid #e8e8e8;
}
.step-preview-item:last-child {
  margin-bottom: 0;
}
.step-preview-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-weight: bold;
}
.step-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  margin-right: 8px;
}
.step-name {
  color: #303133;
}
.step-preview-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  padding-left: 28px;
}
.field-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.field-tip code {
  background: #f4f4f5;
  padding: 1px 4px;
  border-radius: 3px;
  color: #409eff;
  font-family: monospace;
}
</style>
