/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.PluginService;
import org.realtimeetl.biz.vo.PluginVO;
import org.realtimeetl.common.enums.FileSuffixEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.plugin.PluginFactory;
import org.realtimeetl.sdk.model.Plugin;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/01/13 17:18
 */
@Service
public class PluginServiceImpl implements PluginService {

    @Resource
    private PluginFactory pluginFactory;

    @Resource
    private LogService logService;

    @Override
    public List<PluginVO> listPlugins() {
        List<Plugin> pluginAll = pluginFactory.getPluginAll();
        if (CollectionUtils.isEmpty(pluginAll)) {
            return new ArrayList<>();
        }
        return pluginAll.stream().map(this::toPluginVO).collect(Collectors.toList());
    }

    private PluginVO toPluginVO(Plugin plugin) {
        PluginVO vo = new PluginVO();
        BeanUtils.copyProperties(plugin, vo);
        return vo;
    }

    @Override
    public String getPluginPath() {
        return pluginFactory.getPluginPath();
    }

    @Override
    public String getLibraryPath() {
        return pluginFactory.getLibraryPath();
    }

    @Override
    public void loadPlugins() {
        pluginFactory.loadPlugins();
    }

    @Override
    public void unload(String fileName) {
        try {
            pluginFactory.unloadPlugin(fileName);
        } catch (IllegalArgumentException e) {
            throw new BizException(e.getMessage());
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            pluginFactory.deletePlugin(fileName);
            logService.log(LogType.PluginLog.DELETE, fileName);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BizException(e.getMessage());
        }
    }

    @Override
    public void checkFileSuffix(String filename) {
        Assert.hasText(filename, "the plugin filename is null.");
        String suffix = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        FileSuffixEnum fileSuffix = FileSuffixEnum.getFileSuffix(suffix);
        Assert.notNull(fileSuffix, "Illegal file suffix");
        if (FileSuffixEnum.JAR != fileSuffix) {
            String msg = String.format("Invalid file extension \"%s\", only \"%s\" is supported.", filename, FileSuffixEnum.JAR.getName());
            logService.log(LogType.PluginLog.CHECK_ERROR, msg);
            throw new BizException(msg);
        }
    }

}