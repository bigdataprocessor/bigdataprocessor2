package de.embl.cba.bigDataTools2.imaris;

public class DataSet {

    public String directory;
    public String filename;
    public String h5Group;

    public DataSet(String directory , String filename, String h5Group )
    {
        this.directory = directory;
        this.filename = filename;
        this.h5Group = h5Group;
    }

}
