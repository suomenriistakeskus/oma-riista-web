## Development environment

### Getting started

Using Linux or macOS based development environment is recommended. 

### Prerequisites
The following dependencies must be installed to successfully compile, test and run the project:

  - Java SE Development Kit 8 ([Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK](http://openjdk.java.net/projects/jdk8/)) 
  - [PostgreSQL](https://www.postgresql.org/download/) >= 9.5
  - [PostGIS](http://www.postgis.net/) >= 2.3
  - [Maven](https://maven.apache.org/) >= 3.3
  - [Node.js](https://nodejs.org/en/download/) >= 6.0 including NPM
  - [Gulp.js](http://gulpjs.com/)
  - GIT
  - wget
  - unzip

### Download source code

Clone the GIT repository

    git clone https://github.com/suomenriistakeskus/oma-riista-web

### Create developer profile

Default development build profile contains only minimal settings to run the system. 
Each developer must create additional property file to activate all system features
and to customize settings for database and sending mail etc.

To configure the developer specific profile file, create empty property file with login username in filename:

    touch "profiles/config.${USER}.properties"

### Setup database

Environment must be configured so that PostgreSQL utilities do not require password (see pg_hba.conf).

Create database role and password.

    > psql
    CREATE ROLE riistakeskus WITH LOGIN PASSWORD 'riistakeskus';

Initialize schema and populate data. 

    cd scripts/database
    ./create-database.sh riistakeskus

Initial database contains: 
  - Reference data for game species, organisations, harvest quotas etc
  - Public geometry data for RHY and HTA areas
  - Sample data to enable login and usage of most essentials features of the system

Not included:
  - Geometries for real estates (MML Kiinteistörekisteri)
  - Geometries for water areas (extracted from MML Maastotietokanta)
  - Geometries for Metsähallitus land areas

### Run project
In the project root execute:

    mvn jetty:run

Open another terminal to continuously build frontend sources:

    cd frontend
    npm install
    gulp
    
Open browser and navigate to:

    http://localhost:9494
    https://localhost:9170

### Default credentials

Following accounts (username / password) should be available with respective roles and access rights.

   - user / user
   - coordinator / coordinator
   - moderator / moderator
   - admin / admin

### Testing

For instructions see [testing.md](testing.md)

### Deployment

For instructions see [deployment.md](deployment.md)
