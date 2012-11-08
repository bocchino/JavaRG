ALL=sequential-classes parallel-classes
SEQ_JAVA=sequential/java/jrg/benchmarks/treemap/*.java
PAR_JAVA=parallel/java/jrg/benchmarks/treemap/*.java

include ../Makefile.common

# Problem size
SIZE ?= 1024

# Foreach cutoff (min. data size)
JRG_FC ?= 100

# Number of threads
JRG_NT ?= 10

# Targets requiring command-line arguments
# E.g.:
#	make test-par PROG=ListRanking
#	make test-par PROG=ListRanking SIZE=4096
#	make time-par PROG=ListRanking JRG_NT=2 JRG_FC=512
test-par :
	${JRG-PAR-TEST} ${PROG} TEST ${SIZE} ${ARG2} ${ARG3}

test-seq :
	${JRG-SEQ-TEST} ${PROG} TEST ${SIZE} ${ARG2} ${ARG3}

time-seq :
	${JRG-SEQ} ${PROG} TIME ${SIZE} ${ARG2} ${ARG3}

time-par :
	${JRG-PAR} ${PROG} --dpj-foreach-cutoff ${JRG_FC} --dpj-num-threads ${JRG_NT} \
  TIME ${SIZE} ${ARG2} ${ARG3}

.PRECIOUS: java/%.java
