package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class WallpaperCaptionsTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void readsUtf8FileWithWindowsLineEndings() throws Exception {
        File folder = temporary.newFolder();
        File file = new File(folder, WallpaperCaptions.FILE_NAME);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write("one.jpg\tBầu trời\r\ntwo.jpg\tĐêm".getBytes(StandardCharsets.UTF_8));
        }
        Map<String, String> names = WallpaperCaptions.read(folder);
        assertEquals("Bầu trời", names.get("one.jpg"));
        assertEquals("Đêm", names.get("two.jpg"));
    }

    @Test
    public void rejectsInvalidUtf8File() throws Exception {
        File folder = temporary.newFolder();
        try (FileOutputStream out = new FileOutputStream(new File(folder, WallpaperCaptions.FILE_NAME))) {
            out.write(new byte[] { (byte) 0xC3, (byte) 0x28 });
        }
        assertTrue(WallpaperCaptions.read(folder).isEmpty());
    }

    @Test
    public void readsFilenameTabName() {
        Map<String, String> names = WallpaperCaptions.parse(Arrays.asList(
                "001.jpg\tRed Spider Nebula · Hubble",
                "002.jpg\t  Falcon 9 at sunset  "));
        assertEquals("Red Spider Nebula · Hubble", names.get("001.jpg"));
        assertEquals("Falcon 9 at sunset", names.get("002.jpg"));
    }

    @Test
    public void skipsCommentsBlankAndMalformedLines() {
        Map<String, String> names = WallpaperCaptions.parse(Arrays.asList(
                "# captions for the collection", "", "   ", "no-tab-here.jpg Dusk", "\tno file", "003.jpg\t", null));
        assertTrue(names.isEmpty());
    }

    @Test
    public void cutsVeryLongNames() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 30; i++) longName.append("word ");
        Map<String, String> names = WallpaperCaptions.parse(Arrays.asList("004.jpg\t" + longName));
        assertTrue(names.get("004.jpg").length() <= WallpaperCaptions.MAX_LENGTH);
    }

    @Test
    public void laterLineWinsForTheSameFile() {
        Map<String, String> names = WallpaperCaptions.parse(Arrays.asList("005.jpg\tOld", "005.jpg\tNew"));
        assertEquals("New", names.get("005.jpg"));
        assertFalse(names.containsKey("006.jpg"));
    }

    @Test
    public void missingFolderHasNoNames() {
        assertTrue(WallpaperCaptions.read(null).isEmpty());
    }
}
