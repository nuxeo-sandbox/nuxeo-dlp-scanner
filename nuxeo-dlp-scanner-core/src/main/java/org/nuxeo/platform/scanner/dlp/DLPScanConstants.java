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
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.platform.scanner.dlp;

/**
 * Constants used in schema
 * 
 * @since 10.10
 */
public interface DLPScanConstants {

    public String DLP_SCAN_ENABLED = "dlp.scan.enabled";

    public String DLP_SCAN_NEEDED_EVENT = "dlpScanNeeded";

    public String DLP_SENSITIVE_DATA_EVENT = "dlpSensitiveDataFound";

    public String DLP_FACET = "DataLossPrevention";

    public String DLP_STATUS_PROP = "dlp:scanStatus";

    public String DLP_STATUS_PENDING = "pending";

    public String DLP_STATUS_DONE = "done";

    public String DLP_STATUS_FAILED = "failed";

    public String DLP_DATE_PROP = "dlp:scanDate";

    public String DLP_SENSITIVE_DATA = "dlp:sensitiveData";

    public String DLP_FINDINGS = "dlp:findings";

    public String DISABLE_DLP_LISTENER = "disableDLPScanListener";

}
