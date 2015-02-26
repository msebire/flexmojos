package org.sonatype.flexmojos.compiler;

import java.util.List;

/**
 * This class hold the list of flex components to be excluded from a particular Manifest. The Manifest is identified using a URI.
 */
public class ManifestExclusion {

    private String uri;
    private List<String> exclusions;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }
}
