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
package org.nuxeo.platform.scanner.dlp.google;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.rendition.extension.RenditionProvider;
import org.nuxeo.ecm.platform.rendition.service.RenditionDefinition;
import org.nuxeo.platform.scanner.dlp.DataLossPreventionScanner;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 10.10
 */
public class GoogleRenditionProvider implements RenditionProvider {

    protected static final Logger log = LogManager.getLogger(GoogleRenditionProvider.class);

    static final String IMAGE_VARIANT = "image";

    static final String DOC_VARIANT = "document";

    private DataLossPreventionScanner dlpService;

    private DataLossPreventionScanner dlp() {
        if (dlpService == null) {
            dlpService = Framework.getService(DataLossPreventionScanner.class);
        }
        return dlpService;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.nuxeo.ecm.platform.rendition.extension.RenditionProvider#isAvailable(org.nuxeo.ecm.core.api.DocumentModel,
     * org.nuxeo.ecm.platform.rendition.service.RenditionDefinition)
     */
    @Override
    public boolean isAvailable(DocumentModel doc, RenditionDefinition definition) {
        return Boolean.parseBoolean(Framework.getProperty(GoogleDLPConstants.ENABLED, "true"));
    }

    /*
     * (non-Javadoc)
     * @see org.nuxeo.ecm.platform.rendition.extension.RenditionProvider#render(org.nuxeo.ecm.core.api.DocumentModel,
     * org.nuxeo.ecm.platform.rendition.service.RenditionDefinition)
     */
    @Override
    public List<Blob> render(DocumentModel doc, RenditionDefinition definition) {
        BlobHolder input = doc.getAdapter(BlobHolder.class);
        Blob output = null;
        if (IMAGE_VARIANT.equals(definition.getVariantPolicy())) {
            output = redactImage(input.getBlob(), null);
        } else {
            output = redactDocument(input, null);
        }
        if (output == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(output);
    }

    public Blob redact(DocumentModel doc, List<String> features) {
        BlobHolder input = doc.getAdapter(BlobHolder.class);
        Blob output = null;
        if (doc.hasFacet("Picture")) {
            output = redactImage(input.getBlob(), features);
        } else {
            output = redactDocument(input, features);
        }
        if (output == null) {
            return null;
        }
        return output;
    }

    public Blob redact(Blob blob, List<String> features) {
        Blob data = dlp().redact(blob, features);
        if (data.getMimeType() == null) {
            MimetypeRegistry reg = Framework.getService(MimetypeRegistry.class);
            data.setMimeType(reg.getMimetypeFromBlob(data));
        }
        return data;
    }

    public Blob redactImage(Blob input, List<String> features) {
        return redact(input, features);
    }

    public Blob redactDocument(BlobHolder input, List<String> features) {
        ConversionService conv = Framework.getService(ConversionService.class);

        if (!MimetypeRegistry.PDF_MIMETYPE.equals(input.getBlob().getMimeType())) {
            input = conv.convertToMimeType(MimetypeRegistry.PDF_MIMETYPE, input, Collections.emptyMap());
        }

        BlobHolder images = conv.convert("pdf2hiResImage", input,
                Collections.singletonMap("targetFilePath", "conversion_%04d.png"));
        List<Blob> parts = new ArrayList<>(images.getBlobs().size());
        for (Blob img : images.getBlobs()) {
            Blob out = redact(img, features);
            BlobHolder toPdf = conv.convert("image2pdf", new SimpleBlobHolder(out), Collections.emptyMap());
            parts.add(toPdf.getBlob());
        }

        // Sort pages (lexographic)
        Collections.sort(parts, new Comparator<Blob>() {
            @Override
            public int compare(Blob o1, Blob o2) {
                return o1.getFilename().compareTo(o2.getFilename());
            }
        });

        try {
            PDFMergerUtility ut = new PDFMergerUtility();
            for (Blob blob : parts) {
                ut.addSource(blob.getCloseableFile().getFile());
            }
            return appendPDFs(ut, input.getBlob().getFilename());
        } catch (IOException iox) {
            throw new NuxeoException(iox);
        }
    }

    protected Blob appendPDFs(PDFMergerUtility ut, String filename) throws IOException {
        File tempFile = Framework.createTempFile("redacted_" + filename, ".pdf");
        ut.setDestinationFileName(tempFile.getAbsolutePath());
        ut.mergeDocuments(null);
        Blob fb = Blobs.createBlob(tempFile);
        Framework.trackFile(tempFile, fb);
        fb.setFilename(tempFile.getName());
        return fb;
    }

}
