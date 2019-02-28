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
package org.nuxeo.platform.scanner.dlp.operations;

import java.io.IOException;

import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.platform.scanner.dlp.DataLossPreventionScanner;
import org.nuxeo.platform.scanner.dlp.service.RedactionProvider;
import org.nuxeo.platform.scanner.dlp.service.ScanProvider;

/**
 * Call redaction as an operation
 * 
 * @since 10.10
 */
@Operation(id = RedactSensitiveDataOperation.ID, category = Constants.CAT_BLOB, label = "Redact Sensitive Information", description = "Redact sensitive information on a piece of content.")
public class RedactSensitiveDataOperation {

    public static final String ID = "Blob.RedactSensitiveData";

    @Context
    protected DataLossPreventionScanner service;

    @Param(name = "infotypes", required = false)
    protected StringList infoTypes;

    @OperationMethod
    public Blob run(DocumentModel doc) {
        ScanProvider scanner = service.getProvider(service.getDefaultProvider());
        if (scanner.supportsRedaction()) {
            RedactionProvider redact = (RedactionProvider) scanner;
            return redact.redactDocument(doc, infoTypes);
        } else {
            throw new NuxeoException("Redaction not supported");
        }
    }

    @OperationMethod
    public Blob run(Blob blob) throws IOException, OperationException {
        ScanProvider scanner = service.getProvider(service.getDefaultProvider());
        if (scanner.supportsRedaction()) {
            RedactionProvider redact = (RedactionProvider) scanner;
            return redact.redactBlob(blob, infoTypes);
        } else {
            throw new NuxeoException("Redaction not supported");
        }
    }

}
