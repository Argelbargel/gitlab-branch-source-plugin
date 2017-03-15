#! /bin/bash
GITLAB_SERVER=$1
PROJECT_NAME_PREFIX=$2
NUM_PROJECTS=$3
USER=$4
PRIVATE_TOKEN=$5
for i in `seq 1 ${NUM_PROJECTS}`; do
    echo "http://$GITLAB_SERVER/api/v3/projects/$USER%2F$PROJECT_NAME_PREFIX$i"
    curl -sSf -X DELETE --header "PRIVATE-TOKEN: $PRIVATE_TOKEN" "http://$GITLAB_SERVER/api/v3/projects/$USER%2F$PROJECT_NAME_PREFIX$i" > /dev/null
done