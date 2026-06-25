package com.ruoyi.qa.QAFile.domain.enums;

public enum QaFileStatus
{
    NORMAL((short) 0, "正常"),
    UPLOAD_FAILED((short) 1, "上传失败"),
    KG_BUILD_FAILED((short) 2, "图谱构建失败");

    private final short code;
    private final String label;

    QaFileStatus(short code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public short getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public static QaFileStatus fromCode(Short code)
    {
        if (code == null)
        {
            return null;
        }
        for (QaFileStatus status : values())
        {
            if (status.code == code)
            {
                return status;
            }
        }
        return null;
    }
}
