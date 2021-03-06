package org.carlspring.cloud.storage.s3fs;

import org.carlspring.cloud.storage.s3fs.util.AmazonS3ClientMock;
import org.carlspring.cloud.storage.s3fs.util.AmazonS3MockFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.carlspring.cloud.storage.s3fs.AmazonS3Factory.ACCESS_KEY;
import static org.carlspring.cloud.storage.s3fs.AmazonS3Factory.SECRET_KEY;
import static org.carlspring.cloud.storage.s3fs.S3FileSystemProvider.AMAZON_S3_FACTORY_CLASS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class S3UnitTestBase
{

    protected S3FileSystemProvider s3fsProvider;

    protected FileSystem fileSystem;


    @BeforeEach
    public void setProperties()
    {
        System.clearProperty(S3FileSystemProvider.AMAZON_S3_FACTORY_CLASS);
        System.clearProperty(ACCESS_KEY);
        System.clearProperty(SECRET_KEY);

        System.setProperty(AMAZON_S3_FACTORY_CLASS, "org.carlspring.cloud.storage.s3fs.util.AmazonS3MockFactory");
    }

    @BeforeEach
    public void setupS3fsProvider()
    {
        s3fsProvider = spy(new S3FileSystemProvider());

        // stub the possibility to add system envs var
        doReturn(false).when(s3fsProvider).overloadPropertiesWithSystemEnv(any(Properties.class), anyString());
        doReturn(new Properties()).when(s3fsProvider).loadAmazonProperties();
    }

    @AfterEach
    public void closeMemory()
    {
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.clear();

        for (S3FileSystem s3FileSystem : S3FileSystemProvider.getFilesystems().values())
        {
            try
            {
                s3FileSystem.close();
            }
            catch (Exception e)
            {
                //ignore
            }
        }
    }

    @AfterEach
    public void tearDown()
            throws IOException
    {
        try
        {
            if (s3fsProvider != null)
            {
                s3fsProvider.close((S3FileSystem) fileSystem);
            }

            if (fileSystem != null)
            {
                fileSystem.close();
            }

            com.amazonaws.http.IdleConnectionReaper.shutdown();
        }
        catch (Throwable t)
        {
            // e.printStackTrace();
        }
    }

    public S3FileSystemProvider getS3fsProvider()
    {
        return this.s3fsProvider;
    }

}
