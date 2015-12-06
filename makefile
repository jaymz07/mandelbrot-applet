JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
.PHONY: clean
	
sources = $(wildcard *.java)
CLASSES = $(sources:.java=.class)

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class