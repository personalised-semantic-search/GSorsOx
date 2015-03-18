package org.deri.iris.queryrewriting;

public enum RewMetric {

	OVERALL_TIME, DEPGRAPH_TIME, QELIM_TIME, REW_TIME, SUBCHECK_TIME, FACTOR_TIME, GENERATED_QUERIES, EXPLORED_QUERIES, REW_SIZE, OVERHEAD_TIME, AUX_CLEANING_TIME, JOIN_COUNT, ATOM_COUNT, COVERING_CACHE_HITS, ELIM_ATOM_COUNT, MAX_COVERING_CACHE_SIZE, NON_COVERING_CACHE_HITS, MAX_NON_COVERING_CACHE_SIZE, CNS_VIOLATION_TIME, MAPSTO_CACHE_HITS, NOT_MAPSTO_CACHE_HITS, MAX_MAPSTO_CACHE_SIZE, MAPSTO_CHECK_COUNT, COVER_CHECK_COUNT, CARTESIAN_CACHE_HITS, MGU_COUNT, MGU_CACHE_HITS, FACTOR_CACHE_HITS, FACTOR_COUNT, RENAMING_COUNT, RENAMING_TIME, RENAMING_CACHE_HITS, NON_FACTOR_CACHE_HITS, MAX_FACTOR_CACHE_SIZE, MAX_NON_FACTOR_CACHE_SIZE, MAX_NOT_MAPSTO_CACHE_SIZE, UNFOLD_TIME, DECOMPOSITION_SIZE, DECOMPOSITION_TIME, DEPGRAPH_MEM, REW_MEM, SUBCHECKPURGE_COUNT, REW_CNS_TIME, REW_CNS_COUNT, NCPURGE_COUNT, REW_CNS_MEM, MAX_MGU_CACHE_SIZE, MAX_RENAMING_CACHE_SIZE;
}