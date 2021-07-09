FROM ${docker.ass.image}:${alfresco.ass.version}

ARG SOLR_DIR=/opt/alfresco-search-services

# Copy custom shared.properties
COPY shared.properties $SOLR_DIR/solrhome/conf/shared.properties