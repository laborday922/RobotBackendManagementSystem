package com.ruoyi.qa.Dify.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import java.util.List;

public class DifyListDocumentsResponse
{
    @JSONField(name = "data")
    private List<DifyDocument> data;

    @JSONField(name = "total")
    private Integer total;

    public List<DifyDocument> getData()
    {
        return data;
    }

    public void setData(List<DifyDocument> data)
    {
        this.data = data;
    }

    public Integer getTotal()
    {
        return total;
    }

    public void setTotal(Integer total)
    {
        this.total = total;
    }
}

