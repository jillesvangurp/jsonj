# release process
# since I dislike the maven release plugin and consider it FOOBARred, we have a manual process.

# double check everything is committed
# modify pom.xml to have the proper version
# modify README.textile
# mvn clean install
# git add pom.xml README.textile
# git commit -m 'release jsonj-x.y'
# git tag jsonj-x.y
# modify pom.xml to have the proper SNAPSHOT version
# git add pom.xml
# git commit -m 'set SNAPSHOT for next release'
# git push --tags

# now push the artifacts to sonatype and release them:
# git checkout jsonj-x.y
# use the usual password
# mvn clean install -Psignartifacts
# create a bundle
# cd target
# jar -cvf bundle.jar jsonj*
# go to https://oss.sonatype.org, in the Nexus UI, click Staging Upload in the left column. 
# In the Staging Upload panel, select Artifact Bundle as Upload Mode and select the bundle you just created
# now in staging repositories, find the uploaded artifact and click release. It should already be in the "closed" state.

# now update the documentation project on the gh-pages branch; this works easiest with the project checked 
# out in a separate directory and permanently on the gh-pages branch
# replace the jar file, the apidocs with the appropriate versions from the target directory where 
# you just built the release. Fix the link in index.html.
# commit and push (git push origin gh-pages)



