AXIS_USABLE_LIBS = UCLIBC GLIBC
include $(AXIS_TOP_DIR)/tools/build/Rules.axis

PROGS     = serverSixBit

INSTDIR   = $(prefix)/bin/
INSTMODE  = 0755
INSTOWNER = root
INSTGROUP = root

ifeq ($(AXIS_STATIC),static)
# Add libraries here
LDLIBS   += 
endif

OBJS = server.o

all:	$(PROGS)

$(PROGS): $(OBJS)
	$(CC) $(LDFLAGS) $^ $(LIBS) $(LDLIBS) -o $@

install:	$(PROGS)
	$(INSTALL) -d $(INSTDIR)
	$(INSTALL) -m $(INSTMODE) -o $(INSTOWNER) -g $(INSTGROUP) $(PROGS) $(INSTDIR)

clean:
	rm -f $(PROGS) *.o core

