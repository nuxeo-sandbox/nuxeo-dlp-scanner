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

package org.nuxeo.platform.scanner.dlp.service;

import java.util.List;

/**
 * Encapsulate result from a data loss prevention scan
 *
 * @since 10.10
 */
public class ScanResult {

    protected final boolean sensitiveData;

    protected final List<ScanFinding> findings;

    protected final boolean error;

    public ScanResult(List<ScanFinding> findings) {
        this.sensitiveData = findings.stream().anyMatch(f -> f.hasSensitiveData());
        this.findings = findings;
        this.error = false;
    }

    private ScanResult() {
        this.sensitiveData = false;
        this.findings = null;
        this.error = true;
    }

    public static ScanResult makeFailed() {
        return new ScanResult();
    }

    public boolean hasSensitiveData() {
        return sensitiveData;
    }

    public List<ScanFinding> getFindings() {
        return findings;
    }

    public boolean isError() {
        return error;
    }

}
