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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.AbstractLongRunningListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.platform.scanner.dlp.DLPScanConstants;
import org.nuxeo.platform.scanner.dlp.DataLossPreventionScanner;
import org.nuxeo.platform.scanner.dlp.service.ScanFinding;
import org.nuxeo.platform.scanner.dlp.service.ScanProvider;
import org.nuxeo.platform.scanner.dlp.service.ScanResult;
import org.nuxeo.runtime.api.Framework;

/**
 * Async listener that will manage the real work for scanning.
 * <p/>
 * To avoid long transactions, this listeners is split in 3 parts :
 * <p/>
 * <ul>
 * <li>fetch Blobs using the xpath information stored in the EventContext (transactional)</li>
 * <li>call the {@link ScanProvider} (outside of any transaction)</li>
 * <li>update the target documents based on the result collected from the {@link ScanProvider} (transactional)</li>
 * </ul>
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 */
public class DLPScannerProcessor extends AbstractLongRunningListener {

    @Override
    public boolean acceptEvent(Event event) {
        if (DLPScanConstants.DLP_SCAN_NEEDED_EVENT.equals(event.getName())) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean handleEventPreprocessing(EventBundle events, Map<String, Object> data) {
        for (Event event : events) {
            if (DLPScanConstants.DLP_SCAN_NEEDED_EVENT.equals(event.getName())) {
                DLPScanEventContext vContext = DLPScanEventContext.unwrap((DocumentEventContext) event.getContext());
                DocumentModel doc = vContext.getSourceDocument();

                if (doc.isVersion() || doc.isImmutable()) {
                    continue;
                }
                String key = doc.getRepositoryName() + ":" + doc.getId();
                @SuppressWarnings("unchecked")
                Map<String, Blob> blobs = (Map<String, Blob>) data.get(key);
                if (blobs == null) {
                    blobs = new HashMap<String, Blob>();
                }
                for (String path : vContext.getBlobPaths()) {
                    blobs.put(path, (Blob) doc.getPropertyValue(path));
                }
                data.put(key, blobs);
            }
        }
        if (data.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean handleEventLongRunning(List<String> eventNames, Map<String, Object> data) {

        boolean doContinue = false;
        DataLossPreventionScanner scanService = Framework.getService(DataLossPreventionScanner.class);

        for (String key : data.keySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Blob> blobs = (Map<String, Blob>) data.get(key);
            Map<String, ScanResult> results = new HashMap<String, ScanResult>();

            for (String path : blobs.keySet()) {
                try {
                    results.put(path, scanService.execute(blobs.get(path)));
                    doContinue = true;
                } catch (Exception e) {
                    log.error("Error calling ScanProvider", e);
                    results.put(path, ScanResult.makeFailed());
                }
            }
            data.put(key, results);
        }

        return doContinue;
    }

    @Override
    protected void handleEventPostprocessing(EventBundle events, Map<String, Object> data) {

        for (Event event : events) {
            if (DLPScanConstants.DLP_SCAN_NEEDED_EVENT.equals(event.getName())) {
                DLPScanEventContext vContext = DLPScanEventContext.unwrap((DocumentEventContext) event.getContext());
                DocumentModel doc = vContext.getSourceDocument();

                String key = doc.getRepositoryName() + ":" + doc.getId();
                @SuppressWarnings("unchecked")
                Map<String, ScanResult> results = (Map<String, ScanResult>) data.get(key);
                Property findingProp = doc.getProperty(DLPScanConstants.DLP_FINDINGS);
                boolean failed = false;
                boolean sensitive = false;
                if (results != null && results.size() > 0) {
                    for (String path : results.keySet()) {
                        ScanResult res = results.get(path);
                        if (res.hasSensitiveData()) {
                            sensitive = true;
                        }
                        if (res.isError()) {
                            failed = true;
                        }
                        if (res.getFindings() != null) {
                            for (ScanFinding f : res.getFindings()) {
                                Map<String, String> fmap = new HashMap<>();
                                fmap.put("info", f.getInfo());
                                fmap.put("score", f.getScore());
                                fmap.put("type", f.getType());
                                findingProp.addValue(fmap);
                            }
                        }
                    }
                }

                doc.setPropertyValue(DLPScanConstants.DLP_SENSITIVE_DATA, sensitive);
                doc.setPropertyValue(DLPScanConstants.DLP_DATE_PROP, new Date());

                doc.setPropertyValue(DLPScanConstants.DLP_STATUS_PROP,
                        failed ? DLPScanConstants.DLP_STATUS_FAILED : DLPScanConstants.DLP_STATUS_DONE);

                doc.putContextData(DLPScanConstants.DISABLE_DLP_LISTENER, true);
                CoreSession session = doc.getCoreSession();
                session.saveDocument(doc);

                if (sensitive) {
                    EventService eventService = Framework.getService(EventService.class);
                    DocumentEventContext dctx = new DocumentEventContext(session, session.getPrincipal(), doc,
                            vContext.getDestination());
                    eventService.fireEvent(dctx.newEvent(DLPScanConstants.DLP_SENSITIVE_DATA_EVENT));
                }
            }
        }

    }

}
