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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @goal start
 * @phase test
 */
public class StartMongoDb extends AbstractMojo {
    /**
     * @parameter expression="${mongodb.home}"
     */
    private String mongoDbDir;

    /**
     * @parameter expression="${mongodb.version}"
     * @required
     */
    private String mongoDbVersion;

    /**
     * @parameter expression="${mongodb.port}"
     */
    private String port;

    public void execute() throws MojoExecutionException {
        port = null == port ? "27017" : port;
        try {
            chmodMongoD();
            String executable = String.format("%s --dbpath %s --port %s", getMongoD(), mongoDbDir, port);
            Process process = Runtime.getRuntime().exec(executable);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                getLog().info("Waiting for MongoDB to start...");
                if (StringUtils.contains(line, String.format("waiting for connections on port %s", port).toString())) {
                    getLog().info("MongoDB startup complete.");
                    break;
                }
            }
            getLog().info(String.format("Started mongod on port %s", port));

        } catch (IOException e) {
            getLog().error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    private void chmodMongoD() throws IOException {
        String chmod = new StringBuilder("chmod +x ").append(getMongoD()).toString();
        Runtime.getRuntime().exec(chmod);
    }

    private String getMongoD() {
        return new StringBuilder(mongoDbDir).append(IOUtils.DIR_SEPARATOR).append(mongoDbVersion)
                .append(IOUtils.DIR_SEPARATOR)
                .append("bin").append(IOUtils.DIR_SEPARATOR).append("mongod").toString();
    }
}
