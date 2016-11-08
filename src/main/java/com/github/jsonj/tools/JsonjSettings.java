package com.github.jsonj.tools;

interface JsonjSettings {
    /**
     * @return true if jsonj should use SimpleIntMapJsonObject instead of the regular implementation
     */
    default boolean useEfficientStringBasedJsonObject() {
        return false;
    }

    /**
     * @return the default threshold of the number of keys after which the parser handler upgrades the object to a MapBasedJsonObject
     */
    default int upgradeThresholdToMapBasedJsonObject() {
        return 100;
    }
}