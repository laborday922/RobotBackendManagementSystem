<template>
  <div class="app-container">
    <!-- 搜索区域美化 -->
    <div class="search-card">
      <el-form 
        :model="queryParams" 
        ref="queryForm" 
        size="small" 
        :inline="true" 
        v-show="showSearch" 
        label-width="90px"
        class="search-form"
      >
        <el-form-item label="应用编号" prop="appId">
          <el-input
            v-model="queryParams.appId"
            placeholder="请输入应用编号"
            clearable
            @keyup.enter.native="handleQuery"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="应用名称" prop="appName">
          <el-input
            v-model="queryParams.appName"
            placeholder="请输入应用名称"
            clearable
            @keyup.enter.native="handleQuery"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="应用类型" prop="appType">
          <el-select
            v-model="queryParams.appType"
            placeholder="请选择应用类型"
            clearable
            @change="handleQuery"
            class="search-select"
          >
            <el-option 
              v-for="(item, key) in appTypeMap" 
              :key="key" 
              :label="item" 
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-select
            v-model="queryParams.enabled"
            placeholder="请选择启用状态"
            clearable
            @change="handleQuery"
            class="search-select"
          >
            <el-option 
              v-for="(item, key) in enabledMap" 
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
          v-hasPermi="['app:appLibrary:add']"
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
          v-hasPermi="['app:appLibrary:edit']"
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
          v-hasPermi="['app:appLibrary:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['app:appLibrary:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格美化：展示所有字段 + 枚举映射 + 样式优化 -->
    <el-table 
      v-loading="loading" 
      :data="appLibraryList" 
      @selection-change="handleSelectionChange"
      border
      stripe
      highlight-current-row
      class="data-table"
      :header-cell-style="{background: '#f5f7fa', color: '#303133', fontWeight: '500'}"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="应用编号" align="center" prop="appId" min-width="120" />
      <el-table-column label="应用名称" align="center" prop="appName" min-width="120" />
      <el-table-column label="应用类型" align="center" prop="appType" width="100">
        <template slot-scope="scope">
          <el-tag :type="getAppTypeTagType(scope.row.appType)" size="mini">
            {{ appTypeMap[scope.row.appType] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="启用状态" align="center" prop="enabled" width="120">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.enabled"
            :active-value="1"
            :inactive-value="0"
            active-color="#13ce66"
            inactive-color="#ff4949"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['app:appLibrary:edit']"
          />
          <span v-hasNoPermi="['app:appLibrary:edit']" class="status-text">
            <el-tag :type="getEnabledTagType(scope.row.enabled)" size="mini">
              {{ enabledMap[scope.row.enabled] || '未知' }}
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="应用描述" align="center" prop="description" min-width="180" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          {{ formatIsoTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template slot-scope="scope">
          {{ formatIsoTime(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleViewApi(scope.row)"
            v-hasPermi="['app:api:list']"
          >查看详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['app:appLibrary:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['app:appLibrary:remove']"
          >删除</el-button>
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

    <!-- 添加/修改对话框美化 -->
    <el-dialog 
      :title="title" 
      :visible.sync="open" 
      width="600px" 
      append-to-body
      :close-on-click-modal="false"
      class="app-dialog"
    >
      <el-form 
        ref="form" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        size="small"
        class="dialog-form"
      >
        <el-form-item label="应用编号" prop="appId">
          <el-input v-model="form.appId" placeholder="请输入应用编号" />
        </el-form-item>
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="应用类型" prop="appType">
          <el-select v-model="form.appType" placeholder="请选择应用类型">
            <el-option 
              v-for="(item, key) in appTypeMap" 
              :key="key" 
              :label="item" 
              :value="key"
            />
          </el-select>
        </el-form-item>
        
      
        <el-form-item label="应用描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            placeholder="请输入应用描述"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog 
      :title="apiTitle" 
      :visible.sync="apiOpen" 
      width="600px" 
      append-to-body
      :close-on-click-modal="false"
      class="app-dialog"
    >
      <el-form 
        ref="apiForm" 
        :model="apiForm" 
        :rules="apiRules" 
        label-width="100px"
        size="small"
        class="dialog-form"
      >
        <el-form-item label="接口标识" prop="apiKey">
          <el-input v-model="apiForm.apiKey" placeholder="请输入接口标识" />
        </el-form-item>
        <el-form-item label="接口名称" prop="apiName">
          <el-input v-model="apiForm.apiName" placeholder="请输入接口名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="apiForm.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="参数JSON" prop="paramsSchema">
          <el-input
            type="textarea"
            :rows="4"
            v-model="apiForm.paramsSchema"
            placeholder='示例: {"param1":"string"}'
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelApi">取 消</el-button>
        <el-button type="primary" @click="submitApiForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- Param 管理弹窗 -->
    <el-dialog 
      :title="paramTitle" 
      :visible.sync="paramOpen" 
      width="600px" 
      append-to-body
      :close-on-click-modal="false"
      class="app-dialog"
    >
      <el-form 
        ref="paramForm" 
        :model="paramForm" 
        :rules="paramRules" 
        label-width="100px"
        size="small"
        class="dialog-form"
      >
        <el-form-item label="参数标识" prop="paramKey">
          <el-input v-model="paramForm.paramKey" placeholder="请输入参数标识" />
        </el-form-item>
        <el-form-item label="参数名称" prop="paramName">
          <el-input v-model="paramForm.paramName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数类型" prop="paramType">
          <el-select v-model="paramForm.paramType" placeholder="请选择参数类型">
            <el-option label="数字" value="number" />
            <el-option label="字符串" value="string" />
            <el-option label="布尔值" value="boolean" />
            <el-option label="枚举" value="enum" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认值" prop="defaultValue">
          <el-input v-model="paramForm.defaultValue" placeholder="请输入默认值" />
        </el-form-item>
        <el-form-item label="验证规则" prop="validationRule">
          <el-input
            type="textarea"
            :rows="4"
            v-model="paramForm.validationRule"
            placeholder='示例: {"required": true, "min": 1, "max": 100}'
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelParam">取 消</el-button>
        <el-button type="primary" @click="submitParamForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- Constraint 管理弹窗 -->
    <el-dialog 
      :title="constraintTitle" 
      :visible.sync="constraintOpen" 
      width="600px" 
      append-to-body
      :close-on-click-modal="false"
      class="app-dialog"
    >
      <el-form 
        ref="constraintForm" 
        :model="constraintForm" 
        :rules="constraintRules" 
        label-width="100px"
        size="small"
        class="dialog-form"
      >
        <el-form-item label="约束名称" prop="name">
          <el-input v-model="constraintForm.name" placeholder="请输入约束名称" />
        </el-form-item>
        <el-form-item label="约束表达式" prop="expression">
          <el-input
            type="textarea"
            :rows="3"
            v-model="constraintForm.expression"
            placeholder="请输入约束表达式"
          />
        </el-form-item>
        <el-form-item label="错误消息" prop="errorMessage">
          <el-input v-model="constraintForm.errorMessage" placeholder="请输入错误消息" />
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="constraintForm.severity" placeholder="请选择严重程度">
            <el-option label="错误" value="error" />
            <el-option label="警告" value="warning" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelConstraint">取 消</el-button>
        <el-button type="primary" @click="submitConstraintForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- API列表查看对话框 -->
    <el-dialog
      :title="viewApiTitle"
      :visible.sync="viewApiOpen"
      width="1400px"
      append-to-body
      :close-on-click-modal="false"
      class="app-dialog"
    >
      <div class="api-list-container">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <!-- API 标签页 -->
          <el-tab-pane label="API 接口" name="api">
            <el-row class="mb8" type="flex" justify="space-between" align="middle">
              <el-col>
                <span>应用 "{{ currentViewApp ? currentViewApp.appName : '' }}" 的接口列表</span>
              </el-col>
              <el-col>
                <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleAddApi(currentViewApp)">新增接口</el-button>
              </el-col>
            </el-row>
            <el-table
              :data="viewApiList"
              v-loading="viewApiLoading"
              border
              stripe
              size="mini"
              style="width:100%"
            >
              <el-table-column prop="apiKey" label="接口标识" min-width="120" />
              <el-table-column prop="apiName" label="接口名称" min-width="140" />
              <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
              <el-table-column prop="paramsSchema" label="参数JSON" min-width="220">
                <template slot-scope="{row: api}">
                  <el-tooltip class="item" effect="dark" :content="formatJson(api.paramsSchema)" placement="top-start">
                    <span class="text-ellipsis" style="max-width: 210px; display:inline-block;">{{ shortenJson(api.paramsSchema) }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="160">
                <template slot-scope="{row: api}">
                  {{ formatIsoTime(api.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" align="center">
                <template slot-scope="{row: api}">
                  <el-button size="mini" type="text" @click="handleEditApi(api, currentViewApp)">修改</el-button>
                  <el-button size="mini" type="text" @click="handleDeleteApi(api, currentViewApp)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Param 标签页 -->
          <el-tab-pane label="应用参数" name="param">
            <el-row class="mb8" type="flex" justify="space-between" align="middle">
              <el-col>
                <span>应用 "{{ currentViewApp ? currentViewApp.appName : '' }}" 的参数列表</span>
              </el-col>
              <el-col>
                <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleAddParam(currentViewApp)">新增参数</el-button>
              </el-col>
            </el-row>
            <el-table
              :data="viewParamList"
              v-loading="viewParamLoading"
              border
              stripe
              size="mini"
              style="width:100%"
            >
              <el-table-column prop="paramKey" label="参数标识" min-width="120" />
              <el-table-column prop="paramName" label="参数名称" min-width="140" />
              <el-table-column prop="paramType" label="参数类型" width="100" />
              <el-table-column prop="defaultValue" label="默认值" min-width="120" show-overflow-tooltip />
              <el-table-column prop="validationRule" label="验证规则" min-width="180">
                <template slot-scope="{row: param}">
                  <el-tooltip class="item" effect="dark" :content="formatJson(param.validationRule)" placement="top-start">
                    <span class="text-ellipsis" style="max-width: 170px; display:inline-block;">{{ shortenJson(param.validationRule) }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="160">
                <template slot-scope="{row: param}">
                  {{ formatIsoTime(param.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" align="center">
                <template slot-scope="{row: param}">
                  <el-button size="mini" type="text" @click="handleEditParam(param, currentViewApp)">修改</el-button>
                  <el-button size="mini" type="text" @click="handleDeleteParam(param, currentViewApp)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Constraint 标签页 -->
          <el-tab-pane label="约束规则" name="constraint">
            <el-row class="mb8" type="flex" justify="space-between" align="middle">
              <el-col>
                <span>应用 "{{ currentViewApp ? currentViewApp.appName : '' }}" 的约束列表</span>
              </el-col>
              <el-col>
                <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleAddConstraint(currentViewApp)">新增约束</el-button>
              </el-col>
            </el-row>
            <el-table
              :data="viewConstraintList"
              v-loading="viewConstraintLoading"
              border
              stripe
              size="mini"
              style="width:100%"
            >
              <el-table-column prop="name" label="约束名称" min-width="140" />
              <el-table-column prop="expression" label="约束表达式" min-width="200" show-overflow-tooltip />
              <el-table-column prop="errorMessage" label="错误消息" min-width="180" show-overflow-tooltip />
              <el-table-column prop="severity" label="严重程度" width="100">
                <template slot-scope="{row: constraint}">
                  <el-tag :type="constraint.severity === 'error' ? 'danger' : 'warning'" size="mini">
                    {{ constraint.severity === 'error' ? '错误' : '警告' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="160">
                <template slot-scope="{row: constraint}">
                  {{ formatIsoTime(constraint.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" align="center">
                <template slot-scope="{row: constraint}">
                  <el-button size="mini" type="text" @click="handleEditConstraint(constraint, currentViewApp)">修改</el-button>
                  <el-button size="mini" type="text" @click="handleDeleteConstraint(constraint, currentViewApp)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeViewApiDialog">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  listAppLibrary, 
  getAppLibrary, 
  delAppLibrary, 
  addAppLibrary, 
  updateAppLibrary,
  changeStatus 
} from "@/api/app/appLibrary"
import {
  listApi,
  getApi,
  addApi,
  updateApi,
  delApi
} from "@/api/app/api"
import {
  listParam,
  getParam,
  addParam,
  updateParam,
  delParam
} from "@/api/app/param"
import {
  listConstraint,
  getConstraint,
  addConstraint,
  updateConstraint,
  delConstraint
} from "@/api/app/constraint"

export default {
  name: "AppLibrary",
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
      // 应用库表格数据
      appLibraryList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        appId: null,
        appName: null,
        appType: null,
        enabled: null,
      },
      // 表单参数
      form: {},
      // 枚举映射：应用类型
      appTypeMap: {
        0: "交互类",
        1: "控制类",
        2: "监控类"
      },
      // 枚举映射：启用状态
      enabledMap: {
        0: "禁用",
        1: "启用"
      },
      // 标签类型映射
      appTypeTagMap: {
        0: "primary",   // 交互类-蓝色
        1: "success",   // 控制类-绿色
        2: "info"       // 监控类-灰色
      },
      enabledTagMap: {
        0: "danger",    // 禁用-红色
        1: "success"    // 启用-绿色
      },
      // 每个应用对应的API列表缓存
      appApiMap: {},
      // API 管理弹窗
      apiOpen: false,
      apiTitle: "",
      currentApp: null,
      apiForm: {
        id: null,
        appId: null,
        apiKey: "",
        apiName: "",
        description: "",
        paramsSchema: "{}",
        enabled: 1,
        createdAt: null
      },
      apiRules: {
        apiKey: [
          { required: true, message: "接口标识不能为空", trigger: "blur" }
        ],
        apiName: [
          { required: true, message: "接口名称不能为空", trigger: "blur" }
        ],
        paramsSchema: [
          { required: true, message: "参数Schema不能为空", trigger: "blur" },
          { validator: (rule, value, callback) => {
              try {
                JSON.parse(value)
                callback()
              } catch (e) {
                callback(new Error('参数JSON格式错误'))
              }
            }, trigger: 'blur'
          }
        ]
      },
      // API列表查看弹窗
      viewApiOpen: false,
      viewApiTitle: "",
      currentViewApp: null,
      viewApiList: [],
      viewApiLoading: false,
      activeTab: "api", // 默认激活API标签页
      // Param列表
      viewParamList: [],
      viewParamLoading: false,
      // Constraint列表
      viewConstraintList: [],
      viewConstraintLoading: false,
      // 表单校验
      rules: {
        appId: [
          { required: true, message: "应用编号不能为空", trigger: "blur" }
        ],
        appName: [
          { required: true, message: "应用名称不能为空", trigger: "blur" }
        ],
        appType: [
          { required: true, message: "应用类型不能为空", trigger: "change" }
        ],
        enabled: [
          { required: true, message: "启用状态不能为空", trigger: "change" }
        ]
      },
      // Param 管理弹窗
      paramOpen: false,
      paramTitle: "",
      paramForm: {
        id: null,
        appId: null,
        paramKey: "",
        paramName: "",
        paramType: "",
        defaultValue: "",
        validationRule: "",
        createdAt: null
      },
      paramRules: {
        paramKey: [
          { required: true, message: "参数标识不能为空", trigger: "blur" }
        ],
        paramName: [
          { required: true, message: "参数名称不能为空", trigger: "blur" }
        ],
        paramType: [
          { required: true, message: "参数类型不能为空", trigger: "blur" }
        ],
        validationRule: [
          { validator: (rule, value, callback) => {
              if (!value) {
                callback()
                return
              }
              try {
                JSON.parse(value)
                callback()
              } catch (e) {
                callback(new Error('验证规则JSON格式错误'))
              }
            }, trigger: 'blur'
          }
        ]
      },
      // Constraint 管理弹窗
      constraintOpen: false,
      constraintTitle: "",
      constraintForm: {
        id: null,
        appId: null,
        name: "",
        expression: "",
        errorMessage: "",
        severity: "error",
        createdAt: null
      },
      constraintRules: {
        name: [
          { required: true, message: "约束名称不能为空", trigger: "blur" }
        ],
        expression: [
          { required: true, message: "约束表达式不能为空", trigger: "blur" }
        ],
        errorMessage: [
          { required: true, message: "错误消息不能为空", trigger: "blur" }
        ],
        severity: [
          { required: true, message: "严重程度不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询应用库列表 */
    getList() {
      this.loading = true
      listAppLibrary(this.queryParams).then(response => {
        this.appLibraryList = response.rows
        this.total = response.total
        this.loading = false
      })
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
    shortenJson(value) {
      if (!value) return '-'
      const str = typeof value === 'string' ? value : JSON.stringify(value)
      return str.length > 40 ? str.substring(0, 40) + '...': str
    },
    formatJson(value) {
      if (!value) return ''
      try {
        return JSON.stringify(typeof value === 'string' ? JSON.parse(value) : value, null, 2)
      } catch (error) {
        return (typeof value === 'string' ? value : JSON.stringify(value))
      }
    },
    // 获取应用类型标签样式
    getAppTypeTagType(type) {
      return this.appTypeTagMap[type] || "default";
    },
    // 获取启用状态标签样式
    getEnabledTagType(status) {
      return this.enabledTagMap[status] || "default";
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
        appName: null,
        appType: null,
        enabled: null,
        createTime: null,
        updateTime: null,
        description: null,
        version: null
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
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        appId: null,
        appName: null,
        appType: null,
        enabled: null,
      }
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
      this.title = "添加应用库"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getAppLibrary(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改应用库"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateAppLibrary(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addAppLibrary(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除应用库编号为"' + ids + '"的数据项？').then(function() {
        return delAppLibrary(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('app/appLibrary/export', {
        ...this.queryParams
      }, `appLibrary_${new Date().getTime()}.xlsx`)
    },
    /** 启用/停用状态修改 */
    handleStatusChange(row) {
      let text = row.enabled === 1 ? "启用" : "停用"
      this.$modal.confirm('确认要"' + text + '"应用库 "' + row.appName + '" 吗？').then(function() {
        return changeStatus(row.id, row.enabled)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
        // 刷新列表以获取最新数据
        this.getList()
      }).catch(function() {
        // 如果取消或失败，恢复开关状态
        row.enabled = row.enabled === 1 ? 0 : 1
      })
    },
    /** 查看应用API列表 */
    handleViewApi(app) {
      this.currentViewApp = app
      this.viewApiTitle = ` ${app.appName}`
      this.viewApiOpen = true
      this.activeTab = "api"
      // 清空旧数据
      this.viewApiList = []
      this.viewParamList = []
      this.viewConstraintList = []
      // 发起新请求加载API数据
      this.fetchViewApiList(app)
    },
    /** 获取指定应用API列表（用于查看弹窗） */
    fetchViewApiList(app) {
      this.viewApiLoading = true
      listApi({ appId: app.appId }).then(response => {
        this.viewApiList = response.rows || response.data || []
        this.viewApiLoading = false
      }).catch(() => {
        this.viewApiList = []
        this.viewApiLoading = false
      })
    },
    /** 获取指定应用Param列表（用于查看弹窗） */
    fetchViewParamList(app) {
      this.viewParamLoading = true
      listParam({ appId: app.appId }).then(response => {
        this.viewParamList = response.rows || response.data || []
        this.viewParamLoading = false
      }).catch(() => {
        this.viewParamList = []
        this.viewParamLoading = false
      })
    },
    /** 获取指定应用Constraint列表（用于查看弹窗） */
    fetchViewConstraintList(app) {
      this.viewConstraintLoading = true
      listConstraint({ appId: app.appId }).then(response => {
        this.viewConstraintList = response.rows || response.data || []
        this.viewConstraintLoading = false
      }).catch(() => {
        this.viewConstraintList = []
        this.viewConstraintLoading = false
      })
    },
    /** 关闭查看API弹窗 */
    closeViewApiDialog() {
      this.viewApiOpen = false
      this.currentViewApp = null
      this.viewApiList = []
      this.viewParamList = []
      this.viewConstraintList = []
      this.activeTab = "api"
    },
    /** 标签页切换 */
    handleTabClick(tab) {
      if (!this.currentViewApp) return
      switch (tab.name) {
        case "api":
          if (this.viewApiList.length === 0) {
            this.fetchViewApiList(this.currentViewApp)
          }
          break
        case "param":
          if (this.viewParamList.length === 0) {
            this.fetchViewParamList(this.currentViewApp)
          }
          break
        case "constraint":
          if (this.viewConstraintList.length === 0) {
            this.fetchViewConstraintList(this.currentViewApp)
          }
          break
      }
    },
    handleAddApi(app) {
      this.apiForm = {
        id: null,
        appId: app.appId,
        apiKey: "",
        apiName: "",
        description: "",
        paramsSchema: "{}",
        enabled: 1,
        createdAt: null
      }
      this.apiTitle = `新增接口（${app.appName}）`
      this.apiOpen = true
      this.currentApp = app
    },
    /** 修改应用API */
    handleEditApi(api, app) {
      getApi(api.id).then(response => {
        const data = response.data || api
        this.apiForm = {
          id: data.id,
          appId: data.appId || app.appId,
          apiKey: data.apiKey,
          apiName: data.apiName,
          description: data.description || "",
          paramsSchema: data.paramsSchema || "{}",
          enabled: data.enabled === undefined ? 1 : data.enabled,
          createdAt: data.createdAt
        }
        this.apiTitle = `修改接口（${app.appName}）`
        this.apiOpen = true
        this.currentApp = app
      })
    },
    /** 删除应用API */
    handleDeleteApi(api, app) {
      this.$modal.confirm(`是否确认删除接口 "${api.apiName || api.apiKey}" ?`).then(() => {
        return delApi(api.id)
      }).then(() => {
        this.$modal.msgSuccess("删除成功")
        if (this.viewApiOpen && this.currentViewApp && this.currentViewApp.appId === app.appId) {
          this.fetchViewApiList(app)
        }
      }).catch(() => {})
    },
    /** 提交API表单 */
    submitApiForm() {
      this.$refs["apiForm"].validate(valid => {
        if (valid) {
          const action = this.apiForm.id ? updateApi : addApi
          action(this.apiForm).then(() => {
            this.$modal.msgSuccess(this.apiForm.id ? "修改成功" : "新增成功")
            this.apiOpen = false
            if (this.viewApiOpen && this.currentViewApp && this.currentViewApp.appId === this.apiForm.appId) {
              this.fetchViewApiList(this.currentViewApp)
            }
          })
        }
      })
    },
    /** API 弹窗取消 */
    cancelApi() {
      this.apiOpen = false
      this.currentApp = null
      this.apiForm = {
        id: null,
        appId: null,
        apiKey: "",
        apiName: "",
        description: "",
        paramsSchema: "{}",
        enabled: 1,
        createdAt: null
      }
      this.resetForm("apiForm")
    },
    // Param CRUD 方法
    handleAddParam(app) {
      this.paramForm = {
        id: null,
        appId: app.appId,
        paramKey: "",
        paramName: "",
        paramType: "",
        defaultValue: "",
        validationRule: "",
        createdAt: null
      }
      this.paramTitle = `新增参数（${app.appName}）`
      this.paramOpen = true
    },
    handleEditParam(param, app) {
      getParam(param.id).then(response => {
        const data = response.data || param
        this.paramForm = {
          id: data.id,
          appId: data.appId || app.appId,
          paramKey: data.paramKey,
          paramName: data.paramName,
          paramType: data.paramType,
          defaultValue: data.defaultValue || "",
          validationRule: data.validationRule || "",
          createdAt: data.createdAt
        }
        this.paramTitle = `修改参数（${app.appName}）`
        this.paramOpen = true
      })
    },
    handleDeleteParam(param, app) {
      this.$modal.confirm(`是否确认删除参数 "${param.paramName || param.paramKey}" ?`).then(() => {
        return delParam(param.id)
      }).then(() => {
        this.$modal.msgSuccess("删除成功")
        if (this.viewApiOpen && this.currentViewApp && this.currentViewApp.appId === app.appId) {
          this.fetchViewParamList(app)
        }
      }).catch(() => {})
    },
    submitParamForm() {
      this.$refs["paramForm"].validate(valid => {
        if (valid) {
          const action = this.paramForm.id ? updateParam : addParam
          action(this.paramForm).then(() => {
            this.$modal.msgSuccess(this.paramForm.id ? "修改成功" : "新增成功")
            this.paramOpen = false
            if (this.viewApiOpen && this.currentViewApp && this.currentViewApp.appId === this.paramForm.appId) {
              this.fetchViewParamList(this.currentViewApp)
            }
          })
        }
      })
    },
    cancelParam() {
      this.paramOpen = false
      this.paramForm = {
        id: null,
        appId: null,
        paramKey: "",
        paramName: "",
        paramType: "",
        defaultValue: "",
        validationRule: "",
        createdAt: null
      }
      this.resetForm("paramForm")
    },
    // Constraint CRUD 方法
    handleAddConstraint(app) {
      this.constraintForm = {
        id: null,
        appId: app.appId,
        name: "",
        expression: "",
        errorMessage: "",
        severity: "error",
        createdAt: null
      }
      this.constraintTitle = `新增约束（${app.appName}）`
      this.constraintOpen = true
    },
    handleEditConstraint(constraint, app) {
      getConstraint(constraint.id).then(response => {
        const data = response.data || constraint
        this.constraintForm = {
          id: data.id,
          appId: data.appId || app.appId,
          name: data.name,
          expression: data.expression,
          errorMessage: data.errorMessage,
          severity: data.severity || "error",
          createdAt: data.createdAt
        }
        this.constraintTitle = `修改约束（${app.appName}）`
        this.constraintOpen = true
      })
    },
    handleDeleteConstraint(constraint, app) {
      this.$modal.confirm(`是否确认删除约束 "${constraint.name}" ?`).then(() => {
        return delConstraint(constraint.id)
      }).then(() => {
        this.$modal.msgSuccess("删除成功")
        if (this.viewApiOpen && this.currentViewApp && this.currentViewApp.appId === app.appId) {
          this.fetchViewConstraintList(app)
        }
      }).catch(() => {})
    },
    submitConstraintForm() {
      this.$refs["constraintForm"].validate(valid => {
        if (valid) {
          const action = this.constraintForm.id ? updateConstraint : addConstraint
          action(this.constraintForm).then(() => {
            this.$modal.msgSuccess(this.constraintForm.id ? "修改成功" : "新增成功")
            this.constraintOpen = false
            if (this.viewApiOpen && this.currentViewApp && this.currentViewApp.appId === this.constraintForm.appId) {
              this.fetchViewConstraintList(this.currentViewApp)
            }
          })
        }
      })
    },
    cancelConstraint() {
      this.constraintOpen = false
      this.constraintForm = {
        id: null,
        appId: null,
        name: "",
        expression: "",
        errorMessage: "",
        severity: "error",
        createdAt: null
      }
      this.resetForm("constraintForm")
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
  width: 150px;
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

/* API列表容器样式 */
.api-list-container {
  max-height: 600px;
  overflow-y: auto;
}

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  text-align: center;
}

/* 对话框样式 */
.app-dialog /deep/ .el-dialog__header {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 10px;
}
.dialog-form {
  padding: 10px 0;
}
.dialog-footer {
  text-align: right;
  padding-top: 10px;
  border-top: 1px solid #e4e7ed;
}

/* 状态文本样式 */
.status-text {
  display: inline-block;
  margin-left: 5px;
}
</style>