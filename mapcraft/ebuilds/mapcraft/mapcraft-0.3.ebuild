# Copyright 1999-2004 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header$

SLOT="0"
LICENSE="GPL-2"
KEYWORDS="~x86"
DESCRIPTION="Java based map editor for roleplaying games"
SRC_URI="mirror://sourceforge/mapcraft/mapcraft-0.3.tgz"
HOMEPAGE="http://mapcraft.sourceforge.net"
IUSE=""
DEPEND=">=dev-java/sun-jdk-1.4.2"
RDEPEND=""


src_compile() {
	echo src_compile
	echo ${WORKDIR}
	cd ${WORKDIR}/mapcraft-0.3/scripts
	cat mapcraft.sh | sed "s/mapcraft\.jar/\/usr\/share\/mapcraft\/lib\/mapcraft-0.3.jar/" > mapcraft.tmp
	rm mapcraft.sh
	mv mapcraft.tmp mapcraft.sh
	make mapcraft
}

src_install() {
	cd ${WORKDIR}/mapcraft-0.3

	LIBDIR=${D}/usr/share/mapcraft/lib
	BINDIR=${D}/usr/bin
	SAMPLESDIR=${D}/usr/share/mapcraft/samples
	mkdir -p $LIBDIR
	mkdir -p $BINDIR
	mkdir -p $SAMPLESDIR
	
	cp mapcraft.jar $LIBDIR/mapcraft-0.3.jar
	cp scripts/mapcraft $BINDIR
	cp maps/*.map $SAMPLESDIR
}

