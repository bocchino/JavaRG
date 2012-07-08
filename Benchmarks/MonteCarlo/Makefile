PROG = JGFMonteCarloBenchHarness
SRCS= ${wildcard dpj/*.java}
CUTOFF ?= 100
SIZE ?= 0
NT ?=1
HEAP ?= -Xmx1024m

include ../Makefile.common

run-seq :
	${JRG-SEQ} ${HEAP} ${PROG} TIME ${SIZE}

run-par:
	${JRG-PAR} ${HEAP} ${PROG} --dpj-foreach-cutoff ${CUTOFF} --dpj-num-threads ${NT} TIME ${SIZE}

test:
	make run-seq SIZE=0
	make run-seq SIZE=1
	make run-par CUTOFF=100 NT=10 SIZE=0
	make run-par CUTOFF=100 NT=10 SIZE=1
