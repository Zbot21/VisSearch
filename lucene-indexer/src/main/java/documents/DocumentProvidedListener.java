package documents;

/**
 * Created by chris on 4/8/16.
 */
public interface DocumentProvidedListener {
    void documentAdded(DocumentProvidedEventType e);
    enum DocumentProvidedEventType{
        NEW_DOCUMENT
    }
}
