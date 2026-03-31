<template>
  <div>
    <el-form-item
      v-for="field in fields"
      :key="field.id"
      :label="field.label"
      :required="field.required"
    >
      <!-- 文本 -->
      <el-input
        v-if="field.type === 'text'"
        v-model="formData[field.id]"
        :placeholder="'请输入' + field.label"
        @blur="$emit('field-change')"
      />

      <!-- 数字 -->
      <el-input-number
        v-else-if="field.type === 'number'"
        v-model="formData[field.id]"
        style="width:100%"
        :placeholder="'请输入' + field.label"
        @change="$emit('field-change')"
      />

      <!-- 下拉选择 -->
      <el-select
        v-else-if="field.type === 'select'"
        v-model="formData[field.id]"
        placeholder="请选择"
        style="width:100%"
        @change="$emit('field-change')"
      >
        <el-option label="选项1" value="option1" />
        <el-option label="选项2" value="option2" />
      </el-select>

      <!-- 日期 -->
      <el-date-picker
        v-else-if="field.type === 'date'"
        v-model="formData[field.id]"
        type="date"
        placeholder="选择日期"
        style="width:100%"
        @change="$emit('field-change')"
      />

      <!-- 时间 -->
      <el-time-picker
        v-else-if="field.type === 'time'"
        v-model="formData[field.id]"
        placeholder="选择时间"
        style="width:100%"
        @change="$emit('field-change')"
      />

      <!-- 位置 -->
      <el-input
        v-else-if="field.type === 'location'"
        v-model="formData[field.id]"
        placeholder="请输入位置"
        @blur="$emit('field-change')"
      />

      <!-- 图片上传 -->
      <div v-else-if="field.type === 'image'" class="upload-container">
        <el-upload
          :action="uploadAction"
          :headers="uploadHeaders"
          :file-list="getFileList(field.id)"
          :limit="field.maxCount || 5"
          list-type="picture-card"
          :on-success="(res, file) => handleUploadSuccess(res, file, field.id)"
          :on-remove="(file) => handleUploadRemove(file, field.id)"
          :on-exceed="() => handleUploadExceed(field.maxCount || 5)"
          accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
        <div class="upload-tip">
          支持JPG/PNG/GIF，最多{{ field.maxCount || 5 }}张，单张≤{{ field.maxSize || 10 }}MB
        </div>
      </div>

      <!-- 视频上传 -->
      <div v-else-if="field.type === 'video'" class="upload-container">
        <el-upload
          :action="uploadAction"
          :headers="uploadHeaders"
          :file-list="getFileList(field.id)"
          :limit="field.maxCount || 3"
          :on-success="(res, file) => handleUploadSuccess(res, file, field.id)"
          :on-remove="(file) => handleUploadRemove(file, field.id)"
          :on-exceed="() => handleUploadExceed(field.maxCount || 3)"
          accept=".mp4,.avi,.mov,.wmv,.flv,.mkv"
        >
          <el-button size="small" type="primary">
            <i class="el-icon-upload"></i> 上传视频
          </el-button>
        </el-upload>
        <div class="file-list" v-if="formData[field.id] && formData[field.id].length > 0">
          <div v-for="(file, idx) in formData[field.id]" :key="idx" class="file-item">
            <i class="el-icon-video-camera"></i>
            <span class="file-name">{{ file.name }}</span>
            <el-button type="text" size="mini" @click="previewVideo(file.url)">预览</el-button>
          </div>
        </div>
        <div class="upload-tip">
          支持MP4/AVI/MOV等，最多{{ field.maxCount || 3 }}个，单个≤{{ field.maxSize || 100 }}MB
        </div>
      </div>

      <!-- 音频上传 -->
      <div v-else-if="field.type === 'audio'" class="upload-container">
        <el-upload
          :action="uploadAction"
          :headers="uploadHeaders"
          :file-list="getFileList(field.id)"
          :limit="field.maxCount || 5"
          :on-success="(res, file) => handleUploadSuccess(res, file, field.id)"
          :on-remove="(file) => handleUploadRemove(file, field.id)"
          :on-exceed="() => handleUploadExceed(field.maxCount || 5)"
          accept=".mp3,.wav,.wma,.aac,.flac,.m4a"
        >
          <el-button size="small" type="primary">
            <i class="el-icon-upload"></i> 上传音频
          </el-button>
        </el-upload>
        <div class="file-list" v-if="formData[field.id] && formData[field.id].length > 0">
          <div v-for="(file, idx) in formData[field.id]" :key="idx" class="file-item">
            <i class="el-icon-headset"></i>
            <span class="file-name">{{ file.name }}</span>
            <audio :src="file.url" controls style="height: 30px; margin-left: 10px;"></audio>
          </div>
        </div>
        <div class="upload-tip">
          支持MP3/WAV/AAC等，最多{{ field.maxCount || 5 }}个，单个≤{{ field.maxSize || 50 }}MB
        </div>
      </div>

      <!-- 文件上传 -->
      <div v-else-if="field.type === 'file'" class="upload-container">
        <el-upload
          :action="uploadAction"
          :headers="uploadHeaders"
          :file-list="getFileList(field.id)"
          :limit="field.maxCount || 3"
          :on-success="(res, file) => handleUploadSuccess(res, file, field.id)"
          :on-remove="(file) => handleUploadRemove(file, field.id)"
          :on-exceed="() => handleUploadExceed(field.maxCount || 3)"
          :accept="field.accept ? field.accept.join(',') : ''"
        >
          <el-button size="small" type="primary">
            <i class="el-icon-upload"></i> 上传文件
          </el-button>
        </el-upload>
        <div class="file-list" v-if="formData[field.id] && formData[field.id].length > 0">
          <div v-for="(file, idx) in formData[field.id]" :key="idx" class="file-item">
            <i class="el-icon-document"></i>
            <span class="file-name">{{ file.name }}</span>
            <el-button type="text" size="mini" @click="downloadFile(file.url, file.name)">下载</el-button>
          </div>
        </div>
        <div class="upload-tip">
          最多{{ field.maxCount || 3 }}个，单个≤{{ field.maxSize || 100 }}MB
          <span v-if="field.accept">，允许格式：{{ field.accept.join('、') }}</span>
        </div>
      </div>

      <div v-else>未知字段类型</div>
    </el-form-item>
  </div>
</template>

<script>
export default {
  name: 'DynamicFormFields',
  props: {
    fields: {
      type: Array,
      required: true
    },
    formData: {
      type: Object,
      required: true
    },
    uploadAction: {
      type: String,
      required: true
    },
    uploadHeaders: {
      type: Object,
      required: true
    }
  },
  methods: {
    getFileList(fieldId) {
      const files = this.formData[fieldId]
      if (!files || !Array.isArray(files)) return []
      return files.map((file, index) => ({
        name: file.name,
        url: file.url,
        uid: index
      }))
    },
    handleUploadSuccess(res, file, fieldId) {
      if (res.code !== 200) {
        this.$message.error(res.msg || '上传失败')
        return
      }
      if (!this.formData[fieldId]) {
        this.$set(this.formData, fieldId, [])
      }
      this.formData[fieldId].push({
        name: file.name || res.fileName,
        url: res.url,
        fileName: res.fileName || file.name
      })
      this.$message.success('上传成功')
      this.$emit('file-change')
    },
    handleUploadRemove(file, fieldId) {
      const files = this.formData[fieldId]
      if (files) {
        const index = files.findIndex(f => f.url === file.url || f.name === file.name)
        if (index > -1) {
          files.splice(index, 1)
        }
      }
      this.$emit('file-change')
    },
    handleUploadExceed(limit) {
      this.$message.warning(`最多只能上传 ${limit} 个文件`)
    },
    previewVideo(url) {
      this.$emit('preview-video', url)
    },
    downloadFile(url, fileName) {
      this.$emit('download', url, fileName)
    }
  }
}
</script>

<style scoped>
.upload-container {
  width: 100%;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
.file-list {
  margin-top: 10px;
}
.file-item {
  display: flex;
  align-items: center;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}
.file-item i {
  margin-right: 8px;
  color: #409eff;
}
.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 10px;
}
</style>
