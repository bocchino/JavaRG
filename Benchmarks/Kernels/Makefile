ALL = sequential-classes parallel-classes
include ../Makefile.common

# Problem size
SIZE ?= 1024

# Foreach cutoff (min. data size)
JRG_FC ?= 1

# Number of threads
JRG_NT ?= 1

# "Pushbutton" targets
test-all :
	make test-all-seq
	make test-all-par

test-all-seq :
	${JRG-SEQ-TEST} MergeSort4 TEST ${SIZE}

test-all-par :
	${JRG-PAR-TEST} MergeSort4 TEST ${SIZE}

# Targets requiring command-line arguments
# E.g.:
#	make test-par PROG=ListRanking
#	make test-par PROG=ListRanking SIZE=4096
#	make time-par PROG=ListRanking JRG_NT=2 JRG_FC=512
test-par :
	${JRG-PAR-TEST} ${PROG} TEST ${SIZE} ${ARG2} ${ARG3}

test-seq :
	${JRG-SEQ-TEST} ${PROG} TEST ${SIZE} ${ARG2} ${ARG3}

ideal :
	${JRG-INST} ${PROG} IDEAL ${SIZE} ${ARG2} ${ARG3}

time-seq :
	${JRG-SEQ} ${PROG} TIME ${SIZE} ${ARG2} ${ARG3}

time-par :
	${JRG-PAR} ${PROG} --dpj-foreach-cutoff ${JRG_FC} --dpj-num-threads ${JRG_NT} \
  TIME ${SIZE} ${ARG2} ${ARG3}

.PRECIOUS: java/%.java
