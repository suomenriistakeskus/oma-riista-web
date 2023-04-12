FROM maven:3.8.6-amazoncorretto-11


ARG HOST_USER_UID=1000
ARG HOST_USER_GID=1000

RUN yum -y update && \
    yum install -y git bzip2 shadowutils && \
    yum clean all && \
    rm -rf /var/cache/yum

RUN groupadd -g $HOST_USER_GID ciuser && \
  useradd -d /home/ciuser -u $HOST_USER_UID -g $HOST_USER_GID ciuser

USER 1000:1000

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.34.0/install.sh | bash
RUN . /home/ciuser/.nvm/nvm.sh && nvm install v14.0.0

ENV PATH="/home/ciuser/.nvm/versions/node/v14.0.0/bin/:${PATH}"

RUN npm install -g gulp-cli
