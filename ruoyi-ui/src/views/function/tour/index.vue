<template>
  <div class="card">
    <div class="card-header">
      <div class="card-title">
        <i class="fas fa-chalkboard-teacher"></i> 智能讲解
      </div>
      <div class="robot-selector">
        <span class="badge"><i class="fas fa-robot"></i> 导览机器人：</span>
        <el-select v-model="selectedRobotId" placeholder="请选择机器人" style="width:200px;" @change="onRobotChange">
          <el-option v-for="r in robotList" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
      </div>
    </div>

    <div class="card-body">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 讲解通用设置 -->
        <el-tab-pane label="讲解通用设置" name="general">
          <div class="sub-card">
            <div class="form-row">
              <div class="form-group">
                <label>当前地图</label>
                <el-select v-model="generalConfig.mapId" placeholder="请选择地图" @change="onGeneralMapChange">
                  <el-option v-for="map in currentRobotMaps" :key="map.mapId" :label="map.mapName" :value="map.mapId" />
                </el-select>
              </div>
              <div class="form-group">
                <label>讲解路线</label>
                <el-select v-model="generalConfig.routeId" placeholder="请选择路线">
                  <el-option v-for="route in currentRobotRoutes" :key="route.routeId" :label="route.routeName" :value="route.routeId" />
                </el-select>
              </div>
              <div class="form-group">
                <label>讲解音色</label>
                <el-select v-model="generalConfig.voice" placeholder="请选择音色">
                  <el-option label="温柔女声" value="温柔女声" />
                  <el-option label="稳重男声" value="稳重男声" />
                  <el-option label="童声" value="童声" />
                </el-select>
              </div>
            </div>

            <div class="form-group">
              <el-checkbox v-model="generalConfig.voiceInteraction">语音交互（唤醒词可暂停）</el-checkbox>
            </div>

            <div class="form-group">
              <label>开始讲解口令</label>
              <el-input v-model="generalConfig.startCommand" placeholder="开始讲解" maxlength="50" show-word-limit />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>运动前提示播报</label>
                <el-input v-model="generalConfig.beforeTip" placeholder="即将开始讲解，请跟随我" maxlength="200" show-word-limit />
              </div>
              <div class="form-group">
                <label>讲解结束播报</label>
                <el-input v-model="generalConfig.endTip" placeholder="本次讲解结束，谢谢" maxlength="200" show-word-limit />
              </div>
            </div>

            <div class="form-group">
              <label>讲解结束后动作</label>
              <el-radio-group v-model="generalConfig.afterAction">
                <el-radio label="stay">留在原地</el-radio>
                <el-radio label="charge">返回充电桩</el-radio>
              </el-radio-group>
            </div>
          </div>
        </el-tab-pane>

        <!-- 讲解内容管理 -->
        <el-tab-pane label="讲解内容管理" name="content">
          <div class="sub-card">
            <div class="table-header">
              <span><i class="fas fa-list"></i> 讲解内容列表</span>
              <div class="table-actions">
                <el-button type="primary" size="small" @click="openDrawer('add')">
                  <i class="fas fa-plus"></i> 新增讲解内容
                </el-button>
                <el-button size="small" @click="triggerContentImport">
                  <i class="fas fa-upload"></i> 导入
                </el-button>
                <input type="file" ref="contentImportInput" class="hidden-input" accept=".json" @change="handleContentImport">
                <el-button size="small" @click="exportContent">
                  <i class="fas fa-download"></i> 导出
                </el-button>
                <el-button size="small" @click="copyContent">
                  <i class="fas fa-copy"></i> 复制
                </el-button>
                <el-button size="small" type="danger" plain @click="batchDeleteContent">
                  <i class="fas fa-trash"></i> 批量删除
                </el-button>
              </div>
            </div>

            <el-table :data="contentList" border style="width: 100%" @selection-change="handleContentSelectionChange">
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="讲解点名称" prop="pointName" min-width="150" show-overflow-tooltip />
              <el-table-column label="讲解点描述" prop="pointDesc" min-width="200" show-overflow-tooltip />
              <el-table-column label="播报类型" width="100" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.broadcastType === 'audio' ? 'success' : 'info'">
                    {{ scope.row.broadcastType === 'audio' ? '音频' : '文本' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="音色" prop="voiceType" width="100" show-overflow-tooltip />
              <el-table-column label="语速" prop="speechRate" width="80" align="center">
                <template slot-scope="scope">{{ scope.row.speechRate }}%</template>
              </el-table-column>
              <el-table-column label="操作" width="220" align="center" fixed="right">
                <template slot-scope="scope">
                  <div class="table-actions-buttons">
                    <el-button size="mini" type="text" @click="openDrawer('edit', scope.row)">
                      <i class="fas fa-edit"></i> 编辑
                    </el-button>
                    <el-button size="mini" type="text" @click="previewContent(scope.row)">
                      <i class="fas fa-play"></i> 预览
                    </el-button>
                    <el-button size="mini" type="text" class="danger" @click="deleteContent(scope.row.contentId)">
                      <i class="fas fa-trash"></i> 删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-wrap">
              <pagination
                v-show="contentTotal > 0"
                :total="contentTotal"
                :page.sync="contentQuery.pageNum"
                :limit.sync="contentQuery.pageSize"
                @pagination="loadContentList"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 讲解路线管理 -->
        <el-tab-pane label="讲解路线管理" name="route">
          <div class="sub-card">
            <div class="table-header">
              <span><i class="fas fa-route"></i> 讲解路线列表</span>
              <div class="table-actions">
                <el-button type="primary" size="small" @click="addRoute">
                  <i class="fas fa-plus"></i> 新增讲解路线
                </el-button>
                <el-button size="small" @click="triggerRouteImport">
                  <i class="fas fa-upload"></i> 导入
                </el-button>
                <input type="file" ref="routeImportInput" class="hidden-input" accept=".json" @change="handleRouteImport">
                <el-button size="small" @click="exportRoute">
                  <i class="fas fa-download"></i> 导出
                </el-button>
              </div>
            </div>

            <div class="table-wrapper">
              <el-table :data="currentRobotRoutes" border style="width: 100%" fit>
                <el-table-column label="路线名称" prop="routeName" min-width="180" show-overflow-tooltip />
                <el-table-column label="所属地图" min-width="150" show-overflow-tooltip>
                  <template slot-scope="scope">
                    {{ getMapName(scope.row.mapId) }}
                  </template>
                </el-table-column>
                <el-table-column label="点位数量" prop="pointCount" width="100" align="center" />
                <el-table-column label="状态" width="100" align="center">
                  <template slot-scope="scope">
                    <el-switch
                      v-model="scope.row.status"
                      active-value="1"
                      inactive-value="0"
                      @change="toggleRouteStatus(scope.row)"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="240" align="center">
                  <template slot-scope="scope">
                    <div class="table-actions-buttons">
                      <el-button size="mini" type="text" @click="openRouteConfigDrawer(scope.row)">
                        <i class="fas fa-cog"></i> 配置
                      </el-button>
                      <el-button size="mini" type="text" @click="editRoute(scope.row)">
                        <i class="fas fa-edit"></i> 编辑
                      </el-button>
                      <el-button size="mini" type="text" class="danger" @click="deleteSingleRoute(scope.row.routeId)">
                        <i class="fas fa-trash"></i> 删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="footer-actions">
        <el-button type="primary" size="large" @click="saveTourConfig">
          <i class="fas fa-save"></i> 保存讲解配置
        </el-button>
      </div>
    </div>

    <!-- 讲解内容抽屉 -->
    <el-drawer
      :title="drawerMode === 'add' ? '新增讲解内容' : '编辑讲解内容'"
      :visible.sync="drawerVisible"
      direction="rtl"
      size="700px"
      :before-close="closeDrawer"
      :modal="false"
      :wrapperClosable="true"
      :close-on-press-escape="true"
    >
      <div class="drawer-body">
        <el-form :model="drawerForm" label-width="100px" ref="drawerForm" :rules="drawerRules">
          <el-form-item label="讲解点名称" prop="pointName">
            <el-input v-model="drawerForm.pointName" placeholder="例如：企业历程" maxlength="12" show-word-limit />
          </el-form-item>

          <el-form-item label="讲解点描述" prop="pointDesc">
            <el-input type="textarea" v-model="drawerForm.pointDesc" placeholder="可自行添加备注内容" maxlength="50" rows="2" show-word-limit />
          </el-form-item>

          <el-divider></el-divider>

          <el-form-item label="播报类型" prop="broadcastType">
            <el-radio-group v-model="drawerForm.broadcastType">
              <el-radio label="text">播报语</el-radio>
              <el-radio label="audio">音频文件</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item v-if="drawerForm.broadcastType === 'audio'" label="音频文件">
            <el-upload
              class="upload-area"
              drag
              action="#"
              :before-upload="handleAudioUpload"
              :show-file-list="false"
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">将音频文件拖到此处，或<em>点击上传</em></div>
              <div class="el-upload__tip" slot="tip">支持 mp3、wav 格式，大小不超过10MB</div>
            </el-upload>
            <div v-if="drawerForm.audioFile" class="uploaded-file">
              <i class="fas fa-file-audio"></i> {{ drawerForm.audioFile }}
            </div>
          </el-form-item>

          <el-form-item label="播报内容" prop="broadcastText">
            <el-input type="textarea" v-model="drawerForm.broadcastText" placeholder="请输入播报文本" maxlength="4000" rows="3" show-word-limit />
            <div class="inline-variables">
              <el-button size="mini" type="info" plain @click="insertVar('broadcastText', '#点位名称#')">
                <i class="fas fa-tag"></i> 点位名称
              </el-button>
              <el-button size="mini" type="info" plain @click="insertVar('broadcastText', '#机器人名称#')">
                <i class="fas fa-tag"></i> 机器人名称
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="音色">
            <el-select v-model="drawerForm.voiceType" style="width: 100%;">
              <el-option label="温柔女声" value="温柔女声" />
              <el-option label="稳重男声" value="稳重男声" />
              <el-option label="童声" value="童声" />
            </el-select>
          </el-form-item>

          <el-form-item label="语速">
            <el-slider v-model="drawerForm.speechRate" :min="0" :max="100" show-input />
          </el-form-item>

          <el-form-item label="间隔时间">
            <el-input-number v-model="drawerForm.intervalTime" :min="0" :max="10000" :step="100" /> 毫秒
          </el-form-item>

          <el-divider></el-divider>

          <div class="form-row">
            <el-form-item label="手臂动作">
              <el-select v-model="drawerForm.armAction" style="width: 100%;">
                <el-option label="0°" value="0°" />
                <el-option label="45°" value="45°" />
                <el-option label="90°" value="90°" />
                <el-option label="135°" value="135°" />
                <el-option label="180°" value="180°" />
              </el-select>
            </el-form-item>

            <el-form-item label="底盘转角">
              <el-input-number v-model="drawerForm.chassisAngle" :min="-180" :max="180" /> °
            </el-form-item>
          </div>
        </el-form>
      </div>

      <div class="drawer-footer">
        <el-button @click="closeDrawer">取消</el-button>
        <el-button type="primary" @click="saveDrawerForm">保存</el-button>
      </div>
    </el-drawer>

    <!-- 配置路线抽屉 -->
    <el-drawer
      title="配置讲解路线"
      :visible.sync="routeConfigDrawerVisible"
      direction="rtl"
      size="600px"
      :modal="false"
      :wrapperClosable="true"
      :close-on-press-escape="true"
    >
      <div class="drawer-body">
        <el-form :model="routeConfigForm" label-width="100px" ref="routeConfigForm">
          <el-form-item label="路线名称" prop="routeName">
            <el-input v-model="routeConfigForm.routeName" placeholder="请输入路线名称" maxlength="12" show-word-limit />
          </el-form-item>

          <el-form-item label="选择地图" prop="mapId">
            <el-select
              v-model="routeConfigForm.mapId"
              placeholder="请选择地图"
              style="width: 100%;"
              @change="onMapChange"
              clearable
            >
              <el-option
                v-for="map in currentRobotMaps"
                :key="map.mapId"
                :label="map.mapName"
                :value="map.mapId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="选择点位">
            <div style="margin-bottom: 10px;">
              <el-checkbox
                v-model="routeConfigForm.selectAll"
                @change="toggleAllPoints"
                :disabled="!routeConfigForm.mapId || mapPoints.length === 0"
              >全选</el-checkbox>
              <span v-if="routeConfigForm.loadingPoints" style="margin-left: 10px;">
                <i class="el-icon-loading"></i> 加载中...
              </span>
            </div>
            <div class="point-checkbox-grid" v-if="mapPoints && mapPoints.length > 0">
              <el-checkbox
                v-for="point in mapPoints"
                :key="point.sysPointId"
                v-model="routeConfigForm.selectedPoints"
                :label="point.sysPointId"
                @change="updateSelectedPoints"
              >
                {{ point.pointName }}
              </el-checkbox>
            </div>
            <div v-else class="empty-points-tip">
              <i class="el-icon-info"></i>
              {{ routeConfigForm.loadingPoints ? '正在加载点位...' : (routeConfigForm.mapId ? '该地图暂无点位数据，请先在导航页面添加点位' : '请先选择地图') }}
            </div>
          </el-form-item>

          <el-form-item label="关联讲解内容">
            <div class="warning-message" v-if="routeConfigForm.warningMessage">
              <i class="fas fa-exclamation-triangle"></i> {{ routeConfigForm.warningMessage }}
            </div>
            <el-table :data="routeConfigForm.associationList" border style="width: 100%; margin-top: 10px;">
              <el-table-column label="序号" width="60" align="center">
                <template slot-scope="scope">{{ scope.$index + 1 }}</template>
              </el-table-column>
              <el-table-column label="点位名称" prop="pointName" min-width="150" show-overflow-tooltip />
              <el-table-column label="关联讲解内容" width="200">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.contentId" placeholder="请选择" size="small" clearable style="width: 100%;">
                    <el-option
                      v-for="content in contentList"
                      :key="content.contentId"
                      :label="content.pointName"
                      :value="content.contentId"
                    />
                  </el-select>
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
        </el-form>
      </div>

      <div class="drawer-footer">
        <el-button @click="closeRouteConfigDrawer">取消</el-button>
        <el-button type="primary" @click="saveRouteConfig">确定</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getTourGeneral, saveTourGeneral } from "@/api/function/tour";
import { getTourContentList, saveTourContent, deleteTourContent, batchDeleteTourContents } from "@/api/function/tour";
import { getRouteListByRobotId, getRouteDetail, saveRoute, deleteRoute } from "@/api/function/tour";
import { listRobot } from "@/api/mode/robot";
import { getMapList, getPointListByMap, getDefaultPoints } from "@/api/function/map";
import Pagination from "@/components/Pagination";

export default {
  name: "Tour",
  components: { Pagination },
  data() {
    return {
      selectedRobotId: null,
      robotList: [],
      activeTab: 'general',

      currentRobotMaps: [],
      allRoutes: [],

      generalConfig: {
        mapId: null,
        routeId: null,
        voice: '温柔女声',
        voiceInteraction: true,
        startCommand: '开始讲解',
        beforeTip: '即将开始讲解，请跟随我',
        endTip: '本次讲解结束，谢谢',
        afterAction: 'stay'
      },

      contentList: [],
      contentTotal: 0,
      contentQuery: {
        pageNum: 1,
        pageSize: 10
      },
      selectedContentIds: [],

      drawerVisible: false,
      drawerMode: 'add',
      drawerForm: {
        pointName: '',
        pointDesc: '',
        broadcastType: 'text',
        broadcastText: '',
        audioFile: '',
        voiceType: '温柔女声',
        speechRate: 50,
        intervalTime: 0,
        armAction: '0°',
        chassisAngle: 0
      },
      drawerRules: {
        pointName: [
          { required: true, message: '请输入讲解点名称', trigger: 'blur' },
          { max: 12, message: '长度不能超过12个字符', trigger: 'blur' }
        ],
        broadcastText: [
          { required: true, message: '请输入播报内容', trigger: 'blur' }
        ]
      },

      routeConfigDrawerVisible: false,
      currentRouteId: null,
      mapPoints: [],
      routeConfigPrevMapId: null,
      routeConfigMapCache: {},
      routeConfigForm: {
        routeName: '',
        mapId: null,
        selectAll: false,
        selectedPoints: [],
        associationList: [],
        warningMessage: '',
        loadingPoints: false
      }
    };
  },
  computed: {
    currentRobotRoutes() {
      if (!this.selectedRobotId || !this.currentRobotMaps.length) return [];
      const currentRobotMapIds = this.currentRobotMaps.map(m => m.mapId);
      return this.allRoutes.filter(route => currentRobotMapIds.includes(route.mapId));
    }
  },
  async created() {
    await this.loadRobotsByGroup();
    if (this.selectedRobotId) {
      await this.loadMapsForCurrentRobot();
      await this.loadAllRoutes();
      await this.loadGeneralConfig();
      await this.loadContentList();
    }
  },
  methods: {
    loadRobotsByGroup() {
      return listRobot({ groupId: 4, pageNum: 1, pageSize: 100 }).then(response => {
        this.robotList = response.rows || [];
        if (this.robotList.length > 0) {
          this.selectedRobotId = this.robotList[0].id;
        } else {
          this.$message.warning('导览组下没有可用机器人');
        }
      }).catch(error => {
        console.error('获取机器人列表失败:', error);
        this.$message.error('获取机器人列表失败');
      });
    },

    async onRobotChange() {
      this.generalConfig = {
        mapId: null,
        routeId: null,
        voice: '温柔女声',
        voiceInteraction: true,
        startCommand: '开始讲解',
        beforeTip: '即将开始讲解，请跟随我',
        endTip: '本次讲解结束，谢谢',
        afterAction: 'stay'
      };

      await this.loadMapsForCurrentRobot();
      await this.loadAllRoutes();
      await this.loadGeneralConfig();
      await this.loadContentList();
    },

    loadMapsForCurrentRobot() {
      if (!this.selectedRobotId) return Promise.resolve();
      return getMapList({ robotId: this.selectedRobotId }).then(response => {
        let maps = response.rows || response.data || [];
        this.currentRobotMaps = maps.filter(m => m.delFlag === '0' && m.status === '1');

        const hasDefaultMap = this.currentRobotMaps.some(m => m.mapId === 0);
        if (!hasDefaultMap) {
          this.currentRobotMaps.unshift({
            mapId: 0,
            mapName: '默认地图',
            status: '1',
            delFlag: '0',
            pointCount: 0
          });
        }
      }).catch(error => {
        console.error('获取地图列表失败:', error);
        this.currentRobotMaps = [{
          mapId: 0,
          mapName: '默认地图',
          status: '1',
          delFlag: '0',
          pointCount: 0
        }];
      });
    },

    loadAllRoutes() {
      if (!this.selectedRobotId) return Promise.resolve();
      return getRouteListByRobotId(this.selectedRobotId).then(response => {
        this.allRoutes = response.rows || response.data || [];
      }).catch(error => {
        console.error('获取路线列表失败:', error);
        this.allRoutes = [];
      });
    },

    loadGeneralConfig() {
      if (!this.selectedRobotId) return;
      getTourGeneral(this.selectedRobotId).then(response => {
        if (response.data) {
          const data = response.data;
          const mapValid = this.currentRobotMaps.some(m => m.mapId === data.mapId);
          const routeValid = this.currentRobotRoutes.some(r => r.routeId === data.routeId);

          this.generalConfig.mapId = mapValid ? data.mapId : null;
          this.generalConfig.routeId = routeValid ? data.routeId : null;
          this.generalConfig.voice = data.voice || '温柔女声';
          this.generalConfig.voiceInteraction = data.voiceInteraction !== '0';
          this.generalConfig.startCommand = data.startCommand || '开始讲解';
          this.generalConfig.beforeTip = data.beforeTip || '即将开始讲解，请跟随我';
          this.generalConfig.endTip = data.endTip || '本次讲解结束，谢谢';
          this.generalConfig.afterAction = data.afterAction || 'stay';
        }
      }).catch(error => {
        console.error('获取通用配置失败:', error);
      });
    },

    onGeneralMapChange() {
      this.generalConfig.routeId = null;
    },

    loadMapPoints(mapId) {
      console.log('loadMapPoints called, mapId:', mapId);

      if (mapId === null || mapId === undefined || mapId === '') {
        this.mapPoints = [];
        return Promise.resolve();
      }

      const numericMapId = Number(mapId);
      this.routeConfigForm.loadingPoints = true;

      if (numericMapId === 0) {
        return getDefaultPoints().then(response => {
          if (response.code === 200) {
            this.mapPoints = response.data || [];
            if (this.mapPoints.length === 0) {
              this.$message.warning('默认地图下暂无点位数据，请先在导航页面添加点位');
            }
          } else {
            this.mapPoints = [];
            this.$message.error(response.msg || '获取默认地图点位失败');
          }
          this.routeConfigForm.loadingPoints = false;
        }).catch(error => {
          console.error('获取默认地图点位失败:', error);
          this.mapPoints = [];
          this.$message.error('获取默认地图点位失败：' + (error.message || '未知错误'));
          this.routeConfigForm.loadingPoints = false;
        });
      }

      if (!this.selectedRobotId) {
        this.mapPoints = [];
        this.routeConfigForm.loadingPoints = false;
        this.$message.warning('请先选择机器人');
        return Promise.resolve();
      }

      return getPointListByMap(numericMapId, this.selectedRobotId).then(response => {
        this.mapPoints = response.rows || response.data || [];
        this.routeConfigForm.loadingPoints = false;
      }).catch(error => {
        console.error('获取地图点位失败:', error);
        this.mapPoints = [];
        this.$message.error('获取地图点位失败：' + (error.message || '未知错误'));
        this.routeConfigForm.loadingPoints = false;
      });
    },

    async onMapChange(mapId) {
      const prevMapId = this.routeConfigPrevMapId;
      if (prevMapId !== null && prevMapId !== undefined && prevMapId !== '') {
        const prevKey = String(prevMapId);
        this.routeConfigMapCache[prevKey] = {
          selectedPoints: [...(this.routeConfigForm.selectedPoints || [])],
          associationList: [...(this.routeConfigForm.associationList || [])],
          selectAll: !!this.routeConfigForm.selectAll
        };
      }

      this.routeConfigPrevMapId = mapId;

      const key = mapId !== null && mapId !== undefined && mapId !== '' ? String(mapId) : null;
      const cached = key ? this.routeConfigMapCache[key] : null;

      if (cached) {
        this.routeConfigForm.selectedPoints = [...(cached.selectedPoints || [])];
        this.routeConfigForm.associationList = [...(cached.associationList || [])];
        this.routeConfigForm.selectAll = !!cached.selectAll;
        this.updateSelectedPoints();
      } else {
        this.routeConfigForm.selectedPoints = [];
        this.routeConfigForm.associationList = [];
        this.routeConfigForm.selectAll = false;
        this.routeConfigForm.warningMessage = '';
      }

      if (mapId !== null && mapId !== undefined && mapId !== '') {
        await this.loadMapPoints(mapId);
        this.updateSelectedPoints();
      } else {
        this.mapPoints = [];
      }
    },

    getMapName(mapId) {
      const map = this.currentRobotMaps.find(m => m.mapId === mapId);
      return map ? map.mapName : '未知';
    },

    handleContentSelectionChange(selection) {
      this.selectedContentIds = selection.map(item => item.contentId);
    },

    loadContentList() {
      if (!this.selectedRobotId) return;
      getTourContentList(this.selectedRobotId, {
        pageNum: this.contentQuery.pageNum,
        pageSize: this.contentQuery.pageSize
      }).then(response => {
        if (response.rows) {
          this.contentList = response.rows;
          this.contentTotal = response.total || 0;
        } else if (response.data) {
          if (Array.isArray(response.data)) {
            this.contentList = response.data;
            this.contentTotal = response.data.length;
          } else if (response.data.list) {
            this.contentList = response.data.list;
            this.contentTotal = response.data.total || response.data.list.length;
          } else {
            this.contentList = [];
            this.contentTotal = 0;
          }
        } else {
          this.contentList = [];
          this.contentTotal = 0;
        }
      }).catch(error => {
        console.error('获取讲解内容列表失败:', error);
        this.contentList = [];
        this.contentTotal = 0;
      });
    },

    handleTabClick(tab) {
      if (tab.name === 'content') {
        this.loadContentList();
      } else if (tab.name === 'route') {
        this.loadAllRoutes();
      }
    },

    openDrawer(mode, row) {
      this.drawerMode = mode;
      if (mode === 'add') {
        this.drawerForm = {
          pointName: '',
          pointDesc: '',
          broadcastType: 'text',
          broadcastText: '',
          audioFile: '',
          voiceType: '温柔女声',
          speechRate: 50,
          intervalTime: 0,
          armAction: '0°',
          chassisAngle: 0
        };
      } else {
        this.drawerForm = { ...row };
      }
      this.drawerVisible = true;
    },

    closeDrawer() {
      this.drawerVisible = false;
      setTimeout(() => {
        if (this.$refs.drawerForm) {
          this.$refs.drawerForm.resetFields();
        }
      }, 100);
    },

    saveDrawerForm() {
      this.$refs.drawerForm.validate(valid => {
        if (!valid) return;
        const data = { ...this.drawerForm, robotId: this.selectedRobotId };
        saveTourContent(data).then(() => {
          this.$message.success(this.drawerMode === 'add' ? '新增成功' : '修改成功');
          this.closeDrawer();
          this.loadContentList();
        }).catch(error => {
          console.error('保存讲解内容失败:', error);
          this.$message.error('保存失败：' + (error.message || '未知错误'));
        });
      });
    },

    deleteContent(contentId) {
      this.$confirm('确定要删除该讲解内容吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTourContent(contentId).then(() => {
          this.$message.success('删除成功');
          this.loadContentList();
        }).catch(error => {
          console.error('删除失败:', error);
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },

    batchDeleteContent() {
      if (this.selectedContentIds.length === 0) {
        this.$message.warning('请至少选择一条记录');
        return;
      }
      this.$confirm(`确定要删除选中的 ${this.selectedContentIds.length} 条记录吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        batchDeleteTourContents(this.selectedContentIds).then(() => {
          this.$message.success('批量删除成功');
          this.loadContentList();
        }).catch(error => {
          console.error('批量删除失败:', error);
          this.$message.error('批量删除失败');
        });
      }).catch(() => {});
    },

    previewContent(row) {
      this.$alert(
        `<div style="max-height: 400px; overflow-y: auto;">
          <p><strong>讲解点名称：</strong>${row.pointName || '-'}</p>
          <p><strong>讲解点描述：</strong>${row.pointDesc || '-'}</p>
          <p><strong>播报内容：</strong>${row.broadcastText || '-'}</p>
          <p><strong>音色：</strong>${row.voiceType || '-'}</p>
          <p><strong>语速：</strong>${row.speechRate || 0}%</p>
          <p><strong>间隔时间：</strong>${row.intervalTime || 0}ms</p>
        </div>`,
        '预览讲解内容',
        { dangerouslyUseHTMLString: true, confirmButtonText: '关闭' }
      );
    },

    insertVar(field, variable) {
      this.drawerForm[field] = (this.drawerForm[field] || '') + variable;
    },

    handleAudioUpload(file) {
      const isValid = file.type === 'audio/mpeg' || file.type === 'audio/wav';
      if (!isValid) { this.$message.error('只支持 mp3、wav 格式'); return false; }
      if (file.size / 1024 / 1024 > 10) { this.$message.error('文件大小不能超过10MB'); return false; }
      this.drawerForm.audioFile = file.name;
      this.$message.success('音频文件已选择');
      return false;
    },

    triggerContentImport() {
      this.$refs.contentImportInput.click();
    },

    handleContentImport(event) {
      const file = event.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const importedData = JSON.parse(e.target.result);
          let contentsToImport = Array.isArray(importedData) ? importedData : [importedData];
          if (contentsToImport.length === 0) { this.$message.warning('导入数据为空'); return; }
          const isValid = contentsToImport.every(item => item.pointName && typeof item.pointName === 'string');
          if (!isValid) { this.$message.error('导入文件格式不正确，缺少必要字段'); return; }
          this.$confirm(`确定要导入 ${contentsToImport.length} 条讲解内容吗？`, '导入确认', {
            confirmButtonText: '确定导入', cancelButtonText: '取消', type: 'info'
          }).then(() => {
            let successCount = 0, failCount = 0;
            const promises = contentsToImport.map(content => {
              return saveTourContent({ ...content, robotId: this.selectedRobotId, contentId: undefined })
                .then(() => successCount++).catch(() => failCount++);
            });
            Promise.all(promises).then(() => {
              if (successCount > 0) {
                this.$message.success(`导入完成：成功 ${successCount} 条，失败 ${failCount} 条`);
                this.loadContentList();
              } else {
                this.$message.error('导入失败，请检查文件格式');
              }
            });
          }).catch(() => {});
        } catch (error) {
          this.$message.error('文件格式错误，请上传正确的 JSON 文件');
        }
      };
      reader.readAsText(file);
      event.target.value = '';
    },

    exportContent() {
      if (this.contentList.length === 0) { this.$message.warning('暂无数据可导出'); return; }
      const exportData = this.contentList.map(item => ({
        pointName: item.pointName,
        pointDesc: item.pointDesc,
        broadcastType: item.broadcastType,
        broadcastText: item.broadcastText,
        audioFile: item.audioFile,
        voiceType: item.voiceType,
        speechRate: item.speechRate,
        intervalTime: item.intervalTime,
        armAction: item.armAction,
        chassisAngle: item.chassisAngle
      }));
      const dataStr = JSON.stringify(exportData, null, 2);
      const blob = new Blob([dataStr], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `tour_contents_${this.selectedRobotId}_${new Date().getTime()}.json`;
      link.click();
      URL.revokeObjectURL(url);
      this.$message.success(`已导出 ${exportData.length} 条讲解内容`);
    },

    copyContent() {
      if (this.selectedContentIds.length === 0) { this.$message.warning('请至少选择一条要复制的记录'); return; }
      const selectedContents = this.contentList.filter(item => this.selectedContentIds.includes(item.contentId));
      this.$confirm(`确定要复制选中的 ${selectedContents.length} 条讲解内容吗？`, '复制确认', {
        confirmButtonText: '确定复制', cancelButtonText: '取消', type: 'info'
      }).then(() => {
        let successCount = 0, failCount = 0;
        const promises = selectedContents.map(content => {
          return saveTourContent({
            pointName: `${content.pointName}_副本`,
            pointDesc: content.pointDesc,
            broadcastType: content.broadcastType,
            broadcastText: content.broadcastText,
            audioFile: content.audioFile,
            voiceType: content.voiceType,
            speechRate: content.speechRate,
            intervalTime: content.intervalTime,
            armAction: content.armAction,
            chassisAngle: content.chassisAngle,
            robotId: this.selectedRobotId
          }).then(() => successCount++).catch(() => failCount++);
        });
        Promise.all(promises).then(() => {
          if (successCount > 0) {
            this.$message.success(`复制完成：成功 ${successCount} 条，失败 ${failCount} 条`);
            this.loadContentList();
          } else {
            this.$message.error('复制失败');
          }
        });
      }).catch(() => {});
    },

    addRoute() {
      this.currentRouteId = null;
      this.routeConfigPrevMapId = null;
      this.routeConfigMapCache = {};
      this.routeConfigForm = {
        routeName: '',
        mapId: null,
        selectAll: false,
        selectedPoints: [],
        associationList: [],
        warningMessage: '',
        loadingPoints: false
      };
      this.mapPoints = [];
      this.routeConfigDrawerVisible = true;
    },

    editRoute(row) {
      this.openRouteConfigDrawer(row);
    },

    deleteSingleRoute(routeId) {
      this.$confirm('确定要删除该路线吗？', '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        deleteRoute(routeId).then(() => {
          this.$message.success('删除成功');
          this.loadAllRoutes();
        }).catch(error => {
          console.error('删除失败:', error);
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },

    async toggleRouteStatus(row) {
      const oldStatus = row.status === '1' ? '0' : '1';
      try {
        const res = await getRouteDetail(row.routeId);
        const route = res.data || {};
        const routePoints = (route.routePoints || [])
          .filter(p => p && p.sysPointId)
          .map(p => ({ sysPointId: p.sysPointId, contentId: p.contentId, orderNum: p.orderNum }));

        await saveRoute({
          routeId: row.routeId,
          routeName: route.routeName,
          mapId: route.mapId,
          robotId: route.robotId || this.selectedRobotId,
          status: row.status,
          routePoints: routePoints
        });

        this.$message.success('状态更新成功');
        this.loadAllRoutes();
      } catch (error) {
        row.status = oldStatus;
        console.error('状态更新失败:', error);
      }
    },

    async openRouteConfigDrawer(row) {
      this.currentRouteId = row.routeId;
      this.routeConfigPrevMapId = row.mapId !== undefined && row.mapId !== null ? row.mapId : null;
      this.routeConfigMapCache = {};
      this.routeConfigForm = {
        routeName: row.routeName || '',
        mapId: row.mapId !== undefined && row.mapId !== null ? row.mapId : null,
        selectAll: false,
        selectedPoints: [],
        associationList: [],
        warningMessage: '',
        loadingPoints: false
      };
      this.mapPoints = [];
      this.routeConfigDrawerVisible = true;

      try {
        const res = await getRouteDetail(row.routeId);
        const route = res.data || {};

        this.routeConfigForm.routeName = route.routeName || this.routeConfigForm.routeName;
        this.routeConfigForm.mapId = route.mapId !== undefined && route.mapId !== null ? route.mapId : this.routeConfigForm.mapId;
        this.routeConfigPrevMapId = this.routeConfigForm.mapId;

        if (this.routeConfigForm.mapId !== null && this.routeConfigForm.mapId !== undefined) {
          await this.loadMapPoints(this.routeConfigForm.mapId);
        }

        const routePoints = (route.routePoints || []).filter(p => p && p.sysPointId);
        if (routePoints.length > 0) {
          this.routeConfigForm.selectedPoints = routePoints.map(p => p.sysPointId);
          this.routeConfigForm.associationList = routePoints.map(p => ({
            sysPointId: p.sysPointId,
            pointName: (p.point && p.point.pointName) || '',
            contentId: p.contentId || null
          }));
          this.updateSelectedPoints();
        }
        if (this.routeConfigForm.mapId !== null && this.routeConfigForm.mapId !== undefined && this.routeConfigForm.mapId !== '') {
          const key = String(this.routeConfigForm.mapId);
          this.routeConfigMapCache[key] = {
            selectedPoints: [...(this.routeConfigForm.selectedPoints || [])],
            associationList: [...(this.routeConfigForm.associationList || [])],
            selectAll: !!this.routeConfigForm.selectAll
          };
        }
      } catch (error) {
        console.error('获取路线详情失败:', error);
        const keptRouteName = this.routeConfigForm.routeName;
        const keptMapId = this.routeConfigForm.mapId;
        this.routeConfigPrevMapId = keptMapId;
        this.routeConfigMapCache = {};
        this.routeConfigForm = {
          routeName: keptRouteName,
          mapId: keptMapId,
          selectAll: false,
          selectedPoints: [],
          associationList: [],
          warningMessage: '',
          loadingPoints: false
        };
        this.mapPoints = [];
        if (keptMapId !== null && keptMapId !== undefined && keptMapId !== '') {
          await this.loadMapPoints(keptMapId);
          const key = String(keptMapId);
          this.routeConfigMapCache[key] = {
            selectedPoints: [],
            associationList: [],
            selectAll: false
          };
        }
      }
    },

    closeRouteConfigDrawer() {
      this.routeConfigDrawerVisible = false;
      this.currentRouteId = null;
      this.routeConfigPrevMapId = null;
      this.routeConfigMapCache = {};
    },

    toggleAllPoints(val) {
      if (val) {
        this.routeConfigForm.selectedPoints = this.mapPoints.map(p => p.sysPointId);
      } else {
        this.routeConfigForm.selectedPoints = [];
      }
      this.updateSelectedPoints();
    },

    updateSelectedPoints() {
      const newAssociationList = [];
      this.routeConfigForm.selectedPoints.forEach(sysPointId => {
        const point = this.mapPoints.find(p => p.sysPointId === sysPointId);
        const existing = this.routeConfigForm.associationList.find(a => a.sysPointId === sysPointId);
        newAssociationList.push({
          sysPointId: sysPointId,
          pointName: point?.pointName || existing?.pointName || '',
          contentId: existing?.contentId || null
        });
      });
      this.routeConfigForm.associationList = newAssociationList;
      const unassociated = this.routeConfigForm.associationList.filter(a => !a.contentId);
      this.routeConfigForm.warningMessage = unassociated.length > 0 ? `有 ${unassociated.length} 个点位未关联讲解内容` : '';
    },

    saveRouteConfig() {
      if (!this.routeConfigForm.routeName) { this.$message.warning('请输入路线名称'); return; }
      if (!this.routeConfigForm.mapId && this.routeConfigForm.mapId !== 0) { this.$message.warning('请选择地图'); return; }

      const routePoints = this.routeConfigForm.associationList
        .filter(item => item.sysPointId)
        .map((item, index) => ({ sysPointId: item.sysPointId, contentId: item.contentId, orderNum: index + 1 }));

      const data = {
        routeId: this.currentRouteId,
        routeName: this.routeConfigForm.routeName,
        mapId: this.routeConfigForm.mapId,
        robotId: this.selectedRobotId,
        pointCount: routePoints.length,
        routePoints: routePoints
      };

      saveRoute(data).then(() => {
        this.$message.success(this.currentRouteId ? '路线修改成功' : '路线新增成功');
        this.closeRouteConfigDrawer();
        this.loadAllRoutes();
        if (this.generalConfig.mapId === this.routeConfigForm.mapId) {
          this.loadGeneralConfig();
        }
      }).catch(error => {
        console.error('保存路线配置失败:', error);
        this.$message.error('保存路线配置失败');
      });
    },

    triggerRouteImport() {
      this.$refs.routeImportInput.click();
    },

    handleRouteImport(event) {
      const file = event.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const importedData = JSON.parse(e.target.result);
          let routesToImport = Array.isArray(importedData) ? importedData : [importedData];
          if (routesToImport.length === 0) { this.$message.warning('导入数据为空'); return; }
          const isValid = routesToImport.every(item => item.routeName && typeof item.routeName === 'string');
          if (!isValid) { this.$message.error('导入文件格式不正确，缺少必要字段'); return; }
          this.$confirm(`确定要导入 ${routesToImport.length} 条路线吗？`, '导入确认', {
            confirmButtonText: '确定导入', cancelButtonText: '取消', type: 'info'
          }).then(() => {
            let successCount = 0, failCount = 0;
            const promises = routesToImport.map(route => {
              const saveData = {
                ...route,
                routeId: undefined,
                robotId: this.selectedRobotId
              };
              return saveRoute(saveData).then(() => successCount++).catch(() => failCount++);
            });
            Promise.all(promises).then(() => {
              if (successCount > 0) {
                this.$message.success(`导入完成：成功 ${successCount} 条，失败 ${failCount} 条`);
                this.loadAllRoutes();
              } else {
                this.$message.error('导入失败，请检查文件格式');
              }
            });
          }).catch(() => {});
        } catch (error) {
          this.$message.error('文件格式错误，请上传正确的 JSON 文件');
        }
      };
      reader.readAsText(file);
      event.target.value = '';
    },

    exportRoute() {
      const routes = this.currentRobotRoutes;
      if (routes.length === 0) { this.$message.warning('暂无路线数据可导出'); return; }
      const exportData = routes.map(route => ({
        routeName: route.routeName,
        mapId: route.mapId,
        pointCount: route.pointCount,
        status: route.status,
        routePoints: route.routePoints
      }));
      const dataStr = JSON.stringify(exportData, null, 2);
      const blob = new Blob([dataStr], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `tour_routes_${this.selectedRobotId}_${new Date().getTime()}.json`;
      link.click();
      URL.revokeObjectURL(url);
      this.$message.success(`已导出 ${exportData.length} 条路线数据`);
    },

    saveTourConfig() {
      const data = {
        ...this.generalConfig,
        robotId: this.selectedRobotId,
        voiceInteraction: this.generalConfig.voiceInteraction ? '1' : '0'
      };
      saveTourGeneral(data).then(() => {
        this.$message.success('讲解配置已保存');
      }).catch(error => {
        console.error('保存讲解配置失败:', error);
        this.$message.error('保存失败');
      });
    }
  }
};
</script>

<style scoped>
.card {
  background: white;
  border-radius: 10px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.5);
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light, #E5E7EB);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: var(--primary-blue, #3976E4);
}

.card-body {
  padding: 24px 20px;
}

.robot-selector {
  display: flex;
  align-items: center;
  background: #f9fafc;
  padding: 4px 4px 4px 16px;
  border-radius: 40px;
  border: 1px solid #eaeef5;
}

.badge {
  background: #e6f7ff;
  color: var(--primary-blue, #3976E4);
  padding: 5px 14px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  margin-right: 8px;
}

.sub-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid var(--border-light, #E5E7EB);
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.table-header span {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
}

.table-header span i {
  color: var(--primary-blue, #3976E4);
  margin-right: 8px;
}

.table-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.table-actions-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: nowrap;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.form-row .form-group {
  flex: 1;
  min-width: 200px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #4D4D4D;
  font-size: 14px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #dee2e6;
}

.footer-actions .el-button {
  padding: 12px 30px;
  font-size: 15px;
  border-radius: 30px;
}

.drawer-body {
  padding: 20px;
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  width: 100%;
  padding: 15px 20px;
  border-top: 1px solid #dcdfe6;
  background: white;
  text-align: right;
  box-sizing: border-box;
}

.word-counter {
  font-size: 12px;
  color: #909399;
  text-align: right;
  margin-top: 4px;
}

.inline-variables {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.upload-area {
  margin-bottom: 10px;
}

.uploaded-file {
  margin-top: 10px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.uploaded-file i {
  color: var(--primary-blue, #3976E4);
}

.point-checkbox-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin: 10px 0;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  background: #fafafa;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.empty-points-tip {
  padding: 20px;
  text-align: center;
  color: #909399;
  background: #fafafa;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  margin: 10px 0;
}

.empty-points-tip i {
  margin-right: 5px;
}

.warning-message {
  background: #fff2e8;
  border-left: 4px solid #f39c12;
  padding: 12px;
  margin: 10px 0;
  color: #e67e22;
  border-radius: 4px;
}

.hidden-input {
  display: none;
}

.el-button.danger {
  color: #f56c6c;
}

.el-button.danger:hover {
  color: #f78989;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .robot-selector {
    width: 100%;
    justify-content: space-between;
  }
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  .point-checkbox-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .footer-actions {
    flex-direction: column;
  }
  .footer-actions .el-button {
    width: 100%;
  }
  .table-actions-buttons {
    flex-wrap: wrap;
  }
}
</style>
