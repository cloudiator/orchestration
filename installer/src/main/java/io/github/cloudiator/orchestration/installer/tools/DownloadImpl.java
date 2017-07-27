package io.github.cloudiator.orchestration.installer.tools;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 08.02.17.
 */
public class DownloadImpl implements Download {

    private final String url;
    @Nullable private final String fileName;

    DownloadImpl(String url, @Nullable String fileName) {
        checkNotNull(url, "url is null.");
        if (fileName != null) {
            checkArgument(!fileName.isEmpty(), "filename is empty.");
        }
        this.url = url;
        this.fileName = fileName;
    }

    @Override public String url() {
        return url;
    }

    @Override public Optional<String> fileName() {
        return Optional.ofNullable(fileName);
    }
}
