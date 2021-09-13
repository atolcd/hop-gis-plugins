# Apache Hop GIS Plugins - Continuous Integration

Continuous integration is provided by Jenkins pipelines:

📚 **Code review**
* *[atolcd/hop-gis-plugins [review]](https://jenkins.priv.atolcd.com/blue/organizations/jenkins/atolcd--hop-gis-plugins--review/activity)*
* `jenkins/Jenkinsfile-review`
* Automatically triggered (all review commit)

🏭 **Merge**
* *[atolcd/hop-gis-plugins [merge]](https://jenkins.priv.atolcd.com/blue/organizations/jenkins/atolcd--hop-gis-plugins--merge/activity)*
* `jenkins/Jenkinsfile-merge`
* Automatically triggered (any merged commit)
* commit ➞ artifacts archived in jenkins
