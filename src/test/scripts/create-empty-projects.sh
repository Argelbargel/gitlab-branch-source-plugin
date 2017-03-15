#! /bin/bash
GITLAB_SERVER=$1
PROJECT_NAME_PREFIX=$2
NUM_PROJECTS=$3
USER=$4
PRIVATE_TOKEN=$5

for i in `seq 1 ${NUM_PROJECTS}`; do
    curl -sSf -X POST --header "PRIVATE-TOKEN: $PRIVATE_TOKEN" "http://$GITLAB_SERVER/api/v3/projects" -F "name=$PROJECT_NAME_PREFIX$i" -F 'visibility_level=20' > /dev/null
    echo "project $USER/$PROJECT_NAME_PREFIX$i created"
    curl -sSf -X POST --header "PRIVATE-TOKEN: $PRIVATE_TOKEN" "http://$GITLAB_SERVER/api/v3/projects/$USER%2F$PROJECT_NAME_PREFIX$i/repository/files?file_path=Jenkinsfile&branch_name=master&content=echo%20'Hello'&commit_message=create%20a%20Jenkinsfile"  > /dev/null
    echo "    Jenkinsfile created"
done