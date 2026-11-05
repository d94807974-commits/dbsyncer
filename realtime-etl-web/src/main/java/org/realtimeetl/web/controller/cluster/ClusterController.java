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
package org.realtimeetl.web.controller.cluster;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.model.PluginFile;
import org.realtimeetl.sdk.spi.ClusterService;
import org.realtimeetl.web.controller.BaseController;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Cluster management。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-18
 */
@Controller
@RequestMapping("/cluster")
public class ClusterController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ClusterService clusterService;

    @Resource
    private LocalNodeMetricProvider localNodeMetricProvider;

    @Resource
    private ClusterNodeMetricAggregator clusterNodeMetricAggregator;

    /**
     * ClusterListPage
     */
    @GetMapping("/list")
    public String list(ModelMap model) {
        initEditionInfo(model);
        model.put("clusterEnabled", !clusterService.isStandalone());
        return "cluster/list";
    }

    /**
     * Heart rate.Detection (no Worldwide)Login，ForNodesI don't know.
     */
    @GetMapping("/ping")
    @ResponseBody
    public RestResult ping() {
        return RestResult.restSuccess("ok");
    }

    /**
     * NodesminPage
     */
    @PostMapping("/query")
    @ResponseBody
    public RestResult query(HttpServletRequest request) {
        try {
            return RestResult.restSuccess(clusterService.query(getParams(request)));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * UpdateNode Name
     */
    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Modify Cluster Configuration")
    @PostMapping("/edit")
    @ResponseBody
    public RestResult edit(@RequestParam("id") String id, @RequestParam("name") String name) {
        try {
            clusterService.updateNodeName(id, name);
            return RestResult.restSuccess("Successfully modified node name");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * DeleteOfflineNodes
     */
    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Delete Cluster Node")
    @PostMapping("/remove")
    @ResponseBody
    public RestResult remove(@RequestParam("id") String id) {
        try {
            clusterService.removeNode(id);
            return RestResult.restSuccess("Successfully deleted node");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * Together.NodesOperational indicators (inline)+ FarHTTP）。
     */
    @GetMapping("/metrics")
    @ResponseBody
    public RestResult nodesMetrics() {
        try {
            return RestResult.restSuccess(clusterNodeMetricAggregator.collectAll());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/forceExpireGracePeriod")
    @ResponseBody
    public RestResult forceExpireGracePeriod() {
        try {
            return RestResult.restSuccess(clusterService.forceExpireGracePeriod());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * Common internal message portal (bytype/event The route. Must carry{@code X-Cluster-Token}。
     * <p>SynchroniseBack RestResult，For clustersNodes HTTP Notification parsing;notable{@code @Async}，NoIt's easy to show up.200 Emptybody。
     */
    @PostMapping("/internal/message")
    @ResponseBody
    public RestResult message(@RequestBody String message) {
        try {
            return RestResult.restSuccess(clusterService.receiveMessage(message));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @GetMapping("/internal/metrics")
    @ResponseBody
    public RestResult metrics() {
        try {
            return RestResult.restSuccess(localNodeMetricProvider.snapshot());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * Here.PluginList. Must carry{@code X-Cluster-Token}。
     */
    @GetMapping("/internal/plugin/manifest")
    @ResponseBody
    public RestResult pluginManifest() {
        try {
            return RestResult.restSuccess(clusterService.listPluginFiles());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * Read HomePluginDocumentation。Must carry{@code X-Cluster-Token}。
     */
    @GetMapping("/internal/plugin/file")
    public void pluginFile(HttpServletResponse response, @RequestParam("name") String name) {
        try {
            if (!PluginFile.isSafeName(name)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            byte[] body = clusterService.readPluginFile(name);
            if (body == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("application/octet-stream");
            response.setContentLength(body.length);
            OutputStream out = response.getOutputStream();
            out.write(body);
            out.flush();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ReceivedPluginDocumentation。FilenameOn request.{@link PluginFile#FILE_NAME_HEADER}。Must carry{@code X-Cluster-Token}。
     */
    @PostMapping("/internal/plugin/file")
    @ResponseBody
    public RestResult acceptPlugin(HttpServletRequest request) {
        try {
            String fileName = request.getHeader(PluginFile.FILE_NAME_HEADER);
            boolean relay = StringUtil.equals("1", request.getHeader(PluginFile.RELAY_HEADER));
            clusterService.acceptPluginFile(fileName, readPluginBody(request), relay);
            return RestResult.restSuccess(Boolean.TRUE);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * ReceivedPluginDelete。FilenameOn request.{@link PluginFile#FILE_NAME_HEADER}。Must carry{@code X-Cluster-Token}。
     */
    @PostMapping("/internal/plugin/remove")
    @ResponseBody
    public RestResult removePlugin(HttpServletRequest request) {
        try {
            String fileName = request.getHeader(PluginFile.FILE_NAME_HEADER);
            boolean relay = StringUtil.equals("1", request.getHeader(PluginFile.RELAY_HEADER));
            clusterService.acceptPluginRemove(fileName, relay);
            return RestResult.restSuccess(Boolean.TRUE);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    private byte[] readPluginBody(HttpServletRequest request) throws IOException {
        long length = request.getContentLengthLong();
        if (length > PluginFile.MAX_BYTES) {
            throw new BizException("Plugin file exceeding 128MB");
        }
        int initial = length > 0 && length <= Integer.MAX_VALUE ? (int) length : 8192;
        ByteArrayOutputStream out = new ByteArrayOutputStream(initial);
        InputStream in = request.getInputStream();
        byte[] buf = new byte[8192];
        long total = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            total += n;
            if (total > PluginFile.MAX_BYTES) {
                throw new BizException("Plugin file exceeding 128MB");
            }
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

}
