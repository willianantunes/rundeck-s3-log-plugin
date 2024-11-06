package org.rundeck.plugins;

import com.dtolabs.rundeck.core.logging.ExecutionFileStorageException;
import com.dtolabs.rundeck.core.logging.MultiFileStorageRequest;
import com.dtolabs.rundeck.core.logging.StorageFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class S3LogFileStoragePluginIntegrationTest {

    private S3LogFileStoragePlugin plugin;

    @Before
    public void setUp() {
        plugin = new S3LogFileStoragePlugin();
        Map<String, Object> context = new HashMap<>();
        context.put(S3LogFileStoragePlugin.META_PROJECT, "test");
        context.put(S3LogFileStoragePlugin.META_EXECID, "12345");
        context.put("name", "test job");
        context.put("group", "test group");
        context.put("id", "testjobid");
        context.put(S3LogFileStoragePlugin.META_USERNAME, "testuser");

        plugin.setAWSAccessKeyId("root");
        plugin.setAWSSecretKey("password");
        plugin.setBucket("rundeck-sqlserver-bucket");
        plugin.setPath("logs/${job.project}/${job.execid}");
        plugin.setRegion("us-east-1");
        plugin.setEndpoint("http://localhost:9000");
        plugin.setPathStyle(true);

        plugin.initialize(context);
    }

    @Test
    public void testStoreMultiple() throws IOException, ExecutionFileStorageException {
        MultiFileStorageRequest request = new MultiFileStorageRequest() {
            @Override
            public Set<String> getAvailableFiletypes() {
                return Set.of("lorem-1.txt", "lorem-2.txt");
            }

            @Override
            public StorageFile getStorageFile(String filetype) {
                return new StorageFile() {
                    @Override
                    public String getFiletype() {
                        return filetype;
                    }

                    @Override
                    public InputStream getInputStream() {
                        try {
                            return new FileInputStream(new File("src/test/resources/" + filetype));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public long getLength() {
                        return new File("src/test/resources/" + filetype).length();
                    }

                    @Override
                    public Date getLastModified() {
                        return new Date(new File("src/test/resources/" + filetype).lastModified());
                    }

                    @Override
                    public boolean isComplete() {
                        return false;
                    }
                };
            }

            @Override
            public void storageResultForFiletype(String filetype, boolean success) {
                System.out.println("Filetype: " + filetype + " stored successfully: " + success);
            }
        };

        plugin.storeMultiple(request);
    }
}
