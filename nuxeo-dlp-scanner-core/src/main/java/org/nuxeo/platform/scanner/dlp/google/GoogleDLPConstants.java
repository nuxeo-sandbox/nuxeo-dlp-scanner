/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Damon Brown
 */
package org.nuxeo.platform.scanner.dlp.google;

import com.google.privacy.dlp.v2.Likelihood;

/**
 * @author dbrown
 */
public interface GoogleDLPConstants {

    String DLP_NAME = "google.dlp";

    String ENABLED = DLP_NAME + ".enabled";

    String API_PROJECT = DLP_NAME + ".api.project";

    String INFOTYPES = DLP_NAME + ".infotypes";

    String LIKELYHOOD = DLP_NAME + ".likelyhood";

    String SENSITIVITY = DLP_NAME + ".sensitivity";

    String MAX_FINDINGS = DLP_NAME + ".maxfindings";

    String INCLUDE_QUOTE = DLP_NAME + ".quote";

    String CONVERT_TO_TEXT = DLP_NAME + ".convert";

    String DETAILS = DLP_NAME + ".details";

    String CUSTOM = DLP_NAME + ".custom";

    String DEFAULT_LIKELYHOOD = Likelihood.POSSIBLE.toString();

    String DEFAULT_MAX_FINDINGS = "5";

    String DEFAULT_INFOTYPES = "CREDIT_CARD_NUMBER,IBAN_CODE,SWIFT_CODE,US_SOCIAL_SECURITY_NUMBER,US_PASSPORT,US_INDIVIDUAL_TAXPAYER_IDENTIFICATION_NUMBER,US_EMPLOYER_IDENTIFICATION_NUMBER"
            + ",US_BANK_ROUTING_MICR";

}
