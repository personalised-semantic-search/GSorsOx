package cs.ox.ac.uk.experiments.toit2014;

public enum GRewMetric {

//	OVERALL_TIME,
	DEPGRAPH_TIME,DEPGRAPH_MEM,
	REW_SIZE, REW_MEM, REW_TIME,
	REW_CNS_MEM, REW_CNS_TIME,
	PREFGRAPH_CONST_TIME, PREFGRAPH_CONST_MEM, PREFGRAPH_CONST_SIZE_E,PREFGRAPH_CONST_SIZE_V,
	PREFGRAPH_MERGE_TIME, PREFGRAPH_MERGE_MEM, PREFGRAPH_MERGE_SIZE_E,PREFGRAPH_MERGE_SIZE_V,
//	PREFGRAPH_REMOVE_CYCLE_TIME, PREFGRAPH_REMOVE_CYCLE_MEM, PREFGRAPH_REMOVE_CYCLE_SIZE_E,PREFGRAPH_REMOVE_CYCLE_SIZE_V
	PREFGRAPH_TOPK_TIME, PREFGRAPH_TOPK_MEM, PREFGRAPH_TOPK_SIZE_E,PREFGRAPH_TOPK_SIZE_V,
	ANSWER_SIZE, TRANSITIVE_CLOSURE_TIME, PREFGRAPH_TRA_SIZE_V, PREFGRAPH_TRA_SIZE_E
	;
}
