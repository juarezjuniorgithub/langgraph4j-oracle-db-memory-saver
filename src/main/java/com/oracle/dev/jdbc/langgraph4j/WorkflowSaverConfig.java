package com.oracle.dev.jdbc.langgraph4j;

import javax.sql.DataSource;

import org.bsc.langgraph4j.checkpoint.OracleSaver;

public class WorkflowSaverConfig {

  public static OracleSaver createOracleSaver(DataSource dataSource) {
    return OracleSaver.builder().dataSource(dataSource).build();
  }
}
