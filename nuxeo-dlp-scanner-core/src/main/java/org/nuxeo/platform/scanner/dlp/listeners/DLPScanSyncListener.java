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

package org.nuxeo.platform.scanner.dlp.listeners;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.utils.BlobsExtractor;
import org.nuxeo.platform.scanner.dlp.DLPScanConstants;
import org.nuxeo.runtime.api.Framework;

/**
 * Synchronous listener that intercept Document create/update events.
 * <p/>
 * This listener detects if Blobs have been modified inside the doc, and if yes, it will raise the dlpScanNeeded so that
 * the async listener can do the real job in async mode.
 * <p/>
 * The work done in sync includes extracting dirty Blobs xpath that are then tranmisted to the Async listener using a
 * custom extended {@link DLPScanEventContext}
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 */
public class DLPScanSyncListener implements EventListener {

    protected static final Log log = LogFactory.getLog(DLPScanSyncListener.class);

    @Override
    public void handleEvent(Event event) {

        if (event.getContext() instanceof DocumentEventContext) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel targetDoc = docCtx.getSourceDocument();

            Boolean block = (Boolean) event.getContext().getProperty(DLPScanConstants.DISABLE_DLP_LISTENER);
            if (block != null && block) {
                // ignore the event - we are blocked by the caller
                return;
            }
            
            if (targetDoc == null) {
                // ignore the event - we need a document
                return;
            }

            List<String> propertiesPath = null;

            if (DocumentEventTypes.ABOUT_TO_CREATE.equals(event.getName())) {
                // add the facet before save
                markDocumentForScaning(targetDoc);
            } else if (DocumentEventTypes.DOCUMENT_CREATED.equals(event.getName())) {
                // process Blobs now that document is created
                propertiesPath = getBlobsXPath(targetDoc, false);
            } else if (DocumentEventTypes.BEFORE_DOC_UPDATE.equals(event.getName())) {
                // process Blobs before update
                propertiesPath = getBlobsXPath(targetDoc, true);
                markDocumentForScaning(targetDoc);
            }

            if (propertiesPath != null && propertiesPath.size() > 0) {
                DLPScanEventContext dlpScanCtx = new DLPScanEventContext(docCtx, propertiesPath);

                EventService eventService = Framework.getService(EventService.class);
                eventService.fireEvent(dlpScanCtx.newDLPScanEvent());
            }
        }
    }

    protected void markDocumentForScaning(DocumentModel doc) {
        if (!doc.hasFacet(DLPScanConstants.DLP_FACET)) {
            doc.addFacet(DLPScanConstants.DLP_FACET);
        }
        doc.setPropertyValue(DLPScanConstants.DLP_STATUS_PROP, DLPScanConstants.DLP_STATUS_PENDING);
    }

    protected List<String> getBlobsXPath(DocumentModel doc, boolean onlyChangedBlob) {
        List<String> propertiesPath = new ArrayList<String>();
        BlobsExtractor extractor = new BlobsExtractor();
        List<Property> blobProperties = extractor.getBlobsProperties(doc);
        for (Property prop : blobProperties) {
            if (onlyChangedBlob) {
                if (prop.isDirty()) {
                    propertiesPath.add(prop.getXPath());
                }
            } else {
                propertiesPath.add(prop.getXPath());
            }
        }
        return propertiesPath;
    }

}
