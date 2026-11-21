package com.santech.module.biz.utils;

import org.geotools.data.DataStore;
import org.geotools.data.directory.DirectoryDataStore;
import org.geotools.data.directory.FileStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ShpDirectoryDataStore extends DirectoryDataStore {
    private String encode;
    public ShpDirectoryDataStore(File directory, FileStoreFactory dialect) throws IOException {
        super(directory, dialect);
        this.encode = "GBK";
    }

    @Override
    public DataStore getDataStore(String typeName) throws IOException {
        DataStore dataStore = super.getDataStore(typeName);
        if(dataStore instanceof ShapefileDataStore) {
            ((ShapefileDataStore)dataStore).setCharset(Charset.forName(encode));
        }
        return dataStore;
    }
    public void setCharset(String charset) {
        this.encode = charset;
    }

}
