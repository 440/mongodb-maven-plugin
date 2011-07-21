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

import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @goal stop
 * @phase post-integration-test
 */
public class StopMongoDb extends AbstractMojo {

    /**
     * @parameter expression="${mongodb.port}"
     */
    private String port;

    public void execute() throws MojoExecutionException {
        port = null == port ? "27017" : port;

        try {
            Logger.getLogger("com.mongodb").setLevel(Level.OFF);
            getLog().info("Shutting down mongodb...");
            Mongo mongo = new Mongo("127.0.0.1", Integer.valueOf(port));
            mongo.getDB("admin").command(new BasicDBObject("shutdown", 1));
            getLog().info("MongoDB shutdown. Ignore the EOFException...");
        } catch (UnknownHostException e) {
            getLog().error(ExceptionUtils.getFullStackTrace(e));
        } catch (Throwable ex) {
            //ignore
        }
    }
}
