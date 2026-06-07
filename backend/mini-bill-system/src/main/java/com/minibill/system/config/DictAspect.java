package com.minibill.system.config;

import com.minibill.common.annotation.DictText;
import com.minibill.common.result.Result;
import com.minibill.system.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典文本统一处理
 * 查找响应中含有 @DictText 注解的字段，自动从数据库查询并添加 {fieldName}Text 字段返回中文标签
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class DictAspect implements ResponseBodyAdvice<Object> {

    private final DictService dictService;

    /** 缓存在类上的字典字段信息 */
    private static final Map<Class<?>, List<DictFieldInfo>> FIELD_CACHE = new ConcurrentHashMap<>();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) return null;
        if (!selectedContentType.includes(MediaType.APPLICATION_JSON)) return body;

        try {
            handleObject(body);
        } catch (Exception e) {
            log.warn("DictAspect 处理异常", e);
        }
        return body;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void handleObject(Object obj) {
        if (obj == null) return;

        if (obj instanceof Result result) {
            handleObject(result.getData());
            return;
        }

        if (obj instanceof com.baomidou.mybatisplus.extension.plugins.pagination.Page page) {
            handleObject(page.getRecords());
            return;
        }

        if (obj instanceof Collection collection) {
            for (Object item : collection) {
                if (item != null && isBizEntity(item.getClass())) {
                    processEntity(item);
                }
            }
            return;
        }

        if (isBizEntity(obj.getClass())) {
            processEntity(obj);
        }
    }

    private boolean isBizEntity(Class<?> clazz) {
        return clazz.getName().startsWith("com.minibill");
    }

    private void processEntity(Object entity) {
        Class<?> clazz = entity.getClass();
        List<DictFieldInfo> fields = FIELD_CACHE.computeIfAbsent(clazz, this::scanDictFields);

        for (DictFieldInfo info : fields) {
            try {
                Object value = info.field.get(entity);
                if (value == null) continue;

                // 从数据库查字典标签（已含 Redis 缓存）
                String label = dictService.getDictLabelByCodeAndValue(info.dictCode, String.valueOf(value));
                if (label != null) {
                    info.textField.set(entity, label);
                }
            } catch (Exception e) {
                log.warn("设置字典文本失败: {}.{}", clazz.getSimpleName(), info.field.getName(), e);
            }
        }
    }

    private List<DictFieldInfo> scanDictFields(Class<?> clazz) {
        List<DictFieldInfo> list = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                DictText anno = f.getAnnotation(DictText.class);
                if (anno != null) {
                    f.setAccessible(true);
                    String textFieldName = f.getName() + "Text";
                    try {
                        Field tf = clazz.getDeclaredField(textFieldName);
                        tf.setAccessible(true);
                        list.add(new DictFieldInfo(f, tf, anno.dictCode()));
                    } catch (NoSuchFieldException e) {
                        log.warn("实体 {} 缺少字典文本字段 {}{}Text", clazz.getSimpleName(), f.getName());
                    }
                }
            }
        }
        return list;
    }

    private record DictFieldInfo(Field field, Field textField, String dictCode) {}
}
