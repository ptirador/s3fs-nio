package org.carlspring.cloud.storage.s3fs.FileSystemProvider;

import org.carlspring.cloud.storage.s3fs.S3FileSystem;
import org.carlspring.cloud.storage.s3fs.S3FileSystemProvider;
import org.carlspring.cloud.storage.s3fs.S3UnitTestBase;
import org.carlspring.cloud.storage.s3fs.util.AmazonS3ClientMock;
import org.carlspring.cloud.storage.s3fs.util.AmazonS3MockFactory;
import org.carlspring.cloud.storage.s3fs.util.MockBucket;
import org.carlspring.cloud.storage.s3fs.util.S3EndpointConstant;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NewDirectoryStreamTest
        extends S3UnitTestBase
{


    @BeforeEach
    public void setup()
            throws IOException
    {
        s3fsProvider = getS3fsProvider();
        fileSystem = s3fsProvider.newFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST, null);
    }

    @Test
    public void createStreamDirectoryReader()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").file("file1");

        // act
        Path bucket = createNewS3FileSystem().getPath("/bucketA");

        // assert
        assertNewDirectoryStream(bucket, "file1");
    }

    @Test
    public void createAnotherStreamDirectoryReader()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").file("file1", "file2");

        // act
        Path bucket = createNewS3FileSystem().getPath("/bucketA");

        // assert
        assertNewDirectoryStream(bucket, "file1", "file2");
    }

    @Test
    public void createAnotherWithDirStreamDirectoryReader()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").dir("dir1").file("file1");

        // act
        Path bucket = createNewS3FileSystem().getPath("/bucketA");

        // assert
        assertNewDirectoryStream(bucket, "file1", "dir1");
    }

    @Test
    public void createStreamDirectoryFromDirectoryReader()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").dir("dir", "dir/file2").file("dir/file1");

        // act
        Path dir = createNewS3FileSystem().getPath("/bucketA", "dir");

        // assert
        assertNewDirectoryStream(dir, "file1", "file2");
    }

    @Test
    public void removeIteratorStreamDirectoryReader()
    {
        // We're expecting an exception here to be thrown
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            // fixtures
            AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
            client.bucket("bucketA").dir("dir1").file("dir1/file1", "content".getBytes());

            // act
            Path bucket = createNewS3FileSystem().getPath("/bucketA");

            // act
            try (DirectoryStream<Path> dir = Files.newDirectoryStream(bucket))
            {
                dir.iterator().remove();
            }
        });

        assertNotNull(exception);
    }

    @Test
    public void list999Paths()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();

        MockBucket bucketA = client.bucket("bucketA");

        final int count999 = 999;
        for (int i = 0; i < count999; i++)
        {
            bucketA.file(i + "file");
        }

        Path bucket = createNewS3FileSystem().getPath("/bucketA");

        int count = 0;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(bucket))
        {
            Iterator<Path> iterator = files.iterator();
            while (iterator.hasNext())
            {
                iterator.next();
                count++;
            }
        }

        assertEquals(count999, count);
    }

    @Test
    public void list1050Paths()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        MockBucket bucketA = client.bucket("bucketA");

        final int count1050 = 1050;
        for (int i = 0; i < count1050; i++)
        {
            bucketA.file(i + "file");
        }

        Path bucket = createNewS3FileSystem().getPath("/bucketA");

        int count = 0;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(bucket))
        {
            Iterator<Path> iterator = files.iterator();
            while (iterator.hasNext())
            {
                iterator.next();
                count++;
            }
        }

        assertEquals(count1050, count);
    }

    /**
     * check if the directory path contains all the files name
     *
     * @param base  Path
     * @param files String array of file names
     * @throws IOException
     */
    private void assertNewDirectoryStream(Path base, final String... files)
            throws IOException
    {
        try (DirectoryStream<Path> dir = Files.newDirectoryStream(base))
        {
            assertNotNull(dir);
            assertNotNull(dir.iterator());
            assertTrue(dir.iterator().hasNext());

            Set<String> filesNamesExpected = new HashSet<>(Arrays.asList(files));
            Set<String> filesNamesActual = new HashSet<>();

            for (Path path : dir)
            {
                String fileName = path.getFileName().toString();
                filesNamesActual.add(fileName);
            }

            assertEquals(filesNamesExpected, filesNamesActual);
        }
    }

    /**
     * create a new file system for s3 scheme with fake credentials
     * and global endpoint
     *
     * @return FileSystem
     * @throws IOException
     */
    private S3FileSystem createNewS3FileSystem()
            throws IOException
    {
        try
        {
            return s3fsProvider.getFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST);
        }
        catch (FileSystemNotFoundException e)
        {
            return (S3FileSystem) FileSystems.newFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST, null);
        }
    }

}
