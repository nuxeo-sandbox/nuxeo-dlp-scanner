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

import java.io.IOException;
import java.io.Serializable;

import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.platform.scanner.dlp.operations.IdentifySensitiveDataOperation;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

/**
 * Test the operation(s)
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 */
@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.core.test")
@Deploy("org.nuxeo.platform.scanner.dlp.core")
@Deploy("org.nuxeo.platform.scanner.dlp.core:disable-listeners-contrib.xml")
@Ignore
public class TestOperations {

    @Inject
    protected CoreSession session;

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Inject
    protected AutomationService automationService;

    @Before
    public void setUp() {
        Assert.assertNotNull(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
    }

    protected Blob getBlob(String name, String mimeType) throws IOException {
        Blob blob = Blobs.createBlob(getClass().getResourceAsStream("/" + name));
        blob.setFilename(name);
        blob.setMimeType(mimeType);
        return blob;
    }

    @Test
    public void testDocxNoParams() throws Exception {
        runFileNoParams("ssn.docx", "application/msword");
    }

    @Test
    public void testJpegNoParams() throws Exception {
        runFileNoParams("ssn.jpg", "image/jpeg");
    }

    @Test
    public void testCsvNoParams() throws Exception {
        runFileNoParams("ssn.tsv", "text/tsv");
    }

    private void runFileNoParams(String data, String mime) throws Exception {

        DocumentModel doc;

        doc = session.createDocumentModel("/", data, "File");
        doc.setPropertyValue("file:content", (Serializable) getBlob(data, mime));
        doc = session.createDocument(doc);

        // DLP listeners are disabled but ltes wait for Nuxeo to finish process whatever it wants
        transactionalFeature.nextTransaction();
        
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        doc = (DocumentModel) automationService.run(ctx, IdentifySensitiveDataOperation.ID);
        //file = session.getDocument(file.getRef());

        Assert.assertTrue(doc.hasFacet(DLPScanConstants.DLP_FACET));
        Assert.assertTrue((Boolean) doc.getPropertyValue(DLPScanConstants.DLP_SENSITIVE_DATA));
        Assert.assertEquals(DLPScanConstants.DLP_STATUS_DONE, doc.getPropertyValue(DLPScanConstants.DLP_STATUS_PROP));
    }
}
