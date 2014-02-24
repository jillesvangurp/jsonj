

# automated way with staging plugin thingy:
# mvn release:prepare
# mvn release:perform
# mvn nexus-staging:release -Ddescription="Release 1.33"
# Or if that fails, figure out the id from the ui https://github.com/sonatype/nexus-maven-plugins/issues/50
# mvn nexus-staging:release -Ddescription="Release 1.33" -DstagingRepositoryId=comjillesvangurp-1002


