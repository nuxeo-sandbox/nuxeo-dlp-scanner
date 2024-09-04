# vim:set ft=dockerfile:
ARG BASE_IMAGE

FROM ${BASE_IMAGE}

ARG VERSION
ARG SCM_REF
ARG BUILD_TAG
ARG CLID
ARG PACKAGES

LABEL com.customer.sample.version=${VERSION}
LABEL com.customer.sample.scm-ref=${SCM_REF}
LABEL com.customer.sample.build-tag=${BUILD_TAG}

COPY --chown=nuxeo:0 target/nuxeo-dlp-scanner-package-*.zip /home/nuxeo/local-packages/nuxeo-dlp-scanner-package.zip

# (Optional) Include the google credentials.
# Replace the filename with your value
COPY --chown=900:0 google-credential.json /opt/nuxeo/google-credential.json
ENV GOOGLE_APPLICATION_CREDENTIALS=/opt/nuxeo/google-credential.json

RUN /install-packages.sh --offline /home/nuxeo/local-packages/nuxeo-dlp-scanner-package.zip
