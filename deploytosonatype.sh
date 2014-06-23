

# automated way with staging plugin thingy:
# mvn release:prepare
# mvn release:perform

# figure out the id from the release:perform build log
# mvn nexus-staging:release -Ddescription="Release 1.33" -DstagingRepositoryId=comjillesvangurp-1002
# or do it manually here https://oss.sonatype.org/#stagingRepositories
