<template>
  <div class="app-container groups-container">
    <!-- 页面标题卡片 -->
    <div class="title-card">
      <div class="title-content">
        <div class="title-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
        </div>
        <div class="title-text">
          <h2>机器人分组管理</h2>
          <p>高效管理机器人分组，优化调度策略</p>
        </div>
      </div>
    </div>

    <!-- 搜索和操作区域 -->
    <div class="search-card">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px" class="demo-form-inline">
        <el-form-item label="分组名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入分组名称"
            clearable
            @keyup.enter.native="handleQuery"
            size="small"
            prefix-icon="el-icon-search"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            size="small"
            @click="handleAdd"
            v-hasPermi="['robots:groups:add']"
            class="add-btn"
          >新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-edit"
            size="small"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['robots:groups:edit']"
            class="edit-btn"
          >修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            size="small"
            :disabled="multiple"
            @click="handleDelete"
            class="delete-btn"
          >删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="info"
            plain
            icon="el-icon-download"
            size="small"
            @click="handleExport"
            class="export-btn"
          >导出</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>
    </div>

    <!-- 数据表格卡片 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="groupsList"
        @selection-change="handleSelectionChange"
        class="groups-table"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="分组ID" align="center" prop="id" width="80" />
        <el-table-column label="分组名称" align="center" prop="name" min-width="150">
          <template slot-scope="scope">
            <div class="group-name">
              <i class="el-icon-price-tag"></i>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分组描述" align="center" prop="description" min-width="200">
          <template slot-scope="scope">
            <div class="group-description">
              {{ scope.row.description || '暂无描述' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createdAt" width="180">
          <template slot-scope="scope">
            <div class="time-cell">
              <i class="el-icon-time"></i>
              <span>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d} {h}:{i}') }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" align="center" prop="updatedAt" width="180">
          <template slot-scope="scope">
            <div class="time-cell">
              <i class="el-icon-refresh"></i>
              <span>{{ parseTime(scope.row.updatedAt, '{y}-{m}-{d} {h}:{i}') }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              class="edit-link"
            >修改</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              class="delete-link"
            >删除</el-button>
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
    </div>

    <!-- 添加或修改机器人分组对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body @close="cancel" class="groups-dialog">
      <div class="dialog-content">
        <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
          <el-row>
            <el-col :span="24">
              <el-form-item label="分组名称" prop="name">
                <el-input
                  v-model="form.name"
                  placeholder="请输入分组名称"
                  clearable
                  maxlength="50"
                  show-word-limit
                  prefix-icon="el-icon-price-tag"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="分组描述" prop="description">
                <el-input
                  v-model="form.description"
                  placeholder="请输入分组描述"
                  type="textarea"
                  rows="4"
                  clearable
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel" size="small">取消</el-button>
        <el-button type="primary" @click="submitForm" size="small">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['robots:groups:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="groupsList" @selection-change="handleSelectionChange" style="width: 100%" stripe highlight-current-row>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="分组ID" align="center" prop="id" width="70" show-overflow-tooltip />
      <el-table-column label="分组名称" align="center" prop="name" width="140" show-overflow-tooltip />
      <el-table-column label="分组描述" align="center" prop="description" width="200" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createdAt" width="130" show-overflow-tooltip>
        <template slot-scope="scope">
          <i class="el-icon-time"></i>
          <span style="margin-left: 5px;">{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updatedAt" width="130" show-overflow-tooltip>
        <template slot-scope="scope">
          <i class="el-icon-time"></i>
          <span style="margin-left: 5px;">{{ parseTime(scope.row.updatedAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="140">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['robots:groups:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['robots:groups:remove']"
          >删除</el-button>
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

    <!-- 添加或修改机器人分组对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="550px" append-to-body @close="cancel">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-row>
          <el-col :span="24">
            <el-form-item label="分组名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入分组名称" clearable maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="分组描述" prop="description">
              <el-input v-model="form.description" placeholder="请输入分组描述" type="textarea" rows="3" clearable maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listGroups, getGroups, delGroups, addGroups, updateGroups } from "@/api/robots/groups"

export default {
  name: "Groups",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 机器人分组表格数据
      groupsList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "分组名称不能为空", trigger: "blur" }
        ],
        createdAt: [
          { required: true, message: "创建时间不能为空", trigger: "blur" }
        ],
        updatedAt: [
          { required: true, message: "更新时间不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询机器人分组列表 */
    getList() {
      this.loading = true
      listGroups(this.queryParams).then(response => {
        this.groupsList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮 - 快速关闭对话框
    cancel() {
      this.open = false
      this.$nextTick(() => {
        this.reset()
      })
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        name: null,
        description: null,
        createdAt: null,
        updatedAt: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加机器人分组"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getGroups(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改机器人分组"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateGroups(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addGroups(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除机器人分组编号为"' + ids + '"的数据项？').then(function() {
        return delGroups(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('robots/groups/export', {
        ...this.queryParams
      }, `groups_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style lang="scss" scoped>
.groups-container {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: calc(100vh - 84px);

  .title-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    padding: 30px;
    margin-bottom: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);

    .title-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .title-icon {
        width: 60px;
        height: 60px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);

        svg {
          width: 30px;
          height: 30px;
        }
      }

      .title-text {
        h2 {
          margin: 0;
          font-size: 28px;
          font-weight: 600;
          color: #2c3e50;
          background: linear-gradient(135deg, #667eea, #764ba2);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }

        p {
          margin: 5px 0 0 0;
          color: #7f8c8d;
          font-size: 14px;
        }
      }
    }
  }

  .search-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    padding: 25px;
    margin-bottom: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);

    .demo-form-inline {
      margin-bottom: 20px;
    }

    .add-btn {
      background: linear-gradient(135deg, #667eea, #764ba2);
      border: none;
      color: white;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
      }
    }

    .edit-btn {
      background: linear-gradient(135deg, #4CAF50, #45a049);
      border: none;
      color: white;
      transition: all 0.3s ease;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
      }
    }

    .delete-btn {
      background: linear-gradient(135deg, #f44336, #d32f2f);
      border: none;
      color: white;
      transition: all 0.3s ease;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(244, 67, 54, 0.4);
      }
    }

    .export-btn {
      background: linear-gradient(135deg, #2196F3, #1976D2);
      border: none;
      color: white;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(33, 150, 243, 0.4);
      }
    }
  }

  .table-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    padding: 25px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);

    .groups-table {
      ::v-deep .el-table__header-wrapper {
        background: linear-gradient(135deg, #f5f7fa, #c3cfe2);
        border-radius: 8px 8px 0 0;
      }

      ::v-deep .el-table__header th {
        background: transparent;
        color: #2c3e50;
        font-weight: 600;
        border-bottom: 2px solid #e1e8ed;
        height: 50px;
        line-height: 50px;
      }

      ::v-deep .el-table__body-wrapper {
        ::v-deep .el-table__row {
          height: 60px;

          td {
            height: 60px;
            line-height: 20px;
            padding: 0 12px;
            vertical-align: middle;

            .cell {
              display: flex;
              align-items: center;
              height: 100%;
              overflow: hidden;
            }
          }
        }
      }

      ::v-deep .el-table__row {
        transition: all 0.3s ease;

        &:hover {
          background: rgba(102, 126, 234, 0.05);
          transform: scale(1.01);
        }
      }

      .group-name {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 500;
        color: #2c3e50;
        height: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        i {
          color: #667eea;
          font-size: 16px;
          flex-shrink: 0;
        }

        span {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .group-description {
        color: #7f8c8d;
        font-style: italic;
        height: 100%;
        display: flex;
        align-items: center;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .time-cell {
        display: flex;
        align-items: center;
        gap: 6px;
        height: 100%;
        font-size: 13px;
        color: #666;

        i {
          font-size: 14px;
          flex-shrink: 0;
        }

        span {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .edit-link {
        color: #667eea;
        transition: all 0.3s ease;

        &:hover {
          color: #764ba2;
          font-weight: 600;
        }
      }

      .delete-link {
        color: #f44336;
        transition: all 0.3s ease;

        &:hover {
          color: #d32f2f;
          font-weight: 600;
        }
      }
    }
  }
}

.groups-dialog {
  ::v-deep .el-dialog {
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
    overflow: hidden;
  }

  ::v-deep .el-dialog__header {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: white;
    margin: 0;
    padding: 20px 24px;

    .el-dialog__title {
      font-weight: 600;
      font-size: 18px;
    }

    .el-dialog__headerbtn {
      top: 16px;
      right: 20px;

      .el-dialog__close {
        color: white;
        font-size: 20px;

        &:hover {
          color: #e1e8ed;
        }
      }
    }
  }

  ::v-deep .el-dialog__body {
    padding: 30px 24px;
  }

  .dialog-content {
    ::v-deep .el-form-item__label {
      color: #2c3e50;
      font-weight: 500;
    }

    ::v-deep .el-input__inner,
    ::v-deep .el-textarea__inner {
      border-radius: 8px;
      border: 2px solid #e1e8ed;
      transition: all 0.3s ease;

      &:focus {
        border-color: #667eea;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      }
    }
  }

  ::v-deep .el-dialog__footer {
    padding: 20px 24px 24px;
    border-top: 1px solid #e1e8ed;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .groups-container {
    padding: 10px;

    .title-card {
      padding: 20px;

      .title-content {
        flex-direction: column;
        text-align: center;
        gap: 15px;

        .title-text h2 {
          font-size: 24px;
        }
      }
    }

    .search-card,
    .table-card {
      padding: 15px;
    }

    .groups-dialog {
      ::v-deep .el-dialog {
        width: 95% !important;
        margin: 5vh auto !important;
      }
    }
  }
}

// 动画效果
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.title-card,
.search-card,
.table-card {
  animation: fadeInUp 0.6s ease-out;
}

.title-card {
  animation-delay: 0.1s;
}

.search-card {
  animation-delay: 0.2s;
}

.table-card {
  animation-delay: 0.3s;
}
</style>
