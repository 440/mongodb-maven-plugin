package com.wenzani.maven.mongodb;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @goal init
 * @phase clean
 */
public class InitMongoDb extends AbstractMojo {
    /**
     * @parameter expression="${mongodb.home}"
     */
    private String mongoDbDir;

    /**
     * @parameter expression="${mongodb.download.url}"
     * @required
     */
    private String mongoDbUrl;

    /**
     * @parameter expression="${mongodb.version}"
     * @required
     */
    private String mongoDbVersion;

    public void execute() throws MojoExecutionException {
        setDefaultMongoDir();
        initMongoDir();
        downloadIfNotCached();
        extractTarGz();
        getLog().info("init complete");
    }

    private void setDefaultMongoDir() {
        if (null == mongoDbDir) {
            mongoDbDir = ".mongodb";
        }
    }

    private void initMongoDir() {
        File directory = new File(mongoDbDir);
        if (!directory.exists()) {
            getLog().info(String.format("%s does not exist. Creating...", mongoDbDir));
            directory.mkdir();
        }
    }

    private void downloadIfNotCached() {
        File archive = new File(getTarGzFileName());
        if (!archive.exists()) {
            download();
        } else {
            getLog().info("MongoDB tgz already downloaded.");
        }
    }

    private void extractTarGz() {
        try {
            getLog().info("Removing extracted mongodb dir...");

            String extractedDir = new StringBuilder(mongoDbDir).append(IOUtils.DIR_SEPARATOR).append(mongoDbVersion)
                    .toString();

            File extractedDirFile = new File(extractedDir);
            if (extractedDirFile.exists()) {
                FileUtils.deleteDirectory(extractedDirFile);
            }

            getLog().info("Extracting fresh instance of mongodb...");
            extract();
        } catch (IOException e) {
            getLog().error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    private void download() {
        getLog().info(String.format("Downloading MongoDB from %s", mongoDbUrl));
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            URI uri = new URI(mongoDbUrl);
            HttpGet httpget = new HttpGet(uri);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String fileName = getTarGzFileName();
            FileUtils.copyInputStreamToFile(entity.getContent(), new File(fileName));
        } catch (ClientProtocolException e) {
            getLog().error(ExceptionUtils.getFullStackTrace(e));
        } catch (IOException e) {
            getLog().error(ExceptionUtils.getFullStackTrace(e));
        } catch (URISyntaxException e) {
            getLog().error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    private void extract() {
        new TarUtils().untargz(new File(getTarGzFileName()), new File(mongoDbDir));
        FileUtils.deleteQuietly(new File(getTarFileName()));
    }

    private String getTarGzFileName() {
        return new StringBuilder(mongoDbDir).append(IOUtils.DIR_SEPARATOR).append("mongo.tar.gz")
                .toString();
    }

    private String getTarFileName() {
        return new StringBuilder(mongoDbDir).append(IOUtils.DIR_SEPARATOR).append("mongo.tar")
                .toString();
    }
}
