# (Unofficial) D2Lang Maven Plugin

This maven plugin can be integrated in your build when using e.g. Hugo to first generate diagrams that are then referend in the Hugo website.
The plugin is meant to be used for diagrams written in the [D2Lang](https://d2lang.com/tour/intro/).

## Publishing to OSSRH (formerly Maven Central)

See: https://central.sonatype.org/publish/publish-maven/#deployment
For dealing with GPG: https://central.sonatype.org/publish/requirements/gpg/#installing-gnupg

# Deploying a new release

`mvn release:clean release:prepare`

Then:

`mvn release:perform`
