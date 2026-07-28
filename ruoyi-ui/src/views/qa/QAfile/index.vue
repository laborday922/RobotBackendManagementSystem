<template>
  <div class="app-container">
    <el-card class="search-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
        <el-form-item label="文件名" prop="fileName">
          <el-input
            v-model="queryParams.fileName"
            placeholder="请输入文件名"
            clearable
            style="width: 180px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="正常" :value="0"></el-option>
            <el-option label="上传失败" :value="1"></el-option>
            <el-option label="知识库上传失败" :value="2"></el-option>
            <el-option label="图谱构建失败" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>QA文件列表</span>
          <div class="header-actions">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['qa:QAfile:add']">上传文件</el-button>
            <el-button type="danger" icon="el-icon-delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['qa:QAfile:remove']">删除</el-button>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="QAfileList" border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
        <el-table-column label="文件大小(字节)" prop="fileSize" width="140" align="center" />
        <el-table-column label="文件类型" prop="fileType" width="140" align="center" />
        <el-table-column label="处理状态" prop="status" width="140" align="center">
          <template slot-scope="scope">
            <span>{{ statusLabel(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button
              size="small"
              circle
              title="重试处理"
              :disabled="scope.row.status === 0 || scope.row.status === null || scope.row.status === undefined"
              @click="handleRetryProcess(scope.row)"
              v-hasPermi="['qa:QAfile:edit']"
            ><i class="el-icon-refresh"></i></el-button>
            <el-button size="small" type="danger" circle title="删除" @click="handleDelete(scope.row)" v-hasPermi="['qa:QAfile:remove']"><i class="el-icon-delete"></i></el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />

      <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
        <el-upload
          ref="uploader"
          name="file"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="uploadData"
          :auto-upload="true"
          :limit="1"
          :multiple="false"
          :show-file-list="true"
          accept=".txt,.doc,.docx,.pdf"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-exceed="handleExceed"
        >
          <el-button type="primary">选择文件</el-button>
          <div slot="tip" class="el-upload__tip">支持 txt/doc/docx/pdf，选择文件后自动上传</div>
        </el-upload>
        <div slot="footer" class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { listQAfile, delQAfile, retryProcess } from "@/api/qa/QAfile"
import { getToken } from "@/utils/auth"

export default {
  name: "QAfile",
  data() {
    return {
      QAfileList: [],
      open: false,
      loading: true,
      showSearch: true,
      ids: [],
      multiple: true,
      total: 0,
      title: "",
      uploadUrl: process.env.VUE_APP_BASE_API + "/qa/QAfile/upload",
      uploadHeaders: {
        Authorization: "Bearer " + getToken()
      },
      uploadData: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        fileName: null,
        fileContent: null,
        fileType: null,
        status: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQAfile(this.queryParams).then(response => {
        this.QAfileList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.clearUpload()
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    handleAdd() {
      this.open = true
      this.title = "上传QA文件"
      this.uploadData = {}
      this.$nextTick(() => this.clearUpload())
    },
    handleDelete(row) {
      const ids = (row && row.id) ? row.id : this.ids
      this.$modal.confirm(`是否确认删除QA文件管理编号为"${ids}"的数据项？`)
        .then(() => delQAfile(ids))
        .then(() => {
          this.getList()
          this.$modal.msgSuccess("删除成功")
        })
    },
    handleRetryProcess(row) {
      const id = row && row.id ? row.id : null
      if (!id) return
      this.$modal.confirm(`是否确认重新处理该文件？`)
        .then(() => retryProcess(id))
        .then(() => {
          this.getList()
          this.$modal.msgSuccess("已触发重试")
        })
    },
    beforeUpload(file) {
      const fileName = file && file.name ? file.name : ""
      const ext = fileName.includes(".") ? fileName.split(".").pop().toLowerCase() : ""
      const allowed = ["txt", "doc", "docx", "pdf"]
      if (!allowed.includes(ext)) {
        this.$message.error("仅支持上传 txt/doc/docx/pdf")
        return false
      }
      return true
    },
    handleUploadSuccess(response) {
      if (response && response.code === 200) {
        this.$modal.msgSuccess("上传成功")
        this.open = false
        this.getList()
        this.clearUpload()
        return
      }
      const msg = response && response.msg ? response.msg : "上传失败"
      this.$message.error(msg)
    },
    handleUploadError() {
      this.$message.error("上传失败")
    },
    handleExceed() {
      this.$message.warning("一次只能上传一个文件")
    },
    clearUpload() {
      const uploader = this.$refs.uploader
      if (uploader && uploader.clearFiles) {
        uploader.clearFiles()
      }
    },
    statusLabel(status) {
      if (status === 0) return "正常"
      if (status === 1) return "上传失败"
      if (status === 2) return "知识库上传失败"
      if (status === 3) return "图谱构建失败"
      return "-"
    }
  }
}
</script>

<style scoped>
.app-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; gap: 10px; }
.el-table .cell .el-button + .el-button { margin-left: 4px; }
</style>
