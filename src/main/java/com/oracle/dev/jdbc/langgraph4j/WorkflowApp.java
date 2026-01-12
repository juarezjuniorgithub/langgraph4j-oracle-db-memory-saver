package com.oracle.dev.jdbc.langgraph4j;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import java.util.Map;
import java.util.Optional;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.checkpoint.OracleSaver;
import org.bsc.langgraph4j.state.AgentState;

public class WorkflowApp {

  public static void main(String[] args) throws Exception {

    // Initialize OracleSaver with the Oracle UCP data source
    OracleSaver saver = WorkflowSaverConfig.createOracleSaver(OracleDbUtils.getPooledDataSource());

    // Define Node 1
    NodeAction<AgentState> node1Action = state -> {
      System.out.println("action executed by node 1");
      return Map.of("node1_output", "Node 1 completed");
    };

    // Define Node 2
    NodeAction<AgentState> node2Action = state -> {
      System.out.println("action executed by node 2");
      return Map.of("node2_output", "Node 2 completed");
    };

    // Create a state graph with our nodes
    StateGraph<AgentState> graph = new StateGraph<>(AgentState::new).addNode("node1", node_async(node1Action))
        .addNode("node2", node_async(node2Action)).addEdge(START, "node1").addEdge("node1", "node2")
        .addEdge("node2", END);

    // Configure compilation with checkpoint saver
    CompileConfig compileConfig = CompileConfig.builder().checkpointSaver(saver).build();

    // Create runtime configuration
    RunnableConfig runnableConfig = RunnableConfig.builder().build();

    // Compile the graph into an executable workflow
    CompiledGraph<AgentState> workflow = graph.compile(compileConfig);

    // Define input data for the workflow
    Map<String, Object> inputs = Map.of("input", "test_input");

    // Execute the workflow
    Optional<AgentState> result = workflow.invoke(inputs, runnableConfig);

    // Print the result
    System.out.println("Workflow completed. Result: " + result.isPresent());

    // Print the execution history
    workflow.getStateHistory(runnableConfig).forEach(s -> System.out.println("Node executed: " + s.node()));

    // Clean up resources
    saver.release(runnableConfig);
  }
}
