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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

class TarUtils {
    public String untargz(File archive, File outputDir) {
        String absolutePath = archive.getAbsolutePath();
        String root = null;
        boolean first = true;

        while (absolutePath.contains("tar") || absolutePath.contains("gz") || absolutePath.contains("tgz")) {
            absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("."));
        }

        absolutePath = absolutePath + ".tar";

        try {
            GZIPInputStream input = new GZIPInputStream(new FileInputStream(archive));
            FileOutputStream fos = new FileOutputStream(new File(absolutePath));

            IOUtils.copy(input, fos);
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(fos);

            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(absolutePath));

            for (TarArchiveEntry entry = tarArchiveInputStream.getNextTarEntry(); entry != null; ) {
                unpackEntries(tarArchiveInputStream, entry, outputDir);

                if (first && entry.isDirectory()) {
                    root = outputDir + File.separator + entry.getName();
                }

                entry = tarArchiveInputStream.getNextTarEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }

    private void unpackEntries(TarArchiveInputStream tis, TarArchiveEntry entry, File outputDir) throws IOException {
        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            File subDir = new File(outputDir, entry.getName());

            for (TarArchiveEntry e : entry.getDirectoryEntries()) {
                unpackEntries(tis, e, subDir);
            }

            return;
        }

        File outputFile = new File(outputDir, entry.getName());

        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            byte[] content = new byte[(int) entry.getSize()];

            tis.read(content);

            if (content.length > 0) {
                IOUtils.copy(new ByteArrayInputStream(content), outputStream);
            }
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void createDir(File dir) {
        dir.mkdirs();
    }

}
