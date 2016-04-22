/*
 * Copyright (c) today.year Chris Bellis
 * This software is subject to the MIT License, see LICENSE.txt in the root of the repository.
 *
 */

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
