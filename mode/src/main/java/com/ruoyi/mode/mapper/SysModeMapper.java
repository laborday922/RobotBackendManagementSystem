package com.ruoyi.mode.mapper;

import com.ruoyi.mode.domain.SysMode;
import java.util.List;

/**
 * 模式Mapper接口
 *
 * @author ruoyi
 */
public interface SysModeMapper
{

    public SysMode selectSysModeById(SysMode sysMode);


    public List<SysMode> selectSysModeList(SysMode sysMode);


    public int insertSysMode(SysMode sysMode);

    public int updateSysMode(SysMode sysMode);


    public int deleteSysModeById(Long modeId);


    public int deleteSysModeByIds(Long[] modeIds);
}