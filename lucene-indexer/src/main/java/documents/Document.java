/*
 * Copyright (c) 2016 Chris Bellis
 * This software is subject to the MIT License, see LICENSE.txt in the root of the repository.
 */

package documents;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chris on 4/8/16.
 */
public abstract class Document {
    private Map<String, String> metadata;
    public Document(){
        metadata = new HashMap<>();
    }

    public Set<String> getMetadataFields(){
        return getAllFields().stream()
                .filter(s -> !hiddenFields().contains(s))
                .collect(Collectors.toSet());
    }

    public Set<String> getAllFields(){
        return metadata.keySet();
    }

    public String getMetadata(String key){
        return metadata.get(key);
    }
    public void addMetadata(String key, String value){
        metadata.put(key, value);
    }

    public abstract String getName();
    public abstract String getContents();
    public abstract List<String> hiddenFields();

    public int hashCode(){
        Hasher hf = Hashing.md5().newHasher();
        metadata.forEach((k,v) -> hf.putString(v, Charset.defaultCharset()));
        return hf.hash().asInt();
    }
}
