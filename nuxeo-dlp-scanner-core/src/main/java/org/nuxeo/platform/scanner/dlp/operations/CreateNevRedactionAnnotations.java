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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.comment.api.AnnotationImpl;
import org.nuxeo.ecm.platform.comment.api.AnnotationService;
import org.nuxeo.platform.scanner.dlp.DLPScanConstants;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Create NEV annotations based on the stored dlp info
 *
 * @since 10.10
 */
@Operation(id = CreateNevRedactionAnnotations.ID, category = "NEV",
        label = "Create DLP NEV Redaction Annotations",
        description = "Create NEV Redaction annotations based on the stored dlp info")
public class CreateNevRedactionAnnotations {

    public static final Logger log = LogManager.getLogger(CreateNevRedactionAnnotations.class);

    public static final String ID = "NEV.CreateDlpAnnotations";

    @Context
    protected CoreSession session;

    @Context
    protected AnnotationService annotationService;

    @Param(name = "xpath", required = false, values = {"file:content"})
    protected String xpath = "file:content";

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws IOException {
        if (!doc.hasFacet(DLPScanConstants.DLP_FACET)) {
            log.warn("document {} {} doesn't have the DLP facet", doc.getId(), doc.getPathAsString());
            return doc;
        }

        boolean hasSensitiveData = (boolean) doc.getPropertyValue(DLPScanConstants.DLP_SENSITIVE_DATA);

        if (!hasSensitiveData) {
            log.warn("document {} {} doesn't have any sensitive data", doc.getId(), doc.getPathAsString());
            return doc;
        }

        List<Map<String, Serializable>> sensitiveData =
                (List<Map<String, Serializable>>) doc.getPropertyValue(DLPScanConstants.DLP_FINDINGS);

        if (sensitiveData == null || sensitiveData.isEmpty()) {
            return doc;
        }

        ObjectMapper mapper = new ObjectMapper();

        for (Map<String, Serializable> item : sensitiveData) {
            String locationJson = (String) item.get("locationJson");
            if (StringUtils.isBlank(locationJson)) {
                continue;
            }
            JsonNode json = mapper.readTree(locationJson);

            boolean hasLocation = json.get("hasImageLocation").asBoolean();

            if (!hasLocation) {
                continue;
            }

            int pageWidth = json.get("pageWidth").asInt();
            int pageHeight = json.get("pageHeight").asInt();

            JsonNode locations = json.get("imageLocations");

            locations.iterator().forEachRemaining(location -> {
                AnnotationImpl annotation = new AnnotationImpl();
                annotation.setParentId(doc.getId());
                annotation.setXpath(xpath);
                annotation = (AnnotationImpl) annotationService.createAnnotation(session, annotation);

                annotation.setOrigin("ARender");

                //convert coordinates
                int top = location.get("top").asInt();
                int left = location.get("left").asInt();
                int width = location.get("width").asInt();
                int height =  location.get("height").asInt();

                int[] bottomLeft = new int[]{left, pageHeight - (top + height)};
                int[] topRight = new int[]{left + width, pageHeight - top};

                String annotationText = String.format("""
                                <?xml version="1.0" encoding="UTF-8"?><ns0:xfdf xmlns:ns0="http://ns.adobe.com/xfdf/"><ns0:annots><ns0:redact color="#000000" flags="" name="%s" last-modifier="System" page="0" rect="%d,%d,%d,%d" title="%s" creationdate="D:20240919020457+00'00'" opacity="1.0" interior-color="#000000" overlay-text="(b)(2)"/></ns0:annots></ns0:xfdf>""",
                        annotation.getId(),
                        bottomLeft[0], bottomLeft[1], topRight[0], topRight[1],
                        item.get("type"));

                annotation.setEntityId(annotation.getId());
                annotation.setEntity(annotationText);
                annotationService.updateAnnotation(session, annotation.getId(), annotation);
            });
        }

        return doc;
    }

}
