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
package org.nuxeo.platform.scanner.dlp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.platform.scanner.dlp.service.ScanProvider;
import org.nuxeo.platform.scanner.dlp.service.ScanResult;

/**
 * A service that performs Computer DataLossPreventionScanner tasks like classification, OCR, Face Detection ...
 * 
 * @since 10.10
 */
public interface DataLossPreventionScanner extends DLPScanConstants {

    /**
     * @param blob the data blob
     * @return a {@link ScanResult} object
     */
    ScanResult identify(Blob blob) throws IOException;

    /**
     * @param blob the data blob
     * @param infoTypes the info types to request from the service (optional)
     * @param maxResults the maximum number of results per feature
     * @return a {@link ScanResult} object
     */
    ScanResult identify(Blob blob, List<String> features, Integer maxResults) throws IOException;

    /**
     * @param provider the provider to use
     * @param blob the data blob
     * @param infoTypes the info types to request from the service (optional)
     * @param maxResults the maximum number of results per feature
     * @return a {@link ScanResult} object
     */
    ScanResult identify(String provider, Blob blob, List<String> features, Integer maxResults) throws IOException;

    /**
     * @param provider the provider to use
     * @param blobs A list of data blobs
     * @param infoTypes the info types to request from the service (optional)
     * @param maxResults the maximum number of results per feature
     * @return a list of {@link ScanResult} object
     */
    List<ScanResult> identify(String provider, List<Blob> blobs, List<String> features, Integer maxResults)
            throws IOException;

    /**
     * @param blobs A list of data blobs
     * @param infoTypes the info types to request from the service (optional)
     * @param maxResults the maximum number of results per feature
     * @return a list of {@link ScanResult} object
     */
    List<ScanResult> identify(List<Blob> blobs, List<String> features, Integer maxResults) throws IOException;

    /**
     * @param blob the data blob
     * @return a {@link Blob} object
     */
    Blob redact(Blob blob);

    /**
     * @param blob the data blob
     * @param infoTypes the info types to request from the service (optional)
     * @param maxResults the maximum number of results per feature
     * @return a {@link Blob} object
     */
    Blob redact(Blob blob, List<String> features);

    /**
     * @param provider the provider to use
     * @param blob the data blob
     * @param infoTypes the info types to request from the service (optional)
     * @param maxResults the maximum number of results per feature
     * @return a {@link Blob} object
     */
    Blob redact(String provider, Blob blob, List<String> features);

    /**
     * @param provider the provider to use
     * @param blobs A list of data blobs
     * @param infoTypes the info types to request from the service (optional)
     * @param maxResults the maximum number of results per feature
     * @return a list of {@link Blob} object
     */
    List<Blob> redact(String provider, List<Blob> blobs, List<String> features);

    /**
     * @param blobs A list of data blobs
     * @param infoTypes the info types to request from the service (optional)
     * @param maxResults the maximum number of results per feature
     * @return a list of {@link Blob} object
     */
    List<Blob> redact(List<Blob> blobs, List<String> features);

    /**
     * @return The name of default provider
     */
    String getDefaultProvider();

    /**
     * @return The provider object
     */
    ScanProvider getProvider(String name);

    /**
     * @return all registered providers
     */
    Map<String, ScanProvider> getProviders();

}