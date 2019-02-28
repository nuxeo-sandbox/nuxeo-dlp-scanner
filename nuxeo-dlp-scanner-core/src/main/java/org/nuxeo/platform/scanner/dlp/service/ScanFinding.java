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

/**
 * Encapsulate result from a data loss prevention scan
 *
 * @since 10.10
 */
public class ScanFinding {

    protected final boolean sensitiveData;

    protected final String info;

    protected final String type;

    protected final String score;

    public ScanFinding(boolean sensitiveData, String score, String type, String info) {
        this.sensitiveData = sensitiveData;
        this.score = score;
        this.type = type;
        this.info = info;
    }

    public boolean hasSensitiveData() {
        return sensitiveData;
    }

    public String getScore() {
        return score;
    }

    public String getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }

}
