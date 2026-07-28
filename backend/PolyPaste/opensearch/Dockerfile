FROM opensearchproject/opensearch:2.8.0

ENV MINIO_ACCESS_KEY_ID=admin
ENV MINIO_SECRET_ACCESS_KEY=superpassword

RUN /usr/share/opensearch/bin/opensearch-plugin install --batch repository-s3

RUN echo "s3.client.default.endpoint: my-minio-container:9000" >> /usr/share/opensearch/config/opensearch.yml
RUN echo "s3.client.default.protocol: http" >> /usr/share/opensearch/config/opensearch.yml

RUN /usr/share/opensearch/bin/opensearch-keystore create
RUN echo $MINIO_ACCESS_KEY_ID | /usr/share/opensearch/bin/opensearch-keystore add --stdin s3.client.default.access_key
RUN echo $MINIO_SECRET_ACCESS_KEY | /usr/share/opensearch/bin/opensearch-keystore add --stdin s3.client.default.secret_key