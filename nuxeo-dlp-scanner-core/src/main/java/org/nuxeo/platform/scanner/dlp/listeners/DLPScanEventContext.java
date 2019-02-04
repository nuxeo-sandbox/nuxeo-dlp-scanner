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

import java.io.Serializable;
import java.util.List;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.platform.scanner.dlp.DLPScanConstants;

/**
 * Custom EventContext used to propagate info between the synchronous listener and the Asynchrous listener
 * <p/>
 * The Blob xpaths are propagated using a List of String inside the {@link EventContext}.
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 */
public class DLPScanEventContext extends DocumentEventContext {

    private static final long serialVersionUID = 1L;

    public static final String DLP_SCAN_BLOB_XPATHS = "blobXPaths";

    public static final String MARKER_KEY = "contextType";

    public static final String MARKER_VALUE = "SizeUpdateEventContext";

    protected DLPScanEventContext(DocumentEventContext evtCtx) {
        super(evtCtx.getCoreSession(), evtCtx.getPrincipal(), evtCtx.getSourceDocument(), evtCtx.getDestination());
        setProperty(MARKER_KEY, MARKER_VALUE);
    }

    public DLPScanEventContext(DocumentEventContext evtCtx, List<String> blobXPaths) {
        super(evtCtx.getCoreSession(), evtCtx.getPrincipal(), evtCtx.getSourceDocument(), evtCtx.getDestination());
        setProperty(MARKER_KEY, MARKER_VALUE);
        setProperty(DLP_SCAN_BLOB_XPATHS, (Serializable) blobXPaths);
    }

    public static DLPScanEventContext unwrap(DocumentEventContext docCtx) {
        if (MARKER_VALUE.equals(docCtx.getProperty(MARKER_KEY))) {
            DLPScanEventContext ctx = new DLPScanEventContext(docCtx);
            ctx.setProperties(docCtx.getProperties());
            return ctx;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getBlobPaths() {
        return (List<String>) getProperty(DLP_SCAN_BLOB_XPATHS);
    }

    public Event newDLPScanEvent() {
        return newEvent(DLPScanConstants.DLP_SCAN_NEEDED_EVENT);
    }

}
