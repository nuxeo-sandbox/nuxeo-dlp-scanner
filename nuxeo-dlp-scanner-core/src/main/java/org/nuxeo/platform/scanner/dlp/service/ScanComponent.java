/*
 * (C) Copyright 2006-2013 Nuxeo SA (http://nuxeo.com/) and others.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.platform.scanner.dlp.DataLossPreventionScanner;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 */
public class ScanComponent extends DefaultComponent implements DataLossPreventionScanner {

    protected static final String CONFIG_EXT_POINT = "configuration";

    protected static final String PROVIDER_EXT_POINT = "provider";

    protected ScanConfigurationDescriptor config;

    protected Map<String, ScanProvider> providers = new HashMap<>();

    /**
     * Component activated notification. Called when the component is activated. All component dependencies are resolved
     * at that moment. Use this method to initialize the component.
     *
     * @param context the component context.
     */
    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
    }

    /**
     * Component deactivated notification. Called before a component is unregistered. Use this method to do cleanup if
     * any and free any resources held by the component.
     *
     * @param context the component context.
     */
    @Override
    public void deactivate(ComponentContext context) {
        super.deactivate(context);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (CONFIG_EXT_POINT.equals(extensionPoint)) {
            config = (ScanConfigurationDescriptor) contribution;
        } else if (PROVIDER_EXT_POINT.equals(extensionPoint)) {
            ScanProviderDescriptor desc = (ScanProviderDescriptor) contribution;
            try {
                ScanProvider provider = (ScanProvider) desc.getKlass().getConstructor().newInstance();
                providers.put(desc.getProviderName(), provider);
            } catch (ReflectiveOperationException e) {
                throw new NuxeoException(e);
            }
        }
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        // Logic to do when unregistering any contribution
    }

    @Override
    public ScanResult identify(Blob blob) throws IOException {
        return identify(config.getDefaultProviderName(), blob, null, null);
    }

    @Override
    public ScanResult identify(Blob blob, List<String> features, Integer maxResults)
            throws IOException, IllegalStateException {
        return identify(config.getDefaultProviderName(), blob, features, maxResults);
    }

    @Override
    public List<ScanResult> identify(List<Blob> blobs, List<String> features, Integer maxResults)
            throws IOException, IllegalStateException {
        return identify(config.getDefaultProviderName(), blobs, features, maxResults);
    }

    @Override
    public ScanResult identify(String providerName, Blob blob, List<String> features, Integer maxResults)
            throws IOException {
        if (blob == null) {
            throw new IllegalArgumentException("Input Blob cannot be null");
        }

        List<ScanResult> results = identify(providerName, Arrays.asList(blob), features, maxResults);
        if (results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<ScanResult> identify(String providerName, List<Blob> blobs, List<String> features, Integer maxResults)
            throws IOException {
        ScanProvider provider = providers.get(providerName);

        if (provider == null) {
            throw new NuxeoException("Unknown provider: " + providerName);
        }

        if (blobs == null || blobs.size() == 0) {
            throw new IllegalArgumentException("Input Blob list cannot be null or empty");
        } else if (!provider.checkBlobs(blobs)) {
            throw new IllegalArgumentException("Too many blobs or size exceeds the API limit");
        }
        return provider.identify(blobs, features, maxResults);
    }

    @Override
    public Blob redact(Blob blob) {
        return redact(config.getDefaultProviderName(), blob, null);
    }

    @Override
    public Blob redact(Blob blob, List<String> features) {
        return redact(config.getDefaultProviderName(), blob, features);
    }

    @Override
    public List<Blob> redact(List<Blob> blobs, List<String> features) {
        return redact(config.getDefaultProviderName(), blobs, features);
    }

    @Override
    public Blob redact(String provider, Blob blob, List<String> features) {
        if (blob == null) {
            throw new IllegalArgumentException("Input Blob cannot be null");
        }

        List<Blob> results = redact(provider, Arrays.asList(blob), features);
        if (results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Blob> redact(String providerName, List<Blob> blobs, List<String> features) {
        ScanProvider provider = providers.get(providerName);
        RedactionProvider redact = null;
        if (provider.supportsRedaction()) {
            redact = (RedactionProvider) provider;
        }

        if (redact == null) {
            throw new NuxeoException("Unknown provider: " + providerName);
        }

        if (blobs == null || blobs.size() == 0) {
            throw new IllegalArgumentException("Input Blob list cannot be null or empty");
        } else if (!provider.checkBlobs(blobs)) {
            throw new IllegalArgumentException("Too many blobs or size exceeds the API limit");
        }
        return redact.redact(blobs, features);
    }

    @Override
    public String getDefaultProvider() {
        return config.getDefaultProviderName();
    }

    @Override
    public ScanProvider getProvider(String name) {
        return providers.get(name);
    }

    @Override
    public Map<String, ScanProvider> getProviders() {
        return providers;
    }

}
