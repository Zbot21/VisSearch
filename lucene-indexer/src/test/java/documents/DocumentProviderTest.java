package documents;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import org.junit.Assert;
import org.mockito.Mockito;

import java.util.List;

/**
 * DocumentProvider Tester.
 *
 * @author Chris
 * @version 1.0
 * @since <pre>Apr 22, 2016</pre>
 */
public class DocumentProviderTest {

    private DocumentProvider docProvider;
    private DocumentProvidedListener listener;

    @Before
    public void before() throws Exception {
        docProvider = new DocumentProvider();
        listener = Mockito.mock(DocumentProvidedListener.class);
        docProvider.registerDocumentListener(listener);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: newDocumentsAvailable()
     */
    @Test
    public void testNewDocumentsAvailable() throws Exception {
        docProvider.newDocumentsAvailable();
        Mockito.verify(listener).documentAdded(DocumentProvidedListener.DocumentProvidedEventType.NEW_DOCUMENT);
    }

    /**
     * Method: registerDocumentListener(DocumentProvidedListener listener)
     */
    @Test
    public void testRegisterDocumentListener() throws Exception {
        DocumentProvidedListener listener = Mockito.mock(DocumentProvidedListener.class);
        docProvider.registerDocumentListener(listener);
        docProvider.newDocumentsAvailable();
        Mockito.verify(listener).documentAdded(DocumentProvidedListener.DocumentProvidedEventType.NEW_DOCUMENT);
    }

    /**
     * Method: addDocument(Document doc)
     */
    @Test
    public void testAddDocumentDoc() throws Exception {
        docProvider.addDocument(createTestDocument());
        Mockito.verifyZeroInteractions(listener);
        Assert.assertEquals(1, docProvider.documentsAvailable());
    }

    /**
     * Method: addDocument(Document doc, boolean notify)
     */
    @Test
    public void testAddDocumentForDocNotify() throws Exception {
        docProvider.addDocument(createTestDocument(), false);
        Mockito.verifyZeroInteractions(listener);
        Assert.assertEquals(1, docProvider.documentsAvailable());
        docProvider.addDocument(createTestDocument(), true);
        Mockito.verify(listener).documentAdded(DocumentProvidedListener.DocumentProvidedEventType.NEW_DOCUMENT);
        Assert.assertEquals(2, docProvider.documentsAvailable());
    }

    /**
     * Method: addDocuments(Collection<? extends Document> docs)
     */
    @Test
    public void testAddDocuments() throws Exception {
        int gen = 10;
        docProvider.addDocuments(generateTestDocuments(gen));
        Mockito.verify(listener).documentAdded(DocumentProvidedListener.DocumentProvidedEventType.NEW_DOCUMENT);
        Assert.assertEquals(gen, docProvider.documentsAvailable());
    }

    /**
     * Method: documentsAvailable()
     */
    @Test
    public void testDocumentsAvailable() throws Exception {
        int gen = 10;
        int plus_some = 3;
        docProvider.addDocuments(generateTestDocuments(gen));
        for(int i = 0; i< plus_some; i++){
            docProvider.addDocument(createTestDocument());
        }
        Assert.assertEquals(gen + plus_some, docProvider.documentsAvailable());
    }

    /**
     * Method: resetState()
     */
    @Test
    public void testResetState() throws Exception {
        int num_gen = 20;
        docProvider.addDocuments(generateTestDocuments(20));
        Assert.assertEquals(num_gen, docProvider.getDocuments().size());
        Assert.assertEquals(0, docProvider.getDocuments().size());
        docProvider.resetState();
        Assert.assertEquals(num_gen, docProvider.getDocuments().size());
    }

    /**
     * Method: getDocuments()
     */
    @Test
    public void testGetDocuments() throws Exception {
        int num_gen = 20;
        docProvider.addDocuments(generateTestDocuments(20));
        Assert.assertEquals(num_gen, docProvider.getDocuments().size());
    }

    /**
     * Method: getDocuments(long num)
     */
    @Test
    public void testGetDocumentsNum() throws Exception {
        int num_gen = 20;
        int num_get = 10;
        int num_xtr = 5;
        docProvider.addDocuments(generateTestDocuments(num_gen));
        docProvider.getDocuments(num_get);
        Assert.assertEquals(num_gen - num_get, docProvider.documentsAvailable());
        int got = docProvider.getDocuments(num_get + num_xtr).size();
        Assert.assertEquals(num_gen - num_get, got);
    }

    private static List<Document> generateTestDocuments(int num){
        List<Document> docs = Lists.newArrayList();
        for(int i = 0; i<num; i++){
            docs.add(createTestDocument());
        }
        return docs;
    }

    private static Document createTestDocument(){
        return Mockito.mock(Document.class);
    }

} 
