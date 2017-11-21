package ca.ubc.cs.cpsc210.translink.parsers;

/**
 * Represents a file-like entity that is stored in memory rather than on disk
 * Used when reading zip files (KMZ files)
 */
public class MemoryFile {
    private String name;
    private String contents;

    public MemoryFile(String filename, String contents) {
        this.name = filename;
        this.contents = contents;
    }

    public String getName() {
        return name;
    }

    public String getContents() {
        return contents;
    }
}
