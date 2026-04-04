<template>
  <div class="app-container">
    <!-- 搜索区域美化：卡片样式 + 间距优化 -->
    <div class="search-card">
      <el-form 
        :model="queryParams" 
        ref="queryForm" 
        size="small" 
        :inline="true" 
        v-show="showSearch" 
        label-width="110px"
        class="search-form"
      >
        <el-form-item label="关联应用库" prop="appId">
          <el-select
            v-model="queryParams.appId"
            placeholder="请选择关联应用库"
            clearable
            @change="handleQuery"
            class="search-select"
            filterable
            remote
            :remote-method="searchAppLibrary"
            :loading="appLibraryLoading"
          >
            <el-option 
              v-for="item in appLibraryList" 
              :key="item.appId" 
              :label="item.appName" 
              :value="item.appId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="更新状态" prop="updateStatus">
          <el-select
            v-model="queryParams.updateStatus"
            placeholder="请选择更新状态"
            clearable
            @change="handleQuery"
            class="search-select"
          >
            <el-option 
              v-for="(item, key) in updateStatusMap" 
              :key="key" 
              :label="item" 
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域美化 -->
    <el-row :gutter="10" class="mb8 operate-bar">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['app:appUpdate:add']"
        >新建更新</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['app:appUpdate:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格美化：展示所有字段 + 状态标签 + 样式优化 -->
    <el-table 
      v-loading="loading" 
      :data="appUpdateList" 
      border
      stripe
      highlight-current-row
      class="data-table"
      :header-cell-style="{background: '#f5f7fa', color: '#303133', fontWeight: '500'}"
    >
      <el-table-column label="关联应用库" align="center" min-width="120">
        <template slot-scope="scope">
          {{ getAppNameById(scope.row.appId) || scope.row.appId || '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="更新版本号" align="center" prop="updateVersion" min-width="100" />
      <el-table-column label="更新状态" align="center" prop="updateStatus" width="100">
        <template slot-scope="scope">
          <el-tag :type="getUpdateStatusTagType(scope.row.updateStatus)" size="mini">
            {{ updateStatusMap[scope.row.updateStatus] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关联机器人ID" align="center" prop="robotId" min-width="120" show-overflow-tooltip />
      <el-table-column label="更新内容" align="center" prop="updateContent" min-width="200" show-overflow-tooltip />
      <el-table-column label="更新失败原因" align="center" prop="failReason" min-width="180" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ scope.row.failReason || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template slot-scope="scope">
          {{ formatIsoTime(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          {{ formatIsoTime(scope.row.createTime) }}
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页美化 -->
    <div class="pagination-container">
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
        background
      />
    </div>

    <!-- 美化后的新建更新弹窗 -->
    <el-dialog 
      :title="title" 
      :visible.sync="open" 
      width="800px" 
      append-to-body
      :close-on-click-modal="false"
      class="update-dialog"
      :modal-append-to-body="false"
      :destroy-on-close="true"
    >
      <!-- 弹窗内容卡片化 -->
      <div class="dialog-card">
        <el-form 
          ref="form" 
          :model="form" 
          :rules="rules" 
          label-width="120px"
          size="default"
          class="dialog-form"
        >
          <!-- 表单分组 - 基础信息 -->
          <div class="form-section">
            <div class="section-title">
              <i class="el-icon-info"></i> 基础信息
            </div>
            <el-row :gutter="24" class="form-row">
              <el-col :span="12">
                <el-form-item label="关联应用库" prop="appId" class="form-item">
                  <el-select
                    v-model="form.appId"
                    placeholder="请选择关联应用库"
                    filterable
                    @change="handleAppIdChange"
                    class="form-select"
                  >
                    <el-option 
                      v-for="item in appLibraryList" 
                      :key="item.appId" 
                      :label="item.appName" 
                      :value="item.appId"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="更新版本号" prop="updateVersion" class="form-item">
                  <el-input 
                    v-model="form.updateVersion" 
                    placeholder="请输入更新版本号（如：v1.0.1）" 
                    class="form-input"
                    prefix-icon="el-icon-version"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24" class="form-row">
              <el-col :span="12">
                <el-form-item label="更新状态" prop="updateStatus" class="form-item">
                  <el-select 
                    v-model="form.updateStatus" 
                    placeholder="请选择更新状态"
                    class="form-select"
                  >
                    <el-option 
                      v-for="(item, key) in updateStatusMap" 
                      :key="key" 
                      :label="item" 
                      :value="key"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="关联机器人ID" prop="robotId" class="form-item">
                  <el-input 
                    v-model="form.robotId" 
                    placeholder="请输入关联机器人ID（多个用,分隔）" 
                    class="form-input"
                    prefix-icon="el-icon-user"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 表单分组 - 详细信息 -->
          <div class="form-section">
            <div class="section-title">
              <i class="el-icon-edit"></i> 详细信息
            </div>
            <el-row :gutter="24" class="form-row">
              <el-col :span="24">
                <el-form-item label="更新内容" prop="updateContent" class="form-item">
                  <div class="editor-wrapper">
                    <editor v-model="form.updateContent" :min-height="200"/>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24" class="form-row">
              <el-col :span="24">
                <el-form-item label="更新失败原因" prop="failReason" class="form-item">
                  <el-input 
                    v-model="form.failReason" 
                    type="textarea" 
                    placeholder="更新失败时填写原因（选填）"
                    :rows="4"
                    class="form-textarea"
                    resize="vertical"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-form>
      </div>

      <!-- 底部按钮区 -->
      <div slot="footer" class="dialog-footer">
        <el-button 
          type="default" 
          @click="cancel"
          class="btn-cancel"
        >
          <i class="el-icon-close"></i> 取消
        </el-button>
        <el-button 
          type="primary" 
          @click="submitForm"
          class="btn-confirm"
          :loading="submitLoading"
        >
          <i class="el-icon-check"></i> 确认提交
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAppUpdate, getAppUpdate, addAppUpdate } from "@/api/app/appUpdate"
import { listAppLibrary } from "@/api/app/appLibrary" // 导入应用库接口

export default {
  name: "AppUpdate",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 提交加载状态
      submitLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 应用更新记录表格数据
      appUpdateList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        appId: null,
        updateVersion: null,
        updateContent: null,
        updateStatus: null,
        robotId: null,
        failReason: null,
      },
      // 表单参数
      form: {},
      // 更新状态枚举映射（根据实际业务调整值和名称）
      updateStatusMap: {
        0: "待更新",
        1: "更新中",
        2: "更新成功",
        3: "更新失败"
      },
      // 更新状态标签样式映射
      updateStatusTagMap: {
        0: "info",     // 待更新-灰色
        1: "warning",  // 更新中-黄色
        2: "success",  // 更新成功-绿色
        3: "danger"    // 更新失败-红色
      },
      // 应用库列表
      appLibraryList: [],
      // 应用库加载状态
      appLibraryLoading: false,
      // 表单校验
      rules: {
        appId: [
          { required: true, message: "关联应用库不能为空", trigger: "change" }
        ],
        updateVersion: [
          { required: true, message: "更新版本号不能为空", trigger: "blur" }
        ],
        updateContent: [
          { required: true, message: "更新内容不能为空", trigger: "blur" }
        ],
        updateStatus: [
          { required: true, message: "更新状态不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.loadAppLibraryList() // 加载应用库列表
  },
  methods: {
    /** 查询应用更新记录列表 */
    getList() {
      this.loading = true
      listAppUpdate(this.queryParams).then(response => {
        this.appUpdateList = response.rows
        this.total = response.total
        this.loading = false
      })
    },

    /** 加载应用库列表 */
    loadAppLibraryList(query = '') {
      this.appLibraryLoading = true
      listAppLibrary({
        pageNum: 1,
        pageSize: 100, // 加载足够多的应用库数据
        appName: query // 支持按名称搜索
      }).then(response => {
        this.appLibraryList = response.rows || []
        this.appLibraryLoading = false
      }).catch(() => {
        this.appLibraryLoading = false
      })
    },

    /** 远程搜索应用库 */
    searchAppLibrary(query) {
      this.loadAppLibraryList(query)
    },

    /** 根据appId获取应用名称 */
    getAppNameById(appId) {
      if (!appId) return ''
      const app = this.appLibraryList.find(item => item.appId === appId)
      return app ? app.appName : ''
    },

    /** 处理应用库选择变更 */
    handleAppIdChange() {
      // 可选：添加应用库选择后的额外逻辑
    },

    // 格式化ISO时间为yyyy-MM-dd HH:mm:ss
    formatIsoTime(isoTime) {
      if (!isoTime) return '-';
      const date = new Date(isoTime);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hour = String(date.getHours()).padStart(2, '0');
      const minute = String(date.getMinutes()).padStart(2, '0');
      const second = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
    },

    // 获取更新状态标签样式
    getUpdateStatusTagType(status) {
      return this.updateStatusTagMap[status] || "default";
    },

    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },

    // 表单重置
    reset() {
      this.form = {
        id: null,
        appId: null,
        updateVersion: null,
        updateContent: null,
        updateStatus: null,
        robotId: null,
        failReason: null,
        updateTime: null,
        createTime: null
      }
      this.submitLoading = false
      this.resetForm("form")
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        appId: null,
        updateVersion: null,
        updateContent: null,
        updateStatus: null,
        robotId: null,
        failReason: null,
      }
      this.resetForm("queryForm")
      this.handleQuery()
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新建应用更新记录"
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.submitLoading = true
          addAppUpdate(this.form).then(response => {
            this.$modal.msgSuccess("新增成功")
            this.open = false
            this.getList()
            this.submitLoading = false
          }).catch(() => {
            this.submitLoading = false
          })
        }
      })
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('app/appUpdate/export', {
        ...this.queryParams
      }, `appUpdate_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
/* 通用间距 */
.mb8 {
  margin-bottom: 8px;
}

/* 搜索区域样式 */
.search-card {
  background: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
}
.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.search-input {
  width: 180px;
}
.search-select {
  width: 200px; /* 调整下拉框宽度 */
}

/* 操作按钮区域 */
.operate-bar {
  padding: 10px 0;
}

/* 表格样式 */
.data-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  text-align: center;
}

/* ========== 弹窗美化样式 ========== */
.update-dialog {
  --el-dialog-border-radius: 12px;
}

.update-dialog /deep/ .el-dialog__header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.update-dialog /deep/ .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.update-dialog /deep/ .el-dialog__body {
  padding: 0;
  background-color: #f9fafb;
}

/* 弹窗内容卡片 */
.dialog-card {
  background: #ffffff;
  margin: 16px;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

/* 表单分组样式 */
.form-section {
  margin-bottom: 24px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #409eff;
  font-size: 16px;
}

/* 表单行样式 */
.form-row {
  margin-bottom: 16px;
}

.form-row:last-child {
  margin-bottom: 0;
}

/* 表单项样式 */
.form-item {
  margin-bottom: 0;
}

.form-item /deep/ .el-form-item__label {
  font-weight: 500;
  color: #4b5563;
}

/* 输入框样式 */
.form-input {
  width: 100%;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.form-input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

/* 下拉选择框样式 */
.form-select {
  width: 100%;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.form-select:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

/* 文本域样式 */
.form-textarea {
  width: 100%;
  border-radius: 6px;
  transition: all 0.2s ease;
  resize: vertical;
}

.form-textarea:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

/* 编辑器容器 */
.editor-wrapper {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.editor-wrapper:focus-within {
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

/* 弹窗底部按钮区 */
.dialog-footer {
  padding: 16px 24px;
  text-align: right;
  background-color: #f9fafb;
  border-top: 1px solid #f0f2f5;
  margin-top: -16px;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

/* 按钮样式 */
.btn-cancel {
  padding: 8px 20px;
  border-radius: 6px;
  margin-right: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-confirm {
  padding: 8px 24px;
  border-radius: 6px;
  font-weight: 500;
  background-color: #409eff;
  border-color: #409eff;
  transition: all 0.2s ease;
}

.btn-confirm:hover {
  background-color: #6366f1;
  border-color: #6366f1;
}
</style>