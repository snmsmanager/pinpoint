/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.grpc;

import java.util.Objects;

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.grpc.trace.PCommandType;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcThreadDumpMessageConverter;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ThreadDumpMapper;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import com.navercorp.pinpoint.profiler.receiver.service.ActiveThreadDumpCoreService;
import com.navercorp.pinpoint.profiler.receiver.service.ThreadDump;
import com.navercorp.pinpoint.profiler.receiver.service.ThreadDumpRequest;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class GrpcActiveThreadDumpService implements ProfilerGrpcCommandService {

    static final String JAVA = "JAVA";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ActiveThreadDumpCoreService activeThreadDump;
    private final GrpcThreadDumpMessageConverter grpcThreadDumpMessageConverter;


    public GrpcActiveThreadDumpService(
            ActiveTraceRepository activeTraceRepository,
            ThreadDumpMapper threadDumpMapper
    ) {
        Objects.requireNonNull(activeTraceRepository, "activeTraceRepository");

        this.activeThreadDump = new ActiveThreadDumpCoreService(activeTraceRepository);
        this.grpcThreadDumpMessageConverter = new GrpcThreadDumpMessageConverter(threadDumpMapper);
    }

    @Override
    public void handle(PCmdRequest request, ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub) {
        logger.info("simpleCommandService:{}", request);

        PCmdActiveThreadDump commandActiveThreadDump = request.getCommandActiveThreadDump();

        PCmdActiveThreadDumpRes.Builder builder = PCmdActiveThreadDumpRes.newBuilder();

        PCmdResponse commonResponse = PCmdResponse.newBuilder().setResponseId(request.getRequestId()).build();
        builder.setCommonResponse(commonResponse);

        builder.setType(JAVA);
        builder.setSubType(JvmUtils.getType().name());
        builder.setVersion(JvmUtils.getVersion().name());

        List<PActiveThreadDump> activeThreadDumpList = getActiveThreadDumpList(commandActiveThreadDump);
        builder.addAllThreadDump(activeThreadDumpList);

        profilerCommandServiceStub.commandActiveThreadDump(builder.build(), EmptyStreamObserver.create());
    }

    private List<PActiveThreadDump> getActiveThreadDumpList(PCmdActiveThreadDump commandActiveThreadDump) {
        ThreadDumpRequest request = ThreadDumpRequest.create(commandActiveThreadDump);

        Collection<ThreadDump> activeTraceInfoList = activeThreadDump.getActiveThreadDumpList(request);

        return toPActiveThreadDump(activeTraceInfoList);
    }

    private List<PActiveThreadDump> toPActiveThreadDump(Collection<ThreadDump> activeTraceInfoList) {
        final List<PActiveThreadDump> result = new ArrayList<PActiveThreadDump>(activeTraceInfoList.size());
        for (ThreadDump threadDump : activeTraceInfoList) {
            PActiveThreadDump pActiveThreadLightDump = createActiveThreadDump(threadDump);
            result.add(pActiveThreadLightDump);
        }

        return result;
    }

    private PActiveThreadDump createActiveThreadDump(ThreadDump threadDump) {
        final ThreadInfo threadInfo = threadDump.getThreadInfo();
        ThreadDumpMetricSnapshot threadDumpMetricSnapshot = ThreadDumpUtils.createThreadDump(threadInfo);
        PThreadDump pThreadDump = grpcThreadDumpMessageConverter.toMessage(threadDumpMetricSnapshot);

        final ActiveTraceSnapshot activeTraceInfo = threadDump.getActiveTraceSnapshot();
        PActiveThreadDump.Builder builder = PActiveThreadDump.newBuilder();
        builder.setStartTime(activeTraceInfo.getStartTime());
        builder.setLocalTraceId(activeTraceInfo.getLocalTransactionId());
        builder.setThreadDump(pThreadDump);

        if (activeTraceInfo.isSampled()) {
            builder.setSampled(true);
            builder.setTransactionId(activeTraceInfo.getTransactionId());
            builder.setEntryPoint(activeTraceInfo.getEntryPoint());
        }
        return builder.build();
    }

    @Override
    public short getCommandServiceCode() {
        return PCommandType.ACTIVE_THREAD_DUMP_VALUE;
    }

}
