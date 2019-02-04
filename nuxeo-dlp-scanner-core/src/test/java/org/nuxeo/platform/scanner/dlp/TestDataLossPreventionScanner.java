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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * Test Blob scanning in standard use cases
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy("org.nuxeo.ecm.core.test")
@Deploy("org.nuxeo.platform.scanner.dlp.core")
public class TestDataLossPreventionScanner {

    @Inject
    protected CoreSession session;

    @Inject
    protected EventService eventService;

    @Inject
    WorkManager workManager;

    @Before
    public void setUp() {
        /*
         * String skipCheck = System.getenv("GOOGLE_CREDENTIALS_SET"); if (skipCheck == null) {
         * assertNotNull(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")); }
         */
    }

    protected Blob getFakeBlob(int size, String name) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append('a');
        }
        Blob blob = Blobs.createBlob(sb.toString());
        blob.setFilename(name);
        return blob;
    }

    protected Blob getBlob(String name, String mimeType) throws IOException {
        Blob blob = Blobs.createBlob(getClass().getResourceAsStream("/" + name));
        blob.setFilename(name);
        blob.setMimeType(mimeType);
        return blob;
    }

    @Test
    public void testFakeData() throws Exception {

        DocumentModel file;
        DocumentModel file2;
        DocumentModel file3;
        DocumentModel file4;

        try {
            file = session.createDocumentModel("/", "file1", "File");
            file.setPropertyValue("file:content", (Serializable) getFakeBlob(100, "Test1.txt"));
            file = session.createDocument(file);
        } finally {
            TransactionHelper.commitOrRollbackTransaction();
        }

        TransactionHelper.startTransaction();
        try {
            file2 = session.createDocumentModel("/", "file2", "File");
            file2 = session.createDocument(file2);
            session.save();

            file2.setPropertyValue("file:content", (Serializable) getFakeBlob(1001, "Test2.txt"));
            file2 = session.saveDocument(file2);

            file3 = session.createDocumentModel("/", "file3", "File");
            file3.setPropertyValue("file:content", (Serializable) getFakeBlob(100, "Test3doFail.txt"));
            file3 = session.createDocument(file3);
        } finally {
            TransactionHelper.commitOrRollbackTransaction();
        }

        TransactionHelper.startTransaction();
        try {
            file4 = session.createDocumentModel("/", "file4", "File");
            file4.setPropertyValue("file:content", (Serializable) getFakeBlob(100, "Test4.txt"));
            file4 = session.createDocument(file4);
        } finally {
            TransactionHelper.commitOrRollbackTransaction();
        }

        TransactionHelper.startTransaction();
        try {

            List<Map<String, Serializable>> files = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                Map<String, Serializable> map = new HashMap<>();
                map.put("file", (Serializable) getFakeBlob(100, "Test4-" + i + ".txt"));
                files.add(map);
            }

            file4.setPropertyValue("files:files", (Serializable) files);
            file4 = session.saveDocument(file4);
        } finally {
            TransactionHelper.commitOrRollbackTransaction();
        }

        TransactionHelper.startTransaction();
        try {

            @SuppressWarnings("unchecked")
            List<Map<String, Serializable>> files = (List<Map<String, Serializable>>) file4.getPropertyValue(
                    "files:files");

            Map<String, Serializable> map = new HashMap<>();
            map.put("file", (Serializable) getFakeBlob(100, "Test4-b.txt"));
            files.add(map);
            file4.setPropertyValue("files:files", (Serializable) files);
            file4 = session.saveDocument(file4);
        } finally {
            TransactionHelper.commitOrRollbackTransaction();
        }

        TransactionHelper.startTransaction();

        // wait for processing to be done
        workManager.awaitCompletion(10, TimeUnit.SECONDS);

        file = session.getDocument(file.getRef());
        file2 = session.getDocument(file2.getRef());
        file3 = session.getDocument(file3.getRef());

        Assert.assertTrue(file.hasFacet(DLPScanConstants.DLP_FACET));
        Assert.assertTrue(file2.hasFacet(DLPScanConstants.DLP_FACET));
        Assert.assertTrue(file3.hasFacet(DLPScanConstants.DLP_FACET));

        Assert.assertFalse((Boolean) file.getPropertyValue(DLPScanConstants.DLP_SENSITIVE_DATA));
        Assert.assertFalse((Boolean) file2.getPropertyValue(DLPScanConstants.DLP_SENSITIVE_DATA));
        Assert.assertFalse((Boolean) file3.getPropertyValue(DLPScanConstants.DLP_SENSITIVE_DATA));

        Assert.assertEquals(DLPScanConstants.DLP_STATUS_DONE, file.getPropertyValue(DLPScanConstants.DLP_STATUS_PROP));
        Assert.assertEquals(DLPScanConstants.DLP_STATUS_DONE, file2.getPropertyValue(DLPScanConstants.DLP_STATUS_PROP));
        Assert.assertEquals(DLPScanConstants.DLP_STATUS_DONE, file3.getPropertyValue(DLPScanConstants.DLP_STATUS_PROP));

    }

    @Test
    public void testDocx() throws Exception {
        runFile("ssn.docx", "application/msword");
    }

    @Test
    public void testCsv() throws Exception {
        runFile("ssn.tsv", "text/tsv");
    }

    private void runFile(String data, String mime) throws Exception {

        DocumentModel file;

        try {
            file = session.createDocumentModel("/", data, "File");
            file.setPropertyValue("file:content", (Serializable) getBlob(data, mime));
            file = session.createDocument(file);
        } finally {
            TransactionHelper.commitOrRollbackTransaction();
        }

        TransactionHelper.startTransaction();

        // wait for processing to be done
        workManager.awaitCompletion(10, TimeUnit.SECONDS);

        file = session.getDocument(file.getRef());

        Assert.assertTrue(file.hasFacet(DLPScanConstants.DLP_FACET));
        Assert.assertTrue((Boolean) file.getPropertyValue(DLPScanConstants.DLP_SENSITIVE_DATA));
        Assert.assertEquals(DLPScanConstants.DLP_STATUS_DONE, file.getPropertyValue(DLPScanConstants.DLP_STATUS_PROP));
    }
}
