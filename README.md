# Nuxeo Data Loss Prevention Scanner

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=Sandbox/sandbox_nuxeo-dlp-scanner-master)](https://qa.nuxeo.org/jenkins/view/Sandbox/job/Sandbox/job/sandbox_nuxeo-dlp-scanner-master/)

Framework for scanning all content for Data Loss Prevention.  Pattern and confidence matching will identify potential sources of sensitive data in your repository.

## Dependencies

[Google Data Loss Prevention](https://cloud.google.com/dlp/) API service.

## Build and Install

Build with maven (at least 3.3)

```
mvn clean install
```
> Package built here: `nuxeo-dlp-scanner-package/target/nuxeo-dlp-scanner-package-*.zip`

> Install with `nuxeoctl mp-install <package>`

## Configure (nuxeo.conf)

Editor properties:

```
TBD
```

## Support

**These features are sand-boxed and not yet part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

## Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.

More information is available at [www.nuxeo.com](http://www.nuxeo.com).

