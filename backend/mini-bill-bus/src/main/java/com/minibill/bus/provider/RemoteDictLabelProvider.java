package com.minibill.bus.provider;

import com.minibill.api.feign.SystemFeignClient;
import com.minibill.common.service.DictLabelProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程字典标签提供者
 * bus 服务通过 Feign 调用 system 服务获取字典中文标签
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteDictLabelProvider implements DictLabelProvider {

    private final SystemFeignClient systemFeignClient;

    @Override
    public String getDictLabelByCodeAndValue(String dictCode, String value) {
        try {
            return systemFeignClient.getDictLabel(dictCode, value);
        } catch (Exception e) {
            log.warn("远程获取字典标签失败: dictCode={}, value={}", dictCode, value, e);
            return null;
        }
    }
}
