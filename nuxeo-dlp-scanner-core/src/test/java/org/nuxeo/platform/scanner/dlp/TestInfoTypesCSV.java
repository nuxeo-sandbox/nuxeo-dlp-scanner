/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Thierry Delprat
 */
package org.nuxeo.platform.scanner.dlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.platform.scanner.dlp.google.GoogleDLPScanProvider;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.privacy.dlp.v2.InfoTypeDescription;

/**
 * Test Blob scanning in standard use cases
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy("org.nuxeo.ecm.core.test")
@Deploy("org.nuxeo.platform.scanner.dlp.core")
@Ignore
public class TestInfoTypesCSV {

    @Inject
    protected CoreSession session;

    @Inject
    protected DataLossPreventionScanner scanner;

    @Before
    public void setUp() {
        Assert.assertNotNull(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
    }

    @Test
    public void testGetInfoTypes() throws Exception {
        GoogleDLPScanProvider prov = (GoogleDLPScanProvider) scanner.getProvider("google");

        List<InfoTypeDescription> list = prov.getInfoTypes(null, null);
        list = new ArrayList<InfoTypeDescription>(list);
        Collections.sort(list, new Comparator<InfoTypeDescription>() {

            @Override
            public int compare(InfoTypeDescription o1, InfoTypeDescription o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }

        });
        System.out.println("id,label,obsolete");
        for (InfoTypeDescription it : list) {
            System.out.printf("\"%s\",\"%s\",0\n", it.getName(), it.getDisplayName());
        }
    }
}
