#!/bin/bash

sudo rm -rf /tmp/html*
rm -rf /tmp/jvm-1638/hs_error.log
cd /home/ec2-user/workers
#sleep 3m
#sleep $[ ( $RANDOM % 10 ) ]m
#sleep $[ ( $RANDOM % 10 ) ]m
#sleep $[ ( $RANDOM % 10 ) + 1 ]m
export EC2_HOME=/opt/aws/apitools/ec2
export JAVA_HOME=/usr/lib/jvm/java/
export EC2_URL=https://ec2.us-east-1.amazonaws.com
export  AWS_ACCESS_KEY=AKIAJQUAOI7EBC6Y7ESQ
export AWS_SECRET_KEY=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
export  AWS_ACCESS_KEY_ID=AKIAJQUAOI7EBC6Y7ESQ
export AWS_SECRET_ACCESS_KEY=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

export MAVEN_OPTS="-Xmx3048m -Xms3048m"
 /home/ec2-user/apache-maven-3.3.3/bin/mvn exec:java -Dexec.mainClass="com.workers.main.KMain" -Dexec.args="1 google" >/home/ec2-user/work.log &



sleep 50m
#/opt/aws/bin/ec2-terminate-instances $(curl http://169.254.169.254/latest/meta-data/instance-id)
