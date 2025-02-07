/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.model;

import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class TagInformation {

    private final String applicationName;
    private final String agentId;
    private final String metricName;
    private final String fieldName;
    private final List<Tag> tags;

    public TagInformation(String applicationName, String agentId, String metricName, String fieldName, List<Tag> tags) {
        this.applicationName = applicationName;
        this.agentId = agentId;
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.tags = tags;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public List<Tag> getTags() {
        return tags;
    }

}

