ALL = sequential-classes parallel-classes
SRCS= ${wildcard jrg/*.java}
OLDEN= olden/original
include ../Makefile.common

NBODY ?= 100000

run :
	java -cp classes BarnesHut ${NBODY}

test:
	${JRG-PAR} BarnesHut --dpj-num-threads 10 100000 true > output
	diff --brief -s output ref.output

run-inst :
	${JRG-INST} BarnesHut ${NBODY} ${DEBUG}

run-seq :
	${JRG-SEQ} BarnesHut ${NBODY} ${DEBUG}

run-par:
	${JRG-PAR} BarnesHut --dpj-num-threads ${TN} ${NBODY} ${DEBUG}

run-par1:
	java -cp ${RUNTIME}/classes:parallel/classes:${RUNTIME}/classes/EDU/oswego/cs/dl/util/concurrent BarnesHut --dpj-num-threads ${TN} ${NBODY}
