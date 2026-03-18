<template>
  <el-dialog :title="title" :visible.sync="visible" width="750px" append-to-body @close="cancel">
    <el-form ref="form" :model="form" :rules="rules" label-width="120px">
      <!-- 基本信息 -->
      <el-form-item label="任务名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入任务名称" />
      </el-form-item>

      <!-- 任务类型切换 -->
      <el-form-item label="任务类型">
        <el-radio-group v-model="form.type">
          <el-radio label="process">完整流程任务</el-radio>
          <el-radio label="simple">简单动作任务</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 模板选择（流程任务） -->
      <template v-if="form.type === 'process'">
        <el-form-item label="选择模板" prop="templateId">
          <el-select v-model="form.templateId" placeholder="请选择模板" @change="onTemplateChange">
            <el-option v-for="t in templates" :key="t.id" :label="t.name" :value="t.id">
              <div style="display: flex; justify-content: space-between;">
                <span>{{ t.name }}</span>
                <span style="color: #999; font-size: 12px;">{{ getRobotGroupNames(t.robotGroupIds) }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 根据模板动态渲染字段 -->
        <template v-if="currentTemplate">
          <div class="template-fields">
            <div v-for="field in currentTemplate.fields" :key="field.id" class="field-item">
              <el-form-item :label="field.label" :required="field.required">
                <!-- 文本 -->
                <el-input
                  v-if="field.type === 'text'"
                  v-model="form.formData[field.id]"
                  :placeholder="'请输入' + field.label"
                />

                <!-- 数字 -->
                <el-input-number
                  v-else-if="field.type === 'number'"
                  v-model="form.formData[field.id]"
                  style="width:100%"
                  :placeholder="'请输入' + field.label"
                />

                <!-- 下拉选择 -->
                <el-select
                  v-else-if="field.type === 'select'"
                  v-model="form.formData[field.id]"
                  placeholder="请选择"
                  style="width:100%"
                >
                  <el-option label="选项1" value="option1" />
                  <el-option label="选项2" value="option2" />
                  <el-option label="选项3" value="option3" />
                </el-select>

                <!-- 日期 -->
                <el-date-picker
                  v-else-if="field.type === 'date'"
                  v-model="form.formData[field.id]"
                  type="date"
                  placeholder="选择日期"
                  style="width:100%"
                />

                <!-- 时间 -->
                <el-time-picker
                  v-else-if="field.type === 'time'"
                  v-model="form.formData[field.id]"
                  placeholder="选择时间"
                  style="width:100%"
                />

                <!-- 位置选择 -->
                <el-input
                  v-else-if="field.type === 'location'"
                  v-model="form.formData[field.id]"
                  placeholder="请选择位置"
                >
                  <el-button slot="append" @click="selectLocation(field.id)">
                    <i class="fas fa-map-marker-alt"></i>
                  </el-button>
                </el-input>

                <!-- 图片上传 -->
                <div v-else-if="field.type === 'image'" class="file-field-uploader">
                  <el-upload
                    :action="uploadAction"
                    :file-list="form.formData[field.id] || []"
                    list-type="picture-card"
                    :limit="field.maxCount || 5"
                    :before-upload="(file) => beforeUpload(file, field, 'image')"
                    :on-exceed="handleExceed"
                    :on-preview="handlePictureCardPreview"
                    :on-success="(res, file, fileList) => handleUploadSuccess(field.id, fileList)"
                    :on-remove="(file, fileList) => handleRemove(field.id, fileList)"
                    accept="image/*"
                  >
                    <i class="el-icon-plus"></i>
                  </el-upload>
                  <div class="field-hint">
                    支持jpg、png、gif格式，最多{{ field.maxCount || 5 }}张，单张不超过{{ field.maxSize || 10 }}MB
                  </div>
                </div>

                <!-- 音频上传 -->
                <div v-else-if="field.type === 'audio'" class="file-field-uploader">
                  <el-upload
                    :action="uploadAction"
                    :file-list="form.formData[field.id] || []"
                    :limit="field.maxCount || 3"
                    :before-upload="(file) => beforeUpload(file, field, 'audio')"
                    :on-exceed="handleExceed"
                    :on-success="(res, file, fileList) => handleUploadSuccess(field.id, fileList)"
                    :on-remove="(file, fileList) => handleRemove(field.id, fileList)"
                    accept="audio/*"
                  >
                    <el-button type="primary"><i class="fas fa-microphone"></i> 选择音频文件</el-button>
                  </el-upload>
                  <div class="field-hint">
                    支持mp3、wav、ogg格式，最多{{ field.maxCount || 3 }}个，单个不超过{{ field.maxSize || 20 }}MB
                  </div>
                </div>

                <!-- 视频上传 -->
                <div v-else-if="field.type === 'video'" class="file-field-uploader">
                  <el-upload
                    :action="uploadAction"
                    :file-list="form.formData[field.id] || []"
                    :limit="field.maxCount || 2"
                    :before-upload="(file) => beforeUpload(file, field, 'video')"
                    :on-exceed="handleExceed"
                    :on-success="(res, file, fileList) => handleUploadSuccess(field.id, fileList)"
                    :on-remove="(file, fileList) => handleRemove(field.id, fileList)"
                    accept="video/*"
                    drag
                  >
                    <i class="el-icon-upload"></i>
                    <div class="el-upload__text">将视频文件拖到此处，或<em>点击上传</em></div>
                  </el-upload>
                  <div class="field-hint">
                    支持mp4、avi、mov格式，最多{{ field.maxCount || 2 }}个，单个不超过{{ field.maxSize || 50 }}MB
                  </div>
                </div>

                <!-- 文件上传 -->
                <div v-else-if="field.type === 'file'" class="file-field-uploader">
                  <el-upload
                    :action="uploadAction"
                    :file-list="form.formData[field.id] || []"
                    :limit="field.maxCount || 5"
                    :before-upload="(file) => beforeUpload(file, field, 'file')"
                    :on-exceed="handleExceed"
                    :on-success="(res, file, fileList) => handleUploadSuccess(field.id, fileList)"
                    :on-remove="(file, fileList) => handleRemove(field.id, fileList)"
                    :accept="field.accept ? field.accept.join(',') : ''"
                  >
                    <el-button type="primary"><i class="fas fa-file"></i> 选择文件</el-button>
                  </el-upload>
                  <div class="field-hint">
                    最多{{ field.maxCount || 5 }}个文件，单个不超过{{ field.maxSize || 20 }}MB
                  </div>
                </div>
              </el-form-item>
            </div>
          </div>
        </template>
      </template>

      <!-- 简单动作任务 -->
      <template v-if="form.type === 'simple'">
        <el-form-item label="简单作业" prop="simpleAction">
          <el-select v-model="form.simpleAction" placeholder="请选择" style="width:100%">
            <el-option v-for="act in simpleActions" :key="act.value" :label="act.label" :value="act.value" />
          </el-select>
        </el-form-item>
      </template>

      <el-divider />

      <!-- 任务通用设置 -->
      <el-form-item label="优先级">
        <el-radio-group v-model="form.priority">
          <el-radio :label="1">高</el-radio>
          <el-radio :label="2">中</el-radio>
          <el-radio :label="3">低</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="组任务">
        <el-switch v-model="form.isGroupTask" />
      </el-form-item>
      <el-form-item label="任务时长(分)" prop="duration">
        <el-input-number v-model="form.duration" :min="1" :max="480" style="width:100%" />
      </el-form-item>
      <el-form-item label="触发类型" prop="triggerType">
        <el-radio-group v-model="form.triggerType">
          <el-radio label="scheduled">定时</el-radio>
          <el-radio label="battery">电量</el-radio>
          <el-radio label="idle">闲时</el-radio>
        </el-radio-group>
      </el-form-item>

      <template v-if="form.triggerType === 'scheduled'">
        <el-form-item label="Cron表达式">
          <el-input v-model="form.cronExpression" placeholder="如：0 0 9 * * ?">
            <el-button slot="append" @click="showCronHelp">帮助</el-button>
          </el-input>
          <div class="field-hint">格式：秒 分 时 日 月 周，留空表示立即执行</div>
        </el-form-item>
      </template>
      <template v-else-if="form.triggerType === 'battery'">
        <el-form-item label="触发电量(%)">
          <el-slider v-model="form.triggerValue" :max="100" show-input />
        </el-form-item>
      </template>
      <template v-else-if="form.triggerType === 'idle'">
        <el-form-item label="待机时长(分)">
          <el-input-number v-model="form.triggerValue" :min="1" :max="120" style="width:100%" />
        </el-form-item>
      </template>

      <el-form-item :label="form.isGroupTask ? '机器人组' : '机器人'" prop="robotId">
        <el-select
          v-if="form.isGroupTask"
          v-model="form.robotGroupId"
          placeholder="请选择机器人组"
          style="width:100%"
        >
          <el-option v-for="g in robotGroups" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
        <el-select v-else v-model="form.robotId" placeholder="请选择机器人" style="width:100%">
          <el-option v-for="r in availableRobots" :key="r.id" :label="r.name" :value="r.id">
            <div style="display: flex; justify-content: space-between;">
              <span>{{ r.name }}</span>
              <span style="color: #999; font-size: 12px;">电量{{ r.battery }}% | {{ r.status === 'online' ? '在线' : '离线' }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 标准作业流程预览（流程任务时显示） -->
    <div v-if="form.type === 'process' && currentTemplate" style="margin-top:20px; border-top:1px solid #e8e8e8; padding-top:16px;">
      <h4 style="margin-bottom:12px; color:#1890ff;">
        <i class="fas fa-clipboard-list"></i> 标准作业流程预览
      </h4>
      <div style="background:#f6ffed; border-radius:4px; padding:16px; border:1px solid #b7eb8f;">
        <div v-for="(step, index) in currentTemplate.steps" :key="index" style="margin-bottom:12px; padding:12px; background:white; border-radius:4px; border-left:3px solid #52c41a;">
          <div style="display:flex; align-items:flex-start;">
            <div style="width:28px; height:28px; background:#52c41a; color:white; border-radius:50%; text-align:center; line-height:28px; margin-right:12px; font-weight:bold;">{{ index+1 }}</div>
            <div style="flex:1;">
              <div style="font-weight:bold; margin-bottom:4px;">{{ step.name }}</div>
              <div style="color:#666; font-size:13px; margin-bottom:2px;">{{ renderStepDescription(step.description) }}</div>
              <div style="color:#999; font-size:12px;"><i class="fas fa-clock"></i> 预估时间：{{ step.estimatedTime }}分钟</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div slot="footer">
      <el-button @click="cancel">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="loading">确 定</el-button>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog :visible.sync="previewDialog.visible" width="60%" append-to-body>
      <img :src="previewDialog.url" style="width:100%; display:block;" />
    </el-dialog>
  </el-dialog>
</template>

<script>
import {addTask, getTask, listTemplate, updateTask} from '@/api/taskmgt/taskmgt'

export default {
  data() {
    return {
      visible: false,
      mode: 'create', // create/edit/view
      title: '',
      loading: false,
      templates: [],
      robotGroups: [],
      robots: [],
      // 简单动作选项
      simpleActions: [
        { value: '清洁', label: '清洁任务' },
        { value: '巡检', label: '巡检任务' },
        { value: '配送', label: '配送任务' },
        { value: '安防', label: '安防巡逻' },
        { value: '送餐', label: '送餐服务' }
      ],
      // 上传地址（模拟）
      uploadAction: 'https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15',
      // 图片预览
      previewDialog: {
        visible: false,
        url: ''
      },
      form: {
        id: null,
        name: '',
        type: 'process',
        templateId: null,
        simpleAction: '',
        formData: {},
        priority: 2,
        isGroupTask: false,
        duration: 30,
        triggerType: 'scheduled',
        triggerValue: null,
        cronExpression: '',
        robotId: null,
        robotGroupId: null
      },
      rules: {
        name: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
        templateId: [{ required: true, message: '请选择模板', trigger: 'change' }],
        simpleAction: [{ required: true, message: '请选择简单作业', trigger: 'change' }],
        robotId: [{ required: true, message: '请选择机器人', trigger: 'change' }]
      }
    }
  },
  computed: {
    currentTemplate() {
      return this.templates.find(t => t.id === this.form.templateId)
    },
    availableRobots() {
      let list = this.robots.filter(r => r.status === 'online')
      if (this.form.type === 'process' && this.currentTemplate) {
        list = list.filter(r => this.currentTemplate.robotGroupIds.includes(r.groupId))
      } else if (this.form.type === 'simple') {
        // 简单任务默认可用所有在线机器人
      }
      return list
    }
  },
  created() {
    this.loadTemplates()
    this.loadRobots()
    this.loadGroups()
  },
  methods: {
    open(mode, row) {
      this.mode = mode
      if (mode === 'create') {
        this.title = '新建任务'
        this.resetForm()
      } else if (mode === 'edit') {
        this.title = '修改任务'
        getTask(row.id).then(res => {
          const task = res.data
          // 初始化表单数据，处理文件列表格式
          this.form = { ...task }
          // 确保 formData 存在
          if (!this.form.formData) this.form.formData = {}
        })
      } else if (mode === 'view') {
        this.title = '任务详情'
        getTask(row.id).then(res => {
          this.form = res.data
        })
      }
      this.visible = true
    },
    loadTemplates() {
      listTemplate({}).then(res => {
        this.templates = res.rows || []
      })
    },
    loadRobots() {
      // 调用机器人模块接口，此处模拟数据
      this.robots = [
        { id: 1, name: '机器人A', groupId: 1, status: 'online', battery: 85, location: '药房' },
        { id: 2, name: '机器人B', groupId: 1, status: 'online', battery: 70, location: '病房' },
        { id: 3, name: '机器人C', groupId: 2, status: 'online', battery: 92, location: '大厅' },
        { id: 4, name: '机器人D', groupId: 2, status: 'offline', battery: 30, location: '走廊' }
      ]
    },
    loadGroups() {
      // 模拟机器人组数据
      this.robotGroups = [
        { id: 1, name: '配送组' },
        { id: 2, name: '巡检组' },
        { id: 3, name: '清洁组' },
        { id: 4, name: '安防组' }
      ]
    },
    // 获取机器人组名称（用于显示）
    getRobotGroupNames(groupIds) {
      return groupIds.map(id => {
        const g = this.robotGroups.find(g => g.id === id)
        return g ? g.name : id
      }).join(', ')
    },
    // 模板切换时初始化表单数据
    onTemplateChange() {
      const tpl = this.currentTemplate
      if (tpl) {
        const fd = {}
        tpl.fields.forEach(f => {
          if (['image', 'audio', 'video', 'file'].includes(f.type)) {
            fd[f.id] = [] // 文件类型初始为空数组
          } else {
            fd[f.id] = ''
          }
        })
        this.form.formData = fd
      }
    },
    // 位置选择
    selectLocation(fieldId) {
      this.$prompt('请输入位置坐标或选择位置', '位置选择', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '如：门诊大厅-A区'
      }).then(({ value }) => {
        this.$set(this.form.formData, fieldId, value)
        this.$message.success('位置已设置')
      }).catch(() => {})
    },
    // Cron帮助
    showCronHelp() {
      this.$alert(`
        <div style="font-size:14px; line-height:1.6;">
          <h3 style="margin-top:0; color:#1890ff;">Cron表达式格式说明</h3>
          <p>格式：<strong>秒 分 时 日 月 周</strong></p>
          <table style="width:100%; border-collapse:collapse; margin:10px 0; font-size:13px;">
            <tr style="background:#f5f5f5;"><th style="padding:8px; border:1px solid #ddd;">字段</th><th style="padding:8px; border:1px solid #ddd;">允许值</th><th style="padding:8px; border:1px solid #ddd;">特殊字符</th></tr>
            <tr><td style="padding:8px; border:1px solid #ddd;">秒</td><td>0-59</td><td>, - * /</td></tr>
            <tr style="background:#f9f9f9;"><td>分</td><td>0-59</td><td>, - * /</td></tr>
            <tr><td>时</td><td>0-23</td><td>, - * /</td></tr>
            <tr style="background:#f9f9f9;"><td>日</td><td>1-31</td><td>, - * ? / L W</td></tr>
            <tr><td>月</td><td>1-12</td><td>, - * /</td></tr>
            <tr style="background:#f9f9f9;"><td>周</td><td>1-7</td><td>, - * ? / L #</td></tr>
          </table>
          <p><strong>常用示例：</strong></p>
          <ul style="padding-left:20px;">
            <li><code>0 0 9 * * ?</code> - 每天9点执行</li>
            <li><code>0 0/30 9-17 * * ?</code> - 9点到17点每30分钟执行</li>
            <li><code>0 0 12 ? * MON-FRI</code> - 周一至周五12点执行</li>
          </ul>
        </div>
      `, 'Cron表达式帮助', { dangerouslyUseHTMLString: true, width: '600px' })
    },
    // 上传成功
    handleUploadSuccess(fieldId, fileList) {
      this.$set(this.form.formData, fieldId, fileList)
    },
    // 移除文件
    handleRemove(fieldId, fileList) {
      this.$set(this.form.formData, fieldId, fileList)
    },
    // 预览图片
    handlePictureCardPreview(file) {
      this.previewDialog.url = file.url
      this.previewDialog.visible = true
    },
    // 上传前校验
    beforeUpload(file, field, type) {
      const maxSize = (field.maxSize || 10) * 1024 * 1024
      if (file.size > maxSize) {
        this.$message.error(`文件大小不能超过${field.maxSize || 10}MB`)
        return false
      }
      if (type === 'file' && field.accept && field.accept.length) {
        const ext = '.' + file.name.split('.').pop().toLowerCase()
        const accepted = field.accept.join(',').split(',').map(s => s.trim().toLowerCase())
        if (!accepted.some(acc => ext === acc)) {
          this.$message.error(`只允许上传 ${field.accept.join('、')} 格式的文件`)
          return false
        }
      }
      return true
    },
    handleExceed() {
      this.$message.warning('超出最大上传数量限制')
    },
    // 渲染步骤描述（替换字段占位符）
    renderStepDescription(desc) {
      if (!desc) return desc
      let result = desc
      Object.entries(this.form.formData).forEach(([key, value]) => {
        if (typeof value === 'string') {
          result = result.replace(new RegExp(`{{${key}}}`, 'g'), value || `[${key}]`)
        }
      })
      return result
    },
    resetForm() {
      this.form = {
        id: null,
        name: '',
        type: 'process',
        templateId: null,
        simpleAction: '',
        formData: {},
        priority: 2,
        isGroupTask: false,
        duration: 30,
        triggerType: 'scheduled',
        triggerValue: null,
        cronExpression: '',
        robotId: null,
        robotGroupId: null
      }
      this.$nextTick(() => this.$refs.form?.clearValidate())
    },
    cancel() {
      this.visible = false
      this.resetForm()
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.loading = true
          // 处理触发值
          if (this.form.triggerType === 'scheduled') {
            this.form.triggerValue = null
          } else if (this.form.triggerType === 'battery') {
            this.form.triggerValue = this.form.triggerValue || 80
            this.form.cronExpression = ''
          } else if (this.form.triggerType === 'idle') {
            this.form.triggerValue = this.form.triggerValue || 30
            this.form.cronExpression = ''
          }

          // 构建提交数据（可根据需要调整）
          const submitData = { ...this.form }

          if (this.mode === 'create') {
            addTask(submitData).then(() => {
              this.$message.success('创建成功')
              this.$emit('ok')
              this.cancel()
            }).finally(() => this.loading = false)
          } else if (this.mode === 'edit') {
            updateTask(submitData).then(() => {
              this.$message.success('修改成功')
              this.$emit('ok')
              this.cancel()
            }).finally(() => this.loading = false)
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.field-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.file-field-uploader .el-upload {
  text-align: left;
}
.template-fields {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}
</style>
