#!/bin/sh


NOW=$(date +"%m-%d-%Y-%r")
project_dir=`pwd`
log_file="${project_dir}/logs/server.log${NOW}"
echo "${log_file}"
conf_dir="/exlibris/dps/d4_1/system.dir/conf/global.properties"
LCP=${LCP}:$project_dir/lib/commons-io-2.1.jar
LCP=${LCP}:$project_dir/lib/commons-lang-2.6.jar
LCP=${LCP}:$project_dir/lib/commons-logging-1.1.1.jar
LCP=${LCP}:$project_dir/lib/dom4j-1.6.1.jar
LCP=${LCP}:$project_dir/lib/dps-sdk-4.2.1.jar
LCP=${LCP}:$project_dir/lib/log4j-1.2.14.jar
LCP=${LCP}:$project_dir/lib/oracle11g-2.0.4.0.jar
LCP=${LCP}:$project_dir/lib/xmlbeans-2.6.0.jar
LCP=${LCP}:$project_dir/lib/commons-lang3.jar
LCP=${LCP}:$project_dir/lib/cms-converter-tool.jar
file_location=`cat ${project_dir}/properties`
${JAVA_HOME}/bin/java -cp ${LCP} cmsConverter.CmsConverter $conf_dir $file_location >> ${log_file}
STATUS=$?
if [ $STATUS == 2 ]
then
	echo "The synchronization as finished with error please see the log"
elif [ $STATUS == 3 ] 
then
	echo "The synchronization as finished with warning please see the log"
else
	echo "synchronization was completed successfully."
fi
exit 0;