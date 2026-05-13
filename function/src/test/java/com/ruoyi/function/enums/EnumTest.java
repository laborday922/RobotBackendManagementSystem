package com.ruoyi.function.enums;

import com.ruoyi.function.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(classes = TestApplication.class)
@DisplayName("枚举测试")
class EnumTest {

    @Test
    @DisplayName("地图状态枚举测试")
    void testMapStatusEnum() {
        assertThat(MapStatusEnum.ENABLED.getCode()).isEqualTo("1");
        assertThat(MapStatusEnum.ENABLED.getInfo()).isEqualTo("启用");
        assertThat(MapStatusEnum.DISABLED.getCode()).isEqualTo("0");
        assertThat(MapStatusEnum.DISABLED.getInfo()).isEqualTo("禁用");

        assertThat(MapStatusEnum.getByCode("1")).isEqualTo(MapStatusEnum.ENABLED);
        assertThat(MapStatusEnum.getByCode("0")).isEqualTo(MapStatusEnum.DISABLED);
        assertThat(MapStatusEnum.getByCode("invalid")).isNull();

        assertThat(MapStatusEnum.isValid("1")).isTrue();
        assertThat(MapStatusEnum.isValid("0")).isTrue();
        assertThat(MapStatusEnum.isValid("invalid")).isFalse();
    }

    @Test
    @DisplayName("点位类型枚举测试")
    void testPointTypeEnum() {
        assertThat(PointTypeEnum.NORMAL.getCode()).isEqualTo("normal");
        assertThat(PointTypeEnum.NORMAL.getInfo()).isEqualTo("普通点位");
        assertThat(PointTypeEnum.VIP.getCode()).isEqualTo("vip");
        assertThat(PointTypeEnum.SERVICE.getCode()).isEqualTo("service");
        assertThat(PointTypeEnum.EXIT.getCode()).isEqualTo("exit");

        assertThat(PointTypeEnum.getByCode("normal")).isEqualTo(PointTypeEnum.NORMAL);
        assertThat(PointTypeEnum.getByCode("invalid")).isEqualTo(PointTypeEnum.NORMAL);
    }

    @Test
    @DisplayName("导航播报类型枚举测试")
    void testNavVoiceTypeEnum() {
        assertThat(NavVoiceTypeEnum.DEFAULT.getCode()).isEqualTo("default");
        assertThat(NavVoiceTypeEnum.CUSTOM.getCode()).isEqualTo("custom");
        assertThat(NavVoiceTypeEnum.NONE.getCode()).isEqualTo("none");
    }

    @Test
    @DisplayName("播报类型枚举测试")
    void testBroadcastTypeEnum() {
        assertThat(BroadcastTypeEnum.TEXT.getCode()).isEqualTo("text");
        assertThat(BroadcastTypeEnum.AUDIO.getCode()).isEqualTo("audio");

        assertThat(BroadcastTypeEnum.isText("text")).isTrue();
        assertThat(BroadcastTypeEnum.isAudio("audio")).isTrue();
        assertThat(BroadcastTypeEnum.isText("audio")).isFalse();
    }

    @Test
    @DisplayName("讲解内容类型枚举测试")
    void testContentTypeEnum() {
        assertThat(ContentTypeEnum.TEXT.getCode()).isEqualTo("text");
        assertThat(ContentTypeEnum.AUDIO.getCode()).isEqualTo("audio");
        assertThat(ContentTypeEnum.IMAGE.getCode()).isEqualTo("image");
        assertThat(ContentTypeEnum.VIDEO.getCode()).isEqualTo("video");
        assertThat(ContentTypeEnum.MIXED.getCode()).isEqualTo("mixed");
    }

    @Test
    @DisplayName("音色类型枚举测试")
    void testVoiceTypeEnum() {
        assertThat(VoiceTypeEnum.GENTLE_WOMAN.getCode()).isEqualTo("gentle_woman");
        assertThat(VoiceTypeEnum.BRIGHT_WOMAN.getCode()).isEqualTo("bright_woman");
        assertThat(VoiceTypeEnum.DEEP_MAN.getCode()).isEqualTo("deep_man");
        assertThat(VoiceTypeEnum.LIVELY_MAN.getCode()).isEqualTo("lively_man");
        assertThat(VoiceTypeEnum.CHILD.getCode()).isEqualTo("child");
        assertThat(VoiceTypeEnum.CUSTOM.getCode()).isEqualTo("custom");
    }

    @Test
    @DisplayName("讲解结束后动作枚举测试")
    void testAfterActionEnum() {
        assertThat(AfterActionEnum.STAY.getCode()).isEqualTo("stay");
        assertThat(AfterActionEnum.CHARGE.getCode()).isEqualTo("charge");
        assertThat(AfterActionEnum.BACK_START.getCode()).isEqualTo("back_start");
    }
}