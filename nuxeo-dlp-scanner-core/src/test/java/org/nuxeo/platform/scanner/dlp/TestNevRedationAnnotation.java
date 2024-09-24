package org.nuxeo.platform.scanner.dlp;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.comment.CommentFeature;
import org.nuxeo.ecm.platform.comment.api.Annotation;
import org.nuxeo.ecm.platform.comment.api.AnnotationService;
import org.nuxeo.platform.scanner.dlp.operations.CreateNevRedactionAnnotations;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class, CommentFeature.class})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.core.test")
@Deploy("org.nuxeo.platform.scanner.dlp.core")
@Deploy("org.nuxeo.platform.scanner.dlp.core:disable-listeners-contrib.xml")
public class TestNevRedationAnnotation {

    @Inject
    protected CoreSession session;

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected AnnotationService annotationService;


    @Test
    public void testCreateAnnotation() throws Exception {

        DocumentModel doc = session.createDocumentModel(
                session.getRootDocument().getPathAsString(),"File","File");
        doc.addFacet(DLPScanConstants.DLP_FACET);

        Map<String, Serializable> sensitiveData = new HashMap<>();
        sensitiveData.put("type","SSN");
        sensitiveData.put("info","AAA-GG-SSS");
        sensitiveData.put("score","LIKELY");
        sensitiveData.put("locationJson", """
                {"hasImageLocation":true,"pageNumber":1,"pageWidth":540,"pageHeight":358,"imageLocations":[{"top":120,"left":220,"width":124,"height":27}],"hasCodepointRange":false,"hasByteRange":false,"firstImageLocation":{"top":120,"left":220,"width":124,"height":27}}
                """);

        doc.setPropertyValue("dlp:sensitiveData", true);
        doc.setPropertyValue("dlp:scanStatus", "done");
        doc.setPropertyValue("dlp:scanDate", "2024-09-19T00:39:17.688Z");
        doc.setPropertyValue("dlp:findings", (Serializable) List.of(sensitiveData));

        doc = session.createDocument(doc);

        transactionalFeature.nextTransaction();

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        doc = (DocumentModel) automationService.run(ctx, CreateNevRedactionAnnotations.ID);

        transactionalFeature.nextTransaction();

        List<Annotation> annotations = annotationService.getAnnotations(session,doc.getId(),"file:content");

        Assert.assertEquals(1,annotations.size());

    }



}
