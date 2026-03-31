<template>
  <div v-loading="loading" v-if="task">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="任务名称">{{ task.name }}</el-descriptions-item>
      <el-descriptions-item label="模板">{{ task.templateName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="触发方式">{{ getTriggerTypeText(task.taskType) }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <span :class="'status-tag status-' + getStatusClass(task.status)">{{ getStatusText(task.status) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="优先级">
        <el-tag v-if="task.priority === 1" type="danger">高</el-tag>
        <el-tag v-else-if="task.priority === 2" type="warning">中</el-tag>
        <el-tag v-else type="info">低</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="机器人">{{ task.robotName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="机器人组">{{ task.robotGroupName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ task.createTime }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ task.updateTime }}</el-descriptions-item>
      <el-descriptions-item label="任务时长">{{ task.duration }}分钟</el-descriptions-item>
      <el-descriptions-item label="终止原因" v-if="task.terminateReason">{{ task.terminateReason }}</el-descriptions-item>
    </el-descriptions>

    <!-- 表单内容 -->
    <el-divider content-position="left">表单内容</el-divider>
    <div v-if="formFields.length > 0">
      <el-descriptions :column="1" border>
        <el-descriptions-item v-for="field in formFields" :key="field.id" :label="field.label">
          <!-- 普通字段 -->
          <template v-if="!['image','video','audio','file'].includes(field.type)">
            {{ formatFormValue(field, task.formData ? task.formData[field.id] : '') }}
          </template>

          <!-- 图片预览 -->
          <template v-else-if="field.type === 'image'">
            <div v-if="task.formData && task.formData[field.id] && task.formData[field.id].length > 0" class="image-preview-list">
              <div v-for="(img, idx) in task.formData[field.id]" :key="idx" class="image-preview-item">
                <el-image
                  :src="img.url"
                  :preview-src-list="task.formData[field.id].map(i => i.url)"
                  fit="cover"
                  style="width: 100px; height: 100px; border-radius: 4px; cursor: pointer;"
                ></el-image>
                <div class="image-name" :title="img.name">{{ img.name }}</div>
              </div>
            </div>
            <span v-else>-</span>
          </template>

          <!-- 视频预览 -->
          <template v-else-if="field.type === 'video'">
            <div v-if="task.formData && task.formData[field.id] && task.formData[field.id].length > 0">
              <div v-for="(video, idx) in task.formData[field.id]" :key="idx" class="video-item">
                <video :src="video.url" controls style="max-width: 100%; max-height: 200px; border-radius: 4px;"></video>
                <div class="file-actions">
                  <span>{{ video.name }}</span>
                  <el-button type="text" size="small" @click="$emit('download', video.url, video.name)">下载</el-button>
                </div>
              </div>
            </div>
            <span v-else>-</span>
          </template>

          <!-- 音频预览 -->
          <template v-else-if="field.type === 'audio'">
            <div v-if="task.formData && task.formData[field.id] && task.formData[field.id].length > 0">
              <div v-for="(audio, idx) in task.formData[field.id]" :key="idx" class="audio-item">
                <div class="audio-info">
                  <i class="el-icon-headset"></i>
                  <span>{{ audio.name }}</span>
                </div>
                <audio :src="audio.url" controls style="width: 100%; margin-top: 5px;"></audio>
                <el-button type="text" size="small" @click="$emit('download', audio.url, audio.name)">下载</el-button>
              </div>
            </div>
            <span v-else>-</span>
          </template>

          <!-- 文件下载 -->
          <template v-else-if="field.type === 'file'">
            <div v-if="task.formData && task.formData[field.id] && task.formData[field.id].length > 0">
              <div v-for="(file, idx) in task.formData[field.id]" :key="idx" class="file-download-item">
                <i class="el-icon-document"></i>
                <span class="file-name">{{ file.name }}</span>
                <el-button type="primary" size="mini" plain @click="$emit('download', file.url, file.name)">
                  <i class="el-icon-download"></i> 下载
                </el-button>
              </div>
            </div>
            <span v-else>-</span>
          </template>
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
</template>

<script>
export default {
  name: 'TaskDetail',
  props: {
    task: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    },
    formFields: {
      type: Array,
      default: () => []
    },
    taskSteps: {
      type: Array,
      default: () => []
    },
    taskLogs: {
      type: Array,
      default: () => []
    },
    operationList: {
      type: Array,
      default: () => []
    }
  },
  methods: {
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
    getStepStatusText(status) {
      const map = { 0: '未开始', 1: '执行中', 2: '已完成', 3: '已暂停', 4: '已终止' }
      return map[status] || status
    },
    getStepStatusClass(status) {
      const map = { 0: 'pending', 1: 'executing', 2: 'completed', 3: 'paused', 4: 'aborted' }
      return map[status] || 'pending'
    },
    formatFormValue(field, value) {
      if (value === null || value === undefined) return '-'
      if (Array.isArray(value)) {
        return value.map(v => v.name || v).join(', ')
      }
      return String(value)
    }
  }
}
</script>

<style scoped>
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
.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.image-preview-item {
  width: 100px;
}
.image-name {
  font-size: 12px;
  color: #606266;
  text-align: center;
  margin-top: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.video-item, .audio-item {
  margin-bottom: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}
.file-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}
.file-download-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}
.file-download-item i {
  color: #409eff;
  margin-right: 8px;
  font-size: 20px;
}
.file-download-item .file-name {
  flex: 1;
  margin-right: 10px;
}
</style>
