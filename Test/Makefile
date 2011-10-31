ALL = sequential-classes parallel-classes
include Makefile.common

# Problem size
SIZE ?= 1024

# Foreach cutoff (min. data size)
JRG_FC ?= 100

# Number of threads
JRG_NT ?= 10

# "Pushbutton" targets
test-all :
	make test-all-seq
	make test-all-par

test-all-seq :
	${JRG-SEQ-TEST} ArrayAccessInt         TEST ${SIZE}
	${JRG-SEQ-TEST} ArrayAccess            TEST ${SIZE}
	${JRG-SEQ-TEST} ArrayAccessGeneric     TEST ${SIZE}
	${JRG-SEQ-TEST} ArrayAccessGenericInner     TEST ${SIZE}
	${JRG-SEQ-TEST} ArrayOfArrayOfInt      TEST ${SIZE}
	${JRG-SEQ-TEST} DestructiveFieldAccess TEST ${SIZE}
	${JRG-SEQ-TEST} DestructiveArrayAccess TEST ${SIZE}
	${JRG-SEQ-TEST} InstanceofSwitch       TEST ${SIZE}
	${JRG-SEQ-TEST} PardoBasic             TEST ${SIZE}
	${JRG-SEQ-TEST} ForeachBasicSeq        TEST ${SIZE}
	${JRG-SEQ-TEST} ForeachBasicPar        TEST ${SIZE}

test-all-par :
	${JRG-PAR-TEST} PardoBasic             TEST ${SIZE}
	${JRG-PAR-TEST} ForeachBasicPar        TEST ${SIZE}

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
