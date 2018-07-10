/*
 * Copyright (C) 2015 Matt Booth (Kryten2k35).
 * Copyright (C) 2017 The halogenOS Project.
 *
 * Licensed under the Attribution-NonCommercial-ShareAlike 4.0 International
 * (the "License") you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://creativecommons.org/licenses/by-nc-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dot.systemupdates.utils;

import android.os.Environment;

public interface Constants {
    // Developer
    boolean DEBUGGING                       = false;

    // Props
    String OTA_DOWNLOAD_LOC                 = "persist.ota.download_loc";

    // Settings
    String CURRENT_THEME                    = "current_theme";
    String LAST_CHECKED                     = "updater_last_update_check";
    String IS_DOWNLOAD_FINISHED             = "is_download_finished";
    String DELETE_AFTER_INSTALL             = "delete_after_install";
    String INSTALL_PREFS                    = "install_prefs";
    String WIPE_DATA                        = "wipe_data";
    String WIPE_CACHE                       = "wipe_cache";
    String WIPE_DALVIK                      = "wipe_dalvik";

    // Storage
    String SD_CARD                          = Environment.getExternalStorageDirectory().getAbsolutePath();
    String OTA_DOWNLOAD_DIR                 = Tools.doesPropExist(OTA_DOWNLOAD_LOC) ? Tools.getProp(OTA_DOWNLOAD_LOC) : "DotUpdates";
    String INSTALL_AFTER_FLASH_DIR          = "Extras";

    //Notification
    int NOTIFICATION_ID                         = 101;
}
