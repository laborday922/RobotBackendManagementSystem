package com.ruoyi.taskmgt.domain;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.ReturnNo;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.utils.CloneFactory;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.taskmgt.domain.bo.Template;
import com.ruoyi.taskmgt.mapper.TemplatePoMapper;
import com.ruoyi.taskmgt.mapper.po.TemplatePo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.common.utils.DictUtils.SEPARATOR;

@Repository
@Slf4j
public class TemplateRepository {
    private static final String TEMPLATEBYID = "TM%d";
    private final RedisCache redisUtil;
    private final TemplatePoMapper templatePoMapper;
    private final MessageSourceAccessor messageSourceAccessor;

    public TemplateRepository(RedisCache redisUtil, TemplatePoMapper templatePoMapper, MessageSourceAccessor messageSourceAccessor) {
        this.redisUtil = redisUtil;
        this.templatePoMapper = templatePoMapper;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    /**
     * 构造满血的Template对象
     *
     * @param bo 充血的template对象
     * @return 满血的Template对象
     */
    private Template build(Template bo) {
        if (Objects.nonNull(bo)){
            bo.setTemplateRepository(this);
        }
        return bo;
    }

    /**
     * 构成满血的Template对象
     *
     * @param po       Template Po 对象
     * @param redisKey redis key
     * @return 满血的Template对象
     */
    private Template build(TemplatePo po, Optional<String> redisKey) {
        if (Objects.nonNull(po)) {
            Template bo = CloneFactory.copy(new Template(), po);
            bo.setRobotGroupIds(stringToList(po.getRobotGroupId()));
            redisKey.ifPresent(key -> this.redisUtil.setCacheObject(key, bo));
            return this.build(bo);
        }
        return null;
    }

    /**
     * 以id找对象
     *
     * @param id 对象id
     * @return task对象
     */
    public Optional<Template> findById(Long id) {
        Assert.notNull(id, "TemplateRepository.findById: TemplateRepository.findById: id is null");
        String key = String.format(TEMPLATEBYID, id);
        Template bo = this.redisUtil.getCacheObject(key);
        if (Objects.isNull(bo)) {
            return this.templatePoMapper.findById(id).map(po -> this.build(po, Optional.of(key)));
        } else {
            this.build(bo);
            return Optional.of(bo);
        }
    }

    public String getTemplateNameById(Long id){
        Template template = this.findById(id).orElseThrow(()-> {
            String[] args = new String[]{this.messageSourceAccessor.getMessage("Template.name", LocaleContextHolder.getLocale()), id.toString()};
            return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
        });
        return template.getName();
    }

    public Template insert(Template template) {
        Assert.notNull(template, "TemplateRepository.insert: template can not be null.");
        template.setId(null);
        TemplatePo templatePo = CloneFactory.copyNotNull(new TemplatePo(), template);
        templatePo.setRobotGroupId(listToString(template.getRobotGroupIds()));
        templatePo.setCreateTime(new Date());
        templatePo.setCreateBy(SecurityUtils.getUsername());
        try {
            TemplatePo savedPo = this.templatePoMapper.save(templatePo);
            return CloneFactory.copy(new Template(), savedPo);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage();
            log.debug("templateRepository:insert: msg={}", msg);
            if (msg != null && msg.contains("template_name_index")) {
                String[] args = {this.messageSourceAccessor.getMessage("Template.name", LocaleContextHolder.getLocale()), template.getName()};
                throw new TaskmgtException(ReturnNo.SAMEOBJECT, args, this.messageSourceAccessor.getMessage(ReturnNo.SAMEOBJECT.getMessage()));
            }
            throw e;
        }
    }

    public List<String> update(Template template) {
        Assert.notNull(template, "TemplateRepository.update: template can not be null.");
        Assert.notNull(template.getId(), "TemplateRepository.update: template id can not be null.");

        TemplatePo oldPo = this.templatePoMapper.findById(template.getId())
                .orElseThrow(() -> {
                    String[] args = new String[]{this.messageSourceAccessor.getMessage("Template.name", LocaleContextHolder.getLocale()), template.getId().toString()};
                    return new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,
                            this.messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
                });
        TemplatePo newPo = CloneFactory.copyNotNull(oldPo, template);
        newPo.setUpdateTime(new Date());
        newPo.setUpdateBy(SecurityUtils.getUsername());
        newPo.setRobotGroupId(listToString(template.getRobotGroupIds()));
        try {
            this.templatePoMapper.save(newPo);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage();
            log.debug("templateRepository:update: msg={}", msg);
            if (msg != null && msg.contains("template_name_index")) {
                String[] args = {this.messageSourceAccessor.getMessage("Template.name", LocaleContextHolder.getLocale()), template.getName()};
                throw new TaskmgtException(ReturnNo.SAMEOBJECT, args, this.messageSourceAccessor.getMessage(ReturnNo.SAMEOBJECT.getMessage()));
            }
            throw e;
        }
        String keyId = String.format(TEMPLATEBYID, template.getId());
        List<String> key = new ArrayList<>();
        key.add(keyId);
        return key;
    }

    public List<Template> getTemplates(String name, Byte status, Long robotGroupId) {
        Specification<TemplatePo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (robotGroupId != null) {
                String groupIdStr = String.valueOf(robotGroupId);
                Predicate containsGroupId = cb.like(
                        cb.concat(cb.concat(",", root.get("robotGroupIds")), ","),
                        "%," + groupIdStr + ",%"
                );
                predicates.add(containsGroupId);
            }
            predicates.add(cb.notEqual(root.get("status"), Template.DELETED));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<TemplatePo> templatePos = templatePoMapper.findAll(spec);
        return templatePos.stream()
                .map(po -> build(po, Optional.empty()))
                .collect(Collectors.toList());
    }

    public static String listToString(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(SEPARATOR));
    }
    public static List<Long> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(SEPARATOR))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
