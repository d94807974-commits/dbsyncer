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
package org.realtimeetl.web.controller.log;

import org.realtimeetl.biz.MonitorService;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.web.controller.BaseController;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Login logs / Operation logs。
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Controller
@RequestMapping("/log")
public class LogController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MonitorService monitorService;

    @GetMapping("/login")
    public String loginPage(ModelMap model) {
        requirePermission("system:view");
        return "log/login";
    }

    @GetMapping("/oper")
    public String operPage(ModelMap model) {
        requirePermission("system:view");
        model.put("logModules", LogType.listOperModuleNames());
        return "log/oper";
    }

    @PostMapping("/login/query")
    @ResponseBody
    public RestResult queryLogin(HttpServletRequest request) {
        try {
            requirePermission("system:view");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(monitorService.queryLoginLog(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/oper/query")
    @ResponseBody
    public RestResult queryOper(HttpServletRequest request) {
        try {
            requirePermission("system:view");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(monitorService.queryOperLog(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/detail")
    @ResponseBody
    public RestResult detail(@RequestParam("id") String id) {
        try {
            requirePermission("system:view");
            return RestResult.restSuccess(monitorService.getLogDetail(id));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Empty Log")
    @PostMapping("/clear")
    @ResponseBody
    public RestResult clear() {
        try {
            requirePermission("system:edit");
            return RestResult.restSuccess(monitorService.clearLog());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }
}
