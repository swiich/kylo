package com.thinkbiganalytics.jobrepo.rest.controller;

/*-
 * #%L
 * thinkbig-job-repository-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
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
 * #L%
 */

import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NiFiFeedProcessorStatsContainer;
import com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStatsTransform;
import com.thinkbiganalytics.rest.model.LabelValue;
import com.thinkbiganalytics.security.AccessController;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Operations Manager - Feeds", produces = "application/json")
@Path("/v2/provenance-stats")
public class NifiFeedProcessorStatisticsRestControllerV2 {

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private AccessController accessController;

    @Autowired
    private NifiFeedProcessorStatisticsProvider statsProvider;

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the provenance statistics for all feeds.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the provenance stats.", response = com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats.class, responseContainer = "List")
    )
    public Response findStats() {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            List<? extends NifiFeedProcessorStats> list = statsProvider.findWithinTimeWindow(DateTime.now().minusDays(1), DateTime.now());
            List<com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats> model = NifiFeedProcessorStatsTransform.toModel(list);
            return Response.ok(model).build();
        });
    }

    @GET
    @Path("/{feedName}/processor-duration/{timeframe}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns the list of stats for each processor within the given timeframe relative to now")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the list of stats for each processor within the given timeframe relative to now", response = com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats.class, responseContainer = "List")
    )
    public Response findStats(@PathParam("feedName") String feedName, @PathParam("timeframe") @DefaultValue("THREE_MIN") NifiFeedProcessorStatisticsProvider.TimeFrame timeframe) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            NiFiFeedProcessorStatsContainer statsContainer = new NiFiFeedProcessorStatsContainer(timeframe);
            List<? extends NifiFeedProcessorStats> list = statsProvider.findFeedProcessorStatisticsByProcessorName(feedName, statsContainer.getStartTime(),statsContainer.getEndTime());
            List<com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats> model = NifiFeedProcessorStatsTransform.toModel(list);
            statsContainer.setStats(model);
            return Response.ok(statsContainer).build();
        });
    }

    @GET
    @Path("/{feedName}/{timeframe}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the statistics for the specified feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the feed statistics.", response = com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats.class, responseContainer = "List")
    )
    public Response findFeedStats(@PathParam("feedName") String feedName, @PathParam("timeframe") @DefaultValue("THREE_MIN") NifiFeedProcessorStatisticsProvider.TimeFrame timeframe) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            NiFiFeedProcessorStatsContainer statsContainer = new NiFiFeedProcessorStatsContainer(timeframe);
            List<? extends NifiFeedProcessorStats> list = statsProvider.findForFeedStatisticsGroupedByTime(feedName, statsContainer.getStartTime(),statsContainer.getEndTime());
            List<com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats> model = NifiFeedProcessorStatsTransform.toModel(list);
            Integer timeInterval = 5000;
            Long diff = statsContainer.getEndTime().getMillis() - statsContainer.getStartTime().getMillis();
            DateTime start = statsContainer.getStartTime();
            if(model != null && !model.isEmpty()){
                //add in times based
                Long maxTime = model.stream().map(item -> item.getMaxEventTime().getMillis()).max(Long::compare).get();
                Long endTime = statsContainer.getEndTime().getMillis();
                diff = endTime - maxTime;
                start = new DateTime(maxTime);
            }
            Long extraBlankItems = diff/timeInterval;
            Integer extras = extraBlankItems.intValue();
            if(model == null){
                model = new ArrayList<>();
            }
            for(int i=0; i< extras; i++){
                start = start.plus(timeInterval);
                if(start.isBefore(statsContainer.getEndTime())) {
                    com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats stats = new com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats();
                    stats.setFeedName(feedName);
                    stats.setMinEventTime(start);
                    stats.setMaxEventTime(start);
                    model.add(stats);
                }
            }

            statsContainer.setStats(model);
            return Response.ok(statsContainer).build();
        });
    }

    @GET
    @Path("/time-frame-options")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the default time frame options.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the time frame options.", response = LabelValue.class, responseContainer = "List")
    )
    public Response getTimeFrameOptions() {
        List<LabelValue> vals = Arrays.stream(NifiFeedProcessorStatisticsProvider.TimeFrame.values())
            .map(timeFrame -> new LabelValue(timeFrame.getDisplayName(), timeFrame.name()))
            .collect(Collectors.toList());
        return Response.ok(vals).build();
    }
}
