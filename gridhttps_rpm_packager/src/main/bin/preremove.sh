#Copyright (c) Istituto Nazionale di Fisica Nucleare (INFN). 2006-2010.
#Licensed under the Apache License, Version 2.0 (the "License");
#you may not use this file except in compliance with the License.
#You may obtain a copy of the License at
#       http://www.apache.org/licenses/LICENSE-2.0
#Unless required by applicable law or agreed to in writing, software
#distributed under the License is distributed on an "AS IS" BASIS,
#WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#See the License for the specific language governing permissions and
#limitations under the License.

#during an upgrade, the value of the argument passed in is 1
#during an uninstall, the value of the argument passed in is 0
if [ "$1" = "0" ] ; then
	echo 'Removing old links to StoRM GridHTTPS server jars from StoRM tomcat common/lib folder'
	if [ -L /usr/share/tomcat5/common/lib/gridhttps_common.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/gridhttps_common.jar ;
	fi
	if [ -L /usr/share/tomcat5/common/lib/gridhttps_filter.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/gridhttps_filter.jar ;
	fi
	if [ -L /usr/share/tomcat5/common/lib/httpclient-*.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/httpclient-*.jar ;
	fi
	if [ -L /usr/share/tomcat5/common/lib/httpcore-*.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/httpcore-*.jar ;
	fi
	if [ -L /usr/share/tomcat5/common/lib/commons-codec-*.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/commons-codec-*.jar ;
	fi
	
	if [ -L /usr/share/tomcat5/common/lib/commons-modeler*.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/commons-modeler*.jar  ;
	fi
	
	if [ -L /usr/share/tomcat5/server/lib/*commons-modeler*.jar.storm.saved ] ; then
		VAR=`ls /usr/share/tomcat5/server/lib/*commons-modeler*.jar.storm.saved`
		LENGTH=`expr length "$VAR"`
		LENGTH=$(($LENGTH - 12))
		NEW_NAME=`echo $VAR | head -c $LENGTH`
		mv /usr/share/tomcat5/server/lib/*commons-modeler*.jar.storm.saved  $NEW_NAME;
	fi
	
	if [ -L /usr/share/tomcat5/common/lib/trustmanager.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/trustmanager.jar
	fi
	if [ -L /usr/share/tomcat5/server/lib/trustmanager-tomcat.jar ] ; then
		unlink /usr/share/tomcat5/server/lib/trustmanager-tomcat.jar
	fi
	if [ -L /usr/share/tomcat5/common/lib/vomsjapi.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/vomsjapi.jar
	fi
	if [ -L /usr/share/tomcat5/common/lib/[bcprov].jar ] ; then
		unlink /usr/share/tomcat5/common/lib/[bcprov].jar
	fi
	if [ -L /usr/share/tomcat5/common/lib/log4j.jar ] ; then
		unlink /usr/share/tomcat5/common/lib/[log4j].jar
	fi
	if [ -L /usr/share/tomcat5/conf/log4j-trustmanager.properties ] ; then
		unlink /usr/share/tomcat5/conf/log4j-trustmanager.properties
	fi
	if [ -L /usr/share/tomcat5/common/classes/log4j.properties ] ; then
		DEST_FILE=`readlink /usr/share/tomcat5/common/classes/log4j.properties`
		if [ $? -eq 0 ] ; then
			if [ $DEST_FILE="/etc/storm/gridhttps-server/log4j-tomcat.properties" ] ; then
				unlink /usr/share/tomcat5/common/classes/log4j.properties
			fi
		else
			echo "Error. Unable to read link at /usr/share/tomcat5/common/classes/log4j.properties"
		fi
	fi
	
	
	# Remove log files
	rm -f /var/log/storm/storm-gridhttps-server*
	rm -f /var/log/storm/velocity-gridhttps*
	
	# Remove tomcat context files
	rm -f /usr/share/tomcat5/conf/Catalina/localhost/storageArea* 
	
	# Clean yaim stuff
	
	# Delete the link to we.xml generated file 
	if [ -f /etc/tomcat5/tomcat5.conf ] ; then
		# tomcat is still installed. Put back its original configuration file
		if [ -L /usr/share/tomcat5/conf/web.xml -a -f /usr/share/tomcat5/conf/web.xml.storm.saved ] ; then
			unlink /usr/share/tomcat5/conf/web.xml
			mv /usr/share/tomcat5/conf/web.xml.storm.saved /usr/share/tomcat5/conf/web.xml
		fi
		if [ -f /usr/share/tomcat5/conf/server.xml -a -f /usr/share/tomcat5/conf/server.xml.storm.saved ] ; then
			rm -f /usr/share/tomcat5/conf/server.xml
			mv /usr/share/tomcat5/conf/server.xml.storm.saved /usr/share/tomcat5/conf/server.xml
		fi
	else
		# tomcat has been removed. Remove the created configuration files 
		if [ -L /usr/share/tomcat5/conf/web.xml ] ; then
			unlink /usr/share/tomcat5/conf/web.xml
		fi
		if [ -f /usr/share/tomcat5/conf/web.xml.storm.saved ] ; then
			rm -f /usr/share/tomcat5/conf/web.xml.storm.saved
		fi
		if [ -f /usr/share/tomcat5/conf/server.xml ] ; then
			rm -f /usr/share/tomcat5/conf/server.xml
		fi
		if [ -f /usr/share/tomcat5/conf/server.xml.storm.saved ] ; then
			rm -f /usr/share/tomcat5/conf/server.xml.storm.saved 
		fi
	fi
	
	if [ -L /usr/share/tomcat5/conf/log4j-trustmanager.properties ] ; then
		unlink /usr/share/tomcat5/conf/log4j-trustmanager.properties
	fi
	
	# Delete the web.xml file produced by yaim (not owned by the rpm)
	if [ -f /etc/storm/gridhttps-server/web.xml ] ; then
		rm -f /etc/storm/gridhttps-server/web.xml
	fi

	if [ -f /etc/storm/gridhttps-server/web.xml.bkp_* ] ; then
		rm -f /etc/storm/gridhttps-server/web.xml.bkp_*
	fi
	
	#TODO delete tomcat generated certificates
	
	echo 'Removing old links to StoRM GridHTTPS server webapp war from StoRM tomcat webapps folder'
	if [ -L /usr/share/tomcat5/webapps/gridhttps.war ] ; then
		unlink /usr/share/tomcat5/webapps/gridhttps.war ;
	fi
	
	# Delete eventually produced .rpmsave files
	rm -f /etc/storm/gridhttps-server/*.rpmsave
	
elif [ "$1" = "1" ] ; then
	#for now, do nothing
	echo "Nothing to do"
fi
