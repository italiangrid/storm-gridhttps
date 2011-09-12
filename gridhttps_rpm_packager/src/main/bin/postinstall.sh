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
 
#during an install, the value of the argument passed in is 1
#during an upgrade, the value of the argument passed in is 2
if [ "$1" = "1" ] ; then

#Create links only to the jars provided with this rpm, yaim module will create the others (trustmanager ecc)
	echo "Creating links to the provided jars in tomcat lib folders"
	ln -sf /usr/share/java/storm-gridhttps-server/gridhttps_common.jar  /usr/share/tomcat5/common/lib/
	ln -sf /usr/share/java/storm-gridhttps-server/gridhttps_filter.jar  /usr/share/tomcat5/common/lib/
	
	ln -sf /usr/share/java/storm-gridhttps-server/httpclient-*.jar  /usr/share/tomcat5/common/lib/
	ln -sf /usr/share/java/storm-gridhttps-server/httpcore-*.jar  /usr/share/tomcat5/common/lib/
	ln -sf /usr/share/java/storm-gridhttps-server/commons-codec-*.jar  /usr/share/tomcat5/common/lib/
	if [ -L /usr/share/tomcat5/server/lib/*commons-modeler*.jar ] ; then
		echo "backupping the existing link to commons-modeler.jar in server/lib"
		VAR=`ls /usr/share/tomcat5/server/lib/*commons-modeler*.jar`
		mv /usr/share/tomcat5/server/lib/*commons-modeler*.jar  $VAR.storm.saved;
	fi
	if [ -L /usr/share/tomcat5/common/lib/*commons-modeler*.jar ] ; then
		echo "backupping the existing link to commons-modeler.jar in common/lib"
		VAR=`ls /usr/share/tomcat5/common/lib/*commons-modeler*.jar`
		mv /usr/share/tomcat5/common/lib/*commons-modeler*.jar  $VAR.storm.saved;
	fi
	ln -sf /usr/share/java/storm-gridhttps-server/commons-modeler-2.0.1.jar  /usr/share/tomcat5/common/lib/commons-modeler-2.0.1.jar
	
	ln -sf /usr/share/java/storm-gridhttps-server/gridhttps.war  /usr/share/tomcat5/webapps/
	
	if [ ! -f /usr/share/tomcat5/common/classes/log4j.properties -a ! -L /usr/share/tomcat5/common/classes/log4j.properties ] ; then
		ln -sf /etc/storm/gridhttps-server/log4j-tomcat.properties  /usr/share/tomcat5/common/classes/log4j.properties
	fi
	
	
elif [ "$1" = "2" ] ; then
	# Let tomcat reload the webapp
	touch /usr/share/java/storm-gridhttps-server/gridhttps.war
fi