<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文件名" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="正常" :value="0"></el-option>
          <el-option label="上传失败" :value="1"></el-option>
          <el-option label="知识库上传失败" :value="2"></el-option>
          <el-option label="图谱构建失败" :value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="逻辑删除" prop="isDeleted">
        <el-select v-model="queryParams.isDeleted" placeholder="请选择" clearable>
          <el-option label="否" :value="false"></el-option>
          <el-option label="是" :value="true"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['qa:QAfile:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['qa:QAfile:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['qa:QAfile:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['qa:QAfile:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="QAfileList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="文件名" align="center" prop="fileName" />
      <el-table-column label="文件大小(字节)" align="center" prop="fileSize" />
      <el-table-column label="文件类型(doc/docx/pdf)" align="center" prop="fileType" />
      <el-table-column label="处理状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ statusLabel(scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="逻辑删除" align="center" prop="isDeleted">
        <template slot-scope="scope">
          <span v-if="scope.row.isDeleted === true">是</span>
          <span v-else-if="scope.row.isDeleted === false">否</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['qa:QAfile:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh" :disabled="scope.row.status === 0 || scope.row.status === null || scope.row.status === undefined" @click="handleRetryProcess(scope.row)" v-hasPermi="['qa:QAfile:edit']">重试处理</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['qa:QAfile:remove']">删除</el-button>
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

    <!-- 添加或修改QA文件管理对话框 -->
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
        <el-button type="primary">上传文件</el-button>
        <div slot="tip" class="el-upload__tip">支持 txt/doc/docx/pdf，选择文件后自动上传</div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
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
      single: true,
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
        status: null,
        isDeleted: null
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
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.open = true
      this.title = "添加QA文件管理"
      this.uploadData = {}
      this.$nextTick(() => this.clearUpload())
    },
    handleUpdate(row) {
      const id = (row && row.id) ? row.id : (Array.isArray(this.ids) ? this.ids[0] : this.ids)
      this.open = true
      this.title = "更新QA文件"
      this.uploadData = { id }
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
    handleExport() {
      this.download("qa/QAfile/export", { ...this.queryParams }, `QAfile_${new Date().getTime()}.xlsx`)
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
