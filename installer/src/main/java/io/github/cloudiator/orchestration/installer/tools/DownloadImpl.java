package io.github.cloudiator.orchestration.installer.tools;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 08.02.17.
 */
public class DownloadImpl implements Download {

    private final String command;
    private final String filePath;

    public DownloadImpl(String url, String fileName) {
        checkNotNull(url, "downloadCommand is null.");
        if (fileName != null) {
            checkArgument(!fileName.isEmpty(), "filename is empty.");
        }
        this.command = url;
        this.filePath = fileName;
    }

    @Override public String downloadCommand() {
        return command;
    }

    @Override public String filePath() {
        return filePath;
    }
}
