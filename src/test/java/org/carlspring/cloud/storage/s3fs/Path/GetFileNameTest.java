package org.carlspring.cloud.storage.s3fs.Path;

import org.carlspring.cloud.storage.s3fs.S3FileSystem;
import org.carlspring.cloud.storage.s3fs.S3FileSystemProvider;
import org.carlspring.cloud.storage.s3fs.S3Path;
import org.carlspring.cloud.storage.s3fs.S3UnitTestBase;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.carlspring.cloud.storage.s3fs.util.S3EndpointConstant.S3_GLOBAL_URI_TEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GetFileNameTest
        extends S3UnitTestBase
{


    @BeforeEach
    public void setup()
            throws IOException
    {
        s3fsProvider = getS3fsProvider();
        fileSystem = s3fsProvider.newFileSystem(S3_GLOBAL_URI_TEST, null);
    }

    @AfterEach
    public void tearDown()
    {
        s3fsProvider.close((S3FileSystem) fileSystem);
    }

    private S3Path getPath(String path)
    {
        return s3fsProvider.getFileSystem(S3_GLOBAL_URI_TEST).getPath(path);
    }

    @Test
    public void getFileName()
    {
        Path path = getPath("/bucketA/file");
        Path name = path.getFileName();

        assertEquals(getPath("file"), name);
    }

    @Test
    public void getAnotherFileName()
    {
        Path path = getPath("/bucketA/dir/another-file");
        Path fileName = path.getFileName();
        Path dirName = path.getParent().getFileName();

        assertEquals(getPath("another-file"), fileName);
        assertEquals(getPath("dir"), dirName);
    }

    @Test
    public void getFileNameBucket()
    {
        Path path = getPath("/bucket");
        Path name = path.getFileName();

        assertNull(name);
    }

}
