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

import java.io.IOException;
import java.util.List;

import org.nuxeo.ecm.core.api.Blob;

/**
 * Interface that should be implemented to encapsulate call to a Data Loss Prevention Scanning service : command line or
 * WebService
 * 
 * @since 10.10
 */
public interface ScanProvider {

    /**
     * Check to see if the service is enabled
     * 
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Does the provider support redaction
     * 
     * @return true if supported
     */
    boolean supportsRedaction();

    /**
     * @param blobs the blobs to pass to the API
     * @param features the feature to request from the provider
     * @param maxResults the maximum number of results per feature
     * @return {@link ScanResult} objects
     */
    List<ScanResult> identify(List<Blob> blobs, List<String> features, Integer maxResults) throws IOException;

    /**
     * Verifies that the blobs size and format are supported by the provider
     *
     * @param blobs the blobs to pass to the API
     * @return true if fine
     */
    boolean checkBlobs(List<Blob> blobs);

}
