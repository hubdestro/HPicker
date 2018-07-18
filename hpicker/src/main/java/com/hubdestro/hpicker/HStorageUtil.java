/*
 * Copyright (c) [2018] [Ankit Kr]. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hubdestro.hpicker;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by ankit on 8/1/18
 */

public final class HStorageUtil {

    private static final String DIR_ANDROID = "Android";
    private static final String DIR_DATA = "data";
    private static final String DIR_FILES = "files";

    @Nullable
    public static synchronized File getExternalStorageAppFilesFile(
            Context context,
            String fileName) {

        if (context == null) return null;
        if (fileName == null) return null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirs =
                    buildExternalStorageAppFilesDirs
                            (Environment.getExternalStorageDirectory()
                                            .getAbsolutePath(),
                                    context.getPackageName());
            return new File(dirs, fileName);
        }
        return null;
    }


    private synchronized static File buildExternalStorageAppFilesDirs(
            String externalStoragePath, String packageName) {
        return buildPath(externalStoragePath,
                DIR_ANDROID, DIR_DATA,
                packageName, DIR_FILES);
    }


    private synchronized static File buildPath(String base, String... segments) {
        File cur = new File(base);
        for (String segment : segments) {
            cur = new File(cur, segment);
        }
        return cur;
    }
}
