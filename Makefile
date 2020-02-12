#
# Makefile for DeadboltEdit
#
# Default Build: no debug, no obfuscation
#
#
# ################## Makefile Main Options ######################
#
# Please refer to README.txt and the comments in BuildConfig.java
# for information about compiling MacOSXAppAdapterClass.
#
INCLUDE_MACOSX_APPADAPTER= true
#INCLUDE_MACOSX_APPADAPTER= false
#
JC= javac
#
LIBS= -C /Dev/Libs/OpenSSL org
#
JFLAGS_PRODUCTION= -g:none -Xlint:deprecation -Xlint:unchecked -classpath /Dev/Libs/OpenSSL
JFLAGS_DEBUG= -g -Xlint:deprecation -Xlint:unchecked -classpath /Dev/Libs/OpenSSL
#
#
# ************************************************************************
#
# Note: Java Compiler will build all needed class files in source dir when
#       compiling main class.  Basically, javac handles class dependency 
#       checking, so not necessary to do that here.
#
#       See man(1) javac
#
#       MacOSXAppAdapter must be explicitly compiled, because it is
#       a dynamically loaded class and is not recognized as a dependency
#       to javac. (see below)
#
#
# ************************************************************************
DEFS=
SOURCEPATH= src
PKG= org/mwsoftware/deadboltedit

SOURCEDIR= $(SOURCEPATH)/$(PKG)
# The explicit Resources list is used to avoid including the 
# Subversion (.svn) subdirectories in the jar file.
RESOURCES= Resources/Help/*.html Resources/Help/*.png \
           Resources/defaultSettings.dat \
           Resources/images/*.png Resources/images/Silk16x16/*.png \
           Resources/license.html
# ************************************************************************

all: DeadboltEdit/DeadboltEdit.jar


# Default build
DeadboltEdit/DeadboltEdit.jar: $(SOURCEDIR)/*.java $(RESOURCES) $(SOURCEDIR)/Manifestfile Makefile
ifeq ($(INCLUDE_MACOSX_APPADAPTER),true)
	$(JC) $(DEFS) $(JFLAGS_PRODUCTION) -d classes -sourcepath $(SOURCEPATH) $(SOURCEDIR)/MacOSXAppAdapterClass.java
endif
	$(JC) $(DEFS) $(JFLAGS_PRODUCTION) -d classes -sourcepath $(SOURCEPATH) $(SOURCEDIR)/DeadboltEdit.java
	jar -cfm DeadboltEdit/DeadboltEdit.jar $(SOURCEDIR)/Manifestfile $(RESOURCES) -C classes org
	jar -uf DeadboltEdit/DeadboltEdit.jar $(LIBS)
	chmod 755 DeadboltEdit
	chmod 755 DeadboltEdit/DeadboltEdit.jar
	chmod 755 DeadboltEdit/deadboltedit
	chmod 755 DeadboltEdit/deadboltedit.bat
	cp NOTICE.txt DeadboltEdit
	cp LICENSE.txt DeadboltEdit
	chmod 755 DeadboltEdit/NOTICE.txt DeadboltEdit/LICENSE.txt

# Debug build
debug: $(SOURCEDIR)/*.java $(RESOURCES) $(SOURCEDIR)/Manifestfile Makefile
ifeq ($(INCLUDE_MACOSX_APPADAPTER),true)
	$(JC) $(DEFS) $(JFLAGS_DEBUG) -d classes -sourcepath $(SOURCEPATH) $(SOURCEDIR)/MacOSXAppAdapterClass.java
endif
	$(JC) $(DEFS) $(JFLAGS_DEBUG) -d classes -sourcepath $(SOURCEPATH) $(SOURCEDIR)/DeadboltEdit.java
	jar -cfm DeadboltEdit/DeadboltEdit.jar $(SOURCEDIR)/Manifestfile $(RESOURCES) -C classes org
	jar -uf DeadboltEdit/DeadboltEdit.jar $(LIBS)
	chmod 755 DeadboltEdit
	chmod 755 DeadboltEdit/DeadboltEdit.jar
	chmod 755 DeadboltEdit/deadboltedit
	chmod 755 DeadboltEdit/deadboltedit.bat
	cp NOTICE.txt DeadboltEdit
	cp LICENSE.txt DeadboltEdit
	chmod 755 DeadboltEdit/NOTICE.txt DeadboltEdit/LICENSE.txt

#
#
# make class files only (make class)
class: $(SOURCEDIR)/*.java $(RESOURCES) $(SOURCEDIR)/Manifestfile Makefile
ifeq ($(INCLUDE_MACOSX_APPADAPTER),true)
	$(JC) $(DEFS) $(JFLAGS_PRODUCTION) -d classes -sourcepath $(SOURCEPATH) $(SOURCEDIR)/MacOSXAppAdapterClass.java
endif
	$(JC) $(DEFS) $(JFLAGS_PRODUCTION) -d classes -sourcepath $(SOURCEPATH) $(SOURCEDIR)/DeadboltEdit.java
	ls -al classes

# make jar file (after make class)
jar: classes/$(PKG)/*.class
	jar -cfm DeadboltEdit/DeadboltEdit.jar $(SOURCEDIR)/Manifestfile $(RESOURCES) -C classes org
	jar -uf DeadboltEdit/DeadboltEdit.jar $(LIBS)
	chmod 755 DeadboltEdit
	chmod 755 DeadboltEdit/DeadboltEdit.jar
	chmod 755 DeadboltEdit/deadboltedit
	chmod 755 DeadboltEdit/deadboltedit.bat
	cp NOTICE.txt DeadboltEdit
	cp LICENSE.txt DeadboltEdit
	chmod 755 DeadboltEdit/NOTICE.txt DeadboltEdit/LICENSE.txt

#
clean:
	/bin/rm -f DeadboltEdit/*.jar DeadboltEdit/*.tmp *.map
	rm -rf classes/org

# End

