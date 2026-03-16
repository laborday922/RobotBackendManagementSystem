package com.ruoyi.app.mapper;

import java.util.List;
import com.ruoyi.app.domain.TAppLibrary;
import org.apache.ibatis.annotations.Select;

/**
 * 应用库Mapper接口
 * 
 * @author xiaocai
 * @date 2026-03-13
 */
public interface TAppLibraryMapper 
{
    /**
     * 查询应用库
     * 
     * @param id 应用库主键
     * @return 应用库
     */
    public TAppLibrary selectTAppLibraryById(Long id);

    /**
     * 查询应用库列表
     * 
     * @param tAppLibrary 应用库
     * @return 应用库集合
     */
    public List<TAppLibrary> selectTAppLibraryList(TAppLibrary tAppLibrary);

    /**
     * 新增应用库
     * 
     * @param tAppLibrary 应用库
     * @return 结果
     */
    public int insertTAppLibrary(TAppLibrary tAppLibrary);

    /**
     * 修改应用库
     * 
     * @param tAppLibrary 应用库
     * @return 结果
     */
    public int updateTAppLibrary(TAppLibrary tAppLibrary);

    /**
     * 删除应用库
     * 
     * @param id 应用库主键
     * @return 结果
     */
    public int deleteTAppLibraryById(Long id);

    /**
     * 批量删除应用库
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTAppLibraryByIds(Long[] ids);

    @Select("select count(id) from t_app_library where app_id = #{appId}")
    int selectAppLibraryByAppId(String appId);
    @Select("select count(id) from t_app_library where app_name = #{appName}")
    int selectAppLibraryByAppName(String appName);
}
