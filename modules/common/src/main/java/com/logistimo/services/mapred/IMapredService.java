package com.logistimo.services.mapred;

import java.util.Map;

/**
 * Created by charan on 03/03/15.
 */
public interface IMapredService {

  String PARAM_ENTITYKIND = "mapreduce.mapper.inputformat.datastoreinputformat.entitykind";
  String PARAM_BLOBKEY = "mapreduce.mapper.inputformat.blobstoreinputformat.blobkeys";
  String PARAM_DONECALLBACK = "mapreduce.appengine.donecallback.url";
  String PARAM_SHARDCOUNT = "mapreduce.mapper.shardcount";
  String PARAM_INPUTPROCESSINGRATE = "mapreduce.mapper.inputprocessingrate";
  String JOBID_PARAM = "job_id";
  String JOB_STATUS = "job_status";

  String startJob(String configName, Map<String, String> params);
}
