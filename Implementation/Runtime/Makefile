#UNCHECKED= -Xlint:unchecked

ifndef INSTRUMENT		## set this in your env to change behavior
INSTRUMENT=#-instrument		## or take out the comment on this line
endif

JAVAC=javac ${JAVAC_OPTS}
JRGC=jrgc ${JRGC_OPTS}

all : java classes
	cp -Rf forkjoin jsr166y extra166y java
	# Compile FJTask
	${JAVAC} -d classes java/forkjoin/*.java
	make runtime-only
#	make dpjdoc-html

java :
	mkdir $@

classes :
	mkdir $@

runtime-only:
#	# Generate Java 'template' classes
#	perl expandAll.pl
	# Compile the DPJ runtime
	${JRGC} -seq ${INSTRUMENT} -d java -cp classes ${UNCHECKED} jrg/*.java
	${JAVAC} -cp classes -d classes java/DPJRuntime/*.java

# Count DPJ Annotations
countAll :
	${JRGC} -seq -count -d java -cp classes dpj/*.java

classes/DPJRuntime/%.class: java/DPJRuntime/%.java
	${JAVAC} -d classes/DPJRuntime $<

java/DPJRuntime/%.java: dpj/%.java
	${JRGC} -seq ${INSTRUMENT} -d java ${UNCHECKED} $<


# Build javadoc for the OLD version of FJTask that the compiler still uses
# This includes the OLD version of ParallelArray, which we do not use
javadoc-forkjoin :
	javadoc forkjoin/*java -d $@

JRGDOC=${JRG_ROOT}/Implementation/Compiler/dist/bootstrap/bin/javadoc
jrgdoc-html : jrgdoc jrg/*java
	# Generate html documentation for runtime classes
	${JRGDOC} jrg/*java -classpath classes -d jrgdoc

jrgdoc :
	mkdir $@

clean :
	rm -Rf classes
	rm -Rf java
	rm -Rf jrgdoc
	perl clearAll.pl