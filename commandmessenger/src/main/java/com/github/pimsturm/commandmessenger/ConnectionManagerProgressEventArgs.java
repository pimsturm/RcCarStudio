package com.github.pimsturm.commandmessenger;

/**
 * Arguments for event listener of the progress of the Connection manager
 */
public class ConnectionManagerProgressEventArgs {
    private int level;

    /**
     * Gets the progress level
     * @return an integer indicating the level
     */
    public int getLevel() {
        return level;
    }
    public void setLevel(int newLevel) {
        level = newLevel;
    }

    private String description;

    /**
     * Gets the description of the current progress level.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        description = newDescription;
    }
}
