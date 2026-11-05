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
package org.realtimeetl.web.controller.system;

import org.realtimeetl.biz.SystemConfigService;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.web.controller.BaseController;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping(value = "/system")
public class SystemController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @RequestMapping("")
    public String index(ModelMap model) {
        model.put("config", systemConfigService.getSystemConfigVo());
        initEditionInfo(model);
        return "system/list";
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Modify System Configuration")
    @PostMapping("/edit")
    @ResponseBody
    public RestResult edit(HttpServletRequest request) {
        try {
            requirePermission("system:edit");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(systemConfigService.edit(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Generate RSA Key")
    @PostMapping("/generateRSA")
    @ResponseBody
    public RestResult generateRSA(HttpServletRequest request, @RequestParam(value = "keyLength") int keyLength) {
        try {
            requirePermission("system:edit");
            return RestResult.restSuccess(systemConfigService.createRSAConfig(keyLength));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * Generate API KeyOpenAPI Login)
     */
    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Gene Subject")
    @PostMapping("/generateApiSecret")
    @ResponseBody
    public RestResult generateApiSecret(HttpServletRequest request) {
        try {
            requirePermission("system:edit");
            return RestResult.restSuccess(systemConfigProfile.generateApiSecret());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

}