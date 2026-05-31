package com.ruoyi.function.mapper;

import com.ruoyi.function.domain.SysPoint;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 点位配置Mapper接口
 *
 * @author ruoyi
 */
public interface SysPointMapper {

    /**
     * 根据地图ID查询点位列表
     *
     * @param mapId 地图ID
     * @return 点位列表
     */
    List<SysPoint> selectByMapId(@Param("mapId") Long mapId);

    /**
     * 根据点位ID查询详情
     *
     * @param sysPointId 系统点位ID
     * @return 点位信息
     */
    SysPoint selectById(@Param("sysPointId") Long sysPointId);

    /**
     * 查询点位列表（带条件）
     *
     * @param point 点位对象
     * @return 点位列表
     */
    List<SysPoint> selectList(SysPoint point);

    /**
     * 根据多个地图ID查询点位列表
     *
     * @param mapIds 地图ID列表
     * @return 点位列表
     */
    List<SysPoint> selectByMapIds(@Param("mapIds") List<Long> mapIds);

    /**
     * 根据机器人ID查询点位列表
     *
     * @param robotId 机器人ID
     * @return 点位列表
     */
    List<SysPoint> selectByRobotId(@Param("robotId") Long robotId);

    /**
     * 新增点位
     *
     * @param point 点位信息
     * @return 结果
     */
    int insert(SysPoint point);

    /**
     * 更新点位
     *
     * @param point 点位信息
     * @return 结果
     */
    int update(SysPoint point);

    /**
     * 删除点位（物理删除）
     *
     * @param sysPointId 系统点位ID
     * @return 结果
     */
    int deleteById(@Param("sysPointId") Long sysPointId);

    /**
     * 根据地图ID删除所有点位
     *
     * @param mapId 地图ID
     * @return 结果
     */
    int deleteByMapId(@Param("mapId") Long mapId);

    /**
     * 批量删除
     *
     * @param sysPointIds 系统点位ID数组
     * @return 结果
     */
    int deleteByIds(@Param("sysPointIds") Long[] sysPointIds);

    /**
     * 更新点位状态
     *
     * @param sysPointId 系统点位ID
     * @param status 状态
     * @param updateBy 更新者
     * @param updateTime 更新时间
     * @return 结果
     */
    int updateStatus(@Param("sysPointId") Long sysPointId,
                     @Param("status") String status,
                     @Param("updateBy") String updateBy,
                     @Param("updateTime") java.util.Date updateTime);

    /**
     * 根据地图ID查询点位数量
     *
     * @param mapId 地图ID
     * @return 点位数量
     */
    int countByMapId(@Param("mapId") Long mapId);

    /**
     * 检查点位编码是否唯一
     *
     * @param pointCode 点位编码
     * @param pointId 点位ID（排除自身）
     * @return 结果
     */
    int checkPointCodeUnique(@Param("pointCode") String pointCode,
                             @Param("sysPointId") Long sysPointId);
}
