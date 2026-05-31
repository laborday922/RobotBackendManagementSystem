<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <div class="header-left">
          <span><i class="el-icon-location"></i> 导航指引</span>
        </div>
        <div class="header-right">
          <div class="robot-selector">
            <span class="badge"><i class="fas fa-robot"></i> 导览机器人：</span>
            <el-select v-model="selectedRobotId" placeholder="请选择机器人" style="width:200px;" @change="onRobotChange">
              <el-option v-for="r in robotList" :key="r.id" :label="r.name" :value="r.id" />
            </el-select>
          </div>
          <div class="action-buttons">
            <el-button type="primary" size="small" @click="triggerUploadNewMap">
              <i class="el-icon-upload"></i> 上传新地图
            </el-button>
            <el-button type="warning" size="small" plain :disabled="!navConfig.mapId" @click="triggerUpdateCurrentMap">
              <i class="el-icon-refresh"></i> 更新当前地图
            </el-button>
            <el-button type="danger" size="small" plain :disabled="!navConfig.mapId" @click="deleteCurrentMap">
              <i class="el-icon-delete"></i> 删除地图
            </el-button>
          </div>
        </div>
      </div>

      <!-- 隐藏的文件上传输入框 -->
      <input
        type="file"
        ref="newMapFileInput"
        style="display: none;"
        accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
        @change="handleNewMapUpload"
      />
      <input
        type="file"
        ref="updateMapFileInput"
        style="display: none;"
        accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
        @change="handleUpdateMapUpload"
      />

      <el-row :gutter="20">
        <!-- 左侧：地图和点位 -->
        <el-col :span="16">
          <el-card class="sub-card" shadow="never">
            <div class="form-group">
              <label>当前地图</label>
              <el-select v-model="navConfig.mapId" placeholder="请选择地图" @change="changeMap" style="width: 100%">
                <el-option v-for="map in mapList" :key="map.mapId" :label="map.mapName" :value="map.mapId">
                  <span>{{ map.mapName }}</span>
                  <span style="float: right; color: #8492a6;">{{ map.pointCount || 0 }}个点位</span>
                </el-option>
              </el-select>
            </div>

            <!-- 地图预览 -->
            <div class="map-preview" @click="triggerUpdateCurrentMap">
              <img
                v-if="mapImageUrl"
                :src="mapImageUrl"
                class="map-image"
                alt="地图预览"
                @error="handleImageError"
                @load="handleImageLoad"
              />
              <div v-else class="map-placeholder">
                <i class="el-icon-upload"></i>
                <span>点击上传地图</span>
                <span class="upload-hint">支持 jpg、png、gif、webp 格式，不超过20MB</span>
              </div>
              <div v-if="uploadLoading" class="upload-loading">
                <i class="el-icon-loading"></i>
                <span>上传中...</span>
              </div>
            </div>

            <!-- 点位列表区域 -->
            <div class="point-section">
              <div class="point-header">
                <div class="point-header-left">
                  <i class="el-icon-location-information"></i>
                  <strong>点位列表</strong>
                  <span class="point-count">(共 {{ pointList.length }} 个点位)</span>
                </div>
                <el-button type="text" size="small" @click="openAddPointDialog" class="add-point-btn">
                  <i class="el-icon-plus"></i> 添加点位
                </el-button>
              </div>

              <div class="point-list-container">
                <div
                  v-for="point in pointList"
                  :key="point.sysPointId"
                  class="point-item"
                  :class="{ active: selectedPoint && selectedPoint.sysPointId === point.sysPointId }"
                  @click="selectPoint(point)"
                >
                  <div class="point-icon">
                    <i :class="getPointIcon(point.pointType)"></i>
                  </div>
                  <div class="point-info">
                    <div class="point-name">{{ point.pointName }}</div>
                    <div class="point-code" v-if="point.pointCode">{{ point.pointCode }}</div>
                    <div class="point-id" v-if="point.robotPointId">ID: {{ point.robotPointId }}</div>
                  </div>
                  <div class="point-status" v-if="point.status === '0'">
                    <el-tag type="info" size="mini">禁用</el-tag>
                  </div>
                  <div class="point-actions">
                    <el-button type="text" size="small" @click.stop="editPoint(point)">
                      <i class="el-icon-edit"></i>
                    </el-button>
                    <el-button type="text" size="small" @click.stop="deletePoint(point)">
                      <i class="el-icon-delete"></i>
                    </el-button>
                  </div>
                </div>

                <div v-if="pointList.length === 0" class="empty-points">
                  <i class="el-icon-location-outline"></i>
                  <span>暂无点位，请点击上方"添加点位"按钮添加</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：点位播报设置 -->
        <el-col :span="8">
          <el-card class="sub-card" shadow="never">
            <div class="selected-point-info" v-if="selectedPoint">
              <i class="el-icon-location"></i>
              <span>正在配置：<strong>{{ selectedPoint.pointName }}</strong> (ID: {{ selectedPoint.sysPointId }})</span>
            </div>
            <div class="selected-point-info empty" v-else>
              <i class="el-icon-info"></i>
              <span>请从左侧选择一个点位</span>
            </div>

            <div class="form-group">
              <label>播报方式</label>
              <el-radio-group v-model="pointVoiceConfig.voiceType" :disabled="!selectedPoint">
                <el-radio label="default">默认播报语</el-radio>
                <el-radio label="custom">自定义播报</el-radio>
                <el-radio label="none">无播报</el-radio>
              </el-radio-group>
            </div>

            <div v-if="pointVoiceConfig.voiceType === 'custom'" class="custom-voice">
              <div class="form-group">
                <label>出发前播报</label>
                <el-input
                  v-model="pointVoiceConfig.beforeMsg"
                  placeholder="例如：现在带您去点位名称"
                  :disabled="!selectedPoint"
                  type="textarea"
                  :rows="2"
                />
              </div>
<!--              <div class="form-group">-->
<!--                <label>导航中播报</label>-->
<!--                <el-input-->
<!--                  v-model="pointVoiceConfig.duringMsg"-->
<!--                  placeholder="例如：请跟随我，前方到达点位名称"-->
<!--                  :disabled="!selectedPoint"-->
<!--                  type="textarea"-->
<!--                  :rows="2"-->
<!--                />-->
<!--              </div>-->
              <div class="form-group">
                <label>到达后播报</label>
                <el-input
                  v-model="pointVoiceConfig.afterMsg"
                  placeholder="例如：已到达点位名称"
                  :disabled="!selectedPoint"
                  type="textarea"
                  :rows="2"
                />
              </div>
            </div>

            <div v-else-if="pointVoiceConfig.voiceType === 'default'" class="default-tip">
              <i class="el-icon-info"></i> 将使用系统导航设置中的播报内容
            </div>
            <div v-else-if="pointVoiceConfig.voiceType === 'none'" class="none-tip">
              <i class="el-icon-info"></i> 导航到该点位时将不进行语音播报
            </div>

            <el-button
              type="primary"
              style="width: 100%; margin-top: 20px;"
              @click="savePointVoiceConfig"
              :loading="savingVoice"
              :disabled="!selectedPoint"
            >
              <i class="el-icon-check"></i> 保存点位播报配置
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 添加/编辑点位对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="showAddPointDialog"
      width="500px"
      @close="resetPointForm"
    >
      <el-form :model="pointForm" :rules="pointRules" ref="pointForm" label-width="100px">
        <!-- 从机器人获取点位位置（选中的值将直接作为 point_id 存储） -->
        <el-form-item label="点位位置" prop="robotPointId" required>
          <el-select
            v-model="pointForm.robotPointId"
            placeholder="请选择点位位置（从机器人获取）"
            clearable
            filterable
            style="width: 100%"
            @focus="loadPositionsFromRobot"
            :loading="loadingPositions"
          >
            <el-option
              v-for="pos in positionList"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            >
              <span>{{ pos.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px;">
                点位ID: {{ pos.id }}
              </span>
            </el-option>
          </el-select>
          <div style="font-size: 12px; color: #e6a23c; margin-top: 5px;">
            <i class="el-icon-warning"></i> 选择的点位位置ID将直接作为点位ID存储
          </div>
        </el-form-item>

        <el-form-item label="点位名称" prop="pointName">
          <el-input v-model="pointForm.pointName" placeholder="请输入点位名称" />
        </el-form-item>

        <el-form-item label="点位编码" prop="pointCode">
          <el-input v-model="pointForm.pointCode" placeholder="请输入点位编码" />
        </el-form-item>

        <el-form-item label="点位类型" prop="pointType">
          <el-select v-model="pointForm.pointType" placeholder="请选择点位类型" style="width: 100%">
            <el-option label="普通点位" value="normal" />
            <el-option label="VIP点位" value="vip" />
            <el-option label="服务点位" value="service" />
            <el-option label="出口点位" value="exit" />
          </el-select>
        </el-form-item>

        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="pointForm.orderNum" :min="0" :max="999" placeholder="请输入显示顺序" style="width: 100%" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="pointForm.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="pointForm.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showAddPointDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPointForm" :loading="pointSubmitting">确定</el-button>
      </span>
    </el-dialog>

    <!-- 新地图名称设置对话框 -->
    <el-dialog title="设置地图名称" :visible.sync="showNewMapNameDialog" width="400px">
      <el-form :model="newMapForm" label-width="80px">
        <el-form-item label="地图名称">
          <el-input v-model="newMapForm.mapName" placeholder="请输入地图名称" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showNewMapNameDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmNewMapName" :loading="isSavingMapName">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getNavConfig, saveNavConfig } from "@/api/function/nav";
import { getMapList, getPointListByMap, addMap, uploadMap, delMap, updateMap, getMap } from "@/api/function/map";
import {
  addPoint,
  updatePoint,
  deletePoint,
  getPointVoiceConfig,
  savePointVoiceConfig,
  getPointOptionsFromRobot,
  getPointsByRobotId
} from "@/api/function/point";
import { listRobot } from "@/api/mode/robot";

export default {
  name: "Navigation",
  data() {
    return {
      selectedRobotId: null,
      robotList: [],
      navConfig: {
        mapId: null,
        voiceType: 'default',
        beforeMsg: '',
        duringMsg: '',
        afterMsg: ''
      },
      mapList: [],
      pointList: [],
      mapImageUrl: '',
      uploadLoading: false,
      showAddPointDialog: false,
      showNewMapNameDialog: false,
      isSavingConfig: false,
      isSavingMapName: false,
      isUploading: false,
      changeMapTimer: null,
      pendingNewMapData: null,
      newMapForm: {
        mapId: null,
        mapName: ''
      },
      pointForm: {
        sysPointId: null,
        pointName: '',
        pointCode: '',
        pointType: 'normal',
        orderNum: 0,
        status: '1',
        remark: '',
        robotPointId: null
      },
      pointSubmitting: false,
      pointRules: {
        robotPointId: [
          { required: true, message: '请选择点位位置', trigger: 'change' }
        ],
        pointName: [
          { required: true, message: '请输入点位名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ],
        pointType: [
          { required: true, message: '请选择点位类型', trigger: 'change' }
        ]
      },
      tempImageUrl: null,
      selectedPoint: null,
      pointVoiceConfig: {
        voiceType: 'default',
        beforeMsg: '',
        duringMsg: '',
        afterMsg: ''
      },
      savingVoice: false,
      loadingPositions: false,
      positionList: [],
      positionsCache: null,
      lastRobotId: null
    };
  },
  computed: {
    dialogTitle() {
      return this.pointForm.sysPointId ? '编辑点位' : '添加点位';
    }
  },
  beforeDestroy() {
    if (this.tempImageUrl) {
      URL.revokeObjectURL(this.tempImageUrl);
    }
    if (this.changeMapTimer) {
      clearTimeout(this.changeMapTimer);
    }
  },
  created() {
    this.loadRobotsByGroup();
  },
  watch: {
    'navConfig.mapId': function(newVal, oldVal) {
      if (newVal && newVal !== oldVal) {
        this.loadPoints();
        this.loadMapImage();
        this.selectedPoint = null;
        this.resetPointVoiceConfig();
      }
    }
  },
  methods: {
    getPointIcon(pointType) {
      const icons = {
        normal: 'el-icon-location',
        vip: 'el-icon-star-off',
        service: 'el-icon-service',
        exit: 'el-icon-back',
        default: 'el-icon-place'
      };
      return icons[pointType] || icons.default;
    },

    loadRobotsByGroup() {
      listRobot({ groupId: 4, pageNum: 1, pageSize: 100 }).then(response => {
        this.robotList = response.rows || [];
        if (this.robotList.length > 0) {
          this.selectedRobotId = this.robotList[0].id;
          this.loadMaps();
          this.loadNavConfig();
          this.lastRobotId = null;
          this.positionsCache = null;
        } else {
          this.$message.warning('导览组下没有可用机器人');
        }
      }).catch(error => {
        console.error('获取机器人列表失败:', error);
        this.$message.error('获取机器人列表失败');
      });
    },

    onRobotChange() {
      this.navConfig = { mapId: null, voiceType: 'default', beforeMsg: '', duringMsg: '', afterMsg: '' };
      this.mapList = [];
      this.pointList = [];
      this.mapImageUrl = '';
      this.selectedPoint = null;
      this.resetPointVoiceConfig();
      this.lastRobotId = null;
      this.positionsCache = null;
      this.loadMaps();
      this.loadNavConfig();
    },

    triggerUploadNewMap() {
      if (this.$refs.newMapFileInput) this.$refs.newMapFileInput.click();
    },
    triggerUpdateCurrentMap() {
      if (!this.navConfig.mapId) {
        this.$message.warning('请先选择要更新的地图');
        return;
      }
      if (this.$refs.updateMapFileInput) this.$refs.updateMapFileInput.click();
    },

    loadNavConfig() {
      if (!this.selectedRobotId) return;
      getNavConfig(this.selectedRobotId).then(response => {
        if (response.data) {
          const data = response.data;
          this.navConfig.voiceType = data.voiceType || 'default';
          this.navConfig.beforeMsg = data.beforeMsg || '';
          this.navConfig.duringMsg = data.duringMsg || '';
          this.navConfig.afterMsg = data.afterMsg || '';
        }
      }).catch(error => console.error('加载导航配置失败:', error));
    },

    async loadMaps() {
      if (!this.selectedRobotId) return;
      try {
        let response = await getMapList({ robotId: this.selectedRobotId });
        let maps = response.rows || response.data || [];
        let robotMaps = maps.filter(map => {
          const isNotDeleted = map.delFlag === '0' || map.delFlag === 0 || !map.delFlag;
          const isEnabled = map.status === '1';
          return isNotDeleted && isEnabled;
        });

        if (robotMaps.length === 0) {
          const createRes = await addMap({
            mapName: '默认地图',
            robotId: String(this.selectedRobotId),
            status: '1',
            isEnable: 1,
            version: '1.0'
          });
          if (createRes && createRes.code !== 200) {
            throw new Error(createRes.msg || '自动创建地图失败');
          }

          response = await getMapList({ robotId: this.selectedRobotId });
          maps = response.rows || response.data || [];
          robotMaps = maps.filter(map => {
            const isNotDeleted = map.delFlag === '0' || map.delFlag === 0 || !map.delFlag;
            const isEnabled = map.status === '1';
            return isNotDeleted && isEnabled;
          });
        }

        this.mapList = [...robotMaps];

        this.mapList.forEach(map => this.loadPointCount(map));

        const firstRobotMapId = robotMaps.length > 0 ? robotMaps[0].mapId : null;
        const currentMapId = this.navConfig.mapId;
        const currentValid = currentMapId != null && this.mapList.some(m => Number(m.mapId) === Number(currentMapId));

        if (!currentValid) {
          this.navConfig.mapId = firstRobotMapId;
        }

        this.loadPoints();
        this.loadMapImage();
      } catch (error) {
        console.error('加载地图列表失败:', error);
        this.mapList = [];
        this.navConfig.mapId = null;
        this.pointList = [];
        this.mapImageUrl = '';
      }
    },

    loadPointCount(map) {
      if (!this.selectedRobotId) { map.pointCount = 0; return; }
      getPointListByMap(map.mapId, this.selectedRobotId).then(response => {
        const points = response.data || response.rows || [];
        map.pointCount = points.length;
      }).catch(() => { map.pointCount = 0; });
    },

    loadMapImage() {
      const mapId = this.navConfig.mapId || 0;
      if (!mapId) {
        this.mapImageUrl = '';
        return;
      }

      const currentMap = this.mapList.find(m => m.mapId === mapId);
      if (currentMap && currentMap.mapBase64) {
        this.mapImageUrl = currentMap.mapBase64;
        return;
      }
      if (currentMap && currentMap.mapUrl && currentMap.mapUrl.startsWith('data:image')) {
        this.mapImageUrl = currentMap.mapUrl;
        return;
      }
      if (mapId === 0) {
        this.mapImageUrl = '';
        return;
      }
      getMap(mapId).then(response => {
        const mapData = response.data;
        if (mapData) {
          this.mapImageUrl = mapData.mapBase64 || (mapData.mapUrl && mapData.mapUrl.startsWith('data:image') ? mapData.mapUrl : '');
        } else {
          this.mapImageUrl = '';
        }
      }).catch(error => {
        console.error('获取地图详情失败:', error);
        this.mapImageUrl = '';
      });
    },

    changeMap() {
      this.loadPoints();
      this.loadMapImage();
      if (this.changeMapTimer) clearTimeout(this.changeMapTimer);
      this.changeMapTimer = setTimeout(() => this.saveNavConfig(), 500);
    },

    loadPoints() {
      const mapId = this.navConfig.mapId || 0;
      if (!this.selectedRobotId) { this.pointList = []; return; }
      if (!mapId) { this.pointList = []; return; }
      getPointListByMap(mapId, this.selectedRobotId).then(response => {
        this.pointList = response.data || response.rows || [];
        const currentMap = this.mapList.find(m => m.mapId === mapId);
        if (currentMap) currentMap.pointCount = this.pointList.length;
      }).catch(error => {
        console.error('加载点位列表失败:', error);
        this.pointList = [];
      });
    },

    handleNewMapUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (this.isUploading) { this.$message.warning('正在上传中，请勿重复操作'); event.target.value = ''; return; }
      if (!this.selectedRobotId) { this.$message.warning('请先选择机器人'); event.target.value = ''; return; }
      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
      if (!allowedTypes.includes(file.type)) { this.$message.error('请上传图片文件'); event.target.value = ''; return; }
      if (file.size > 20 * 1024 * 1024) { this.$message.error('文件大小不能超过20MB'); event.target.value = ''; return; }
      if (this.tempImageUrl) URL.revokeObjectURL(this.tempImageUrl);
      this.tempImageUrl = URL.createObjectURL(file);
      this.mapImageUrl = this.tempImageUrl;
      this.isUploading = true;
      this.uploadLoading = true;
      const formData = new FormData();
      formData.append('file', file);
      formData.append('robotId', this.selectedRobotId);
      uploadMap(formData).then(response => {
        this.uploadLoading = false;
        const mapData = response.data;
        if (mapData && mapData.mapId) {
          this.pendingNewMapData = mapData;
          if (mapData.mapUrl && mapData.mapUrl.startsWith('data:image')) this.mapImageUrl = mapData.mapUrl;
          else if (mapData.mapBase64) this.mapImageUrl = mapData.mapBase64;
          this.newMapForm.mapId = mapData.mapId;
          this.newMapForm.mapName = mapData.mapName || '';
          this.showNewMapNameDialog = true;
          this.isUploading = false;
        } else {
          this.$message.error('上传失败：返回数据格式错误');
          this.isUploading = false;
          if (this.navConfig.mapId) this.loadMapImage();
          else this.mapImageUrl = '';
        }
      }).catch(error => {
        this.uploadLoading = false;
        this.isUploading = false;
        console.error('地图上传失败:', error);
        this.$message.error('地图上传失败：' + (error.message || '未知错误'));
        if (this.navConfig.mapId) this.loadMapImage();
        else this.mapImageUrl = '';
      });
      event.target.value = '';
    },

    handleUpdateMapUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      if (this.isUploading) { this.$message.warning('正在上传中，请勿重复操作'); event.target.value = ''; return; }
      if (!this.navConfig.mapId) { this.$message.warning('请先选择要更新的地图'); event.target.value = ''; return; }
      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
      if (!allowedTypes.includes(file.type)) { this.$message.error('请上传图片文件'); event.target.value = ''; return; }
      if (file.size > 20 * 1024 * 1024) { this.$message.error('文件大小不能超过20MB'); event.target.value = ''; return; }
      if (this.tempImageUrl) URL.revokeObjectURL(this.tempImageUrl);
      this.tempImageUrl = URL.createObjectURL(file);
      this.mapImageUrl = this.tempImageUrl;
      this.isUploading = true;
      this.uploadLoading = true;
      const formData = new FormData();
      formData.append('file', file);
      formData.append('mapId', this.navConfig.mapId);
      uploadMap(formData).then(response => {
        this.uploadLoading = false;
        this.isUploading = false;
        const mapData = response.data;
        if (mapData && mapData.mapId) {
          if (mapData.mapUrl && mapData.mapUrl.startsWith('data:image')) this.mapImageUrl = mapData.mapUrl;
          else if (mapData.mapBase64) this.mapImageUrl = mapData.mapBase64;
          const idx = this.mapList.findIndex(m => m.mapId === mapData.mapId);
          if (idx !== -1) {
            this.mapList[idx] = { ...this.mapList[idx], ...mapData, mapBase64: mapData.mapBase64 || mapData.mapUrl, mapUrl: mapData.mapUrl || mapData.mapBase64 };
          }
          setTimeout(() => this.saveNavConfig(), 500);
          this.$message.success('地图更新成功');
        } else {
          this.$message.error('更新失败：返回数据格式错误');
          this.loadMapImage();
        }
      }).catch(error => {
        this.uploadLoading = false;
        this.isUploading = false;
        console.error('地图更新失败:', error);
        this.$message.error('地图更新失败：' + (error.message || '未知错误'));
        this.loadMapImage();
      });
      event.target.value = '';
    },

    confirmNewMapName() {
      if (this.isSavingMapName) return;
      if (!this.newMapForm.mapName.trim()) { this.$message.warning('请输入地图名称'); return; }
      this.isSavingMapName = true;
      updateMap({ mapId: this.newMapForm.mapId, mapName: this.newMapForm.mapName }).then(() => {
        this.$message.success('地图创建成功');
        this.showNewMapNameDialog = false;
        if (this.pendingNewMapData) {
          const newMap = {
            ...this.pendingNewMapData,
            mapName: this.newMapForm.mapName,
            mapBase64: this.pendingNewMapData.mapBase64 || this.pendingNewMapData.mapUrl,
            mapUrl: this.pendingNewMapData.mapUrl || this.pendingNewMapData.mapBase64,
            delFlag: '0', status: '1'
          };
          this.mapList.unshift(newMap);
          this.navConfig.mapId = newMap.mapId;
          this.loadPoints();
          setTimeout(() => this.saveNavConfig(), 500);
        }
        this.pendingNewMapData = null;
      }).catch(error => {
        console.error('保存地图名称失败:', error);
        if (error.message !== '数据正在处理，请勿重复提交') this.$message.error('保存地图名称失败');
      }).finally(() => setTimeout(() => { this.isSavingMapName = false; }, 1000));
    },

    deleteCurrentMap() {
      if (!this.navConfig.mapId) { this.$message.warning('请先选择要删除的地图'); return; }
      const currentMap = this.mapList.find(m => m.mapId === this.navConfig.mapId);
      if (!currentMap) return;
      this.$confirm(`确定要删除地图"${currentMap.mapName}"吗？删除后该地图下的所有点位也将被删除！`, '警告', {
        confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        delMap(this.navConfig.mapId).then(() => {
          this.$message.success('地图删除成功');
          this.navConfig.mapId = null;
          this.pointList = [];
          this.mapImageUrl = '';
          this.selectedPoint = null;
          this.resetPointVoiceConfig();
          this.loadMaps();
          this.saveNavConfig();
        }).catch(error => {
          console.error('删除地图失败:', error);
          this.$message.error('删除地图失败');
        });
      }).catch(() => {});
    },

    handleImageLoad() { console.log('图片加载成功'); },
    handleImageError() {
      console.error('图片加载失败');
      if (this.mapImageUrl && this.mapImageUrl.startsWith('blob:')) return;
      if (this.mapImageUrl && this.mapImageUrl.startsWith('data:image')) this.$message.error('图片数据加载失败，请重新上传');
      else this.$message.warning('地图图片加载失败');
    },

    async selectPoint(point) {
      if (point.status === '0') { this.$message.warning('该点位已禁用，无法配置播报'); return; }
      this.selectedPoint = point;
      this.$message.info(`已选中点位：${point.pointName} (ID: ${point.sysPointId})`);
      await this.loadPointVoiceConfig(point.sysPointId);
    },

    async loadPointVoiceConfig(sysPointId) {
      try {
        const res = await getPointVoiceConfig(sysPointId);
        if (res.code === 200 && res.data) {
          this.pointVoiceConfig = {
            voiceType: res.data.voiceType || 'default',
            beforeMsg: res.data.beforeMsg || '',
            duringMsg: res.data.duringMsg || '',
            afterMsg: res.data.afterMsg || ''
          };
        } else this.resetPointVoiceConfig();
      } catch (error) {
        console.error('加载点位播报配置失败:', error);
        this.resetPointVoiceConfig();
      }
    },

    resetPointVoiceConfig() {
      this.pointVoiceConfig = { voiceType: 'default', beforeMsg: '', duringMsg: '', afterMsg: '' };
    },

    async savePointVoiceConfig() {
      if (!this.selectedPoint) { this.$message.warning('请先选择一个点位'); return; }
      this.savingVoice = true;
      try {
        const requestData = {
          sysPointId: this.selectedPoint.sysPointId,
          voiceType: this.pointVoiceConfig.voiceType,
          beforeMsg: this.pointVoiceConfig.beforeMsg,
          duringMsg: this.pointVoiceConfig.duringMsg,
          afterMsg: this.pointVoiceConfig.afterMsg
        };
        const res = await savePointVoiceConfig(requestData);
        if (res.code === 200) {
          this.$message.success(`点位「${this.selectedPoint.pointName}」播报配置保存成功`);
          await this.loadPointVoiceConfig(this.selectedPoint.sysPointId);
        } else this.$message.error(res.msg || '保存失败');
      } catch (error) {
        console.error('保存失败:', error);
        this.$message.error('保存失败：' + (error.message || '未知错误'));
      } finally { this.savingVoice = false; }
    },

    saveNavConfig() {
      if (this.isSavingConfig) return;
      if (!this.selectedRobotId) return;
      this.isSavingConfig = true;
      const data = { robotId: this.selectedRobotId, ...this.navConfig };
      saveNavConfig(data).then(() => console.log('导航配置已保存')).catch(error => {
        console.error('保存导航配置失败:', error);
        if (error.message !== '数据正在处理，请勿重复提交') this.$message.error('保存导航设置失败');
      }).finally(() => setTimeout(() => { this.isSavingConfig = false; }, 1000));
    },

    openAddPointDialog() {
      this.resetPointForm();
      this.positionList = [];
      this.showAddPointDialog = true;
    },

    /**
     * 从机器人获取点位位置列表
     * 返回的 value 将直接作为 point_id 存储
     */
    async loadPositionsFromRobot() {
      if (!this.selectedRobotId) {
        this.$message.warning('请先选择机器人');
        return;
      }

      if (this.positionsCache && this.lastRobotId === this.selectedRobotId) {
        this.positionList = this.positionsCache;
        return;
      }

      this.loadingPositions = true;
      try {
        const res = await getPointOptionsFromRobot(this.selectedRobotId);
        if (res.code === 200 && res.data) {
          const positionParam = res.data.find(p => p.paramKey === 'position');
          if (positionParam && positionParam.options && positionParam.options.length > 0) {
            this.positionList = positionParam.options.map(opt => ({
              id: parseInt(opt.value),  // 确保是数字类型，将作为 point_id
              name: opt.label
            }));
            this.positionsCache = this.positionList;
            this.lastRobotId = this.selectedRobotId;
            this.$message.success(`成功加载 ${this.positionList.length} 个点位`);
          } else {
            this.positionList = [];
            this.$message.warning('机器人未返回点位数据或接口返回格式异常');
          }
        } else {
          this.positionList = [];
          this.$message.error(res.msg || '获取点位失败');
        }
      } catch (error) {
        console.error('加载点位失败', error);
        this.$message.error('加载失败：' + (error.message || '未知错误'));
        this.positionList = [];
      } finally {
        this.loadingPositions = false;
      }
    },

    editPoint(point) {
      this.pointForm = {
        sysPointId: point.sysPointId,
        pointName: point.pointName,
        pointCode: point.pointCode || '',
        pointType: point.pointType || 'normal',
        orderNum: point.orderNum || 0,
        status: point.status || '1',
        remark: point.remark || '',
        robotPointId: point.robotPointId
      };
      this.positionList = [];
      this.showAddPointDialog = true;
    },

    deletePoint(point) {
      this.$confirm(`确定要删除点位"${point.pointName}"吗？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        deletePoint(point.sysPointId).then(() => {
          this.$message.success('删除成功');
          this.loadPoints();
          if (this.selectedPoint && this.selectedPoint.sysPointId === point.sysPointId) {
            this.selectedPoint = null;
            this.resetPointVoiceConfig();
          }
        }).catch(error => {
          console.error('删除点位失败:', error);
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },

    resetPointForm() {
      this.pointForm = {
        sysPointId: null,
        pointName: '',
        pointCode: '',
        pointType: 'normal',
        orderNum: 0,
        status: '1',
        remark: '',
        robotPointId: null
      };
      if (this.$refs.pointForm) this.$refs.pointForm.resetFields();
    },

    submitPointForm() {
      this.$refs.pointForm.validate((valid) => {
        if (!valid) return;

        // 检查是否选择了点位位置
        if (!this.pointForm.robotPointId) {
          this.$message.warning('请选择点位位置');
          return;
        }

        this.pointSubmitting = true;

        const mapId = this.navConfig.mapId;
        if (!mapId) {
          this.$message.warning('请先选择地图');
          this.pointSubmitting = false;
          return;
        }

        const formData = {
          robotPointId: this.pointForm.robotPointId,
          mapId: mapId,
          robotId: this.selectedRobotId,
          pointName: this.pointForm.pointName,
          pointCode: this.pointForm.pointCode,
          pointType: this.pointForm.pointType,
          orderNum: this.pointForm.orderNum,
          status: this.pointForm.status,
          remark: this.pointForm.remark,
          robotPositionId: this.pointForm.robotPointId
        };

        if (this.pointForm.sysPointId) {
          formData.sysPointId = this.pointForm.sysPointId;
        }

        const apiCall = this.pointForm.sysPointId ? updatePoint(formData) : addPoint(formData);

        apiCall.then(() => {
          this.$message.success(this.pointForm.sysPointId ? '更新成功' : '添加成功');
          this.showAddPointDialog = false;
          this.loadPoints();
        }).catch(error => {
          console.error('保存点位失败:', error);
          let errorMsg = '保存失败';
          if (error.response && error.response.data && error.response.data.msg) {
            errorMsg = error.response.data.msg;
          } else if (error.message) {
            errorMsg = error.message;
          }
          this.$message.error(errorMsg);
        }).finally(() => {
          this.pointSubmitting = false;
        });
      });
    }
  }
};
</script>

<style scoped>
/* 样式保持不变，与原来相同 */
.app-container {
  padding: 20px;
}

.box-card {
  border-radius: 8px;
}

.box-card >>> .el-card__header {
  padding: 16px 20px;
}

.clearfix {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left span {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-left i {
  font-size: 18px;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-left: auto;
  justify-content: flex-end;
}

.robot-selector {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #f9fafc 100%);
  padding: 4px 4px 4px 16px;
  border-radius: 32px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.robot-selector:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.12);
}

.badge {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  color: #409eff;
  padding: 5px 14px;
  border-radius: 28px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  margin-right: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.badge i {
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  border-radius: 6px;
  transition: all 0.25s ease;
}

.action-buttons .el-button--primary {
  background: linear-gradient(135deg, #409eff, #3a8ee6);
  border: none;
  color: #fff;
}

.action-buttons .el-button--warning.is-plain {
  border-color: #e6a23c;
  color: #fff;
  background-color: #e6a23c;
}

.action-buttons .el-button--warning.is-plain:hover {
  background: #e6a23c;
  color: #fff;
  border-color: #e6a23c;
  opacity: 0.85;
}

.action-buttons .el-button--danger.is-plain {
  border-color: #f56c6c;
  color: #f56c6c;
}

.action-buttons .el-button--danger.is-plain:hover {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
}

.action-buttons .el-button i {
  margin-right: 4px;
}

@media (max-width: 992px) {
  .clearfix {
    flex-direction: column;
    align-items: flex-start;
  }
  .header-right {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    margin-left: 0;
  }
  .robot-selector {
    justify-content: space-between;
  }
  .action-buttons {
    justify-content: flex-start;
  }
}

@media (max-width: 576px) {
  .action-buttons {
    flex-direction: column;
  }
  .action-buttons .el-button {
    width: 100%;
    margin-left: 0 !important;
  }
  .robot-selector {
    flex-wrap: wrap;
    padding: 8px;
  }
}

.sub-card {
  margin-bottom: 0;
  border: 1px solid #e9ecef;
  border-radius: 8px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.map-preview {
  background: #f8f9fa;
  height: 280px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #dee2e6;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
}

.map-preview:hover {
  background: #f1f3f5;
  border-color: #409eff;
}

.map-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #f5f5f5;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.map-placeholder i {
  font-size: 56px;
  color: #adb5bd;
  margin-bottom: 12px;
}

.map-placeholder span {
  font-size: 14px;
  color: #6c757d;
}

.upload-hint {
  font-size: 12px;
  color: #adb5bd;
  margin-top: 8px;
}

.upload-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.upload-loading i {
  font-size: 36px;
  margin-bottom: 12px;
}

.point-section {
  margin-top: 8px;
}

.point-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.point-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.point-header-left i {
  color: #409eff;
  font-size: 16px;
}

.point-header-left strong {
  color: #2c3e50;
  font-size: 15px;
}

.point-count {
  color: #909399;
  font-size: 12px;
  font-weight: normal;
}

.add-point-btn {
  padding: 0;
  font-size: 12px;
}

.point-list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 4px;
}

.point-list-container::-webkit-scrollbar {
  width: 6px;
}

.point-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.point-list-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.point-list-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.point-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.point-item.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.point-item.active::before {
  transform: scaleY(1);
}

.point-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #409eff;
  transform: scaleY(0);
  transition: transform 0.25s ease;
}

.point-item:hover {
  transform: translateX(4px);
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
  background: #f8fcff;
}

.point-item:hover::before {
  transform: scaleY(1);
}

.point-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 8px;
  margin-right: 12px;
  transition: all 0.25s ease;
}

.point-icon i {
  font-size: 18px;
  color: #6c757d;
  transition: color 0.25s ease;
}

.point-item:hover .point-icon {
  background: #e9ecef;
}

.point-info {
  flex: 1;
  min-width: 0;
}

.point-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  transition: color 0.25s ease;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.point-code {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.point-id {
  font-size: 10px;
  color: #c0c4cc;
  margin-top: 2px;
  font-family: monospace;
}

.point-status {
  margin-left: 8px;
}

.point-actions {
  display: flex;
  gap: 4px;
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.point-item:hover .point-actions {
  opacity: 1;
}

.point-actions .el-button {
  padding: 4px 6px;
  color: #909399;
}

.point-actions .el-button:hover {
  color: #409eff;
}

.empty-points {
  text-align: center;
  padding: 48px 20px;
  color: #909399;
}

.empty-points i {
  font-size: 48px;
  margin-bottom: 12px;
  color: #dcdfe6;
}

.empty-points span {
  display: block;
  font-size: 14px;
}

.selected-point-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #409eff;
}

.selected-point-info i {
  color: #409eff;
  font-size: 16px;
}

.selected-point-info strong {
  color: #409eff;
}

.selected-point-info.empty {
  background: #f5f7fa;
  border-left-color: #909399;
}

.selected-point-info.empty i {
  color: #909399;
}

.custom-voice {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.default-tip, .none-tip {
  padding: 20px;
  text-align: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #909399;
}

.default-tip i, .none-tip i {
  margin-right: 8px;
}
</style>
