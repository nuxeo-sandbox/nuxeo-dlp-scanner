# Nuxeo Data Loss Prevention Scanner
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

Obtain a Google Credentials JSON file and set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable with the path to the file.

See: https://cloud.google.com/docs/authentication/production

The service role should be assigned at least the `DLP User` role.  The Scanner service will work without any additional configuration.

### Google DLP Scanner Default Properties

```
# Stop listener from processing events
dlp.scan.enabled=(true)|false

# Stop Google checks by setting this to false
google.dlp.enabled=(true)|false

# List of Info Types to use
# See: https://cloud.google.com/dlp/docs/infotypes-reference
google.dlp.infotypes=CREDIT_CARD_NUMBER,IBAN_CODE,SWIFT_CODE,US_SOCIAL_SECURITY_NUMBER,US_PASSPORT,US_INDIVIDUAL_TAXPAYER_IDENTIFICATION_NUMBER,US_EMPLOYER_IDENTIFICATION_NUMBER,US_BANK_ROUTING_MICR

# Sensitivity and Likelihood one of:
# UNRECOGNIZED
# LIKELIHOOD_UNSPECIFIED
# VERY_UNLIKELY
# UNLIKELY
# POSSIBLE
# LIKELY
# VERY_LIKELY
google.dlp.likelyhood=POSSIBLE
google.dlp.sensitivity=POSSIBLE

# Maximum number of findings before terminating
google.dlp.maxfindings=5

# Attach the sensitive data item as metadata
google.dlp.quote=(true)|false

# Convert non-text types to textual representations
google.dlp.convert=(true)|false

# GCP Project Name (not required when credentials are properly configured)
google.dlp.api.project=ProjectName
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

