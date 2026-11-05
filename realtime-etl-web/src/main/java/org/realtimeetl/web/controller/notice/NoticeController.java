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
package org.realtimeetl.web.controller.notice;

import org.realtimeetl.biz.GroupService;
import org.realtimeetl.biz.NoticeRuleService;
import org.realtimeetl.biz.NoticeStrategyService;
import org.realtimeetl.biz.SystemConfigService;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.sdk.model.NoticeConfig;
import org.realtimeetl.sdk.notice.MessageService;
import org.realtimeetl.web.controller.BaseController;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

@Controller
@RequestMapping(value = "/notice")
public class NoticeController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private MessageService messageService;

    @Resource
    private GroupService groupService;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private NoticeRuleService noticeRuleService;

    @Resource
    private NoticeStrategyService noticeStrategyService;

    @RequestMapping("")
    public String index(ModelMap model) {
        SystemConfig systemConfig = systemConfigService.getSystemConfig();
        NoticeConfig noticeConfig = systemConfig.getNoticeConfig();
        if (noticeConfig == null) {
            noticeConfig = new NoticeConfig();
            systemConfig.setNoticeConfig(noticeConfig);
        }
        model.put("config", noticeConfig);
        model.put("strategies", noticeStrategyService.listAll());
        model.put("noticeRules", noticeRuleService.listAll());
        model.put("groups", groupService.list());
        Paging<Mapping> mappingPaging = mappingProfile.query(1, 500, null, null);
        model.put("mappings", mappingPaging == null ? new ArrayList<>() : mappingPaging.getData());
        return "notice/list";
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Modify alarm notice")
    @PostMapping("/edit")
    @ResponseBody
    public RestResult edit(HttpServletRequest request) {
        try {
            requirePermission("notice:edit");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(systemConfigService.editNoticeConfig(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Save alarm strategy")
    @PostMapping("/strategy/save")
    @ResponseBody
    public RestResult saveStrategy(HttpServletRequest request) {
        try {
            requirePermission("notice:edit");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(noticeStrategyService.save(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Delete alarm strategy")
    @PostMapping("/strategy/remove")
    @ResponseBody
    public RestResult removeStrategy(HttpServletRequest request) {
        try {
            requirePermission("notice:edit");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(noticeStrategyService.remove(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Keep the alarm rules.")
    @PostMapping("/rule/save")
    @ResponseBody
    public RestResult saveRule(HttpServletRequest request) {
        try {
            requirePermission("notice:edit");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(noticeRuleService.save(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Delete alarm rules")
    @PostMapping("/rule/remove")
    @ResponseBody
    public RestResult removeRule(HttpServletRequest request) {
        try {
            requirePermission("notice:edit");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(noticeRuleService.remove(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/test")
    @ResponseBody
    public RestResult test(HttpServletRequest request) {
        try {
            requirePermission("notice:edit");
            return RestResult.restSuccess(messageService.testSendMessage());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail("Sweden");
        }
    }

}
