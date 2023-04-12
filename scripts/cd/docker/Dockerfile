FROM maven:3.8.6-amazoncorretto-11


ARG HOST_USER_UID=1000
ARG HOST_USER_GID=1000

RUN yum -y update && \
    yum install -y git bzip2 shadowutils jq python3 python3-pip && \
    yum clean all && \
    rm -rf /var/cache/yum

ENV USER_HOME_DIR="/home/ciuser"
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
RUN groupadd -g $HOST_USER_GID ciuser && \
  useradd -d $USER_HOME_DIR -u $HOST_USER_UID -g $HOST_USER_GID ciuser

USER 1000:1000

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.34.0/install.sh | bash
RUN . ${USER_HOME_DIR}/.nvm/nvm.sh && nvm install 11.15.0

RUN pip3  --no-cache-dir install awscli

ENV PATH="${PATH}:${USER_HOME_DIR}/.nvm/versions/node/v11.15.0/bin/:${USER_HOME_DIR}/.local/bin"

RUN npm install -g gulp-cli

